#!/bin/bash
# 修复 IntelliJ IDEA 模块配置脚本

echo "========================================="
echo "修复 IntelliJ IDEA Maven 模块配置"
echo "========================================="
echo ""

# 检查 entrytest.iml 文件
if [ -f "entrytest.iml" ]; then
    echo "发现 entrytest.iml 文件，配置可能不正确"
    echo ""
    echo "当前配置内容："
    cat entrytest.iml
    echo ""
    echo "========================================="
    echo "请按照以下步骤操作："
    echo "========================================="
    echo ""
    echo "方法 1: 删除并重新导入（推荐）"
    echo "1. 关闭 IntelliJ IDEA"
    echo "2. 执行以下命令删除错误的模块配置："
    echo "   rm entrytest.iml"
    echo "3. 重新打开项目，IDEA 会自动重新生成正确的配置"
    echo ""
    echo "方法 2: 在 IDEA 中重新导入"
    echo "1. File → Project Structure → Modules"
    echo "2. 删除 entrytest 模块（点击 - 按钮）"
    echo "3. File → New → Project from Existing Sources..."
    echo "4. 选择项目目录，选择 Maven 导入"
    echo ""
    echo "方法 3: 刷新 Maven 项目"
    echo "1. 右键 pom.xml → Maven → Reload Project"
    echo "2. File → Invalidate Caches... → Invalidate and Restart"
    echo ""
    echo "========================================="
    echo ""
    
    read -p "是否现在删除 entrytest.iml 文件？(y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "备份并删除 entrytest.iml..."
        cp entrytest.iml entrytest.iml.backup
        rm entrytest.iml
        echo "✓ 已删除 entrytest.iml（备份为 entrytest.iml.backup）"
        echo ""
        echo "现在请："
        echo "1. 关闭 IntelliJ IDEA"
        echo "2. 重新打开项目"
        echo "3. IDEA 会自动重新生成正确的 Maven 模块配置"
    else
        echo "已取消。请手动按照上述步骤操作。"
    fi
else
    echo "未找到 entrytest.iml 文件，配置可能正常"
fi

echo ""
echo "验证 Maven 依赖："
export JAVA_HOME=/Users/zhenhuan.hou/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home
mvn dependency:tree | grep -i mockito || echo "未找到 Mockito 依赖，请检查 pom.xml"

