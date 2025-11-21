# 系统架构文档

## 系统概述

Redis Script Query System 是一个基于Spring Boot的Web应用，允许用户通过Groovy脚本动态查询Redis数据，并使用本地部署的大语言模型（LLM）进行脚本安全性和规范性分析。

## 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        用户浏览器                              │
│                     (Web Interface)                          │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/HTTPS
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Controller Layer                         │  │
│  │  ┌──────────────────┐  ┌──────────────────┐         │  │
│  │  │ WebController    │  │ ScriptController │         │  │
│  │  └──────────────────┘  └──────────────────┘         │  │
│  └──────────────────────────────────────────────────────┘  │
│                         │                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Service Layer                            │  │
│  │  ┌──────────────────┐  ┌──────────────────┐         │  │
│  │  │ScriptEngineService│ │LlmAnalysisService│         │  │
│  │  └──────────────────┘  └──────────────────┘         │  │
│  └──────────────────────────────────────────────────────┘  │
│                         │                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Configuration Layer                      │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐            │  │
│  │  │RedisConfig│ │ScriptConfig│ │LlmConfig│            │  │
│  │  └──────────┘ └──────────┘ └──────────┘            │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────┬──────────────────────────┬───────────────────┘
             │                          │
             ▼                          ▼
    ┌────────────────┐        ┌────────────────┐
    │     Redis      │        │  Ollama LLM    │
    │   (Database)   │        │  (Analysis)    │
    └────────────────┘        └────────────────┘
```

## 核心组件

### 1. Controller Layer（控制器层）

#### WebController
- **职责**: 提供Web页面访问
- **端点**:
  - `GET /` - 返回主页面

#### ScriptController
- **职责**: 处理脚本相关的REST API请求
- **端点**:
  - `POST /api/script/execute` - 执行脚本
  - `POST /api/script/test` - 试运行脚本
  - `POST /api/script/analyze` - 分析脚本
  - `GET /api/script/health` - 健康检查

### 2. Service Layer（服务层）

#### ScriptEngineService
- **职责**: Groovy脚本执行引擎
- **核心功能**:
  - 脚本解析和编译
  - 脚本执行（带超时控制）
  - 安全验证（禁止危险操作）
  - Redis操作封装
  - 脚本缓存（可选）

**关键方法**:
```java
public ScriptExecutionResult executeScript(String scriptText, boolean testRun)
private void validateScript(String script)
private Binding createBinding()
private Object executeWithTimeout(Script script, long timeoutMs)
```

**内部类 RedisOperations**:
提供安全的Redis操作接口：
- `get(key)` - 获取字符串值
- `set(key, value)` - 设置字符串值
- `hget(key, field)` - 获取Hash字段
- `hgetAll(key)` - 获取Hash所有字段
- `keys(pattern)` - 查询键
- `lrange(key, start, end)` - 获取List范围
- 等等...

#### LlmAnalysisService
- **职责**: 使用LLM分析脚本
- **核心功能**:
  - 基础静态分析
  - LLM深度分析
  - 安全性评分
  - 代码质量评分
  - 性能建议
  - 最佳实践建议

**关键方法**:
```java
public ScriptAnalysisResult analyzeScript(String script)
private ScriptAnalysisResult performBasicAnalysis(String script)
private String getLlmAnalysis(String script)
private String buildAnalysisPrompt(String script)
```

### 3. Configuration Layer（配置层）

#### RedisConfig
- **职责**: Redis连接配置
- **配置内容**:
  - RedisTemplate配置
  - 序列化器配置
  - 连接池配置

#### ScriptConfig
- **职责**: 脚本引擎配置
- **配置项**:
  - 最大执行时间
  - 缓存设置
  - 允许的命令白名单
  - 禁止的模式黑名单

#### LlmConfig
- **职责**: LLM服务配置
- **配置项**:
  - API地址
  - 模型名称
  - 超时设置
  - 生成参数（temperature, max_tokens）

### 4. Model Layer（模型层）

#### 请求模型
- `ScriptExecutionRequest` - 脚本执行请求
- `ScriptAnalysisRequest` - 脚本分析请求

#### 响应模型
- `ScriptExecutionResult` - 脚本执行结果
- `ScriptAnalysisResult` - 脚本分析结果

## 数据流

### 脚本执行流程

```
用户输入脚本
    │
    ▼
ScriptController.executeScript()
    │
    ▼
ScriptEngineService.executeScript()
    │
    ├─► validateScript() ──► 安全检查
    │
    ├─► createBinding() ──► 创建执行环境
    │
    ├─► GroovyShell.parse() ──► 解析脚本
    │
    ├─► executeWithTimeout() ──► 执行（带超时）
    │   │
    │   └─► RedisOperations ──► Redis查询
    │
    └─► ScriptExecutionResult ──► 返回结果
```

### 脚本分析流程

```
用户请求分析
    │
    ▼
ScriptController.analyzeScript()
    │
    ▼
LlmAnalysisService.analyzeScript()
    │
    ├─► performBasicAnalysis() ──► 静态分析
    │   │
    │   ├─► 检查禁止模式
    │   ├─► 检查危险操作
    │   ├─► 代码质量检查
    │   └─► 性能检查
    │
    ├─► getLlmAnalysis() ──► LLM分析
    │   │
    │   ├─► buildAnalysisPrompt() ──► 构建提示词
    │   ├─► HTTP请求到Ollama
    │   └─► 解析LLM响应
    │
    └─► ScriptAnalysisResult ──► 返回结果
```

## 安全机制

### 1. 脚本验证层

**禁止模式检查**:
- FLUSHDB, FLUSHALL（清空数据库）
- SHUTDOWN（关闭服务器）
- CONFIG（修改配置）
- SCRIPT KILL/FLUSH（脚本管理）
- SAVE, BGSAVE（持久化操作）

**危险操作检查**:
- System.exit（系统退出）
- Runtime.getRuntime（运行时操作）
- java.io.File（文件操作）
- Class.forName（动态类加载）

### 2. 命令白名单

只允许执行配置中定义的Redis命令：
- 读操作：GET, HGET, HGETALL, KEYS, LRANGE, SMEMBERS, ZRANGE
- 写操作：SET, HSET（可配置）
- 元数据：TTL, EXISTS

### 3. 执行超时

- 默认5秒超时
- 防止无限循环
- 使用ExecutorService实现

### 4. 沙箱环境

- Groovy脚本在受限环境中执行
- 只能访问预定义的Redis操作
- 无法访问系统资源

## 性能优化

### 1. 脚本缓存

```java
private final ConcurrentHashMap<String, Script> scriptCache;
```

- 缓存已编译的脚本
- 减少重复编译开销
- 可配置缓存大小

### 2. 连接池

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
```

- Redis连接池
- 复用连接
- 减少连接开销

### 3. 异步执行

```java
private final ExecutorService executorService;
```

- 使用线程池执行脚本
- 支持超时控制
- 不阻塞主线程

## 扩展性

### 1. 添加新的Redis操作

在 `ScriptEngineService.RedisOperations` 中添加新方法：

```java
public Object myOperation(String key) {
    return redisTemplate.opsForValue().get(key);
}
```

### 2. 自定义分析规则

在 `LlmAnalysisService.performBasicAnalysis()` 中添加规则：

```java
if (script.contains("PATTERN")) {
    securityIssues.add("Custom security issue");
    securityScore -= 10;
}
```

### 3. 支持其他脚本语言

实现新的 `ScriptEngine` 接口：
- JavaScript (Nashorn/GraalVM)
- Python (Jython)
- Ruby (JRuby)

### 4. 集成其他LLM

修改 `LlmAnalysisService` 支持：
- OpenAI API
- Claude API
- 本地部署的其他模型

## 监控和日志

### 日志级别

```yaml
logging:
  level:
    root: INFO
    org.example: DEBUG
    org.springframework.data.redis: DEBUG
```

### 日志内容

- 脚本执行日志
- 安全验证日志
- LLM分析日志
- 错误和异常日志

### 日志文件

```
logs/redis-script-query.log
```

## 部署架构

### 单机部署

```
┌─────────────────────┐
│  Application Server │
│  ┌───────────────┐  │
│  │ Spring Boot   │  │
│  └───────────────┘  │
│  ┌───────────────┐  │
│  │    Redis      │  │
│  └───────────────┘  │
│  ┌───────────────┐  │
│  │    Ollama     │  │
│  └───────────────┘  │
└─────────────────────┘
```

### 分布式部署

```
┌──────────────┐     ┌──────────────┐
│ Spring Boot  │────▶│    Redis     │
│ Application  │     │   Cluster    │
└──────┬───────┘     └──────────────┘
       │
       │             ┌──────────────┐
       └────────────▶│    Ollama    │
                     │   Service    │
                     └──────────────┘
```

## 技术选型理由

### Spring Boot
- 快速开发
- 自动配置
- 生产就绪
- 丰富的生态系统

### Groovy
- JVM原生支持
- 动态语言特性
- 与Java无缝集成
- 简洁的语法

### Redis
- 高性能
- 丰富的数据结构
- 广泛使用
- 易于部署

### Ollama
- 本地部署
- 隐私保护
- 免费开源
- 支持多种模型

## 未来改进方向

1. **脚本管理**
   - 脚本保存和版本控制
   - 脚本分享和导入导出
   - 脚本模板库

2. **权限控制**
   - 用户认证和授权
   - 脚本执行权限
   - 数据访问控制

3. **监控和告警**
   - 执行统计
   - 性能监控
   - 异常告警

4. **高级功能**
   - 定时任务
   - 批量执行
   - 结果导出（CSV, JSON, Excel）

5. **UI增强**
   - 代码高亮
   - 自动补全
   - 调试功能
