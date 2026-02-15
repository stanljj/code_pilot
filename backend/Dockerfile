# CodePilot Mini 后端 - 多阶段构建
# 构建阶段：使用 openjdk:17-jdk（含 JDK + apt）
FROM openjdk:17-jdk AS builder
WORKDIR /build

COPY pom.xml .
# 安装 curl（Debian 系统）
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# 下载并安装 Maven
RUN curl -sL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.tar.gz | tar xz -C /usr/share && \
    ln -s /usr/share/apache-maven-3.9.6/bin/mvn /usr/bin/mvn

RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn package -DskipTests -B

# 运行阶段：使用 openjdk:17-jre-slim（轻量、安全）
FROM openjdk:17-jre-slim
WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]