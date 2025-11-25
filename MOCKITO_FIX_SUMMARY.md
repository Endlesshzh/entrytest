# Mockito 问题修复总结

## 问题描述

执行 Test 方法时报错：找不到 Mockito 或无法 mock 类

**实际错误：**
```
Mockito cannot mock this class: class org.example.config.ScriptConfig
Java 25 (69) is not supported by the current version of Byte Buddy which officially supports Java 22 (66)
```

## 根本原因

1. **Java 25 兼容性问题**：Byte Buddy（Mockito 的底层依赖）官方只支持到 Java 22
2. **Mockito 无法 mock 某些类**：由于 Byte Buddy 的限制，无法修改类字节码

## 解决方案

### 1. 修改测试类使用 Spring Boot 测试方式

**修改的文件：**
- `src/test/java/org/example/service/LlmAnalysisServiceTest.java`
- `src/test/java/org/example/util/RedisDataGeneratorTest.java`

**主要变更：**
- 将 `@ExtendWith(MockitoExtension.class)` 改为 `@ExtendWith(SpringExtension.class)`
- 将 `@Mock` 改为 `@MockBean`（对于 Spring Bean）
- 保持 `@Mock` 用于非 Spring Bean（如接口）

### 2. 启用 Byte Buddy 实验性支持

**修改的文件：** `pom.xml`

在 `maven-surefire-plugin` 配置中添加：
```xml
<configuration>
    <argLine>-Dnet.bytebuddy.experimental=true</argLine>
</configuration>
```

这允许 Byte Buddy 在 Java 25 上以实验模式运行。

### 3. 升级 Mockito 版本

**修改的文件：** `pom.xml`

将 Mockito 从 5.5.0 升级到 5.11.0（虽然仍然不完全支持 Java 25，但配合实验性标志可以工作）

## 测试结果

### ✅ RedisDataGeneratorTest
- **测试数量：** 18
- **通过：** 18
- **失败：** 0
- **错误：** 0

### ✅ LlmAnalysisServiceTest
- **测试数量：** 15
- **通过：** 15
- **失败：** 0
- **错误：** 0

## 修改详情

### LlmAnalysisServiceTest.java

**之前：**
```java
@ExtendWith(MockitoExtension.class)
class LlmAnalysisServiceTest {
    @Mock
    private ScriptConfig scriptConfig;
    
    @Mock
    private LlmServiceFactory llmServiceFactory;
    
    @Mock
    private LlmService llmService;
}
```

**之后：**
```java
@ExtendWith(SpringExtension.class)
class LlmAnalysisServiceTest {
    @MockBean
    private ScriptConfig scriptConfig;
    
    @MockBean
    private LlmServiceFactory llmServiceFactory;
    
    @Mock
    private LlmService llmService;  // 接口，保持 @Mock
}
```

### RedisDataGeneratorTest.java

**之前：**
```java
@ExtendWith(MockitoExtension.class)
class RedisDataGeneratorTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    // ...
}
```

**之后：**
```java
@ExtendWith(SpringExtension.class)
class RedisDataGeneratorTest {
    @MockBean
    private RedisTemplate<String, Object> redisTemplate;
    // ... 其他 Operations 保持 @Mock
}
```

### pom.xml

**Surefire 插件配置：**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <argLine>-Dnet.bytebuddy.experimental=true</argLine>
    </configuration>
</plugin>
```

**Mockito 版本管理：**
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.11.0</version>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>5.11.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 关键要点

1. **使用 `@MockBean` 替代 `@Mock`**：对于 Spring Bean，使用 `@MockBean` 可以避免 Byte Buddy 的限制
2. **启用实验性支持**：`-Dnet.bytebuddy.experimental=true` 允许 Byte Buddy 在 Java 25 上运行
3. **Spring 测试框架**：使用 `SpringExtension` 可以更好地集成 Spring Boot 测试

## 验证命令

```bash
# 运行 RedisDataGeneratorTest
mvn test -Dtest=RedisDataGeneratorTest

# 运行 LlmAnalysisServiceTest
mvn test -Dtest=LlmAnalysisServiceTest

# 运行所有测试
mvn test
```

## 注意事项

1. **Java 25 是预览版本**：建议在生产环境使用 Java 17 或 Java 21 LTS 版本
2. **实验性标志**：`net.bytebuddy.experimental=true` 是实验性的，可能在某些情况下不稳定
3. **未来升级**：当 Byte Buddy 正式支持 Java 25 时，可以移除实验性标志

## 相关文件

- `pom.xml` - Maven 配置（已更新）
- `src/test/java/org/example/service/LlmAnalysisServiceTest.java` - 已修改
- `src/test/java/org/example/util/RedisDataGeneratorTest.java` - 已修改
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` - Mockito 扩展配置

## 总结

✅ **问题已解决**
- 所有测试都能正常运行
- Mockito 可以正常工作
- 与 Java 25 兼容（通过实验性标志）

**建议：** 如果可能，考虑使用 Java 17 或 Java 21 LTS 版本以获得更好的稳定性和兼容性。

