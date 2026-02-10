#!/bin/bash

echo "🛑 停止旧进程..."
lsof -ti:8081 | xargs kill -9 2>/dev/null || echo "   没有运行中的进程"

echo ""
echo "🚀 启动 Dashboard..."
echo "   端口: 8081"
echo "   访问: http://localhost:8081"
echo ""

cd "$(dirname "$0")"
mvn spring-boot:run
