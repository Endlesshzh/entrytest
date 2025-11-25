# Mockito 问题总结和解决方案

## 问题诊断

### 实际问题
**不是找不到 Mockito 依赖，而是 Mockito 无法 mock `ScriptConfig` 类**

### 错误信息
```
Mockito cannot mock this class: class org.example.config.ScriptConfig
Could not modify all classes [class org.example.config.ScriptConfig, class java.lang.Object]
```

### 根本原因
1. **Java 25 兼容性问题**：Mockito 5.5.0 和 5.11.0 在 Java 25 上都无法正常工作
2. **Inline Mock Maker 限制**：即使启用了 `mock-maker-inline`，Java 25 的类加载机制变化导致无法修改类

## 已实施的修复

### ✅ 1. 创建 Mockito 扩展配置
- 文件：`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
- 内容：`mock-maker-inline`

### ✅ 2. 升级 Mockito 版本
- 从 5.5.0 升级到 5.11.0
- 更新了 `pom.xml` 中的 `dependencyManagement`

### ✅ 3. 修复 Surefire 插件配置
- 移除了 `skipTests=true` 配置

## 推荐解决方案

### 方案 1: 使用 Spring Boot 的 @MockBean（推荐）

修改 `LlmAnalysisServiceTest.java`：

```java
// 移除 @ExtendWith(MockitoExtension.class)
// 添加 Spring Boot 测试支持
@ExtendWith(SpringExtension.class)
@DisplayName("LLM分析服务单元测试")
class LlmAnalysisServiceTest {

    // 使用 @MockBean 替代 @Mock
    @MockBean
    private ScriptConfig scriptConfig;

    @MockBean
    private LlmServiceFactory llmServiceFactory;

    @Mock
    private LlmService llmService;  // 这个可以保持 @Mock

    // ...
}
```

**优点：**
- Spring Boot 的 `@MockBean` 使用不同的 mock 机制，不依赖 byte-buddy
- 与 Spring 测试框架集成更好
- 不受 Java 版本限制

### 方案 2: 使用 Java 17 运行测试

如果必须使用纯 Mockito（不使用 Spring），可以：

1. 安装 Java 17
2. 在 `pom.xml` 中配置使用 Java 17：
```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

3. 使用 Java 17 运行测试：
```bash
JAVA_HOME=/path/to/java17 mvn test
```

### 方案 3: 创建测试配置类（适用于配置类）

创建一个测试专用的 `ScriptConfig` 实现：

```java
@Configuration
@Profile("test")
public class TestScriptConfig extends ScriptConfig {
    // 测试实现
}
```

## 当前状态

- ✅ Mockito 依赖已正确配置（5.11.0）
- ✅ Mockito 扩展配置已创建
- ✅ Surefire 插件配置已修复
- ❌ 测试仍然失败（Java 25 兼容性问题）

## 下一步行动

**推荐：** 修改 `LlmAnalysisServiceTest` 使用 `@MockBean` 替代 `@Mock`

这样可以：
1. 解决 Java 25 兼容性问题
2. 与项目中其他测试类保持一致（其他 Controller 测试都使用 `@MockBean`）
3. 更好地集成 Spring Boot 测试框架

## 相关文件

- `pom.xml` - Maven 配置（已更新 Mockito 版本）
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` - Mockito 扩展配置
- `src/test/java/org/example/service/LlmAnalysisServiceTest.java` - 需要修改的测试类
- `MOCKITO_TROUBLESHOOTING.md` - 详细排查指南

