FROM maven:3-amazoncorretto-17 as builder
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM openjdk:17
WORKDIR /app
EXPOSE 8080
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT java -jar app.jar

ENV ACCESS_KEY=${ACCESS_KEY}
ENV SECRET=${SECRET}
ENV BUCKET_NAME=${BUCKET_NAME}
ENV SESSION_TOKEN=${SESSION_TOKEN}
ENV REGION=${REGION}