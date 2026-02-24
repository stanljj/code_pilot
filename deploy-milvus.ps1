# CodePilot Mini - Milvus 向量库一键部署脚本 (PowerShell版)
# 用于在 Windows 服务器上快速部署 Milvus 向量数据库服务

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "CodePilot Mini - Milvus 向量库一键部署脚本" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

# 检查 Docker 是否安装
try {
    $dockerVersion = docker --version 2>$null
    if (-not $dockerVersion) {
        Write-Host "错误: 未找到 Docker，请先安装 Docker" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ Docker 已安装: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到 Docker，请先安装 Docker" -ForegroundColor Red
    exit 1
}

# 检查 Docker Compose 是否安装
try {
    $composeVersion = docker compose version 2>$null
    if (-not $composeVersion) {
        Write-Host "错误: 未找到 Docker Compose，请先安装 Docker Compose" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ Docker Compose 已安装: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到 Docker Compose，请先安装 Docker Compose" -ForegroundColor Red
    exit 1
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 检测网络状况并选择合适的镜像源
Write-Host "检测网络连接..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri "https://registry-1.docker.io" -TimeoutSec 10 -UseBasicParsing
    Write-Host "网络连接正常，使用默认镜像源" -ForegroundColor Green
    $composeFile = "docker-compose.milvus.yml"
} catch {
    Write-Host "检测到网络连接问题，优先使用修复网络连接问题的配置" -ForegroundColor Yellow
    $composeFile = "docker-compose.milvus-network-fixed.yml"
    
    # 检查修复网络连接问题的配置文件是否存在
    if (-not (Test-Path "$scriptDir\$composeFile")) {
        Write-Host "警告: 未找到修复网络连接问题的配置文件 $composeFile" -ForegroundColor Yellow
        # 尝试使用修复版的完全本地镜像配置
        $composeFile = "docker-compose.milvus-fixed.yml"
        if (-not (Test-Path "$scriptDir\$composeFile")) {
            # 尝试使用完全本地镜像配置
            $composeFile = "docker-compose.milvus-all-local.yml"
            if (-not (Test-Path "$scriptDir\$composeFile")) {
                # 尝试使用本地已有镜像配置
                $composeFile = "docker-compose.milvus-local.yml"
                if (-not (Test-Path "$scriptDir\$composeFile")) {
                    # 尝试使用现有镜像配置
                    $composeFile = "docker-compose.milvus-existing.yml"
                    if (-not (Test-Path "$scriptDir\$composeFile")) {
                        # 尝试使用已验证的DaoCloud镜像
                        $composeFile = "docker-compose.milvus-dao.yml"
                        if (-not (Test-Path "$scriptDir\$composeFile")) {
                            # 尝试使用直接镜像作为备选
                            $composeFile = "docker-compose.milvus-direct.yml"
                            if (-not (Test-Path "$scriptDir\$composeFile")) {
                                # 尝试使用中科大镜像作为备选
                                $composeFile = "docker-compose.milvus-ustc.yml"
                                if (-not (Test-Path "$scriptDir\$composeFile")) {
                                    # 尝试使用中国区镜像作为备选
                                    $composeFile = "docker-compose.milvus-cn.yml"
                                    if (-not (Test-Path "$scriptDir\$composeFile")) {
                                        Write-Host "警告: 未找到任何可用的镜像配置文件，将尝试使用默认配置" -ForegroundColor Yellow
                                        $composeFile = "docker-compose.milvus.yml"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

Write-Host "`n正在启动 Milvus 向量数据库服务..." -ForegroundColor Yellow
Write-Host ""

# 启动 Milvus 服务
Set-Location $scriptDir
docker compose -f $composeFile up -d

Write-Host ""
Write-Host "等待 Milvus 服务启动..." -ForegroundColor Yellow

# 等待服务启动
Start-Sleep -Seconds 10

# 检查服务状态
Write-Host ""
Write-Host "检查服务状态:" -ForegroundColor Yellow
docker compose -f docker-compose.milvus.yml ps

Write-Host ""
Write-Host "===========================================" -ForegroundColor Green
Write-Host "Milvus 向量数据库部署完成!" -ForegroundColor Green
Write-Host ""
Write-Host "服务信息:" -ForegroundColor Green
Write-Host "- Milvus gRPC 服务: localhost:19530" -ForegroundColor Green
Write-Host "- Milvus REST API: localhost:9091" -ForegroundColor Green
Write-Host "- Milvus Metrics: localhost:19121" -ForegroundColor Green
Write-Host "- MinIO 控制台: http://localhost:9001 (用户名: minioadmin, 密码: minioadmin)" -ForegroundColor Green
Write-Host ""
Write-Host "如需停止服务，请运行: docker compose -f docker-compose.milvus.yml down" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green

# 提示用户验证连接
Write-Host ""
$confirm = Read-Host "是否要验证 Milvus 连接? (y/n)"
if ($confirm -eq 'y' -or $confirm -eq 'Y') {
    Write-Host "正在验证 Milvus 连接..." -ForegroundColor Yellow
    
    # 检查端口是否开放
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect("localhost", 19530)
        $tcpClient.Close()
        Write-Host "✓ Milvus gRPC 服务连接正常" -ForegroundColor Green
    } catch {
        Write-Host "✗ Milvus gRPC 服务连接失败" -ForegroundColor Red
    }
    
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect("localhost", 9091)
        $tcpClient.Close()
        Write-Host "✓ Milvus REST API 服务连接正常" -ForegroundColor Green
    } catch {
        Write-Host "✗ Milvus REST API 服务连接失败" -ForegroundColor Red
    }
}