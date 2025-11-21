# 快速开始指南

## 项目概述

这是一个基于Groovy脚本的动态Redis查询系统，具有以下核心功能：

✅ **Groovy脚本执行** - 动态查询Redis数据
✅ **Web界面** - 友好的脚本编辑和执行界面
✅ **试运行功能** - 安全测试脚本
✅ **LLM智能分析** - 本地大模型分析脚本安全性和规范性
✅ **REST API** - 完整的API接口

## 一、环境准备

### 1. 安装Redis

```bash
# macOS
brew install redis
brew services start redis

# 验证Redis运行
redis-cli ping
# 应该返回: PONG
```

### 2. 安装Ollama（本地LLM）

```bash
# macOS/Linux - 安装Ollama
curl -fsSL https://ollama.com/install.sh | sh

# 启动Ollama服务（在新终端窗口）
ollama serve

# 拉取模型（在另一个终端）
ollama pull llama2

# 验证Ollama
curl http://localhost:11434/api/tags
```

**推荐模型：**
- `llama2` - 通用模型（推荐）
- `codellama` - 代码分析专用（更好的分析效果）
- `mistral` - 轻量级（速度快）

### 3. 准备测试数据

```bash
# 使用提供的脚本
chmod +x scripts/setup-test-data.sh
./scripts/setup-test-data.sh
```

或手动添加：

```bash
redis-cli SET mykey "Hello, Redis!"
redis-cli HSET user:1001 name "张三" age "25" city "北京"
redis-cli HSET user:1002 name "李四" age "30" city "上海"
redis-cli LPUSH mylist "item1" "item2" "item3"
```

## 二、在IntelliJ IDEA中运行

### 方法1：直接运行（推荐）

1. **打开项目**
   - 在IntelliJ IDEA中打开项目
   - IDEA会自动识别为Maven项目

2. **等待依赖下载**
   - IDEA会自动下载Maven依赖
   - 等待右下角进度条完成

3. **运行应用**
   - 找到 `RedisScriptQueryApplication.java`
   - 右键点击 → Run 'RedisScriptQueryApplication'
   - 或点击类旁边的绿色运行按钮

4. **访问应用**
   - 打开浏览器访问: http://localhost:8080

### 方法2：使用Maven运行

1. **打开Terminal（IntelliJ内置终端）**
   - View → Tool Windows → Terminal

2. **运行命令**
   ```bash
   mvn spring-boot:run
   ```

3. **访问应用**
   - 打开浏览器访问: http://localhost:8080

## 三、使用Docker（可选）

如果你想使用Docker运行Redis和Ollama：

```bash
# 启动服务
docker-compose up -d

# 拉取Ollama模型
docker exec -it ollama-llm ollama pull llama2

# 停止服务
docker-compose down
```

## 四、使用Web界面

### 1. 编写脚本

在脚本编辑器中输入Groovy脚本，例如：

```groovy
// 获取用户信息
def userData = redis.hgetAll('user:1001')
return userData
```

### 2. 试运行

点击 **"🧪 试运行"** 按钮测试脚本，不会影响实际数据。

### 3. LLM分析

点击 **"🤖 LLM分析"** 按钮，系统会分析：
- 🔒 安全性评分
- ⭐ 代码质量评分
- ⚡ 性能建议
- 📚 最佳实践建议

### 4. 执行脚本

确认无误后，点击 **"▶️ 执行脚本"** 正式执行。

## 五、示例脚本

### 示例1：获取单个值
```groovy
def value = redis.get('mykey')
return value
```

### 示例2：获取用户信息
```groovy
def userData = redis.hgetAll('user:1001')
return userData
```

### 示例3：查询所有用户
```groovy
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

### 示例4：统计分析
```groovy
def userKeys = redis.keys('user:*')
def totalAge = 0
def count = 0

userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData && userData.age) {
        totalAge += userData.age as Integer
        count++
    }
}

return [
    totalUsers: count,
    averageAge: count > 0 ? totalAge / count : 0
]
```

更多示例请查看 `EXAMPLES.md`

## 六、REST API使用

### 执行脚本
```bash
curl -X POST http://localhost:8080/api/script/execute \
  -H "Content-Type: application/json" \
  -d '{
    "script": "def value = redis.get(\"mykey\")\nreturn value",
    "scriptName": "test-script",
    "testRun": false
  }'
```

### 分析脚本
```bash
curl -X POST http://localhost:8080/api/script/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "script": "def value = redis.get(\"mykey\")\nreturn value"
  }'
```

## 七、配置说明

配置文件位置：`src/main/resources/application.yml`

### Redis配置
```yaml
spring:
  redis:
    host: localhost    # Redis主机
    port: 6379        # Redis端口
    password:         # Redis密码（如果有）
```

### LLM配置
```yaml
llm:
  api-url: http://localhost:11434/api/generate
  model: llama2     # 使用的模型
  timeout: 60       # 超时时间（秒）
```

### 脚本安全配置
```yaml
script:
  max-execution-time: 5000  # 最大执行时间（毫秒）
  allowed-commands:         # 允许的Redis命令
    - GET
    - SET
    - HGET
    - HGETALL
    # ... 更多命令
```

## 八、故障排查

### 问题1：无法连接Redis
```bash
# 检查Redis是否运行
redis-cli ping

# 如果没有运行，启动Redis
brew services start redis  # macOS
sudo systemctl start redis # Linux
```

### 问题2：无法连接Ollama
```bash
# 检查Ollama服务
curl http://localhost:11434/api/tags

# 如果失败，启动Ollama
ollama serve
```

### 问题3：LLM分析失败
- 确保Ollama服务正在运行
- 确保已拉取模型：`ollama list`
- 检查模型名称是否正确（在application.yml中）

### 问题4：Maven依赖下载失败
- 在IntelliJ中：File → Invalidate Caches → Invalidate and Restart
- 或在终端运行：`mvn clean install`

### 问题5：端口被占用
如果8080端口被占用，修改 `application.yml`：
```yaml
server:
  port: 8081  # 改为其他端口
```

## 九、项目结构

```
entrytest/
├── src/main/java/org/example/
│   ├── RedisScriptQueryApplication.java  # 主应用
│   ├── config/                           # 配置
│   ├── controller/                       # 控制器
│   ├── service/                          # 服务层
│   └── model/                            # 数据模型
├── src/main/resources/
│   ├── application.yml                   # 配置文件
│   └── templates/index.html              # Web界面
├── scripts/                              # 辅助脚本
├── README.md                             # 详细文档
├── EXAMPLES.md                           # 脚本示例
└── QUICKSTART.md                         # 本文件
```

## 十、下一步

1. ✅ 查看 `EXAMPLES.md` 了解更多脚本示例
2. ✅ 查看 `README.md` 了解详细功能说明
3. ✅ 尝试编写自己的脚本
4. ✅ 使用LLM分析功能优化脚本

## 需要帮助？

- 查看 `README.md` 获取详细文档
- 查看 `EXAMPLES.md` 获取更多示例
- 检查日志文件：`logs/redis-script-query.log`

祝使用愉快！🚀
