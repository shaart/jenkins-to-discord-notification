FROM adoptopenjdk:11-jre-hotspot
VOLUME /tmp
COPY build/libs/jenkins-to-discord-notification-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]