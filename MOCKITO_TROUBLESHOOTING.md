# Mockito 运行时问题排查指南

## 问题描述

执行 Test 方法时报错：找不到 Mockito 或无法 mock 类

## 根本原因

**不是找不到 Mockito 依赖，而是 Mockito 无法 mock 某些类**

### 常见错误信息

```
Mockito cannot mock this class: class org.example.config.ScriptConfig
Could not modify all classes [class org.example.config.ScriptConfig, class java.lang.Object]
```

## 解决方案

### 方案 1: 启用 Mockito Inline Mock Maker（推荐）

创建文件：`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`

内容：
```
mock-maker-inline
```

**优点：**
- 可以 mock final 类、final 方法、静态方法
- 不需要修改源代码
- 适用于 Java 17+ 和 Java 25

**已实施：** ✅ 已创建该文件

### 方案 2: 使用 Spring Boot 的 @MockBean（适用于 Spring 测试）

如果测试类使用了 `@SpringBootTest`，可以使用 `@MockBean` 替代 `@Mock`：

```java
@SpringBootTest
class LlmAnalysisServiceTest {
    
    @MockBean
    private ScriptConfig scriptConfig;  // 使用 @MockBean 替代 @Mock
    
    // ...
}
```

### 方案 3: 修改类为非 final（不推荐）

如果类被声明为 `final`，可以移除 `final` 关键字，但这会改变类的设计。

## 已实施的修复

1. ✅ 创建了 `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` 文件
2. ✅ 修复了 `pom.xml` 中的 `skipTests` 配置（从 `true` 改为注释掉）

## 验证步骤

### 1. 检查 Mockito 扩展文件

```bash
cat src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
```

应该输出：`mock-maker-inline`

### 2. 运行测试

```bash
mvn test -Dtest=LlmAnalysisServiceTest#testGetAvailableProviders
```

### 3. 如果仍然失败

检查 Java 版本兼容性：

```bash
java -version
```

Mockito 5.5.0 支持：
- Java 8+
- 但 Java 25 可能需要更新版本的 Mockito

如果使用 Java 25，考虑升级 Mockito：

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.11.0</version>  <!-- 或最新版本 -->
</dependency>
```

## 其他可能的问题

### 问题 1: IDE 中找不到 Mockito

**症状：** IDE 显示红色错误，提示找不到 `org.mockito.*`

**解决方案：**
1. **IntelliJ IDEA:**
   - File -> Invalidate Caches / Restart
   - 右键 `pom.xml` -> Maven -> Reload Project

2. **VS Code / Cursor:**
   - Cmd+Shift+P -> Reload Window
   - 运行 `mvn clean install`

3. **Eclipse:**
   - 右键项目 -> Maven -> Update Project
   - Project -> Clean

### 问题 2: 类路径问题

**症状：** 编译成功，但运行时 `ClassNotFoundException`

**解决方案：**
```bash
# 清理并重新构建
mvn clean install

# 检查依赖是否正确下载
mvn dependency:resolve

# 检查测试类路径
mvn dependency:build-classpath -DincludeScope=test
```

### 问题 3: Maven Surefire 插件跳过测试

**症状：** 运行 `mvn test` 但测试被跳过

**检查：**
```bash
grep -A 5 "maven-surefire-plugin" pom.xml
```

**修复：** 确保 `skipTests` 不是 `true`，或使用 `-DskipTests=false` 运行

## 诊断脚本

运行诊断脚本获取详细信息：

```bash
./diagnose-mockito.sh
```

## 相关文件

- `pom.xml` - Maven 配置
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` - Mockito 扩展配置
- `diagnose-mockito.sh` - 诊断脚本
- `MOCKITO_DEPENDENCY_ANALYSIS.md` - 依赖分析报告

## 总结

**关键点：**
1. ✅ Mockito 依赖已正确配置（5.5.0）
2. ✅ 已启用 inline mock maker 以支持 mock final 类
3. ✅ 已修复 surefire 插件配置
4. ⚠️ 如果使用 Java 25，可能需要升级 Mockito 版本

**下一步：**
- 运行测试验证修复是否生效
- 如果仍有问题，考虑升级 Mockito 到最新版本

