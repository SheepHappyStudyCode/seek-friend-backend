# Docker 镜像构建
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>
FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests
#copy target/seek-friend-backend-0.0.1-SNAPSHOT.jar ./target/

# Run the web service on container startup.
CMD ["java","-jar","/app/target/seek-friend-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]

## 后端
#docker build -t seek-friend-backend:v0.0.1 .
#
## 后端
#docker run -p 8080:8080 seek-friend-backend:v0.0.1




