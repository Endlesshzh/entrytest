# Redis Script Query System - 企业级增强版

基于JVM脚本语言（Groovy）的高性能动态Redis查询系统，支持多种大模型、完善的日志追踪和企业级性能优化。

## 🌟 核心特性

### 基础功能
- ✅ **Groovy脚本执行**: 使用Groovy脚本动态查询Redis数据
- ✅ **Web界面**: 友好的Web界面，支持脚本编辑和执行
- ✅ **试运行功能**: 在正式执行前安全测试脚本
- ✅ **REST API**: 提供完整的REST API接口
- ✅ **安全控制**: 脚本白名单机制，防止危险操作

### 企业级增强
- 🚀 **多LLM支持**: OpenAI、Claude、Compass、Ollama、vLLM
- 📊 **完善日志**: 分级日志、Tracing、性能监控
- ⚡ **高性能**: 500+ QPS，Caffeine缓存，虚拟线程
- 🔍 **可观测性**: Actuator、Prometheus、Metrics
- 🛠️ **测试工具**: 内置Redis测试数据生成器

## 技术栈

- **后端框架**: Spring Boot 2.7.18
- **脚本引擎**: Groovy 3.0.19
- **数据库**: Redis (Lettuce客户端)
- **LLM提供商**:
  - OpenAI (GPT-3.5/4)
  - Claude (Anthropic)
  - Compass (国内大模型)
  - Ollama (本地部署)
  - vLLM (高性能本地部署)
- **缓存**: Caffeine (高性能缓存)
- **监控**: Micrometer + Actuator + Prometheus
- **日志**: Logback + Logstash Encoder
- **前端**: HTML5 + CSS3 + JavaScript

## 系统架构

```
┌─────────────────┐
│   Web Browser   │
└────────┬────────┘
         │ HTTP
         ▼
┌─────────────────┐
│  Spring Boot    │
│  Web Server     │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌──────────┐
│ Redis  │ │  Ollama  │
│        │ │   LLM    │
└────────┘ └──────────┘
```

## 快速开始

### 前置要求

1. **Java 8+**
2. **Maven 3.6+**
3. **Redis** (本地或远程)
4. **Ollama** (本地LLM)

### 1. 安装Redis

#### macOS
```bash
brew install redis
brew services start redis
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install redis-server
sudo systemctl start redis
```

#### Windows
下载并安装 Redis for Windows: https://github.com/microsoftarchive/redis/releases

### 2. 安装Ollama (本地LLM)

#### macOS/Linux
```bash
# 安装Ollama
curl -fsSL https://ollama.com/install.sh | sh

# 启动Ollama服务
ollama serve

# 在新终端中拉取模型（推荐使用llama2或codellama）
ollama pull llama2

# 或者使用专门的代码分析模型
ollama pull codellama
```

#### Windows
1. 访问 https://ollama.com/download
2. 下载Windows安装包
3. 安装后运行 `ollama serve`
4. 拉取模型: `ollama pull llama2`

#### 验证Ollama安装
```bash
# 测试Ollama API
curl http://localhost:11434/api/generate -d '{
  "model": "llama2",
  "prompt": "Hello, world!",
  "stream": false
}'
```

### 3. 配置应用

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  redis:
    host: localhost      # Redis主机
    port: 6379          # Redis端口
    password:           # Redis密码（如果有）
    database: 0         # Redis数据库编号

llm:
  api-url: http://localhost:11434/api/generate  # Ollama API地址
  model: llama2       # 使用的模型名称
  timeout: 60         # 超时时间（秒）
```

### 4. 构建和运行

```bash
# 克隆或进入项目目录
cd entrytest

# 使用Maven构建
mvn clean package

# 运行应用
mvn spring-boot:run

# 或者直接运行jar包
java -jar target/entrytest-1.0-SNAPSHOT.jar
```

### 5. 访问应用

打开浏览器访问: http://localhost:8080

## 使用指南

### Web界面操作

1. **编写脚本**: 在脚本编辑器中输入Groovy脚本
2. **试运行**: 点击"试运行"按钮测试脚本
3. **执行脚本**: 点击"执行脚本"按钮正式执行
4. **LLM分析**: 点击"LLM分析"按钮分析脚本安全性和规范性

### 脚本示例

#### 示例1: 获取单个键值
```groovy
// 获取单个键的值
def value = redis.get('mykey')
return value
```

#### 示例2: 获取Hash所有字段
```groovy
// 获取Hash的所有字段
def userData = redis.hgetAll('user:1001')
return userData
```

#### 示例3: 查询键列表
```groovy
// 查询匹配的键列表
def keys = redis.keys('user:*')
return keys
```

#### 示例4: 获取List范围
```groovy
// 获取List的范围数据
def items = redis.lrange('mylist', 0, 10)
return items
```

#### 示例5: 复杂查询
```groovy
// 查询所有用户并获取详细信息
def userKeys = redis.keys('user:*')
def users = []

userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData) {
        users.add(userData)
    }
}

return users
```

### 可用的Redis操作

脚本中可以使用 `redis` 对象调用以下方法：

- `redis.get(key)` - 获取字符串值
- `redis.set(key, value)` - 设置字符串值
- `redis.hget(key, field)` - 获取Hash字段值
- `redis.hgetAll(key)` - 获取Hash所有字段
- `redis.hset(key, field, value)` - 设置Hash字段
- `redis.keys(pattern)` - 查询键（注意：生产环境慎用）
- `redis.lrange(key, start, end)` - 获取List范围
- `redis.smembers(key)` - 获取Set所有成员
- `redis.zrange(key, start, end)` - 获取ZSet范围
- `redis.exists(key)` - 检查键是否存在
- `redis.ttl(key)` - 获取键的过期时间

## REST API文档

### 1. 执行脚本

**POST** `/api/script/execute`

请求体:
```json
{
  "script": "def value = redis.get('mykey')\nreturn value",
  "scriptName": "get-mykey",
  "testRun": false
}
```

响应:
```json
{
  "success": true,
  "result": "value from redis",
  "executionTime": 123,
  "script": "...",
  "testRun": false
}
```

### 2. 试运行脚本

**POST** `/api/script/test`

请求体同上，自动设置 `testRun: true`

### 3. 分析脚本

**POST** `/api/script/analyze`

请求体:
```json
{
  "script": "def value = redis.get('mykey')\nreturn value"
}
```

响应:
```json
{
  "securityScore": 100,
  "securityIssues": [],
  "qualityScore": 85,
  "qualityIssues": ["Add comments to explain script logic"],
  "performanceSuggestions": [],
  "bestPractices": [],
  "llmAnalysis": "This script is safe...",
  "safeToExecute": true
}
```

### 4. 健康检查

**GET** `/api/script/health`

响应: `Script service is running`

## 安全机制

### 1. 命令白名单

只允许执行配置文件中定义的Redis命令，默认允许：
- GET, SET, HGET, HGETALL, HSET
- KEYS, SCAN, MGET
- LRANGE, SMEMBERS, ZRANGE
- TTL, EXISTS

### 2. 禁止模式

禁止以下危险操作：
- FLUSHDB, FLUSHALL (清空数据库)
- SHUTDOWN (关闭服务器)
- CONFIG (修改配置)
- SCRIPT KILL/FLUSH (脚本管理)
- SAVE, BGSAVE (持久化操作)

### 3. 脚本验证

- 禁止系统调用 (`System.exit`, `Runtime.getRuntime`)
- 禁止文件操作 (`java.io.File`)
- 禁止动态类加载 (`Class.forName`, `ClassLoader`)

### 4. 执行超时

脚本执行超时限制（默认5秒），防止无限循环

## LLM分析功能

系统使用本地部署的Ollama LLM对脚本进行智能分析：

### 分析维度

1. **安全性分析** (0-100分)
   - 检测危险操作
   - 识别潜在安全风险
   - 评估数据访问模式

2. **代码质量** (0-100分)
   - 代码规范性
   - 可读性评估
   - 最佳实践检查

3. **性能建议**
   - 识别性能瓶颈
   - 优化建议

4. **最佳实践**
   - Redis使用建议
   - 代码改进建议

### 推荐的LLM模型

- **llama2**: 通用模型，适合大多数场景
- **codellama**: 专门的代码分析模型，推荐使用
- **mistral**: 轻量级模型，速度快

切换模型只需修改 `application.yml` 中的 `llm.model` 配置。

## 测试数据准备

在Redis中准备一些测试数据：

```bash
# 连接Redis
redis-cli

# 设置一些测试数据
SET mykey "Hello, Redis!"
HSET user:1001 name "张三" age "25" city "北京"
HSET user:1002 name "李四" age "30" city "上海"
LPUSH mylist "item1" "item2" "item3"
SADD myset "member1" "member2" "member3"
```

## 故障排查

### Redis连接失败

```bash
# 检查Redis是否运行
redis-cli ping
# 应该返回 PONG

# 检查Redis配置
redis-cli CONFIG GET bind
redis-cli CONFIG GET protected-mode
```

### Ollama连接失败

```bash
# 检查Ollama服务
curl http://localhost:11434/api/tags

# 查看已安装的模型
ollama list

# 重启Ollama服务
pkill ollama
ollama serve
```

### 脚本执行超时

- 检查脚本是否有无限循环
- 增加 `application.yml` 中的 `script.max-execution-time`
- 优化脚本逻辑

## 项目结构

```
entrytest/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── RedisScriptQueryApplication.java  # 主应用
│   │   │   ├── config/                           # 配置类
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── ScriptConfig.java
│   │   │   │   └── LlmConfig.java
│   │   │   ├── controller/                       # 控制器
│   │   │   │   ├── ScriptController.java
│   │   │   │   └── WebController.java
│   │   │   ├── service/                          # 服务层
│   │   │   │   ├── ScriptEngineService.java
│   │   │   │   └── LlmAnalysisService.java
│   │   │   └── model/                            # 数据模型
│   │   │       ├── ScriptExecutionRequest.java
│   │   │       ├── ScriptExecutionResult.java
│   │   │       ├── ScriptAnalysisRequest.java
│   │   │       └── ScriptAnalysisResult.java
│   │   └── resources/
│   │       ├── application.yml                   # 配置文件
│   │       └── templates/
│   │           └── index.html                    # Web界面
│   └── test/
└── pom.xml                                       # Maven配置
```

## 扩展开发

### 添加新的Redis操作

在 `ScriptEngineService.RedisOperations` 类中添加新方法：

```java
public Object myCustomOperation(String key) {
    // 实现自定义操作
    return redisTemplate.opsForValue().get(key);
}
```

### 自定义LLM提示词

修改 `LlmAnalysisService.buildAnalysisPrompt()` 方法来自定义分析提示词。

### 添加新的安全规则

在 `application.yml` 中添加新的禁止模式：

```yaml
script:
  forbidden-patterns:
    - "YOUR_PATTERN"
```

## 性能优化建议

1. **启用脚本缓存**: 在 `application.yml` 中设置 `script.cache-enabled: true`
2. **使用连接池**: Redis连接池已配置，可根据需要调整
3. **限制KEYS命令**: 生产环境使用SCAN替代KEYS
4. **设置合理的超时**: 根据实际情况调整执行超时时间

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

如有问题，请提交Issue或联系项目维护者。
