# 学生管理系统 - 项目文档

## 项目概述

这是一个基于 **Spring Framework 6.2.3** 开发的学生管理系统（Student Management System），采用传统的 Java EE 分层架构，使用 Maven 构建，打包为 WAR 格式部署到支持 Jakarta EE 9+ 的 Servlet 容器（如 Tomcat 10.1+）。

项目主要实现了学生信息的增删改查（CRUD）功能，并集成了以下特性：
- **AOP 日志记录**：通过自定义 `@OperationLog` 注解实现操作日志自动记录到控制台和数据库
- **拦截器权限验证**：基于 Token 的简易认证机制
- **XSS 防护**：输入过滤和 HTML 实体编码
- **安全响应头**：通过过滤器添加多种安全响应头
- **Swagger API 文档**：OpenAPI 3.0 规范的自动化 API 文档

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 25 | Java 开发工具包 |
| Spring Framework | 6.2.3 | 核心框架（Spring MVC、Spring JDBC、Spring AOP） |
| MyBatis | 3.5.19 | ORM 持久层框架 |
| MyBatis-Spring | 3.0.4 | MyBatis 与 Spring 集成 |
| MySQL Connector | 9.2.0 | MySQL 数据库驱动 |
| Druid | 1.2.24 | 数据库连接池 |
| Jackson | 2.18.3 | JSON 序列化/反序列化 |
| Springdoc OpenAPI | 2.7.0 | Swagger/OpenAPI 文档 |
| Logback | 1.5.18 | 日志框架 |
| SLF4J | 2.0.16 | 日志门面 |
| AspectJ | 1.9.22.1 | AOP 实现 |
| Lombok | 1.18.36 | 代码简化工具 |
| Jakarta Servlet API | 6.0.0 | Servlet 规范（Jakarta EE 9+） |

## 项目结构

```
homework1/
├── docs/                          # 项目文档
│   ├── READEME.md                 # 项目运行指南
│   └── imgs/                      # 文档图片
├── sql/                           # 数据库脚本
│   └── init.sql                   # 数据库初始化脚本（含建表和示例数据）
└── zeyuli-job/                    # Maven 项目根目录
    ├── pom.xml                    # Maven 配置文件
    └── src/
        ├── main/
        │   ├── java/com/example/  # Java 源代码
        │   │   ├── annotation/    # 自定义注解
        │   │   │   └── OperationLog.java      # 操作日志注解
        │   │   ├── aspect/        # AOP 切面
        │   │   │   └── LogAspect.java         # 日志记录切面
        │   │   ├── common/        # 通用工具类
        │   │   │   ├── Result.java            # 统一响应结果封装
        │   │   │   └── XssUtils.java          # XSS 防护工具
        │   │   ├── config/        # 配置类
        │   │   │   ├── SecurityConfig.java    # 安全过滤器配置
        │   │   │   └── SwaggerConfig.java     # Swagger 文档配置
        │   │   ├── controller/    # 控制器层（REST API）
        │   │   │   └── StudentController.java
        │   │   ├── enums/         # 枚举类
        │   │   │   └── OperationEnum.java     # 操作类型枚举
        │   │   ├── interceptor/   # 拦截器
        │   │   │   └── AutoInterceptor.java   # 权限验证拦截器
        │   │   ├── mapper/        # 数据访问层接口
        │   │   │   ├── ClassInfoMapper.java
        │   │   │   ├── OperationLogMapper.java
        │   │   │   └── StudentMapper.java
        │   │   ├── pojo/          # 实体类（POJO）
        │   │   │   ├── ClassInfo.java
        │   │   │   ├── OperationLog.java
        │   │   │   └── Student.java
        │   │   ├── service/       # 服务层接口
        │   │   │   └── StudentService.java
        │   │   └── service/impl/  # 服务层实现
        │   │       └── StudentServiceImpl.java
        │   ├── resources/         # 配置文件
        │   │   ├── db.properties              # 数据库配置
        │   │   ├── logback.xml                # 日志配置（支持异步日志）
        │   │   ├── mappers/                   # MyBatis XML 映射文件
        │   │   │   ├── ClassInfoMapper.xml
        │   │   │   ├── OperationLogMapper.xml
        │   │   │   └── StudentMapper.xml
        │   │   └── spring/                    # Spring 配置文件
        │   │       ├── applicationContext.xml # Spring 核心配置
        │   │       └── spring-mvc.xml         # Spring MVC 配置
        │   └── webapp/            # Web 应用根目录
        │       ├── index.html     # 首页（含 API 说明）
        │       ├── 404.html       # 404 错误页面
        │       ├── 500.html       # 500 错误页面
        │       └── WEB-INF/
        │           └── web.xml    # Web 应用配置（Jakarta EE 6.0）
        └── test/java/             # 测试代码（当前为空）
```

## 架构说明

### 分层架构

1. **Controller 层** (`com.example.controller`)
   - 处理 HTTP 请求和响应
   - 提供 RESTful API 接口
   - 使用 `@RestController` 注解
   - 集成 Swagger/OpenAPI 注解生成 API 文档
   - 返回 `ResponseEntity<Result<T>>` 统一响应格式

2. **Service 层** (`com.example.service` / `com.example.service.impl`)
   - 业务逻辑处理
   - 接口与实现分离
   - 使用 `@Service` 标注实现类
   - 声明式事务管理（`@Transactional`）
   - 查询方法标记 `readOnly = true`
   - 写操作指定 `rollbackFor = Exception.class`
   - 使用 `@OperationLog` 注解记录操作日志

3. **Mapper/DAO 层** (`com.example.mapper`)
   - 数据访问层接口
   - 使用 MyBatis 实现
   - XML 映射文件位于 `resources/mappers/`
   - 使用 `@Param` 注解标注参数

4. **POJO 层** (`com.example.pojo`)
   - 实体类，与数据库表对应
   - 使用 Lombok `@Data` 简化代码
   - 实现 `Serializable` 接口
   - 包含：Student、ClassInfo、OperationLog

### 横切关注点

1. **AOP 日志** (`LogAspect`)
   - 基于 `@OperationLog` 注解的切面
   - 在方法成功执行后记录日志（`@AfterReturning`）
   - 控制台输出格式：`[时间] [操作类型] 类名.方法名 - 描述`
   - 同时记录操作日志到 `s_operation_log` 表
   - 包含 IP 地址、User-Agent 等信息
   - 支持敏感信息脱敏（如 password）
   - 日志记录失败不影响主业务流程

2. **权限拦截器** (`AutoInterceptor`)
   - 实现 `HandlerInterceptor` 接口
   - 拦截所有 HTTP 请求（除 Swagger 相关路径）
   - 验证请求头中的 `Authorization` Token
   - 支持 `Bearer token` 或直接 `token` 格式
   - 测试 Token: `valid-token-123456`
   - 验证失败返回 401 Unauthorized

3. **安全过滤器** (`SecurityConfig`)
   - 实现 `Filter` 接口
   - 添加安全响应头：
     - `X-Content-Type-Options: nosniff`
     - `X-Frame-Options: DENY`
     - `X-XSS-Protection: 1; mode=block`
     - `Content-Security-Policy`
     - `Referrer-Policy: strict-origin-when-cross-origin`

4. **XSS 防护** (`XssUtils`)
   - `filter()`：移除危险 HTML 标签和脚本
   - `encode()`：HTML 实体编码特殊字符
   - `sanitize()`：清理并过滤输入（trim + filter）

## 数据库设计

数据库名：`student_db`

### 表结构

**s_student** - 学生信息表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT AUTO_INCREMENT | 主键 |
| age | INT NOT NULL | 年龄 |
| name | VARCHAR(20) NOT NULL | 姓名 |
| class_id | INT | 班级 ID（外键，关联 s_class） |

**s_class** - 班级信息表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT AUTO_INCREMENT | 主键 |
| class_name | VARCHAR(20) NOT NULL | 班级名称 |

**s_operation_log** - 操作日志表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT AUTO_INCREMENT | 主键 |
| method_name | VARCHAR(100) NOT NULL | 操作方法名 |
| operation_time | DATETIME NOT NULL | 操作时间 |
| operation_desc | VARCHAR(255) | 操作描述 |
| ip_address | VARCHAR(50) | IP 地址 |
| user_agent | VARCHAR(255) | 用户代理 |

索引：
- `idx_operation_time` on `operation_time`
- `idx_method_name` on `method_name`

### 数据库配置

配置文件：`zeyuli-job/src/main/resources/db.properties`

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/student_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
jdbc.username=root
jdbc.password=root
```

### 初始化脚本

执行 `sql/init.sql` 创建数据库表和示例数据，包含：
- 3 个班级：计算机一班、计算机二班、软件工程一班
- 4 个学生：张三、李四、王五、赵六

## 构建与运行

### 环境要求

- JDK 25+
- Maven 3.9+
- MySQL 8.0+
- Tomcat 10.1+（**必须**，因使用 Jakarta EE 9+）

**⚠️ 重要提示：** 本项目使用 Jakarta EE 9+（`jakarta.servlet` 包名），必须使用 **Tomcat 10.1 或更高版本**。Tomcat 9 及以下版本无法运行。

### 构建命令

```bash
cd homework1/zeyuli-job
mvn clean package
```

构建成功后，在 `target/` 目录生成 `student-management.war`。

### 部署运行

1. 创建 MySQL 数据库并执行 `sql/init.sql`
2. 修改 `db.properties` 中的数据库连接信息
3. 将生成的 WAR 文件部署到 Tomcat 的 `webapps` 目录
4. 启动 Tomcat
5. 访问应用：`http://localhost:8080/student-management/`

### IDEA 中运行

1. 打开 IDEA，导入项目
2. 点击右上角 "Add Configuration"
3. 点击 "+"，选择 "Tomcat Server" → "Local"
4. 配置 Tomcat 安装路径（10.1+）
5. 在 "Deployment" 选项卡中，添加 Artifact `student-management:war`
6. 设置 Application context 为 `/student-management`
7. 点击运行按钮

## API 文档

启动应用后访问：

- **Swagger UI**：`http://localhost:8080/student-management/swagger-ui/index.html`
- **OpenAPI JSON**：`http://localhost:8080/student-management/v3/api-docs`

### 学生管理接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/student/{id}` | 根据 ID 查询学生 | 需要 |
| GET | `/student` | 查询所有学生 | 需要 |
| POST | `/student` | 添加学生 | 需要 |
| PUT | `/student/{id}` | 更新学生信息 | 需要 |
| DELETE | `/student/{id}` | 删除学生 | 需要 |

### 认证方式

除 Swagger 相关路径外，所有 API 请求需要在 Header 中携带：

```
Authorization: valid-token-123456
```

或使用 Bearer 格式：

```
Authorization: Bearer valid-token-123456
```

### 使用 curl 测试 API

```bash
# 1. 查询所有学生
curl -X GET http://localhost:8080/student-management/student \
  -H "Authorization: valid-token-123456"

# 2. 根据 ID 查询学生
curl -X GET http://localhost:8080/student-management/student/1 \
  -H "Authorization: valid-token-123456"

# 3. 添加学生
curl -X POST http://localhost:8080/student-management/student \
  -H "Content-Type: application/json" \
  -H "Authorization: valid-token-123456" \
  -d '{"name":"张三","age":20,"classId":1}'

# 4. 更新学生
curl -X PUT "http://localhost:8080/student-management/student/1?name=李雷&age=21" \
  -H "Authorization: valid-token-123456"

# 5. 删除学生
curl -X DELETE http://localhost:8080/student-management/student/1 \
  -H "Authorization: valid-token-123456"
```

## 核心配置文件

### Spring 核心配置 (`applicationContext.xml`)

- 加载数据库配置（`db.properties`）
- 启用注解扫描（排除 Controller）
- 配置 Druid 数据源（含连接池参数和防火墙）
- 配置 SqlSessionFactory（MyBatis）
  - 开启驼峰命名自动映射
  - 配置日志实现为 StdOutImpl
- 配置 Mapper 扫描
- 配置声明式事务管理器
- 启用 AOP 自动代理

### Spring MVC 配置 (`spring-mvc.xml`)

- 启用 Controller 注解扫描
- 配置注解驱动（包含 Jackson JSON 转换器）
  - 日期格式：`yyyy-MM-dd HH:mm:ss`
  - 序列化时忽略 null 值
- 配置静态资源处理
- 配置拦截器（排除 Swagger 路径）
- 配置跨域支持（CORS）
- 配置 Swagger 静态资源映射

### Web 配置 (`web.xml`)

- 字符编码过滤器（UTF-8）
- Spring 上下文监听器
- Spring MVC DispatcherServlet
- 欢迎页面（index.html）
- 错误页面配置（404、500）

## 日志配置

使用 Logback 记录日志，配置说明：

### 日志输出

- **控制台输出**：同步输出，格式 `%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n`
- **文件输出**：异步输出，按天滚动，保留 30 天，单个文件最大 100MB
- **错误日志**：单独输出到 `student-management-error.log`，仅 ERROR 级别

### 日志级别

- 根日志级别：INFO
- Spring 框架日志级别：WARN
- MyBatis Mapper 日志级别：DEBUG

### AOP 操作日志

控制台额外输出格式：`[yyyy.MM.dd HH:mm:ss] [操作类型] 类名.方法名 - 描述`

日志文件位置：`${catalina.base}/logs/student-management.log`

## 开发规范

### 代码风格

- 使用 **4 空格** 缩进
- 类名使用 **PascalCase**（如 `StudentController`）
- 方法名和变量名使用 **camelCase**（如 `findStudentById`）
- 包名使用全小写
- 注释使用 **中文**

### 分层规范

1. **Controller 层**
   - 使用 `@RestController` 注解
   - 使用 `@RequestMapping` 定义路径前缀
   - 返回 `ResponseEntity<Result<T>>` 统一响应格式
   - 使用 Swagger 注解标注 API 信息（`@Operation`、`@ApiResponse` 等）

2. **Service 层**
   - 接口与实现分离
   - 实现类使用 `@Service` 注解
   - 使用 `@OperationLog` 注解记录操作日志
   - 事务方法使用 `@Transactional` 注解
   - 查询方法标记 `readOnly = true`
   - 写操作指定 `rollbackFor = Exception.class`
   - 对用户输入进行 XSS 过滤（`XssUtils.sanitize`）
   - 对输出数据进行 XSS 编码（`XssUtils.encode`）

3. **Mapper 层**
   - 定义接口方法
   - 使用 `@Param` 注解标注参数
   - SQL 写在对应的 XML 文件中
   - 使用动态 SQL（`<if>`、`<set>` 等）

4. **POJO 类**
   - 使用 Lombok `@Data`、`@NoArgsConstructor`、`@AllArgsConstructor`
   - 实现 `Serializable` 接口
   - 定义 `serialVersionUID`

### @OperationLog 注解使用

```java
@OperationLog(OperationEnum.QUERY)  // 简单记录，使用枚举默认描述
@OperationLog(value = OperationEnum.UPDATE, desc = "更新学生信息")  // 自定义描述
@OperationLog(value = OperationEnum.CREATE, desc = "添加学生", saveParams = true)  // 记录参数
```

属性说明：
- `value`：操作类型（QUERY、CREATE、UPDATE、DELETE、LOGIN、LOGOUT、EXPORT、IMPORT、OTHER）
- `desc`：自定义操作描述（可选）
- `saveParams`：是否保存请求参数（默认 true）
- `saveResult`：是否保存响应结果（默认 false）

## 安全考虑

1. **XSS 防护**
   - 输入过滤：移除危险的 HTML 标签和脚本
   - 输出编码：对响应数据进行 HTML 实体编码
   - 响应头：`X-XSS-Protection`、`Content-Security-Policy`

2. **认证授权**
   - Token 认证：基于 `Authorization` 请求头
   - 拦截器验证：未携带有效 Token 返回 401
   - 注意：当前实现为简化版，生产环境建议使用 JWT

3. **其他安全响应头**
   - `X-Content-Type-Options: nosniff` - 防止 MIME 嗅探
   - `X-Frame-Options: DENY` - 防止点击劫持
   - `Referrer-Policy` - 控制 Referrer 信息

4. **SQL 注入防护**
   - 使用 MyBatis 参数化查询
   - Druid 连接池内置 SQL 防火墙

## 测试说明

当前项目未包含单元测试。如需添加测试，建议在 `src/test/java` 目录下创建：

- **Controller 层测试**：使用 `MockMvc`
- **Service 层测试**：使用 Mockito 进行 mock
- **Mapper 层测试**：使用内存数据库（如 H2）

## 常见问题

### Q1: 启动报错 `ClassNotFoundException: jakarta.servlet.ServletContext`

**原因：** Tomcat 版本过低，本项目需要 Tomcat 10.1+（支持 Jakarta EE 9+）。

**解决：** 升级 Tomcat 到 10.1 或更高版本。

### Q2: 数据库连接失败

**检查：**
1. MySQL 服务是否启动
2. `db.properties` 中的用户名、密码是否正确
3. 数据库 `student_db` 是否已创建
4. 是否执行了 `init.sql` 脚本

### Q3: 接口返回 401 未授权

**检查：**
1. 请求头中是否包含 `Authorization`
2. Token 是否为 `valid-token-123456`
3. 注意 Swagger UI 页面不需要认证，但直接调用 API 需要

### Q4: 中文乱码

**检查：**
1. 数据库字符集是否为 `utf8mb4`
2. JDBC URL 是否包含 `useUnicode=true&characterEncoding=utf8`
3. 请求头是否设置 `Content-Type: application/json;charset=UTF-8`
