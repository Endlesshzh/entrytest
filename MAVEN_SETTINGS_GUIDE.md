# Maven Settings 配置指南

## 问题分析

如果 Mockito 依赖无法正确识别，可能是 Maven 的 `settings.xml` 配置问题。Maven 有两个级别的 settings.xml：

1. **全局 settings.xml**: `$MAVEN_HOME/conf/settings.xml`（所有用户共享）
2. **用户 settings.xml**: `~/.m2/settings.xml`（仅当前用户）

## 检查当前配置

### 1. 检查是否存在 settings.xml

```bash
# 检查用户级配置
ls -la ~/.m2/settings.xml

# 检查项目级配置
ls -la .mvn/settings.xml
```

### 2. 检查 Maven 使用的配置

```bash
# 查看 Maven 使用的 settings.xml 路径
mvn help:effective-settings
```

## 创建/配置 settings.xml

### 方案 1: 创建用户级 settings.xml（推荐）

如果 `~/.m2/settings.xml` 不存在，创建一个：

```bash
mkdir -p ~/.m2
cat > ~/.m2/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    
    <!-- 本地仓库路径 -->
    <localRepository>${user.home}/.m2/repository</localRepository>
    
    <!-- 镜像配置（如果需要） -->
    <mirrors>
        <!-- 阿里云镜像（可选，加速下载） -->
        <mirror>
            <id>aliyunmaven</id>
            <mirrorOf>central</mirrorOf>
            <name>Aliyun Maven Central</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
    
    <!-- 配置文件激活 -->
    <profiles>
        <profile>
            <id>default</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <repositories>
                <repository>
                    <id>central</id>
                    <name>Maven Central Repository</name>
                    <url>https://repo1.maven.org/maven2</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <id>central</id>
                    <name>Maven Plugin Repository</name>
                    <url>https://repo1.maven.org/maven2</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </pluginRepository>
            </pluginRepositories>
        </profile>
    </profiles>
    
    <activeProfiles>
        <activeProfile>default</activeProfile>
    </activeProfiles>
</settings>
EOF
```

### 方案 2: 项目级 settings.xml

在项目根目录创建 `.mvn/settings.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    
    <repositories>
        <repository>
            <id>central</id>
            <name>Maven Central Repository</name>
            <url>https://repo1.maven.org/maven2</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>
</settings>
```

## IDE 配置检查

### IntelliJ IDEA

1. **检查 Maven 设置**
   - `File` → `Settings` (Windows/Linux) 或 `IntelliJ IDEA` → `Preferences` (Mac)
   - `Build, Execution, Deployment` → `Build Tools` → `Maven`
   - 确认以下设置：
     - ✅ `Maven home path`: 指向正确的 Maven 安装目录
     - ✅ `User settings file`: 指向 `~/.m2/settings.xml`
     - ✅ `Local repository`: 指向 `~/.m2/repository`

2. **重新导入 Maven 项目**
   - 右键 `pom.xml` → `Maven` → `Reload Project`
   - 或使用快捷键 `Cmd + Shift + I` (Mac) / `Ctrl + Shift + I` (Windows/Linux)

3. **清理缓存**
   - `File` → `Invalidate Caches / Restart`
   - 选择 `Invalidate and Restart`

### Eclipse

1. **检查 Maven 设置**
   - `Window` → `Preferences` → `Maven`
   - 确认 `User Settings` 指向正确的 `settings.xml`

2. **更新项目**
   - 右键项目 → `Maven` → `Update Project`
   - 勾选 `Force Update of Snapshots/Releases`

### VS Code

1. **检查 Java 扩展设置**
   - 打开设置 (`Cmd + ,`)
   - 搜索 "maven"
   - 确认 Maven 路径和 settings.xml 路径

2. **重新加载窗口**
   - `Cmd + Shift + P` → `Java: Clean Java Language Server Workspace`

## 验证配置

### 1. 检查 Maven 是否能访问仓库

```bash
# 如果已安装 Maven
mvn dependency:resolve -U
```

### 2. 检查依赖是否正确下载

```bash
# 查看本地仓库中的 Mockito
ls -la ~/.m2/repository/org/mockito/
```

### 3. 查看有效设置

```bash
# 如果已安装 Maven
mvn help:effective-settings
```

## 常见问题

### 问题 1: 网络问题导致无法下载依赖

**解决方案**:
- 使用镜像仓库（如阿里云镜像）
- 检查代理设置
- 检查防火墙设置

### 问题 2: 权限问题

**解决方案**:
```bash
# 确保 Maven 仓库目录有写权限
chmod -R 755 ~/.m2/repository
```

### 问题 3: IDE 使用的 Maven 版本不对

**解决方案**:
- 在 IDE 设置中指定正确的 Maven 路径
- 确保使用 Maven 3.6+ 版本

## 快速修复步骤

1. **创建/检查 settings.xml**
   ```bash
   # 如果不存在，创建基本配置
   test -f ~/.m2/settings.xml || (mkdir -p ~/.m2 && cat > ~/.m2/settings.xml << 'EOF'
   <?xml version="1.0" encoding="UTF-8"?>
   <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
             http://maven.apache.org/xsd/settings-1.0.0.xsd">
       <localRepository>${user.home}/.m2/repository</localRepository>
   </settings>
   EOF
   )
   ```

2. **在 IDE 中刷新 Maven 项目**
   - IntelliJ IDEA: 右键 `pom.xml` → `Maven` → `Reload Project`

3. **清理并重新构建**
   - 在 IDE 的 Maven 工具窗口中执行 `clean` → `install`

4. **验证依赖**
   - 检查测试文件中的 Mockito 导入是否正常

## 如果仍然不行

1. **完全清理 Maven 缓存**
   ```bash
   rm -rf ~/.m2/repository/org/mockito/
   rm -rf target/
   ```

2. **检查 pom.xml 语法**
   - 确保 XML 格式正确
   - 检查是否有未闭合的标签

3. **查看详细日志**
   - 在 IDE 的 Maven 输出中查看详细错误信息
   - 检查是否有网络或权限问题

