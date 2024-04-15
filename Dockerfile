#FROM maven:3.8.2-openjdk-11-slim AS build
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#
#RUN mvn package -DskipTests
#CMD ["java", "-jar", "/app/youyuan-backend-0.0.1-SNAPSHOT.jar"]

# 构建阶段
FROM maven:3.8.2-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# 运行阶段
FROM openjdk:11-jre-slim
WORKDIR /app
# 从构建阶段复制生成的jar文件到运行阶段的镜像中
COPY --from=build /app/target/youyuan-backend-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-jar", "youyuan-backend-0.0.1-SNAPSHOT.jar"]