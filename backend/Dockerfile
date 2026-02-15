# CodePilot Mini 后端 - 多阶段构建
# 构建阶段：使用 openjdk:17（含 JDK）
FROM openjdk:17 AS builder
WORKDIR /build

COPY pom.xml .
RUN apt-get update && apt-get install -y curl && \
    curl -sL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.tar.gz | tar xz -C /usr/share && \
    ln -s /usr/share/apache-maven-3.9.6/bin/mvn /usr/bin/mvn

RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn package -DskipTests -B

# 运行阶段：使用 openjdk:17-jre（轻量运行时）
FROM openjdk:17-jre
WORKDIR /app

ENV TZ=Asia/Shanghai
# Debian 系统设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]