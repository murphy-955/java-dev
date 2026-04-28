## 第1题（5分）

### 简述 Spring Bean 的生命周期，并列举4种初始化方法的实现方式。

**Q: Spring Bean 的生命周期**
**A：**

1. 生成BD：扫描xml/注解信息，生成`BeanDefinition`，注册到`BeanDefinatioRegistor`
2. 实例化：处理@Autowired\@Resource以及setter
   方法注入依赖，通过jvm反射生成裸对象，将并放入一级缓存（k是Object，v是lombard表达式的ObjectFactory）。并在这里提供AOP钩子
3. BeanNameAware回调处理：用于获取BeanName等相关配置
4. 初始化： BeanPostProcessor（以下简称BPP）.postProcessorBeforeInitialization(), → @PostConstruct修饰的方法 →
   @Bean制定的initMethod方法 → 初始化对象 → BPP.PostProcessor.postProcessorAfterInitialization()
5. AOP创建（可选）：匹配PointCut --> ProxyFactory构建代理对象 --> CGLIB/JDK动态代理创建代理对象
6. 完成(Bean的创建)：清理二、三级缓存。将对象放入一级缓存中。
7. 销毁：@PreDestroy修饰的方法 --> 实现了DisposableBean接口的destroy()方法 --> @Bean指定的destroyMethod方法

**Q：4种初始化方法的实现方式。**
**A：**
按执行顺序先后依次是

1. BPP.postProcessorBeforeInitialization()
2. @PostConstruct修饰的方法
3. @Bean指定的initMethod方法
4. BPP.postProcessorAfterInitialization()

## 第2题（5分）

### 什么是 AOP？简述 Spring AOP 中 JDK 动态代理和 CGLIB 动态代理的区别及适用场景。

**Q：什么是AOP？**
**A：**

面向切面编程，允许程序员将重复的代码抽离出来，形成新的模块，从而提高代码的复用性。
举个栗子：

```Service.java
@CommonLog
public Object doSomething() {}
```

```CommonLog.java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommonLog {}
```

```CommonLogAspect.java
// 标识一个切面类
@Aspect
@Component
public class CommonLogAspect { 
   // 切点，标识为@CommonLog注解修饰的方法进行动态代理
   @Pointcut("@annation(com.zeyuli.annation.CommonLog)")
   public void pointcut() {}
   
   // 环绕通知
   @Around("pointcut()")
   public Object doSomethingAroundMethod(ProceedingJoinPoint jp) throws Throwable {
      // jp是这里的切点，可以对它做一些不可描述的事情
      Object[] args = jp.getArgs();
      Object result = jp.proceed(args);
      
      .................
   }
}
```

**Q:JDK 动态代理和 CGLIB 动态代理的区别及适用场景**
**A：**

| 名称   | 底层原理                    | 性能                        | 场景                            |   
|------|-------------------------|---------------------------|-------------------------------|
| JDK  | 需要被代理的对象实现接口，通过反射执行目标方法 | 创建代理较快，但是运行较慢             | 如果对象已经extends别的类了，只能使用jdk动态代理 |   
| CGLB | 由对象extend代理对象，运行时创建代理对象 | 创建代理对象较慢，但是运行更快，有ASM字节码优化 | 没有接口，且不为final的类               |                 |

## 第3题（5分）

### 简述 Spring MVC 的执行流程，从用户请求到响应返回的完整过程。

**Q：Spring MVC 的执行流程，从用户请求到响应返回的完整过程。**
**A:**

1. 用户请求到达服务器，从服务器网卡，经过tomcat服务器的Acpector、Poller线程，最终进入worker线程处理。
2. `DispatchorServlet` 交给`HandlerMapping`处理，返回`HandlerAdapter`。
3. `HandlerAdpter`处理，交给`Handler`处理，最终返回。

## 第4题（5分）

### 简述 SSM 框架整合的思路，以及 Spring、Spring MVC、MyBatis 三个框架各自的分工。

**Q:SSM 框架整合的思路**
**A:**
SSM 框架整合思路：

1. 创建一个项目，并引入Spring、Spring MVC、MyBatis。
2. 配置数据库url、用户名、密码。配置数据库连接池
3. 配置MVC

**Q:Spring、Spring MVC、MyBatis 分别负责什么？**
**A:**

- **Spring：** 将对象的生命周期交给IOC管理，而不是程序员。负责DI，AOP，事务的管理
- **Spring MVC：** 处理前端的请求（序炼化、反序列化、参数的简单校验），返回响应。不用编写繁杂的Servlet代码
- **MyBatis：** 负责数据库的连接、数据的持久化，执行SQL，返回结果。

## 第5题（5分）

### MyBatis 中 #{} 和 ${} 的区别是什么？什么时候必须用 ${}？

**Q：MyBatis 中 #{} 和 ${} 的区别是什么？**
**A：**

| 名称    | 区别         |
|-------|------------|
| `#{}` | 预编译替换的字符   |
| `${}` | 直接替换{}内的字符 |

如执行`SELECT * FROM user WHERE pwd=? AND name=?`这一语句。如果有恶意输入（`OR 1=1`），那么`${}`会将sql语句替换成
`SELECT * FROM user WHERE pwd= OR 1=1`
而`#{}`则将sql语句编译成

```sql
SELECT *
FROM user
WHERE pwd = `OR 1=1`
  AND name = `OR 1=1`
```

**什么时候必须用 ${}**
**A:**

对于表名/列名等无法使用预编译的字段，必须使用${}。但建议配合白名单，在Java代码层面做校验。

## 第6题（5分）

### 简述 MyBatis 动态 SQL 中 `<where>`、`<set>`、`<trim>` 三个标签的作用及区别。

**Q：MyBatis 动态 SQL 中 `<where>`、`<set>`、`<trim>` 三个标签的作用及区别。**
**A:**

| 标签       | 作用         | 区别                             |
|----------|------------|--------------------------------|
| `<where>` | 替代 WHERE语句 | 如果条件为空，则不添加WHERE               |
| `<set>`  | 替代SET语句    | 如果条件为空，则不添加SET                 |
| `<trim>` | 进行sql语句的修剪 | 添加的sql语句会进行修剪，去掉多余的`AND`、`OR`等 |

## 第7题（5分）

### MyBatis 的一级缓存和二级缓存有什么区别？如何开启二级缓存？

**Q：MyBatis 的一级缓存和二级缓存有什么区别？**
**A：**
一级缓存：Spring IOC容器中，一个Bean对象对应一个一级缓存。
二级缓存：Spring IOC容器中，多个Bean对象对应一个二级缓存。

## 第8题（5分）

### 在 Spring 中，@Autowired 和 @Resource 有什么区别？@Qualifier 的作用是什么？

**Q:@Autowired 和 @Resource 有什么区别？**
**A:**

| 名称         | 区别            |
|------------|---------------|
| @Autowired | spring提供的注解规范 |
| @Resource  | jdk提供的注解规范    |

**Q:@Qualifier的作用是什么**
**A:**

`@Qualifier` 用于给一个Bean起别名，`@Autowired`默认按照类型进行注入，如果多个Bean类型相同，则按照`@Qualifier`指定的别名进行注入。


