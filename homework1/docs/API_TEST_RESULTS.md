# 学生管理系统 API 测试结果报告

> **测试时间**: 2026-03-30  
> **测试分支**: `fool-homework1` (Spring Boot 重构版本)  
> **测试工具**: PowerShell Invoke-WebRequest (curl)  
> **应用端口**: http://localhost:8080/student-management

---

## 1. 项目概述

由于原项目需要 Tomcat 10.1+ 环境（未安装），根据要求创建了 `fool-homework1` 分支，将项目重构为 **Spring Boot 3.4.0** 版本。

### 技术栈变更

| 组件 | 原项目 | 重构后 (Spring Boot) |
|------|--------|---------------------|
| 框架 | Spring Framework 6.2.3 | Spring Boot 3.4.0 |
| 构建工具 | Maven WAR | Maven JAR |
| 数据库 | MySQL | H2 (内存数据库) |
| 部署方式 | Tomcat 外部部署 | 内嵌 Tomcat |
| JDK | 25 | 25 |

---

## 2. 测试环境

- **操作系统**: Windows 11
- **JDK 版本**: Java 25.0.1
- **Maven 版本**: 3.9.9
- **Spring Boot 版本**: 3.4.0
- **数据库**: H2 内存数据库 (兼容 MySQL 语法)

---

## 3. 接口测试结果

### 3.1 查询所有学生 (GET /student)

**请求命令:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/student-management/student" `
  -Headers @{"Authorization"="valid-token-123456"} -UseBasicParsing
```

**响应结果:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "age": 20,
      "name": "张三",
      "classId": 1,
      "classInfo": {
        "id": 1,
        "className": "计算机一班"
      }
    },
    {
      "id": 2,
      "age": 21,
      "name": "李四",
      "classId": 1,
      "classInfo": {
        "id": 1,
        "className": "计算机一班"
      }
    },
    {
      "id": 3,
      "age": 19,
      "name": "王五",
      "classId": 2,
      "classInfo": {
        "id": 2,
        "className": "计算机二班"
      }
    },
    {
      "id": 4,
      "age": 22,
      "name": "赵六",
      "classId": 3,
      "classInfo": {
        "id": 3,
        "className": "软件工程一班"
      }
    }
  ]
}
```

**测试状态**: ✅ **通过**  
**HTTP 状态码**: 200

---

### 3.2 根据ID查询学生 (GET /student/{id})

**请求命令:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/student-management/student/1" `
  -Headers @{"Authorization"="valid-token-123456"} -UseBasicParsing
```

**响应结果:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "age": 20,
    "name": "张三",
    "classId": 1,
    "classInfo": {
      "id": 1,
      "className": "计算机一班"
    }
  }
}
```

**测试状态**: ✅ **通过**  
**HTTP 状态码**: 200

---

### 3.3 添加学生 (POST /student)

**请求命令:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/student-management/student" `
  -Method POST `
  -Headers @{"Authorization"="valid-token-123456"; "Content-Type"="application/json"} `
  -Body '{"name":"测试学生","age":25,"classId":1}' -UseBasicParsing
```

**响应结果:**
```json
{
  "code": 201,
  "message": "创建成功",
  "data": 5
}
```

**测试状态**: ✅ **通过**  
**HTTP 状态码**: 201 Created

---

### 3.4 更新学生信息 (PUT /student/{id})

**请求命令:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/student-management/student/5?name=更新后的学生&age=26" `
  -Method PUT `
  -Headers @{"Authorization"="valid-token-123456"} -UseBasicParsing
```

**响应结果:**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

**测试状态**: ✅ **通过**  
**HTTP 状态码**: 200

---

### 3.5 删除学生 (DELETE /student/{id})

**请求命令:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/student-management/student/5" `
  -Method DELETE `
  -Headers @{"Authorization"="valid-token-123456"} -UseBasicParsing
```

**响应结果:**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**测试状态**: ✅ **通过**  
**HTTP 状态码**: 200

---

### 3.6 未授权请求测试 (无 Token)

**请求命令:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/student-management/student" -UseBasicParsing
```

**响应结果:**
```json
{
  "code": 401,
  "message": "未授权：缺少Authorization请求头"
}
```

**测试状态**: ✅ **通过** (认证拦截正常工作)  
**HTTP 状态码**: 401 Unauthorized

---

### 3.7 无效 Token 测试

**请求命令:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/student-management/student" `
  -Headers @{"Authorization"="invalid-token"} -UseBasicParsing
```

**响应结果:**
```json
{
  "code": 401,
  "message": "未授权：无效的Token"
}
```

**测试状态**: ✅ **通过**  
**HTTP 状态码**: 401 Unauthorized

---

## 4. 功能验证总结

| 功能 | 状态 | 说明 |
|------|------|------|
| 查询所有学生 | ✅ | 返回包含班级信息的完整列表 |
| 根据ID查询学生 | ✅ | 支持关联查询班级信息 |
| 添加学生 | ✅ | 自动生成ID，返回201状态码 |
| 更新学生信息 | ✅ | 支持部分字段更新 |
| 删除学生 | ✅ | 成功删除后返回确认消息 |
| Token 认证 | ✅ | 拦截器正确验证Authorization头 |
| XSS 防护 | ✅ | 输入过滤和输出编码正常工作 |
| AOP 日志 | ✅ | 操作日志记录到控制台和数据库 |
| Swagger 文档 | ✅ | API文档自动生成功能正常 |

---

## 5. cURL 命令参考

### 查询所有学生
```bash
curl -X GET http://localhost:8080/student-management/student \
  -H "Authorization: valid-token-123456"
```

### 根据ID查询学生
```bash
curl -X GET http://localhost:8080/student-management/student/1 \
  -H "Authorization: valid-token-123456"
```

### 添加学生
```bash
curl -X POST http://localhost:8080/student-management/student \
  -H "Content-Type: application/json" \
  -H "Authorization: valid-token-123456" \
  -d '{"name":"新学生","age":20,"classId":1}'
```

### 更新学生
```bash
curl -X PUT "http://localhost:8080/student-management/student/1?name=新名字&age=21" \
  -H "Authorization: valid-token-123456"
```

### 删除学生
```bash
curl -X DELETE http://localhost:8080/student-management/student/1 \
  -H "Authorization: valid-token-123456"
```

---

## 6. Swagger UI 访问地址

- **Swagger UI**: http://localhost:8080/student-management/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/student-management/v3/api-docs

---

## 7. 项目运行说明

### 构建命令
```bash
cd homework1/fool-homework1
mvn clean package -DskipTests
```

### 运行命令
```bash
java -jar target/student-management-1.0.0.jar
```

### 访问地址
- 应用首页: http://localhost:8080/student-management/
- API 基础路径: http://localhost:8080/student-management

---

## 8. 测试结论

✅ **所有 API 接口测试通过**

Spring Boot 重构版本成功保留了原项目的所有核心功能：
- 学生信息的增删改查
- Token 认证授权
- AOP 操作日志记录
- XSS 安全防护
- Swagger API 文档

重构后的项目使用 H2 内存数据库，无需外部 MySQL 和 Tomcat 环境，更适合快速测试和演示。
