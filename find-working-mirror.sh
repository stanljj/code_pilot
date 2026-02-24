#!/bin/bash

# 测试不同镜像源的可用性
# 帮助找到在中国大陆可用的Docker镜像源

echo "==========================================="
echo "测试Docker镜像源可用性"
echo "==========================================="

# 测试的镜像源列表
MIRRORS=(
    "dockerproxy.com"
    "hub-mirror.c.163.com"
    "mirror.ccs.tencentyun.com"
    "registry.docker-cn.com"
    "docker.m.daocloud.io"
)

# 要测试的镜像
TEST_IMAGE="library/hello-world:latest"

echo "测试网络连接..."
for mirror in "${MIRRORS[@]}"; do
    echo -n "测试镜像源: $mirror ... "
    
    # 构建完整的镜像地址
    FULL_IMAGE="$mirror/$TEST_IMAGE"
    
    # 尝试拉取镜像
    if timeout 30 docker pull "$FULL_IMAGE" > /dev/null 2>&1; then
        echo "✓ 可用"
        echo "成功拉取镜像: $FULL_IMAGE"
        # 删除测试镜像
        docker rmi "$FULL_IMAGE" > /dev/null 2>&1
        # 记录可用的镜像源
        WORKING_MIRROR="$mirror"
        break
    else
        echo "✗ 不可用"
    fi
done

echo ""
if [ -n "$WORKING_MIRROR" ]; then
    echo "找到可用的镜像源: $WORKING_MIRROR"
    echo ""
    echo "建议的完整镜像地址:"
    echo "  etcd: $WORKING_MIRROR/quay.io/coreos/etcd:v3.5.5"
    echo "  minio: $WORKING_MIRROR/minio/minio:RELEASE.2023-09-04T19-57-37Z"
    echo "  milvus: $WORKING_MIRROR/milvusdb/milvus-standalone:v2.4.4"
else
    echo "未找到可用的镜像源"
    echo "建议尝试以下方法:"
    echo "1. 检查网络连接"
    echo "2. 手动配置Docker代理"
    echo "3. 联系网络管理员"
fi