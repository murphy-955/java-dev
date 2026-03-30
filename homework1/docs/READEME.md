# 学生管理系统 - 项目运行指南

## 项目简介

这是一个基于 **Spring Framework 6.x** + **MyBatis** 开发的学生管理系统，采用 RESTful API 设计风格，实现了学生信息的增删改查（CRUD）功能，并集成了
AOP 日志记录、拦截器权限验证、Swagger API 文档等特性。

## 技术栈

| 技术               | 版本     | 说明                                      |
|------------------|--------|-----------------------------------------|
| JDK              | 25+    | Java 开发工具包                              |
| Spring Framework | 6.2.3  | 核心框架（Spring MVC、Spring JDBC、Spring AOP） |
| MyBatis          | 3.5.19 | ORM 持久层框架                               |
| MySQL            | 8.0+   | 数据库                                     |
| Tomcat           | 10.1+  | Servlet 容器（需支持 Jakarta EE 9+）           |
| Maven            | 3.9+   | 构建工具                                    |

## 环境准备

### 1. 安装 JDK 25

下载并安装 JDK 25，配置环境变量：

```bash
# 验证安装
java -version
# 输出应显示 java version "25" 或更高版本
```

### 2. 安装 Maven

下载并安装 Maven 3.9+，配置环境变量：

```bash
# 验证安装
mvn -version
```

### 3. 安装 MySQL 8.0+

下载并安装 MySQL 8.0+，创建数据库用户。

### 4. 安装 Tomcat 10.1+

**⚠️ 重要提示：** 本项目使用 Jakarta EE 9+（`jakarta.servlet` 包名），必须使用 **Tomcat 10.1 或更高版本**。Tomcat 9
及以下版本无法运行。

## 项目运行步骤

### 步骤一：克隆/下载项目

```bash
cd homework1/zeyuli-job
```

### 步骤二：初始化数据库

1. 登录 MySQL，创建数据库：

```sql
CREATE
DATABASE student_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本（位于 `sql/init.sql`）：

```bash
# 方式1：命令行执行
mysql -u root -p student_db < sql/init.sql

# 方式2：使用 MySQL 客户端工具（如 Navicat、DataGrip）导入执行
```

脚本将创建以下表：

- `s_student` - 学生信息表
- `s_class` - 班级信息表
- `s_operation_log` - 操作日志表

并插入示例数据。

### 步骤三：配置数据库连接

编辑 `src/main/resources/db.properties`：

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/student_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
jdbc.username=root
jdbc.password=你的密码
```

根据你的 MySQL 配置修改 `username` 和 `password`。

### 步骤四：构建项目

在项目根目录（`zeyuli-job`）执行：

```bash
mvn clean package
```

构建成功后，会在 `target/` 目录生成 `student-management.war` 文件。

### 步骤五：部署到 Tomcat

**方式1：自动部署（推荐）**

将生成的 `student-management.war` 复制到 Tomcat 的 `webapps` 目录：

```bash
cp target/student-management.war /path/to/tomcat/webapps/
```

启动 Tomcat：

```bash
# Linux/Mac
/path/to/tomcat/bin/startup.sh

# Windows
/path/to/tomcat/bin/startup.bat
```

**方式2：IDEA 中配置 Tomcat 运行**

1. 打开 IDEA，导入项目
2. 点击右上角 "Add Configuration"
3. 点击 "+"，选择 "Tomcat Server" → "Local"
4. 配置 Tomcat 安装路径
5. 在 "Deployment" 选项卡中，点击 "+" 添加 Artifact，选择 `student-management:war`
6. 设置 Application context 为 `/student-management`
7. 点击运行按钮

### 步骤六：验证部署

1. **访问首页**：
   ```
   http://localhost:8080/student-management/
   ```

2. **访问 Swagger API 文档**：
   ```
   http://localhost:8080/student-management/swagger-ui/index.html
   ```

3. **查看 OpenAPI JSON**：
   ```
   http://localhost:8080/student-management/v3/api-docs
   ```

## API 使用说明

### 认证方式

除 Swagger 相关路径外，所有 API 请求需要在请求头中携带 Token：

```
Authorization: valid-token-123456
```

或使用 Bearer 格式：

```
Authorization: Bearer valid-token-123456
```

### API 接口列表

| 方法     | 路径              | 说明                   |
|--------|-----------------|----------------------|
| GET    | `/student/{id}` | 根据 ID 查询学生           |
| GET    | `/student`      | 查询所有学生               |
| POST   | `/student`      | 添加学生（JSON 请求体）       |
| PUT    | `/student/{id}` | 更新学生信息（name, age 参数） |
| DELETE | `/student/{id}` | 删除学生                 |

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

### 使用 Postman 测试

1. 导入 OpenAPI 定义：`http://localhost:8080/student-management/v3/api-docs`
2. 或在请求头中添加 `Authorization: valid-token-123456`
3. 发送请求测试各接口

## 日志查看

### 控制台日志

启动 Tomcat 后，日志会直接输出到控制台，格式如下：

```
2026-03-27 10:30:15 [http-nio-8080-exec-1] INFO  com.example.interceptor.AutoInterceptor - [2026-03-27 10:30:15] 拦截到请求: GET /student-management/student
2026-03-27 10:30:15 [http-nio-8080-exec-1] INFO  com.example.aspect.LogAspect - [2026.03.27 10:30:15] [查询] StudentServiceImpl.findAllStudents - 查询所有学生列表
```

### 文件日志

日志文件默认输出到 Tomcat 的 `logs` 目录：

- `student-management.log` - 所有级别日志（INFO 及以上）
- `student-management-error.log` - 仅错误日志（ERROR 级别）

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

## 项目结构

```
zeyuli-job/
├── src/main/java/com/example/
│   ├── annotation/      # 自定义注解（@OperationLog）
│   ├── aspect/          # AOP 切面（日志记录）
│   ├── common/          # 通用工具类（Result, XssUtils）
│   ├── config/          # 配置类（SecurityConfig, SwaggerConfig）
│   ├── controller/      # 控制器层（REST API）
│   ├── enums/           # 枚举类（OperationType）
│   ├── interceptor/     # 拦截器（权限验证）
│   ├── mapper/          # 数据访问层接口
│   ├── pojo/            # 实体类（Student, ClassInfo, OperationLog）
│   └── service/         # 服务层
├── src/main/resources/
│   ├── mappers/         # MyBatis XML 映射文件
│   ├── spring/          # Spring 配置文件
│   ├── db.properties    # 数据库配置
│   └── logback.xml      # 日志配置
└── pom.xml              # Maven 配置
```

## 开发规范

- 使用 **4 空格** 缩进
- 类名使用 **PascalCase**（如 `StudentController`）
- 方法名和变量名使用 **camelCase**（如 `findStudentById`）
- 注释使用 **中文**
- 所有 POJO 使用 Lombok 简化代码
- Service 层使用 `@OperationLog` 注解记录操作日志

---

如有问题，请联系项目维护者。
