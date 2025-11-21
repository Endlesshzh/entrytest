# SWEEP.md - 项目开发指南

## 项目信息
- **项目名称**: Redis Script Query System
- **Spring Boot 版本**: 3.1.5
- **Java 版本**: 17
- **构建工具**: Maven

## 常用命令

### Maven 命令
由于系统中没有安装 Maven CLI，请使用 IntelliJ IDEA 的 Maven 工具：

#### 重新加载 Maven 项目
- **方法 1**: 右键点击 `pom.xml` → Maven → Reload project
- **方法 2**: 打开 Maven 工具窗口 (View → Tool Windows → Maven)，点击刷新按钮 🔄
- **方法 3**: 使用快捷键 `Cmd + Shift + I` (Mac)

#### 清理和构建
- 在 Maven 工具窗口中: Lifecycle → clean → install

#### 运行应用
- 找到主类 (通常在 `src/main/java/org/example/` 下)
- 右键点击 → Run 或使用快捷键 `Ctrl + Shift + R`

### 依赖管理

#### 添加新依赖后的步骤
1. 编辑 `pom.xml` 添加依赖
2. 右键 `pom.xml` → Maven → Reload project
3. 等待 IntelliJ 下载依赖

#### 常见依赖问题
- **依赖未找到**: 确保 Maven 仓库配置正确，重新加载项目
- **版本冲突**: 检查 `pom.xml` 中的依赖版本是否兼容

## 项目结构

```
entrytest/
├── src/main/java/org/example/
│   ├── controller/          # REST API 控制器
│   ├── service/             # 业务逻辑服务
│   ├── util/                # 工具类
│   └── Application.java     # 主启动类
├── src/main/resources/
│   ├── application.yml      # 应用配置
│   └── logback-spring.xml   # 日志配置
├── logs/                    # 日志文件目录
└── pom.xml                  # Maven 配置
```

## 重要依赖

### 核心依赖
- **Spring Boot Starter Web**: Web 应用框架
- **Spring Boot Data Redis**: Redis 集成
- **Lettuce**: Redis 客户端
- **Groovy**: 脚本引擎支持

### 日志相关
- **logstash-logback-encoder** (版本 6.6): JSON 格式日志编码器
  - 用于 `logback-spring.xml` 中的 LogstashEncoder
  - 如果启动时报错 `ClassNotFoundException: net.logstash.logback.encoder.LogstashEncoder`
  - 确保此依赖已在 `pom.xml` 中启用（未被注释）

### 其他重要依赖
- **Caffeine**: 高性能缓存
- **OkHttp**: HTTP 客户端 (用于 LLM API 调用)
- **Micrometer Tracing**: 分布式追踪

## 代码风格

### 命名规范
- **类名**: PascalCase (例如: `RedisDataGenerator`)
- **方法名**: camelCase (例如: `generateTestData`)
- **常量**: UPPER_SNAKE_CASE (例如: `MAX_RETRY_COUNT`)
- **包名**: 小写 (例如: `org.example.util`)

### 注释规范
- 使用 JavaDoc 注释公共类和方法
- 中文注释用于解释复杂逻辑
- 英文注释用于代码文档

## 常见问题解决

### 1. Logback 配置错误
**错误**: `ClassNotFoundException: net.logstash.logback.encoder.LogstashEncoder`

**解决方案**:
1. 检查 `pom.xml` 中 `logstash-logback-encoder` 依赖是否启用
2. 重新加载 Maven 项目
3. 重启应用

### 2. Redis 连接失败
**检查项**:
- Redis 服务是否运行
- `application.yml` 中的 Redis 配置是否正确
- 网络连接是否正常

### 3. 端口占用
**解决方案**:
- 修改 `application.yml` 中的 `server.port`
- 或者停止占用端口的进程

## 日志配置

### 日志文件位置
- **所有日志**: `logs/redis-script-query.log`
- **错误日志**: `logs/redis-script-query-error.log`
- **性能日志**: `logs/redis-script-query-perf.log`

### 日志级别调整
在 `logback-spring.xml` 中修改对应 logger 的 level 属性：
- DEBUG: 详细调试信息
- INFO: 一般信息
- WARN: 警告信息
- ERROR: 错误信息

## 性能优化建议

### Redis 连接池配置
在 `application.yml` 中调整 Lettuce 连接池参数

### 缓存策略
- 使用 Caffeine 缓存频繁访问的数据
- 设置合理的缓存过期时间

### 异步日志
- 已配置 AsyncAppender 提升日志性能
- 可调整 queueSize 参数优化

## 开发工作流

1. **开始新功能**
   - 创建新分支
   - 编写代码
   - 添加单元测试

2. **测试**
   - 运行单元测试
   - 本地集成测试

3. **提交代码**
   - 确保代码格式正确
   - 提交前运行测试
   - 编写清晰的提交信息

## 参考文档

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/3.1.5/reference/html/)
- [Logback 配置指南](https://logback.qos.ch/manual/configuration.html)
- [Redis 命令参考](https://redis.io/commands/)
