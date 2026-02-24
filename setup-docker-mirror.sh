#!/bin/bash

# 配置Docker镜像加速器
# 解决中国大陆网络访问Docker Hub的问题

echo "==========================================="
echo "配置Docker镜像加速器"
echo "==========================================="

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo "请以root用户运行此脚本"
    echo "使用: sudo $0"
    exit 1
fi

# 创建Docker配置目录
mkdir -p /etc/docker

# 备份现有配置（如果存在）
if [ -f /etc/docker/daemon.json ]; then
    echo "备份现有配置到 /etc/docker/daemon.json.backup"
    cp /etc/docker/daemon.json /etc/docker/daemon.json.backup
fi

# 创建新的配置文件
cat > /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.com",
    "https://hub-mirror.c.163.com",
    "https://mirror.ccs.tencentyun.com"
  ],
  "insecure-registries": [],
  "debug": false,
  "experimental": false
}
EOF

echo "配置文件已创建: /etc/docker/daemon.json"

# 重启Docker服务
echo "正在重启Docker服务..."
systemctl restart docker

if [ $? -eq 0 ]; then
    echo "✓ Docker服务重启成功"
    echo ""
    echo "配置的镜像加速器："
    echo "- 中科大镜像: https://docker.mirrors.ustc.edu.cn"
    echo "- DockerProxy: https://dockerproxy.com"
    echo "- DaoCloud: https://docker.m.daocloud.io"
    echo "- 网易云: https://hub-mirror.c.163.com"
    echo ""
    echo "现在您可以尝试重新部署Milvus服务："
    echo "./deploy-milvus.sh"
else
    echo "✗ Docker服务重启失败，请手动检查"
    echo "您可以手动运行: systemctl restart docker"
fi