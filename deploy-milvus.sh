#!/bin/bash

# CodePilot Mini - Milvus 向量库一键部署脚本
# 用于在服务器上快速部署 Milvus 向量数据库服务

set -e  # 遇到错误立即退出

echo "==========================================="
echo "CodePilot Mini - Milvus 向量库一键部署脚本"
echo "==========================================="

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: 未找到 Docker，请先安装 Docker"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker compose &> /dev/null; then
    echo "错误: 未找到 Docker Compose，请先安装 Docker Compose"
    exit 1
fi

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 检测网络状况并选择合适的镜像源
echo "检测网络连接..."
if curl -s --connect-timeout 5 https://registry-1.docker.io > /dev/null 2>&1; then
    echo "网络连接正常，使用默认镜像源"
    COMPOSE_FILE="docker-compose.milvus.yml"
else
    echo "检测到网络连接问题，优先使用修复网络连接问题的配置"
    COMPOSE_FILE="docker-compose.milvus-network-fixed.yml"
    
    # 检查修复网络连接问题的配置文件是否存在
    if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
        echo "警告: 未找到修复网络连接问题的配置文件 ${COMPOSE_FILE}"
        # 尝试使用修复版的完全本地镜像配置
        COMPOSE_FILE="docker-compose.milvus-fixed.yml"
        if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
            # 尝试使用完全本地镜像配置
            COMPOSE_FILE="docker-compose.milvus-all-local.yml"
            if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
                # 尝试使用本地已有镜像配置
                COMPOSE_FILE="docker-compose.milvus-local.yml"
                if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
                    # 尝试使用现有镜像配置
                    COMPOSE_FILE="docker-compose.milvus-existing.yml"
                    if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
                        # 尝试使用已验证的DaoCloud镜像
                        COMPOSE_FILE="docker-compose.milvus-dao.yml"
                        if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
                            # 尝试使用直接镜像作为备选
                            COMPOSE_FILE="docker-compose.milvus-direct.yml"
                            if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
                                # 尝试使用中科大镜像作为备选
                                COMPOSE_FILE="docker-compose.milvus-ustc.yml"
                                if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
                                    # 尝试使用中国区镜像作为备选
                                    COMPOSE_FILE="docker-compose.milvus-cn.yml"
                                    if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
                                        echo "警告: 未找到任何可用的镜像配置文件，将尝试使用默认配置"
                                        COMPOSE_FILE="docker-compose.milvus.yml"
                                    fi
                                fi
                            fi
                        fi
                    fi
                fi
            fi
        fi
    fi
fi

echo "正在启动 Milvus 向量数据库服务..."
echo

# 启动 Milvus 服务
cd "$SCRIPT_DIR"
docker compose -f $COMPOSE_FILE up -d

echo
echo "等待 Milvus 服务启动..."

# 等待服务启动
sleep 10

# 检查服务状态
echo
echo "检查服务状态:"
docker compose -f $COMPOSE_FILE ps

echo
echo "==========================================="
echo "Milvus 向量数据库部署完成!"
echo
echo "服务信息:"
echo "- Milvus gRPC 服务: localhost:19530"
echo "- Milvus REST API: localhost:9091" 
echo "- Milvus Metrics: localhost:19121"
echo "- MinIO 控制台: http://localhost:9001 (用户名: minioadmin, 密码: minioadmin)"
echo
echo "如需停止服务，请运行: docker compose -f $COMPOSE_FILE down"
echo "==========================================="

# 提示用户验证连接
echo
read -p "是否要验证 Milvus 连接? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "正在验证 Milvus 连接..."
    # 简单检查端口是否开放
    if nc -z localhost 19530; then
        echo "✓ Milvus gRPC 服务连接正常"
    else
        echo "✗ Milvus gRPC 服务连接失败"
    fi
    
    if nc -z localhost 9091; then
        echo "✓ Milvus REST API 服务连接正常"
    else
        echo "✗ Milvus REST API 服务连接失败"
    fi
fi