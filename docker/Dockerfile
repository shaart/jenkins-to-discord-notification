FROM adoptopenjdk:11-jre-hotspot
VOLUME /tmp
COPY build/libs/jenkins-to-discord-notification-*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
