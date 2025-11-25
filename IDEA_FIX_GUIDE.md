# IntelliJ IDEA Mockito 依赖问题修复指南

## 问题描述
IDEA 中编译时报错：`java: 程序包org.mockito不存在`，但 Maven 命令行可以正常编译和运行测试。

## 根本原因
IDEA 的模块配置文件 (`entrytest.iml`) 配置不正确，没有正确识别 Maven 项目的测试依赖。

## 解决方案（按顺序尝试）

### 方案 1: 重新导入 Maven 项目（推荐）

1. **关闭 IDEA**

2. **删除错误的模块配置文件**
   ```bash
   cd /Users/zhenhuan.hou/IdeaProjects/entrytest
   rm entrytest.iml
   ```

3. **重新打开项目**
   - 打开 IDEA
   - `File` → `Open` → 选择项目目录
   - IDEA 会提示检测到 Maven 项目，选择 `Import Maven Project`

4. **等待 IDEA 导入完成**
   - 查看右下角的进度条
   - 等待 Maven 依赖下载完成

### 方案 2: 在 IDEA 中重新导入（不关闭 IDEA）

1. **删除模块**
   - `File` → `Project Structure` (Cmd+; / Ctrl+Alt+Shift+S)
   - `Modules` → 选择 `entrytest` → 点击 `-` 删除

2. **重新导入 Maven 项目**
   - `File` → `New` → `Project from Existing Sources...`
   - 选择项目目录
   - 选择 `Import project from external model` → `Maven`
   - 点击 `Next` → `Finish`

### 方案 3: 刷新 Maven 项目

1. **打开 Maven 工具窗口**
   - `View` → `Tool Windows` → `Maven`
   - 或点击右侧边栏的 `Maven` 标签

2. **重新加载项目**
   - 在 Maven 工具窗口中，点击刷新图标 🔄
   - 或右键 `pom.xml` → `Maven` → `Reload Project`

3. **清理缓存**
   - `File` → `Invalidate Caches...`
   - 选择 `Invalidate and Restart`

### 方案 4: 检查项目设置

1. **检查 JDK 设置**
   - `File` → `Project Structure` → `Project`
   - 确保 `SDK` 设置为 `17` (Java 17)
   - 确保 `Language level` 设置为 `17`

2. **检查模块设置**
   - `File` → `Project Structure` → `Modules` → `entrytest`
   - 确保 `src/main/java` 标记为 `Sources` (蓝色)
   - 确保 `src/test/java` 标记为 `Test Sources` (绿色)
   - 检查 `Dependencies` 标签页，确保有 `Maven: org.mockito:mockito-core:5.11.0` (test)

3. **检查编译器设置**
   - `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Java Compiler`
   - 确保 `Project bytecode version` 设置为 `17`

### 方案 5: 手动修复模块配置（高级）

如果以上方法都不行，可以手动修复 `entrytest.iml`：

1. **关闭 IDEA**

2. **删除错误的 iml 文件**
   ```bash
   rm entrytest.iml
   ```

3. **重新打开项目**
   - IDEA 会自动重新生成正确的 Maven 模块配置

## 验证修复

修复后，验证步骤：

1. **检查导入**
   - 打开 `src/test/java/org/example/util/RedisDataGeneratorTest.java`
   - 检查 `import org.mockito.*` 是否还有红色错误
   - 应该可以正常识别 Mockito 类

2. **检查项目结构**
   - `File` → `Project Structure` → `Modules` → `entrytest` → `Dependencies`
   - 应该能看到 `Maven: org.mockito:mockito-core:5.11.0` (test scope)

3. **运行测试**
   - 右键测试类 → `Run 'RedisDataGeneratorTest'`
   - 或运行单个测试方法 `testGenerateTestData_Complete`

## 如果问题仍然存在

1. **检查 Maven 设置**
   - `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven`
   - 确保 `Maven home directory` 指向正确的 Maven 安装
   - 确保 `User settings file` 和 `Local repository` 配置正确

2. **检查 Maven 自动导入**
   - `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven` → `Importing`
   - 确保 `Import Maven projects automatically` 已勾选

3. **使用命令行验证**
   ```bash
   cd /Users/zhenhuan.hou/IdeaProjects/entrytest
   export JAVA_HOME=/Users/zhenhuan.hou/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home
   mvn clean compile test-compile
   ```
   如果命令行可以编译，说明问题只在 IDEA 配置

## 临时解决方案

如果修复困难，但测试可以正常运行：

1. **使用 Maven 运行测试**
   - 在 IDEA 的终端中运行：
     ```bash
     export JAVA_HOME=/Users/zhenhuan.hou/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home
     mvn test -Dtest=RedisDataGeneratorTest#testGenerateTestData_Complete
     ```

2. **配置 IDEA 的 Run Configuration**
   - `Run` → `Edit Configurations...`
   - 添加新的 `Maven` 配置
   - `Working directory`: 项目根目录
   - `Command line`: `test -Dtest=RedisDataGeneratorTest#testGenerateTestData_Complete`
   - `JRE`: 选择 Java 17

