#!/bin/bash

echo "🔨 重新构建前端..."

cd "$(dirname "$0")/src/main/frontend"

echo "📦 安装依赖..."
npm install

echo "🏗️  构建前端..."
npm run build

echo "✅ 前端构建完成！"
echo ""
echo "现在重启Dashboard应用："
echo "  cd .."
echo "  ./start.sh"
