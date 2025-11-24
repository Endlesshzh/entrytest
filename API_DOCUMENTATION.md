# Redis Script Query System - API 接口文档

## 📋 概述

本文档描述了基于Groovy脚本的动态Redis查询系统的所有REST API接口。

**基础URL**: `http://localhost:8080`

**Content-Type**: `application/json`

**跨域支持**: 所有接口支持CORS，允许跨域访问

---

## 🔧 脚本操作接口

### 1. 执行脚本

执行Groovy脚本并返回结果。

**接口地址**: `POST /api/script/execute`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "script": "def value = redis.get('mykey')\nreturn value",
  "scriptName": "user-script",
  "testRun": false
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| script | String | 是 | 要执行的Groovy脚本代码 |
| scriptName | String | 否 | 脚本名称/标识符，用于日志记录 |
| testRun | Boolean | 否 | 是否为试运行模式（默认false） |

**响应示例**:

成功响应:
```json
{
  "success": true,
  "result": "some-value",
  "error": null,
  "executionTime": 15,
  "script": "def value = redis.get('mykey')\nreturn value",
  "testRun": false
}
```

失败响应:
```json
{
  "success": false,
  "result": null,
  "error": "Script execution failed: ...",
  "executionTime": 0,
  "script": "def value = redis.get('mykey')\nreturn value",
  "testRun": false
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| success | Boolean | 执行是否成功 |
| result | Object | 脚本执行返回的结果数据 |
| error | String | 错误信息（执行失败时） |
| executionTime | Long | 执行耗时（毫秒） |
| script | String | 执行的脚本内容 |
| testRun | Boolean | 是否为试运行模式 |

---

### 2. 试运行脚本

以试运行模式执行脚本（安全测试，不影响实际数据）。

**接口地址**: `POST /api/script/test`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "script": "def value = redis.get('mykey')\nreturn value",
  "scriptName": "test-script"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| script | String | 是 | 要执行的Groovy脚本代码 |
| scriptName | String | 否 | 脚本名称/标识符 |

**响应格式**: 与 `/api/script/execute` 相同

**说明**: 
- 此接口会自动将 `testRun` 设置为 `true`
- 试运行模式下，脚本执行不会对Redis数据进行实际修改
- 主要用于测试脚本逻辑是否正确

---

### 3. 分析脚本

使用大模型分析脚本的安全性、代码质量等。

**接口地址**: `POST /api/script/analyze`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "script": "def value = redis.get('mykey')\nreturn value"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| script | String | 是 | 要分析的Groovy脚本代码 |

**响应示例**:

```json
{
  "securityScore": 85,
  "securityIssues": [
    "检测到keys()操作，可能影响性能"
  ],
  "qualityScore": 90,
  "qualityIssues": [
    "建议添加错误处理"
  ],
  "performanceSuggestions": [
    "使用SCAN代替KEYS以提高性能"
  ],
  "bestPractices": [
    "建议使用具体的键名而不是通配符查询"
  ],
  "llmAnalysis": "该脚本用于获取Redis键值，逻辑简单清晰。建议添加空值检查和异常处理...",
  "safeToExecute": true
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| securityScore | Integer | 安全性评分（0-100） |
| securityIssues | List<String> | 发现的安全问题列表 |
| qualityScore | Integer | 代码质量评分（0-100） |
| qualityIssues | List<String> | 代码质量问题列表 |
| performanceSuggestions | List<String> | 性能优化建议 |
| bestPractices | List<String> | 最佳实践建议 |
| llmAnalysis | String | LLM深度分析结果 |
| safeToExecute | Boolean | 是否安全可执行 |

**分析维度**:
- **安全性分析**: 检测危险操作、安全风险、潜在漏洞
- **代码质量**: 规范性、可读性、可维护性评估
- **性能建议**: 识别性能瓶颈、优化建议
- **最佳实践**: Redis使用建议、代码改进建议
- **LLM深度分析**: 使用大模型进行智能分析和建议

---

### 4. 健康检查

检查脚本服务是否正常运行。

**接口地址**: `GET /api/script/health`

**请求参数**: 无

**响应示例**:
```
Script service is running
```

**响应格式**: 纯文本字符串

---

## 📊 数据管理接口

### 5. 生成测试数据

在Redis中生成测试数据（用户、产品、订单等）。

**接口地址**: `POST /api/data/generate`

**请求参数** (Query Parameters):

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| users | Integer | 否 | 100 | 生成的用户数量 |
| products | Integer | 否 | 50 | 生成的产品数量 |
| orders | Integer | 否 | 200 | 生成的订单数量 |

**请求示例**:
```
POST /api/data/generate?users=100&products=50&orders=200
```

**响应示例**:

成功响应:
```json
{
  "success": true,
  "message": "Test data generated successfully",
  "statistics": {
    "totalUsers": 100,
    "totalProducts": 50,
    "totalOrders": 200,
    "totalKeys": 350
  }
}
```

失败响应:
```json
{
  "success": false,
  "error": "Failed to generate test data: ..."
}
```

---

### 6. 清空测试数据

清空Redis中的所有测试数据。

**接口地址**: `DELETE /api/data/clear`

**请求参数**: 无

**响应示例**:

成功响应:
```json
{
  "success": true,
  "message": "All test data cleared"
}
```

失败响应:
```json
{
  "success": false,
  "error": "Failed to clear test data: ..."
}
```

---

### 7. 获取数据统计

获取Redis中测试数据的统计信息。

**接口地址**: `GET /api/data/statistics`

**请求参数**: 无

**响应示例**:

成功响应:
```json
{
  "success": true,
  "statistics": {
    "totalUsers": 100,
    "totalProducts": 50,
    "totalOrders": 200,
    "totalKeys": 350
  }
}
```

失败响应:
```json
{
  "success": false,
  "error": "Failed to get statistics: ..."
}
```

---

## 🌐 Web页面接口

### 8. 获取主页面

返回Web界面的HTML页面。

**接口地址**: `GET /`

**请求参数**: 无

**响应**: HTML页面内容

**说明**: 
- 返回包含脚本编辑器、执行结果展示、LLM分析结果展示的完整Web界面
- 支持在浏览器中直接使用，无需额外前端部署

---

## 📝 脚本编写规范

### 可用的Redis操作

在Groovy脚本中，可以通过 `redis` 对象访问以下Redis操作：

#### 字符串操作
- `redis.get(key)` - 获取字符串值
- `redis.set(key, value)` - 设置字符串值

#### Hash操作
- `redis.hget(key, field)` - 获取Hash字段值
- `redis.hgetAll(key)` - 获取Hash所有字段
- `redis.hset(key, field, value)` - 设置Hash字段值

#### List操作
- `redis.lrange(key, start, end)` - 获取List范围数据
- `redis.llen(key)` - 获取List长度

#### Set操作
- `redis.smembers(key)` - 获取Set所有成员
- `redis.scard(key)` - 获取Set大小

#### Sorted Set操作
- `redis.zrange(key, start, end)` - 获取Sorted Set范围数据
- `redis.zcard(key)` - 获取Sorted Set大小

#### 键操作
- `redis.keys(pattern)` - 查询匹配的键（注意性能）
- `redis.exists(key)` - 检查键是否存在
- `redis.type(key)` - 获取键的类型

### 脚本示例

#### 示例1: 获取单个值
```groovy
def value = redis.get('mykey')
return value
```

#### 示例2: 获取Hash所有字段
```groovy
def userData = redis.hgetAll('user:1001')
return userData
```

#### 示例3: 查询键列表
```groovy
def keys = redis.keys('user:*')
return keys
```

#### 示例4: 获取List范围数据
```groovy
def items = redis.lrange('mylist', 0, 10)
return items
```

#### 示例5: 复杂查询
```groovy
// 获取所有用户键
def userKeys = redis.keys('user:*')

// 遍历并获取用户信息
def users = []
userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData && userData.age && Integer.parseInt(userData.age) > 18) {
        users.add(userData)
    }
}

return users
```

---

## ⚠️ 错误处理

### 常见错误码

所有接口在发生错误时，都会返回HTTP 200状态码，但响应体中的 `success` 字段为 `false`。

### 错误类型

1. **脚本执行错误**
   - 脚本语法错误
   - Redis连接错误
   - 脚本执行超时
   - 安全验证失败

2. **LLM分析错误**
   - LLM服务不可用
   - 网络连接错误
   - 分析超时

3. **数据操作错误**
   - Redis连接失败
   - 数据格式错误
   - 操作权限不足

---

## 🔒 安全机制

### 脚本安全限制

1. **命令白名单**: 只允许使用预定义的Redis操作
2. **禁止模式黑名单**: 禁止使用危险操作（如FLUSHALL、DEL等）
3. **执行超时控制**: 脚本执行有超时限制，防止无限循环
4. **试运行模式**: 支持试运行，不影响实际数据

### 安全建议

- 在生产环境中，建议配置更严格的安全策略
- 定期审查脚本内容
- 使用LLM分析功能检查脚本安全性
- 避免在生产环境直接执行未经验证的脚本

---

## 📚 更多信息

- 项目架构文档: `ARCHITECTURE.md`
- 快速开始指南: `QUICKSTART.md`
- 示例脚本: `scripts/groovy/`
- 项目总结: `PROJECT_SUMMARY.md`

---

## 📞 技术支持

如有问题，请查看项目文档或提交Issue。

