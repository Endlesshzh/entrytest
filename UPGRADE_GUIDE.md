# 升级指南 - 企业级增强版

## 🚀 新增功能

### 1. 多LLM提供商支持

系统现在支持多种大模型提供商，可以灵活切换：

#### 支持的提供商

| 提供商 | 类型 | 说明 |
|--------|------|------|
| **OpenAI** | 云端 | GPT-3.5/GPT-4 |
| **Claude** | 云端 | Anthropic Claude 3 |
| **Compass** | 云端 | 国内大模型（文心一言等） |
| **Ollama** | 本地 | 本地部署，隐私保护 |
| **vLLM** | 本地 | 高性能本地部署 |

#### 配置方式

在 `application.yml` 中配置：

```yaml
llm:
  # 设置主要使用的提供商
  primary-provider: OPENAI  # 可选: OPENAI, CLAUDE, COMPASS, OLLAMA, VLLM

  # OpenAI配置
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-3.5-turbo

  # Claude配置
  claude:
    api-key: ${CLAUDE_API_KEY}
    model: claude-3-sonnet-20240229

  # Compass配置（国内大模型）
  compass:
    api-url: ${COMPASS_API_URL}
    api-key: ${COMPASS_API_KEY}
    model: compass-1
```

#### 环境变量配置

```bash
# OpenAI
export OPENAI_API_KEY="sk-..."

# Claude
export CLAUDE_API_KEY="sk-ant-..."

# Compass
export COMPASS_API_URL="https://..."
export COMPASS_API_KEY="..."
```

### 2. 本地大模型部署（vLLM）

#### 安装vLLM

```bash
# 使用pip安装
pip install vllm

# 启动vLLM服务
python -m vllm.entrypoints.openai.api_server \
  --model meta-llama/Llama-2-7b-chat-hf \
  --port 8000
```

#### 配置vLLM

```yaml
llm:
  primary-provider: VLLM
  vllm:
    api-url: http://localhost:8000/v1/chat/completions
    model: meta-llama/Llama-2-7b-chat-hf
```

#### 其他本地部署选项

**LMDeploy**:
```bash
lmdeploy serve api_server \
  --model-path /path/to/model \
  --server-port 8000
```

**SGLang**:
```bash
python -m sglang.launch_server \
  --model-path meta-llama/Llama-2-7b-chat-hf \
  --port 8000
```

### 3. 完善的日志系统

#### 日志分级

- **ERROR**: 错误日志，记录所有错误
- **WARN**: 警告日志
- **INFO**: 信息日志，记录关键操作
- **DEBUG**: 调试日志，详细的执行信息

#### 日志文件

```
logs/
├── redis-script-query.log        # 所有日志
├── redis-script-query-error.log  # 错误日志
└── redis-script-query-perf.log   # 性能日志
```

#### Tracing支持

每个请求都有唯一的 `traceId` 和 `spanId`，方便追踪：

```json
{
  "timestamp": "2024-01-01T12:00:00.000Z",
  "level": "INFO",
  "traceId": "abc123",
  "spanId": "def456",
  "message": "Script executed successfully",
  "executionTime": 45
}
```

#### 查看日志

```bash
# 实时查看所有日志
tail -f logs/redis-script-query.log

# 查看错误日志
tail -f logs/redis-script-query-error.log

# 查看性能日志
tail -f logs/redis-script-query-perf.log

# 搜索特定traceId的日志
grep "abc123" logs/redis-script-query.log
```

### 4. 性能优化（500+ QPS）

#### 优化措施

1. **Caffeine缓存**: 高性能脚本缓存
2. **Virtual Threads**: Java 21虚拟线程（如果可用）
3. **异步日志**: 异步写入日志，不阻塞主线程
4. **连接池优化**: Redis连接池配置优化
5. **脚本预编译**: 缓存已编译的脚本

#### 性能配置

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 20    # 增加连接池大小
        max-idle: 10
        min-idle: 5

script:
  cache-enabled: true
  cache-size: 1000        # 增加缓存大小
  max-execution-time: 5000
```

#### 性能监控

访问 Actuator 端点查看性能指标：

```bash
# 查看所有指标
curl http://localhost:8080/actuator/metrics

# 查看脚本执行指标
curl http://localhost:8080/actuator/metrics/script.execution.time

# Prometheus格式
curl http://localhost:8080/actuator/prometheus
```

### 5. Redis测试数据生成

#### API接口

**生成测试数据**:
```bash
curl -X POST "http://localhost:8080/api/data/generate?users=1000&products=500&orders=2000"
```

**清空测试数据**:
```bash
curl -X DELETE "http://localhost:8080/api/data/clear"
```

**查看数据统计**:
```bash
curl http://localhost:8080/api/data/statistics
```

#### 生成的数据类型

- **用户数据** (`user:*`): 包含姓名、年龄、城市、部门等
- **商品数据** (`product:*`): 包含名称、分类、价格、库存等
- **订单数据** (`order:*`): 包含用户ID、商品ID、金额、状态等
- **会话数据** (`session:*`): 带TTL的会话信息
- **指标数据**: 页面访问、活跃用户、排行榜等

## 📊 监控和指标

### Actuator端点

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info

# 所有指标
curl http://localhost:8080/actuator/metrics

# 日志级别
curl http://localhost:8080/actuator/loggers
```

### 修改日志级别（运行时）

```bash
# 设置DEBUG级别
curl -X POST http://localhost:8080/actuator/loggers/org.example.service \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# 设置INFO级别
curl -X POST http://localhost:8080/actuator/loggers/org.example.service \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "INFO"}'
```

## 🔧 配置示例

### 完整配置示例

```yaml
server:
  port: 8080

spring:
  application:
    name: redis-script-query-system

  redis:
    host: localhost
    port: 6379
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

llm:
  primary-provider: OPENAI
  timeout: 60
  temperature: 0.3
  max-tokens: 2000

  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-3.5-turbo

  claude:
    api-key: ${CLAUDE_API_KEY}
    model: claude-3-sonnet-20240229

script:
  max-execution-time: 5000
  cache-enabled: true
  cache-size: 1000

logging:
  level:
    root: INFO
    org.example: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,loggers
```

## 🚀 性能测试

### 使用Apache Bench测试

```bash
# 测试脚本执行性能
ab -n 10000 -c 100 -p script.json -T application/json \
  http://localhost:8080/api/script/execute
```

### 使用wrk测试

```bash
# 安装wrk
brew install wrk

# 测试
wrk -t12 -c400 -d30s --latency \
  -s script.lua \
  http://localhost:8080/api/script/execute
```

### 预期性能

- **单机QPS**: 500+
- **平均响应时间**: < 100ms
- **P99响应时间**: < 500ms

## 📝 迁移步骤

### 从旧版本升级

1. **备份数据**
   ```bash
   redis-cli SAVE
   ```

2. **更新依赖**
   ```bash
   mvn clean install
   ```

3. **更新配置文件**
   - 参考新的 `application.yml` 格式
   - 配置LLM提供商

4. **设置环境变量**
   ```bash
   export OPENAI_API_KEY="..."
   export CLAUDE_API_KEY="..."
   ```

5. **重启应用**
   ```bash
   mvn spring-boot:run
   ```

## 🔍 故障排查

### LLM连接失败

```bash
# 检查OpenAI连接
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer $OPENAI_API_KEY"

# 检查Claude连接
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $CLAUDE_API_KEY" \
  -H "anthropic-version: 2023-06-01"

# 检查本地vLLM
curl http://localhost:8000/v1/models
```

### 性能问题

1. **检查缓存命中率**
   ```bash
   curl http://localhost:8080/actuator/metrics/cache.gets
   ```

2. **检查Redis连接池**
   ```bash
   curl http://localhost:8080/actuator/metrics/lettuce.pool
   ```

3. **查看慢查询日志**
   ```bash
   grep "execution.time.ms" logs/redis-script-query-perf.log | \
     awk '{if($NF > 1000) print}'
   ```

### 日志问题

```bash
# 检查日志文件权限
ls -la logs/

# 检查磁盘空间
df -h

# 清理旧日志
find logs/ -name "*.log.*" -mtime +30 -delete
```

## 📚 最佳实践

1. **生产环境使用云端LLM**: OpenAI或Claude，稳定性更好
2. **开发环境使用本地LLM**: Ollama或vLLM，成本更低
3. **启用缓存**: 提高性能，减少LLM调用
4. **监控日志**: 定期检查错误日志
5. **性能测试**: 上线前进行压力测试
6. **备份数据**: 定期备份Redis数据

## 🎯 下一步

- [ ] 集成Prometheus + Grafana监控
- [ ] 添加用户认证和权限控制
- [ ] 实现脚本版本管理
- [ ] 支持定时任务执行
- [ ] 添加结果导出功能（CSV, Excel）
