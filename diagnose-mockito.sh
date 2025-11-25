#!/bin/bash

# Mockito 运行时问题诊断脚本

echo "=========================================="
echo "Mockito 运行时问题诊断"
echo "=========================================="
echo ""

# 1. 检查 Maven 依赖
echo "1. 检查 Maven 依赖树中的 Mockito..."
mvn dependency:tree 2>/dev/null | grep -i mockito || echo "❌ 未找到 Mockito 依赖"
echo ""

# 2. 检查本地仓库
echo "2. 检查本地 Maven 仓库..."
M2_REPO="${HOME}/.m2/repository"
MOCKITO_CORE="${M2_REPO}/org/mockito/mockito-core/5.5.0"
MOCKITO_JUNIT="${M2_REPO}/org/mockito/mockito-junit-jupiter/5.5.0"

if [ -f "${MOCKITO_CORE}/mockito-core-5.5.0.jar" ]; then
    echo "✅ mockito-core-5.5.0.jar 存在"
    jar -tf "${MOCKITO_CORE}/mockito-core-5.5.0.jar" | grep -E "MockitoExtension|Mockito" | head -3
else
    echo "❌ mockito-core-5.5.0.jar 不存在"
fi

if [ -f "${MOCKITO_JUNIT}/mockito-junit-jupiter-5.5.0.jar" ]; then
    echo "✅ mockito-junit-jupiter-5.5.0.jar 存在"
else
    echo "❌ mockito-junit-jupiter-5.5.0.jar 不存在"
fi
echo ""

# 3. 检查编译后的测试类
echo "3. 检查编译后的测试类..."
if [ -d "target/test-classes" ]; then
    echo "✅ target/test-classes 目录存在"
    find target/test-classes -name "*Test.class" | head -5
else
    echo "❌ target/test-classes 目录不存在"
    echo "   运行: mvn test-compile"
fi
echo ""

# 4. 检查测试类路径
echo "4. 检查测试类路径..."
TEST_CLASSPATH=$(mvn dependency:build-classpath -DincludeScope=test -q -Dmdep.outputFile=/dev/stdout 2>/dev/null)
if [ -n "$TEST_CLASSPATH" ]; then
    echo "✅ 测试类路径已生成"
    echo "$TEST_CLASSPATH" | tr ':' '\n' | grep -i mockito | head -3
else
    echo "❌ 无法生成测试类路径"
fi
echo ""

# 5. 尝试编译测试
echo "5. 编译测试代码..."
mvn test-compile 2>&1 | tail -10
echo ""

# 6. 检查 pom.xml 配置
echo "6. 检查 pom.xml 中的 Mockito 配置..."
if grep -q "mockito-core" pom.xml && grep -q "mockito-junit-jupiter" pom.xml; then
    echo "✅ pom.xml 中包含 Mockito 依赖"
    grep -A 3 "mockito-core" pom.xml | head -6
else
    echo "❌ pom.xml 中缺少 Mockito 依赖"
fi
echo ""

# 7. 检查 surefire 插件配置
echo "7. 检查 maven-surefire-plugin 配置..."
if grep -q "skipTests.*true" pom.xml; then
    echo "⚠️  警告: pom.xml 中配置了 skipTests=true"
    echo "   这会导致测试被跳过，即使运行 mvn test"
    echo "   建议移除或设置为 false"
else
    echo "✅ surefire 插件配置正常"
fi
echo ""

# 8. 尝试运行一个简单的测试
echo "8. 尝试运行测试（如果配置允许）..."
mvn test -Dtest=LlmAnalysisServiceTest#testGetAvailableProviders 2>&1 | tail -20
echo ""

# 9. IDE 相关建议
echo "=========================================="
echo "IDE 相关建议"
echo "=========================================="
echo ""
echo "如果是在 IDE 中运行测试时找不到 Mockito："
echo ""
echo "IntelliJ IDEA:"
echo "  1. File -> Invalidate Caches / Restart"
echo "  2. 右键 pom.xml -> Maven -> Reload Project"
echo "  3. File -> Project Structure -> Modules -> 检查依赖"
echo "  4. Settings -> Build -> Build Tools -> Maven -> 检查 Maven 路径"
echo ""
echo "Eclipse:"
echo "  1. 右键项目 -> Maven -> Update Project"
echo "  2. Project -> Clean"
echo "  3. 检查 .classpath 文件中的依赖"
echo ""
echo "VS Code / Cursor:"
echo "  1. 重新加载窗口: Cmd+Shift+P -> Reload Window"
echo "  2. 检查 Java 扩展是否正确识别 Maven 项目"
echo "  3. 运行: mvn clean install"
echo ""

