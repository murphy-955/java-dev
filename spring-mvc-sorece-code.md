# 按这个顺序在 IDEA 里跟踪，你会看到 Interceptor 和异常处理是怎么被 Spring 调用的。

## 路径一：HandlerInterceptor 三方法的精确调用链

### 第1站：Interceptor 是怎么被收集的

类：`org.springframework.web.servlet.handler.AbstractHandlerMapping#getHandler`

打断点在这里，看返回值 HandlerExecutionChain：

- handler → 你的 Controller 方法
- interceptorList → 所有匹配的拦截器（按配置顺序排列）
- 怎么匹配的：MappedInterceptor 根据 includePatterns / excludePatterns 判断当前请求路径是否需要拦截。

## 第2站：preHandle 的调用与短路逻辑

类：`org.springframework.web.servlet.DispatcherServlet#applyPreHandle`

```java
// DispatcherServlet.java 源码片段
private boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
    for (int i = 0; i < this.interceptorList.size(); i++) {
        HandlerInterceptor interceptor = this.interceptorList.get(i);
        if (!interceptor.preHandle(request, response, this.handler)) {
// 如果某个 preHandle 返回 false，触发 afterCompletion
            triggerAfterCompletion(request, response, null);
            return false;  // 请求中断，不进入 Controller
        }
        this.interceptorIndex = i;  // 记录执行到哪了
    }
    return true;
}
```

**关键观察：**

- interceptorIndex 记录了成功执行到第几个拦截器
- 如果第 N 个 preHandle 返回 false，只有前 N 个拦截器会执行 afterCompletion
- Controller 不会执行
- 断点：在 applyPreHandle 第一行，看 interceptorList 顺序。

### 第3站：postHandle 的调用（倒序！）

类：`org.springframework.web.servlet.DispatcherServlet#applyPostHandle`

```java
private void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv) throws Exception {
// 注意：倒序遍历
    for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
        HandlerInterceptor interceptor = this.interceptorList.get(i);
        interceptor.postHandle(request, response, this.handler, mv);
    }
}
```

**关键观察：**

- postHandle 是 逆序 执行的：后配的拦截器先执行 postHandle
- 执行时机：Controller 方法已经跑完，但 视图还没渲染
- 这里可以修改 ModelAndView（改视图名、加数据）

## 第4站：afterCompletion 的调用（倒序！）

类：`org.springframework.web.servlet.DispatcherServlet#triggerAfterCompletion`

```java
private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex) {
// 倒序遍历，只执行到 interceptorIndex（preHandle 成功的那几个）
    for (int i = this.interceptorIndex; i >= 0; i--) {
        HandlerInterceptor interceptor = this.interceptorList.get(i);
        interceptor.afterCompletion(request, response, this.handler, ex);
    }
}
```

调用时机：

- 正常流程：视图渲染完成后调用
- preHandle 返回 false 时：立即调用（只调用已执行的）
- Controller 抛异常时：异常被包装后传入 ex 参数
  考试常考：afterCompletion 里能不能拿到异常？能，ex 参数就是。

## 第5站：拦截器执行顺序总结图

假设配置了拦截器 A → B → C：

```
请求进入
├── A.preHandle()   → true
├── B.preHandle()   → true
├── C.preHandle()   → true
│
├── Controller 执行
│
├── C.postHandle()  ← 倒序
├── B.postHandle()
├── A.postHandle()
│
├── 视图渲染
│
├── C.afterCompletion() ← 倒序
├── B.afterCompletion()
└── A.afterCompletion()
如果 B.preHandle() 返回 false：

请求进入
├── A.preHandle()   → true
├── B.preHandle()   → false
│
├── triggerAfterCompletion()
│ └── A.afterCompletion()  ← 只有 A 执行
│
└── 请求中断，Controller 不执行
断点建议：在你的拦截器实现类的三个方法里都打断点，看实际调用顺序。
```

#### 路径二：@ControllerAdvice + @ExceptionHandler 的精确调用链

##### 第1站：异常从哪里开始被处理

类：`org.springframework.web.servlet.DispatcherServlet#processDispatchResult`

```java
private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
                                   @Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
                                   @Nullable Exception exception) throws Exception {

    boolean errorView = false;

    // 如果 Controller 抛了异常
    if (exception != null) {
        if (exception instanceof ModelAndViewDefiningException) {
            mv = ((ModelAndViewDefiningException) exception).getModelAndView();
        } else {
            Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
            // 核心：调用异常处理器
            mv = processHandlerException(request, response, handler, exception);
            errorView = (mv != null);
        }
    }

    // 渲染视图或处理输出
    if (mv != null && !mv.wasCleared()) {
        render(mv, request, response);
    }

}
```

关键：异常不是直接跳到 @ExceptionHandler，而是先经过 processHandlerException()。

##### 第2站：异常处理器的遍历

类：`org.springframework.web.servlet.DispatcherServlet#processHandlerException`

```java

@Nullable
protected ModelAndView processHandlerException(HttpServletRequest request,
                                               HttpServletResponse response, @Nullable Object handler, Exception ex) throws Exception {

    // 遍历所有注册的 HandlerExceptionResolver
    for (HandlerExceptionResolver resolver : this.handlerExceptionResolvers) {
        ModelAndView exMv = resolver.resolveException(request, response, handler, ex);
        if (exMv != null) {
            return exMv;  // 第一个能处理的 resolver 生效，后面的不执行
        }
    }
    throw ex;  // 没人处理，抛出去变成 500

}
```

默认的 Resolver 列表（按优先级）：

- ExceptionHandlerExceptionResolver → 处理 @ExceptionHandler
- ResponseStatusExceptionResolver → 处理 @ResponseStatus
- DefaultHandlerExceptionResolver → 处理 Spring 内置异常（如 NoHandlerFoundException）

##### 第3站：@ExceptionHandler 是怎么被找到的

类：
`org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver#doResolveHandlerMethodException`

```java

@Nullable
protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request,
                                                       HttpServletResponse response, @Nullable HandlerMethod handlerMethod, Exception exception) {

    // 1. 获取当前请求匹配的 @ExceptionHandler 方法
    ServletInvocableHandlerMethod exceptionHandlerMethod = getExceptionHandlerMethod(handlerMethod, exception);

    if (exceptionHandlerMethod == null) {
        return null;  // 没找到，交给下一个 resolver
    }

    // 2. 设置参数解析器、返回值处理器（和正常 Controller 一样）
    exceptionHandlerMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
    exceptionHandlerMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);

    // 3. 执行 @ExceptionHandler 方法
    exceptionHandlerMethod.invokeAndHandle(request, response, exception);

}
```

核心方法：getExceptionHandlerMethod()

类：`org.springframework.web.method.annotation.ExceptionHandlerMethodResolver#getMappedMethod`

它会：

- 遍历所有 @ControllerAdvice 类里标记了 @ExceptionHandler 的方法
- 比较异常类型：当前异常是否匹配 @ExceptionHandler(value = XxxException.class)
- 匹配规则：精确匹配 > 子类匹配 > 父类匹配
-

断点：在你的 @ControllerAdvice 类的 @ExceptionHandler 方法里打断点，同时在
ExceptionHandlerExceptionResolver.getExceptionHandlerMethod() 里也打一个，看匹配过程。

##### 第4站：@ControllerAdvice 的扫描时机

类：`org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver#initApplicationContext`

Spring 启动时扫描所有 @ControllerAdvice：

```java

@Override
protected void initApplicationContext() throws BeansException {
// 扫描所有 @ControllerAdvice Bean
    for (ControllerAdviceBean adviceBean : ControllerAdviceBean.findAnnotatedBeans(getApplicationContext())) {
// 解析这个 advice 里所有 @ExceptionHandler 方法
        Class<?> beanType = adviceBean.getBeanType();
        if (beanType != null) {
            ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(beanType);
            this.exceptionHandlerAdviceCache.put(adviceBean, resolver);
        }
    }
}
```

关键观察：@ControllerAdvice 是在 Spring 启动时就被扫描并缓存的，不是每次异常发生时才扫描。

##### 第5站：异常处理后的流程

回到 processDispatchResult：

- @ExceptionHandler 可以返回 ModelAndView（跳转错误页面）
- 也可以返回 @ResponseBody（返回 JSON 错误信息）
- 如果返回 ModelAndView，会走 render() 渲染视图
- 如果返回 @ResponseBody，由 RequestResponseBodyMethodProcessor 处理，不走视图渲染
  注意：异常处理完成后，仍然会执行拦截器的 afterCompletion()，并把原始异常传入 ex 参数。

给你画的完整流程图

```
正常请求（带拦截器 A→B，无异常）
DispatcherServlet.doDispatch()
├── getHandler() → HandlerExecutionChain [handler + A + B]
├── getHandlerAdapter()
├── applyPreHandle()
│ ├── A.preHandle() → true
│ └── B.preHandle() → true
├── ha.handle() → Controller 执行 → 返回 ModelAndView
├── applyPostHandle()          ← 倒序
│ ├── B.postHandle()
│ └── A.postHandle()
├── render() → 视图渲染
└── triggerAfterCompletion()    ← 倒序
├── B.afterCompletion(ex=null)
└── A.afterCompletion(ex=null)
异常请求（Controller 抛异常）
DispatcherServlet.doDispatch()
├── getHandler() → HandlerExecutionChain [handler + A + B]
├── applyPreHandle()
│ ├── A.preHandle() → true
│ └── B.preHandle() → true
├── ha.handle() → Controller 抛异常 → catch 住
│
├── processDispatchResult()
│ └── processHandlerException()
│ ├── ExceptionHandlerExceptionResolver
│ │ └── 找 @ControllerAdvice 的 @ExceptionHandler
│ │ └── 匹配成功 → 执行 → 返回 ModelAndView/JSON
│ └── （如果没匹配到，抛给上层 → 500）
│
├── applyPostHandle() ← 异常处理后仍然执行！
│ └── ...
├── render() ← 如果有 ModelAndView
└── triggerAfterCompletion(ex=原始异常)  ← 异常传入 afterCompletion
├── B.afterCompletion(ex)
└── A.afterCompletion(ex)
```

实际操作建议
实验1：验证拦截器顺序
在你的项目里配两个拦截器：

```java

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(new BInterceptor()).addPathPatterns("/**");
    }
}
```

在 A 和 B 的三个方法里都 System.out.println("A/B.xxx")，发起请求看控制台输出顺序。

实验2：验证 preHandle 返回 false
让 B.preHandle() 返回 false，
看：
Controller 是否执行？（否）
- A.afterCompletion() 是否执行？（是）
- B.afterCompletion() 是否执行？（否，因为 B.preHandle 没成功）

实验3：验证异常处理流程
Controller 里 throw new RuntimeException("test")
@ControllerAdvice 里 @ExceptionHandler(RuntimeException.class) 捕获
在 afterCompletion() 里打断点，看 ex 参数是否不为 null
实验4：验证 @ExceptionHandler 匹配优先级
同一个 @ControllerAdvice 里写两个方法：

@ExceptionHandler(Exception.class)          // 父类
public Result handle(Exception e) { ... }

@ExceptionHandler(RuntimeException.class)   // 子类
public Result handle(RuntimeException e) { ... }
Controller 抛 RuntimeException，看进哪个方法。（应该是子类匹配优先）

走完这四个实验，你不仅能画出流程图，还能在考试里写出源码级别的答案。

需要我把这个也写成 markdown 文件传到你服务器上吗？❤️‍🔥