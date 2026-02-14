# CodePilot Mini 后端 - 多阶段构建，最终镜像仅含 JAR + JRE
# 构建阶段：Maven 打包
FROM openjdk:17-jdk-alpine AS builder
WORKDIR /build

# 先只复制 pom，利用 Docker 缓存
COPY pom.xml .
RUN apk add --no-cache curl && \
    curl -sL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.tar.gz | tar xz -C /usr/share && \
    ln -s /usr/share/apache-maven-3.9.6/bin/mvn /usr/bin/mvn

RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn package -DskipTests -B

# 运行阶段：仅 JRE + 应用 JAR
FROM openjdk:17-jre-alpine
WORKDIR /app

# 时区（可选）
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]