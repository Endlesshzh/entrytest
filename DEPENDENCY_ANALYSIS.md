# Mockito 依赖分析说明

## 当前配置

### 1. Mockito 版本配置
```xml
<properties>
    <mockito.version>5.5.0</mockito.version>
</properties>
```

### 2. 依赖配置策略
- **排除** `spring-boot-starter-test` 中的默认 Mockito 依赖
- **显式添加** `mockito-core` 和 `mockito-junit-jupiter`，确保版本正确

### 3. 完整依赖配置
```xml
<!-- Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <exclusions>
        <!-- 排除默认的 Mockito，使用显式指定的版本 -->
        <exclusion>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- Mockito for testing - 显式指定版本确保依赖正确 -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>${mockito.version}</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>${mockito.version}</version>
    <scope>test</scope>
</dependency>
```

## 如何验证依赖

### 方法 1: 使用 Maven 命令（推荐）
```bash
# 查看依赖树，过滤 Mockito 相关
mvn dependency:tree | grep -i mockito

# 查看所有测试依赖
mvn dependency:tree -Dscope=test

# 查看完整的依赖树
mvn dependency:tree > dependency-tree.txt
```

### 方法 2: 在 IDE 中检查
- **IntelliJ IDEA**: 
  - 右键 `pom.xml` → `Maven` → `Show Dependencies`
  - 或 `View` → `Tool Windows` → `Maven` → 展开 `Dependencies`
  
- **Eclipse**: 
  - 右键项目 → `Maven` → `Show Dependencies`

### 方法 3: 检查编译后的类路径
```bash
# 查看测试类路径中的 Mockito
find target/test-classes -name "*mockito*" -o -name "*Mockito*"
```

## 常见问题排查

### 问题 1: IDE 中显示找不到 Mockito
**解决方案**:
1. 刷新 Maven 项目（IDEA: 右键 `pom.xml` → `Maven` → `Reload Project`）
2. 清理并重新构建：`mvn clean install`
3. 检查 IDE 的 Maven 设置，确保使用正确的 Maven 版本

### 问题 2: 编译错误 "找不到符号: Mockito"
**解决方案**:
1. 确认 `pom.xml` 已保存
2. 运行 `mvn clean compile test-compile`
3. 检查 `target/test-classes` 目录是否存在 Mockito 类

### 问题 3: 运行时 ClassNotFoundException
**解决方案**:
1. 检查依赖是否正确下载：`mvn dependency:resolve`
2. 查看本地仓库：`~/.m2/repository/org/mockito/`
3. 强制更新依赖：`mvn clean install -U`

## 依赖版本兼容性

| Spring Boot 版本 | 推荐的 Mockito 版本 | 说明 |
|-----------------|-------------------|------|
| 3.1.5           | 5.5.0             | 当前使用 |
| 3.1.x           | 5.4.0 - 5.6.0     | 兼容范围 |
| 3.0.x           | 5.3.0 - 5.5.0     | 兼容范围 |

## 验证步骤

1. **检查 pom.xml 配置**
   - ✅ Mockito 版本属性已定义
   - ✅ spring-boot-starter-test 中排除了 Mockito
   - ✅ 显式添加了 mockito-core 和 mockito-junit-jupiter

2. **刷新 Maven 项目**
   - 在 IDE 中重新加载 Maven 项目

3. **验证导入**
   - 检查测试文件中的 `import org.mockito.*` 是否正常
   - 确认没有红色错误提示

4. **运行测试**
   ```bash
   mvn test -Dtest=LlmAnalysisServiceTest
   ```

## 如果问题仍然存在

1. **删除本地 Maven 缓存**
   ```bash
   rm -rf ~/.m2/repository/org/mockito/
   mvn clean install -U
   ```

2. **检查网络和仓库配置**
   - 确保可以访问 Maven 中央仓库
   - 检查是否有代理设置问题

3. **查看详细错误信息**
   - 运行 `mvn clean compile test-compile -X` 查看详细日志
   - 检查 IDE 的错误日志

