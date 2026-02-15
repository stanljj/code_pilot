# CodePilot Mini 后端 - 多阶段构建
# 构建阶段：使用官方 maven 镜像（含 JDK + Maven + apt）
FROM maven:3.9.6-openjdk-17 AS builder
WORKDIR /build

# 复制 pom 并下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# 复制源码并打包
COPY src ./src
RUN mvn package -DskipTests -B

# 运行阶段：使用轻量 JRE
FROM openjdk:17-jre-slim
WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]