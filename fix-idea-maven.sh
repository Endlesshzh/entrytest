#!/bin/bash
# 修复 IntelliJ IDEA Maven 项目配置脚本

echo "正在修复 IntelliJ IDEA Maven 项目配置..."

# 1. 确保使用 Java 17
export JAVA_HOME=/Users/zhenhuan.hou/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home
echo "✓ 设置 JAVA_HOME 为 Java 17"

# 2. 清理并重新下载依赖
echo "正在清理并重新下载 Maven 依赖..."
mvn clean dependency:resolve -U

# 3. 验证 Mockito 依赖
echo ""
echo "验证 Mockito 依赖："
mvn dependency:tree | grep -i mockito

echo ""
echo "========================================="
echo "请在 IntelliJ IDEA 中执行以下操作："
echo "========================================="
echo "1. 右键点击 pom.xml → Maven → Reload Project"
echo "2. File → Invalidate Caches... → Invalidate and Restart"
echo "3. File → Project Structure → Project → 确保 SDK 设置为 Java 17"
echo "4. File → Settings → Build, Execution, Deployment → Compiler → Java Compiler"
echo "   确保 Project bytecode version 设置为 17"
echo "5. 重新构建项目：Build → Rebuild Project"
echo "========================================="

