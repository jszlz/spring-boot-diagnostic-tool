#!/bin/bash

echo "正在启动诊断监控仪表板..."
echo "端口: 8081"
echo "访问地址: http://localhost:8081"
echo ""

cd "$(dirname "$0")"
mvn spring-boot:run
