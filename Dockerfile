# Сборка jar происходит внутри контейнера

# Стадия сборки
FROM gradle:8.7-jdk21 AS build

WORKDIR /home/gradle/project

# Копируем весь проект внутрь контейнера
COPY --chown=gradle:gradle . .

# Сборка Spring Boot JAR
RUN gradle bootJar --no-daemon

# Финальный контейнер
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Копируем собранный .jar из стадии build
COPY --from=build /home/gradle/project/build/libs/*.jar auth-micro.jar

# Запуск Spring Boot-приложения
ENTRYPOINT ["java", "-jar", "auth-micro.jar"]

EXPOSE 8080
