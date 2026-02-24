#!/bin/bash

# 测试Docker镜像加速器连接
# 用于验证不同镜像源的可用性

echo "==========================================="
echo "Docker镜像加速器连接测试"
echo "==========================================="

# 测试的镜像列表
MIRRORS=(
    "https://docker.m.daocloud.io"
    "https://dockerproxy.com"
    "https://docker.mirrors.ustc.edu.cn"
    "https://registry-1.docker.io"
)

IMAGES=(
    "docker.m.daocloud.io/quay.io/coreos/etcd:v3.5.5"
    "docker.m.daocloud.io/minio/minio:RELEASE.2023-09-04T19-57-37Z"
    "docker.m.daocloud.io/milvusdb/milvus-standalone:v2.4.4"
)

echo "测试镜像加速器连接..."
for mirror in "${MIRRORS[@]}"; do
    echo -n "测试 $mirror ... "
    if curl -s --connect-timeout 5 "$mirror" > /dev/null 2>&1; then
        echo "✓ 可访问"
    else
        echo "✗ 无法访问"
    fi
done

echo ""
echo "测试镜像拉取..."
for image in "${IMAGES[@]}"; do
    echo "测试拉取镜像: $image"
    if docker pull "$image" --quiet; then
        echo "✓ 拉取成功"
        # 删除测试镜像以节省空间
        docker rmi "$image" > /dev/null 2>&1
    else
        echo "✗ 拉取失败"
    fi
    echo ""
done

echo "测试完成!"