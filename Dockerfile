# 使用官方的OpenJDK 8 JRE Alpine镜像作为基础镜像
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 将JAR文件复制到容器中
COPY seek-friend-backend-0.0.1-SNAPSHOT.jar app.jar

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=prod

# 指定启动命令
ENTRYPOINT ["java","-jar","app.jar"]