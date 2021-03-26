FROM openjdk:11
ARG JAR_FILE=target/*.jar

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost/sitemonitor
ENV SPRING_DATASOURCE_USERNAME=sm
ENV SPRING_DATASOURCE_PASSWORD=sm
ENV TBOT_TOKEN=fill-value
ENV JAVA_OPTS="-XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=2 -Xms128m -Xmx128m"
ENV SERVER_PORT=16101

COPY ${JAR_FILE} sitemonitor-server.jar

EXPOSE 16101/tcp

CMD ["java", "-Xms128m", "-Xmx128m", "-jar", "sitemonitor-server.jar"]
