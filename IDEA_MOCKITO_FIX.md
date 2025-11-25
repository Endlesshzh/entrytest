# IntelliJ IDEA Mockito 找不到包问题解决方案

## 问题描述

在 IntelliJ IDEA 中编译时出现错误：
```
java: 程序包org.mockito不存在
```

但 Maven 命令行编译正常，说明是 IDE 配置问题。

## 解决方案

### 方法 1: 重新导入 Maven 项目（推荐）

1. **右键点击 `pom.xml`**
   - 选择 `Maven` → `Reload Project`
   - 或者 `Maven` → `Reimport`

2. **或者使用 Maven 工具窗口**
   - 打开右侧 `Maven` 工具窗口
   - 点击刷新按钮（🔄）重新加载项目

### 方法 2: 清理并重新构建

1. **清理项目**
   - `Build` → `Clean Project`

2. **重新构建**
   - `Build` → `Rebuild Project`

3. **重新导入 Maven**
   - 右键 `pom.xml` → `Maven` → `Reload Project`

### 方法 3: 使缓存失效并重启

1. **使缓存失效**
   - `File` → `Invalidate Caches...`
   - 选择 `Invalidate and Restart`

2. **重启后重新导入 Maven**
   - 右键 `pom.xml` → `Maven` → `Reload Project`

### 方法 4: 检查 Maven 设置

1. **打开 Maven 设置**
   - `File` → `Settings` (Windows/Linux)
   - `IntelliJ IDEA` → `Preferences` (macOS)
   - 导航到 `Build, Execution, Deployment` → `Build Tools` → `Maven`

2. **检查配置**
   - **Maven home path**: 确保指向正确的 Maven 安装目录
   - **User settings file**: 确保指向正确的 `settings.xml`
   - **Local repository**: 确保指向正确的本地仓库路径（通常是 `~/.m2/repository`）

3. **检查 JDK 设置**
   - `File` → `Project Structure` → `Project`
   - 确保 `SDK` 设置为 Java 17
   - `File` → `Project Structure` → `Modules`
   - 确保每个模块的 `Language level` 设置为 17

### 方法 5: 手动下载依赖

在终端中运行：

```bash
cd /Users/zhenhuan.hou/IdeaProjects/entrytest
mvn clean install -U
```

然后重新导入 Maven 项目。

### 方法 6: 检查项目结构

1. **打开项目结构**
   - `File` → `Project Structure` (Ctrl+Alt+Shift+S / Cmd+;)

2. **检查模块设置**
   - 确保 `src/test/java` 被标记为 `Test Sources`
   - 确保 `src/main/java` 被标记为 `Sources`

3. **检查依赖**
   - `Project Structure` → `Libraries`
   - 确保 Mockito 相关的库存在

## 验证修复

修复后，验证步骤：

1. **检查导入**
   - 打开 `RedisDataGeneratorTest.java`
   - 检查 `import org.mockito.*` 是否还有红色错误

2. **运行测试**
   - 右键测试类 → `Run 'RedisDataGeneratorTest'`
   - 或者运行单个测试方法

3. **命令行验证**
   ```bash
   cd /Users/zhenhuan.hou/IdeaProjects/entrytest
   mvn test-compile
   ```

## 常见原因

1. **IDE 缓存问题**：IDE 缓存了旧的依赖信息
2. **Maven 未同步**：IDE 没有正确同步 Maven 依赖
3. **JDK 配置错误**：IDE 使用了错误的 JDK 版本
4. **项目结构问题**：测试目录没有被正确识别为测试源

## 快速修复命令

在项目根目录执行：

```bash
cd /Users/zhenhuan.hou/IdeaProjects/entrytest

# 清理并重新下载依赖
mvn clean install -U

# 验证依赖
mvn dependency:tree | grep mockito
```

然后在 IDEA 中：
1. 右键 `pom.xml` → `Maven` → `Reload Project`
2. `File` → `Invalidate Caches...` → `Invalidate and Restart`

## 预防措施

1. **定期同步 Maven**
   - 修改 `pom.xml` 后立即重新导入

2. **使用 Maven 工具窗口**
   - 保持 Maven 工具窗口打开，方便查看依赖状态

3. **检查自动导入**
   - `Settings` → `Build Tools` → `Maven` → `Importing`
   - 确保 `Import Maven projects automatically` 已启用

## 如果问题仍然存在

1. **检查 `.idea` 目录**
   - 删除 `.idea` 目录（会丢失 IDE 特定设置）
   - 重新打开项目

2. **检查 Maven 本地仓库**
   ```bash
   ls ~/.m2/repository/org/mockito/
   ```
   确保 Mockito 依赖已下载

3. **检查项目 JDK**
   - 确保项目使用 Java 17
   - `File` → `Project Structure` → `Project` → `SDK`

4. **重新克隆项目**（最后手段）
   - 如果是从 Git 克隆的，可以重新克隆

## 联系信息

如果以上方法都无法解决问题，请检查：
- IntelliJ IDEA 版本
- Maven 版本
- Java 版本
- 项目配置

