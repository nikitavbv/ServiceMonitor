FROM gradle:jdk10-slim

LABEL maintainer="nikitavbv@gmail.com"

RUN mkdir /home/gradle/app
WORKDIR /home/gradle/app

ADD src /home/gradle/app/src
ADD build.gradle /home/gradle/app/build.gradle
ADD src/main/resources/application.properties.docker /home/gradle/app/src/main/resources/application.properties

EXPOSE 80

RUN gradle assemble

CMD ["java", "-Xmx64M", "-Xms64M", "-jar", "/home/gradle/app/build/libs/app-0.0.1-SNAPSHOT.jar"]
