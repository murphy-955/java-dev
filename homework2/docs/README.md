# 学生管理系统 - 项目运行指南

## 项目简介

基于 **Spring Framework 6.x** + **MyBatis** 开发的学生信息管理程序，实现了以下功能：

- **一对多双向关联**：学生与班级信息双向关联查询
- **RESTful API**：提供标准的 REST 接口
- **声明式事务**：使用 Spring @Transactional 注解
- **AOP 日志**：操作后在控制台打印 `时间戳 + 方法名`
- **权限验证**：基于 Token 的简易认证机制

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 25+ | Java 开发工具包 |
| Spring Framework | 6.2.5 | 核心框架 |
| MyBatis | 3.5.19 | ORM 持久层框架 |
| H2 Database | 2.2.224 | 内存数据库（测试用） |
| Tomcat | 10.1+ | Servlet 容器 |
| Maven | 3.9+ | 构建工具 |

## 环境要求

### 1. 安装 JDK 25+

```bash
# 验证安装
java -version
# 输出应显示 java version "25" 或更高版本
```

### 2. 安装 Maven 3.9+

```bash
# 验证安装
mvn -version
```

## 项目运行步骤

### 步骤一：进入项目目录

```bash
cd homework2/student-management
```

### 步骤二：编译打包

```bash
mvn clean package -DskipTests
```

构建成功后，会在 `target/` 目录生成 `student-management.war` 文件。

### 步骤三：运行项目（使用 Cargo 插件）

```bash
mvn cargo:run
```

此命令会自动：
1. 下载 Tomcat 10.1.30
2. 部署 WAR 包
3. 启动服务

启动成功后，控制台会显示：
```
Tomcat 10.1.30 started on port [8080]
Press Ctrl-C to stop the container...
```

### 步骤四：验证部署

1. **访问首页**：
   ```
   http://localhost:8080/student-management/
   ```

2. **查看初始数据**：
   - 系统已预置测试数据（计算机一班、计算机二班、软件工程一班 + 4名学生）

## API 使用说明

### 认证方式

所有 API 请求需要在请求头中携带 Token：

```
Authorization: valid-token-123456
```

### 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/student/{id}` | 根据 ID 查询学生信息 |
| GET | `/classInfo/{id}` | 根据 ID 查询班级信息 |
| PUT | `/student/{id}?name=xxx&age=xxx` | 更新学生信息 |

### 使用 curl 测试 API

```bash
# 1. 查询学生信息（id=1）
curl -X GET http://localhost:8080/student-management/student/1 \
  -H "Authorization: valid-token-123456"

# 2. 查询班级信息（id=1，包含学生列表）
curl -X GET http://localhost:8080/student-management/classInfo/1 \
  -H "Authorization: valid-token-123456"

# 3. 更新学生信息（id=2，改为李雷，21岁）
# 注意：中文需要URL编码，李雷 = %E6%9D%8E%E9%9B%B7
curl -X PUT "http://localhost:8080/student-management/student/2?name=%E6%9D%8E%E9%9B%B7&age=21" \
  -H "Authorization: valid-token-123456"

# 4. 验证更新结果
curl -X GET http://localhost:8080/student-management/student/2 \
  -H "Authorization: valid-token-123456"

# 5. 未授权访问（应返回401）
curl -X GET http://localhost:8080/student-management/student/1
```

### 预期输出示例

**查询学生（id=1）**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "张三",
    "age": 20,
    "classInfo": {
      "id": 1,
      "name": "计算机一班"
    },
    "cid": 1
  }
}
```

**查询班级（id=1）**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "计算机一班",
    "students": [
      {"id": 1, "name": "张三", "age": 20, "cid": 1},
      {"id": 2, "name": "李四", "age": 21, "cid": 1}
    ]
  }
}
```

## AOP 日志输出

调用 Service 层方法后，控制台会输出：

```
2026.04.10 16:16:52 getStudentById
2026.04.10 16:16:52 getClassInfoById
2026.04.10 16:16:52 updateStudent
```

## 项目结构

```
student-management/
├── pom.xml                          # Maven配置
├── sql/
│   └── init.sql                     # MySQL初始化脚本（可选）
└── src/
    └── main/
        ├── java/com/example/
        │   ├── aspect/
        │   │   └── LogAspect.java              # AOP日志切面
        │   ├── controller/
        │   │   ├── ClassInfoController.java    # 班级控制器
        │   │   └── StudentController.java      # 学生控制器
        │   ├── interceptor/
        │   │   └── AutoInterceptor.java        # 权限验证拦截器
        │   ├── mapper/
        │   │   ├── ClassInfoMapper.java        # 班级Mapper
        │   │   └── StudentMapper.java          # 学生Mapper
        │   ├── pojo/
        │   │   ├── ClassInfo.java              # 班级实体（一对多）
        │   │   └── Student.java                # 学生实体（多对一）
        │   ├── service/
        │   │   ├── ClassInfoService.java       # 班级服务接口
        │   │   ├── StudentService.java         # 学生服务接口
        │   │   └── impl/
        │   │       ├── ClassInfoServiceImpl.java   # 班级服务实现（@Transactional）
        │   │       └── StudentServiceImpl.java     # 学生服务实现（@Transactional）
        │   └── pojo/...
        ├── resources/
        │   ├── db.properties          # 数据库配置（H2）
        │   ├── logback.xml            # 日志配置
        │   ├── schema.sql             # H2建表脚本
        │   ├── mappers/
        │   │   ├── ClassInfoMapper.xml    # 班级SQL映射
        │   │   └── StudentMapper.xml      # 学生SQL映射
        │   └── spring/
        │       ├── applicationContext.xml # Spring核心配置
        │       └── spring-mvc.xml         # Spring MVC配置
        └── webapp/
            ├── index.html             # 首页
            └── WEB-INF/
                └── web.xml            # Web配置
```

## 常见问题

### Q1: 端口 8080 被占用

**解决：** 修改 `pom.xml` 中的 Cargo 配置：

```xml
<properties>
    <cargo.servlet.port>8081</cargo.servlet.port>
</properties>
```

### Q2: 启动报错 `Unsupported class file major version 69`

**原因：** Spring Framework 版本过低，不支持 JDK 25。

**解决：** 确保使用 Spring Framework 6.2.5+（已在 pom.xml 中配置）。

### Q3: 接口返回 401 未授权

**检查：**
1. 请求头中是否包含 `Authorization: valid-token-123456`
2. Token 是否正确（不含引号）

### Q4: 中文参数报错

**原因：** URL 中直接包含中文，需进行 URL 编码。

**解决：**
- 李雷 → `%E6%9D%8E%E9%9B%B7`
- 使用在线工具进行编码转换

### Q5: AOP 日志没有输出

**检查：** `applicationContext.xml` 中是否扫描了 aspect 包：

```xml
<context:component-scan base-package="com.example.service,com.example.aspect"/>
<aop:aspectj-autoproxy/>
```

## 开发规范（黄山版）

- 使用 **4 空格** 缩进
- 类名使用 **UpperCamelCase**
- 方法名和变量名使用 **lowerCamelCase**
- 注释使用 **中文**
- Service 层方法使用 `@Transactional` 声明事务
- 查询方法标记 `readOnly = true`
- 写操作指定 `rollbackFor = Exception.class`

---

如有问题，请参考项目源码或联系维护者。
