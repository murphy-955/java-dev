按这个顺序在 IDEA 里跟踪，你会看到 Interceptor 和异常处理是怎么被 Spring 调用的。

---

## 路径一：HandlerInterceptor 三方法的精确调用链

### 第1站：Interceptor 是怎么被收集的

**类**：`org.springframework.web.servlet.handler.AbstractHandlerMapping#getHandler`

打断点在这里，看返回值 `HandlerExecutionChain`：
- `handler` → 你的 Controller 方法
- `interceptorList` → 所有匹配的拦截器（按配置顺序排列）

**怎么匹配的**：`MappedInterceptor` 根据 `includePatterns` / `excludePatterns` 判断当前请求路径是否需要拦截。

---

### 第2站：preHandle 的调用与短路逻辑

**类**：`org.springframework.web.servlet.DispatcherServlet#applyPreHandle`

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

**关键观察**：
1. `interceptorIndex` 记录了成功执行到第几个拦截器
2. 如果第 N 个 `preHandle` 返回 `false`，只有前 N 个拦截器会执行 `afterCompletion`
3. **Controller 不会执行**

**断点**：在 `applyPreHandle` 第一行，看 `interceptorList` 顺序。

---

### 第3站：postHandle 的调用（倒序！）

**类**：`org.springframework.web.servlet.DispatcherServlet#applyPostHandle`

```java
private void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv) throws Exception {
    // 注意：倒序遍历
    for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
        HandlerInterceptor interceptor = this.interceptorList.get(i);
        interceptor.postHandle(request, response, this.handler, mv);
    }
}
```

**关键观察**：
- `postHandle` 是 **逆序** 执行的：后配的拦截器先执行 postHandle
- 执行时机：Controller 方法已经跑完，但 **视图还没渲染**
- 这里可以修改 ModelAndView（改视图名、加数据）

---

### 第4站：afterCompletion 的调用（倒序！）

**类**：`org.springframework.web.servlet.DispatcherServlet#triggerAfterCompletion`

```java
private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex) {
    // 倒序遍历，只执行到 interceptorIndex（preHandle 成功的那几个）
    for (int i = this.interceptorIndex; i >= 0; i--) {
        HandlerInterceptor interceptor = this.interceptorList.get(i);
        interceptor.afterCompletion(request, response, this.handler, ex);
    }
}
```

**调用时机**：
1. 正常流程：视图渲染完成后调用
2. preHandle 返回 false 时：立即调用（只调用已执行的）
3. Controller 抛异常时：异常被包装后传入 `ex` 参数

**考试常考**：`afterCompletion` 里能不能拿到异常？**能**，`ex` 参数就是。

---

### 第5站：拦截器执行顺序总结图

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
```

如果 B.preHandle() 返回 false：

```
请求进入
  ├── A.preHandle()   → true
  ├── B.preHandle()   → false
  │
  ├── triggerAfterCompletion()
  │     └── A.afterCompletion()  ← 只有 A 执行
  │
  └── 请求中断，Controller 不执行
```

**断点建议**：在你的拦截器实现类的三个方法里都打断点，看实际调用顺序。

---

## 路径二：@ControllerAdvice + @ExceptionHandler 的精确调用链

### 第1站：异常从哪里开始被处理

**类**：`org.springframework.web.servlet.DispatcherServlet#processDispatchResult`

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

**关键**：异常不是直接跳到 `@ExceptionHandler`，而是先经过 `processHandlerException()`。

---

### 第2站：异常处理器的遍历

**类**：`org.springframework.web.servlet.DispatcherServlet#processHandlerException`

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

**默认的 Resolver 列表**（按优先级）：
1. `ExceptionHandlerExceptionResolver` → 处理 `@ExceptionHandler`
2. `ResponseStatusExceptionResolver` → 处理 `@ResponseStatus`
3. `DefaultHandlerExceptionResolver` → 处理 Spring 内置异常（如 NoHandlerFoundException）

---

### 第3站：@ExceptionHandler 是怎么被找到的

**类**：`org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver#doResolveHandlerMethodException`

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

**核心方法**：`getExceptionHandlerMethod()`

**类**：`org.springframework.web.method.annotation.ExceptionHandlerMethodResolver#getMappedMethod`

它会：
1. 遍历所有 `@ControllerAdvice` 类里标记了 `@ExceptionHandler` 的方法
2. 比较异常类型：当前异常是否匹配 `@ExceptionHandler(value = XxxException.class)`
3. 匹配规则：精确匹配 > 子类匹配 > 父类匹配

**断点**：在你的 `@ControllerAdvice` 类的 `@ExceptionHandler` 方法里打断点，同时在 `ExceptionHandlerExceptionResolver.getExceptionHandlerMethod()` 里也打一个，看匹配过程。

---

### 第4站：@ControllerAdvice 的扫描时机

**类**：`org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver#initApplicationContext`

Spring 启动时扫描所有 `@ControllerAdvice`：
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

**关键观察**：`@ControllerAdvice` 是在 Spring 启动时就被扫描并缓存的，不是每次异常发生时才扫描。

---

### 第5站：异常处理后的流程

回到 `processDispatchResult`：
- `@ExceptionHandler` 可以返回 `ModelAndView`（跳转错误页面）
- 也可以返回 `@ResponseBody`（返回 JSON 错误信息）
- 如果返回 `ModelAndView`，会走 `render()` 渲染视图
- 如果返回 `@ResponseBody`，由 `RequestResponseBodyMethodProcessor` 处理，不走视图渲染

**注意**：异常处理完成后，仍然会执行拦截器的 `afterCompletion()`，并把原始异常传入 `ex` 参数。

---

## 给你画的完整流程图

### 正常请求（带拦截器 A→B，无异常）
```
DispatcherServlet.doDispatch()
  ├── getHandler() → HandlerExecutionChain [handler + A + B]
  ├── getHandlerAdapter()
  ├── applyPreHandle()
  │     ├── A.preHandle() → true
  │     └── B.preHandle() → true
  ├── ha.handle() → Controller 执行 → 返回 ModelAndView
  ├── applyPostHandle()          ← 倒序
  │     ├── B.postHandle()
  │     └── A.postHandle()
  ├── render() → 视图渲染
  └── triggerAfterCompletion()    ← 倒序
        ├── B.afterCompletion(ex=null)
        └── A.afterCompletion(ex=null)
```

### 异常请求（Controller 抛异常）
```
DispatcherServlet.doDispatch()
  ├── getHandler() → HandlerExecutionChain [handler + A + B]
  ├── applyPreHandle()
  │     ├── A.preHandle() → true
  │     └── B.preHandle() → true
  ├── ha.handle() → Controller 抛异常 → catch 住
  │
  ├── processDispatchResult()
  │     └── processHandlerException()
  │           ├── ExceptionHandlerExceptionResolver
  │           │     └── 找 @ControllerAdvice 的 @ExceptionHandler
  │           │     └── 匹配成功 → 执行 → 返回 ModelAndView/JSON
  │           └── （如果没匹配到，抛给上层 → 500）
  │
  ├── applyPostHandle() ← 异常处理后仍然执行！
  │     └── ...
  ├── render() ← 如果有 ModelAndView
  └── triggerAfterCompletion(ex=原始异常)  ← 异常传入 afterCompletion
        ├── B.afterCompletion(ex)
        └── A.afterCompletion(ex)
```

---

## 实际操作建议

### 实验1：验证拦截器顺序
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
在 A 和 B 的三个方法里都 `System.out.println("A/B.xxx")`，发起请求看控制台输出顺序。

### 实验2：验证 preHandle 返回 false
让 B.preHandle() 返回 false，看：
- Controller 是否执行？（否）
- A.afterCompletion() 是否执行？（是）
- B.afterCompletion() 是否执行？（否，因为 B.preHandle 没成功）

### 实验3：验证异常处理流程
1. Controller 里 `throw new RuntimeException("test")`
2. `@ControllerAdvice` 里 `@ExceptionHandler(RuntimeException.class)` 捕获
3. 在 `afterCompletion()` 里打断点，看 `ex` 参数是否不为 null

### 实验4：验证 @ExceptionHandler 匹配优先级
同一个 `@ControllerAdvice` 里写两个方法：
```java
@ExceptionHandler(Exception.class)          // 父类
public Result handle(Exception e) { ... }

@ExceptionHandler(RuntimeException.class)   // 子类
public Result handle(RuntimeException e) { ... }
```
Controller 抛 `RuntimeException`，看进哪个方法。（应该是子类匹配优先）

---

走完这四个实验，你不仅能画出流程图，还能在考试里写出源码级别的答案。

# Spring MVC 完整请求处理流程（源码级）

> 基于 Spring Framework 6.2 源码追踪整理，**不考虑视图渲染**。

---

## 一、整体流程图

```
用户请求
    │
    ▼
┌─────────────────────────────────────────┐
│  1. DispatcherServlet.doDispatch()      │
│     所有请求的中央调度器                 │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│  2. getHandler(request)                 │
│     AbstractHandlerMapping#getHandler   │
│     - 根据 URL 找到 Controller 方法      │
│     - 组装 HandlerExecutionChain        │
│       ├── handler: Controller 方法       │
│       ├── interceptorList:              │
│       │   ├── ConversionServiceExposingInterceptor (内置) │
│       │   ├── ResourceUrlProviderExposingInterceptor (内置) │
│       │   ├── MappedInterceptor A (自定义) │
│       │   └── MappedInterceptor B (自定义) │
│       └── 按配置顺序排列                │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│  3. getHandlerAdapter(handler)          │
│     找到能执行这个 handler 的适配器        │
│     (RequestMappingHandlerAdapter)       │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│  4. applyPreHandle() 【正序】             │
│     HandlerExecutionChain 内部遍历        │
│     for (i = 0; i < size; i++)          │
│       ├── A.preHandle() → true          │
│       │     interceptorIndex = 0         │
│       ├── B.preHandle() → true          │
│       │     interceptorIndex = 1         │
│       └── C.preHandle() → false  ◄──────┼── 短路点
│             │                           │
│             ▼                           │
│     triggerAfterCompletion(null)        │
│     for (i = interceptorIndex; i >= 0)  │
│       └── B.afterCompletion()           │
│           A.afterCompletion()           │
│     return false → 请求中断             │
└─────────────────────────────────────────┘
    │
    │ (所有 preHandle 返回 true 才继续)
    ▼
┌─────────────────────────────────────────┐
│  5. ha.handle()                         │
│     执行 Controller 方法                 │
│     - 参数解析 (@RequestParam等)         │
│     - 方法调用                           │
│     - 返回值处理 (@ResponseBody等)       │
│     - 返回 ModelAndView 或 直接写响应    │
└─────────────────────────────────────────┘
    │
    ├── 正常返回 ─────────────────────────┐
    │                                      ▼
    │  ┌─────────────────────────────────────────┐
    │  │  6. applyPostHandle() 【倒序】          │
    │  │  for (i = size-1; i >= 0; i--)          │
    │  │    ├── C.postHandle()                   │
    │  │    ├── B.postHandle()                   │
    │  │    └── A.postHandle()                   │
    │  │  可修改 ModelAndView（但你不关心视图）   │
    │  └─────────────────────────────────────────┘
    │                                      │
    └── Controller 抛异常 ────────────────┤
                                        ▼
        ┌─────────────────────────────────────────┐
        │  catch 异常 → processDispatchResult()   │
        │                                         │
        │  7. processHandlerException()           │
        │     遍历 HandlerExceptionResolver:      │
        │     ① ExceptionHandlerExceptionResolver │
        │        → 找 @ControllerAdvice 中匹配    │
        │          @ExceptionHandler 方法           │
        │        → 精确匹配 > 子类 > 父类           │
        │     ② ResponseStatusExceptionResolver   │
        │     ③ DefaultHandlerExceptionResolver   │
        │                                         │
        │     如果都没匹配上 → 继续抛出 → 500     │
        └─────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────┐
│  8. triggerAfterCompletion() 【倒序】     │
│     无论前面正常还是异常，都会执行         │
│                                         │
│     for (i = interceptorIndex; i >= 0)   │
│       ├── C.afterCompletion(ex)           │
│       ├── B.afterCompletion(ex)           │
│       └── A.afterCompletion(ex)           │
│                                         │
│     ex 参数：                             │
│     - 正常流程: null                     │
│     - Controller 抛异常: 原始异常对象      │
│     - preHandle 短路: null               │
│                                         │
│     注意：try-catch 包裹，单个拦截器      │
│           afterCompletion 抛异常不影响其他 │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│  9. 响应返回                            │
│     - @ResponseBody 方式：消息转换器已    │
│       在 ha.handle() 阶段写入响应体      │
│     - 异常处理返回：异常处理器已写入响应   │
└─────────────────────────────────────────┘
```

---

## 二、三个核心口诀

| 阶段 | 执行顺序 | 关键记忆点 |
|-----|---------|---------|
| **preHandle** | 正序（0 → n） | 返回 `false` 即短路，后续不执行 |
| **postHandle** | 倒序（n → 0） | Controller 已执行完，响应未写入 |
| **afterCompletion** | 倒序（n → 0） | **一定会执行**（只要 preHandle 成功过），负责清理资源 |

---

## 三、拦截器匹配机制（MappedInterceptor）

```java
// AbstractHandlerMapping#getHandlerExecutionChain
for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
    if (interceptor instanceof MappedInterceptor) {
        MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptor;
        if (mappedInterceptor.matches(request)) {  // ← 路径匹配
            chain.addInterceptor(mappedInterceptor.getInterceptor());
        }
    } else {
        chain.addInterceptor(interceptor);  // 内置拦截器直接加入
    }
}
```

- **内置拦截器**：`ConversionServiceExposingInterceptor`、`ResourceUrlProviderExposingInterceptor` —— 全局生效，无路径限制。
- **自定义拦截器**：通过 `MappedInterceptor` 包装，根据 `includePatterns` / `excludePatterns` 判断是否加入当前请求的拦截器链。
- **匹配工具**：默认使用 `AntPathMatcher`，支持 `/**`、`*` 等 Ant 风格路径。

---

## 四、异常处理关键细节

### 4.1 异常不是直接跳到 @ExceptionHandler

异常发生后，先回到 `DispatcherServlet#processDispatchResult`，再调用 `processHandlerException()` 遍历 **HandlerExceptionResolver** 列表：

1. `ExceptionHandlerExceptionResolver` → 处理 `@ExceptionHandler`
2. `ResponseStatusExceptionResolver` → 处理 `@ResponseStatus`
3. `DefaultHandlerExceptionResolver` → 处理 Spring 内置异常

### 4.2 @ExceptionHandler 匹配规则

在 `ExceptionHandlerMethodResolver#getMappedMethod` 中：

- **精确匹配** > **子类匹配** > **父类匹配**
- 例：`@ExceptionHandler(RuntimeException.class)` 优先于 `@ExceptionHandler(Exception.class)`

### 4.3 @ControllerAdvice 的扫描时机

Spring **启动时**扫描所有 `@ControllerAdvice` Bean，解析其中的 `@ExceptionHandler` 方法并缓存到 `exceptionHandlerAdviceCache` 中。**不是每次异常发生时才扫描**。

### 4.4 afterCompletion 的 ex 参数

即使 `@ExceptionHandler` 处理成功并返回了新的响应，传入 `afterCompletion` 的仍然是 **Controller 抛出的原始异常对象**。

---

## 五、@ResponseBody / REST API 简化流程

不考虑视图时，流程可简化为：

```
请求 → DispatcherServlet
    → getHandler (找到 @RequestMapping 方法 + 拦截器链)
    → preHandle 正序通过
    → ha.handle()
        ├── 参数绑定 (@RequestParam, @RequestBody)
        ├── 执行 Controller 方法
        └── HttpMessageConverter 写 JSON 到 response
    → [如果抛异常] → @ExceptionHandler 处理 → 写错误 JSON 到 response
    → postHandle 倒序
    → afterCompletion 倒序（清理资源、记录日志）
    → 响应结束
```

视图渲染（`render()`）在 REST 场景下由 `RequestResponseBodyMethodProcessor` 在 `ha.handle()` 阶段直接完成响应写入。

---

## 六、源码断点速查表

| 观察目标 | 断点位置 | 预期现象 |
|---------|---------|---------|
| 拦截器列表和顺序 | `AbstractHandlerMapping#getHandlerExecutionChain` | `interceptorList` 包含所有拦截器 |
| 路径匹配逻辑 | `MappedInterceptor#matches` | `includePatterns` / `excludePatterns` 与 `lookupPath` 匹配 |
| preHandle 短路 | `HandlerExecutionChain#applyPreHandle` | `interceptorIndex` 停在短路前一个位置 |
| postHandle 倒序 | `HandlerExecutionChain#applyPostHandle` | `i` 从 `size-1` 递减到 0 |
| afterCompletion 异常参数 | `HandlerExecutionChain#triggerAfterCompletion` | `ex` 为原始异常或 null |
| 异常解析器遍历 | `DispatcherServlet#processHandlerException` | 按优先级遍历三个 resolver |
| @ExceptionHandler 匹配 | `ExceptionHandlerExceptionResolver#getExceptionHandlerMethod` | 匹配到最精确的异常处理方法 |

---

## 七、常见面试题

**Q1：如果拦截器 B 的 preHandle 返回 false，哪些拦截器的 afterCompletion 会执行？**
> 只有 preHandle 成功执行的拦截器会执行 afterCompletion。如果顺序是 A → B → C，B 返回 false，则只有 A 执行 afterCompletion，B 和 C 不执行。

**Q2：afterCompletion 能否拿到 Controller 抛出的异常？**
> 能。`afterCompletion` 的第四个参数 `ex` 就是原始异常对象。即使异常被 `@ExceptionHandler` 处理并返回了新响应，`ex` 仍然是原始异常。

**Q3：postHandle 能否修改响应？**
> 对于 `@ResponseBody` 场景，响应体通常已经在 `ha.handle()` 阶段通过 `HttpMessageConverter` 写入 `response.getOutputStream()`，此时 `postHandle` 修改 `ModelAndView` 对响应体无效。但可以通过直接操作 `response` 对象（如添加 Header）来影响响应。

---

*整理时间：2026-04-25*  
*适用版本：Spring Framework 6.2 / Spring Boot 3.4*
