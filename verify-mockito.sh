#!/bin/bash

# Mockito 依赖验证脚本

echo "=========================================="
echo "Mockito 依赖验证脚本"
echo "=========================================="
echo ""

# 检查 Maven 是否安装
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未安装或不在 PATH 中"
    echo "请先安装 Maven 或将其添加到 PATH"
    exit 1
fi

echo "✅ Maven 已安装"
echo ""

# 检查 pom.xml 是否存在
if [ ! -f "pom.xml" ]; then
    echo "❌ pom.xml 文件不存在"
    exit 1
fi

echo "✅ pom.xml 文件存在"
echo ""

# 清理并下载依赖
echo "正在清理项目..."
mvn clean > /dev/null 2>&1

echo "正在下载依赖..."
mvn dependency:resolve -q

echo ""
echo "=========================================="
echo "检查 Mockito 依赖"
echo "=========================================="
echo ""

# 检查依赖树中的 Mockito
echo "查找 Mockito 相关依赖..."
mvn dependency:tree 2>/dev/null | grep -i mockito || echo "⚠️  未在依赖树中找到 Mockito"

echo ""
echo "=========================================="
echo "检查本地 Maven 仓库"
echo "=========================================="
echo ""

M2_REPO="${HOME}/.m2/repository"
MOCKITO_CORE="${M2_REPO}/org/mockito/mockito-core"
MOCKITO_JUNIT="${M2_REPO}/org/mockito/mockito-junit-jupiter"

if [ -d "$MOCKITO_CORE" ]; then
    echo "✅ mockito-core 存在于本地仓库:"
    ls -la "$MOCKITO_CORE" | head -5
else
    echo "❌ mockito-core 不存在于本地仓库"
fi

echo ""

if [ -d "$MOCKITO_JUNIT" ]; then
    echo "✅ mockito-junit-jupiter 存在于本地仓库:"
    ls -la "$MOCKITO_JUNIT" | head -5
else
    echo "❌ mockito-junit-jupiter 不存在于本地仓库"
fi

echo ""
echo "=========================================="
echo "检查编译后的类文件"
echo "=========================================="
echo ""

if [ -d "target/test-classes" ]; then
    echo "✅ target/test-classes 目录存在"
    echo "查找 Mockito 相关类文件..."
    find target/test-classes -name "*Mockito*" -o -name "*mockito*" 2>/dev/null | head -5 || echo "⚠️  未找到 Mockito 相关类文件"
else
    echo "⚠️  target/test-classes 目录不存在，请先编译项目"
fi

echo ""
echo "=========================================="
echo "建议的下一步操作"
echo "=========================================="
echo ""
echo "1. 如果依赖未下载，运行: mvn clean install -U"
echo "2. 如果 IDE 中看不到，尝试:"
echo "   - IntelliJ IDEA: File -> Invalidate Caches / Restart"
echo "   - 右键 pom.xml -> Maven -> Reload Project"
echo "3. 检查 IDE 的 Maven 设置，确保使用正确的 Maven 版本"
echo "4. 查看完整依赖树: mvn dependency:tree > deps.txt"
echo ""

