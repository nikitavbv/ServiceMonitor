FROM ubuntu:bionic

LABEL maintainer="nikitavbv@gmail.com"

RUN mkdir /app
WORKDIR /app
EXPOSE 80

RUN apt update \
    && apt-get install -y software-properties-common curl unzip zip
RUN add-apt-repository ppa:openjdk-r/ppa \
    && apt update \
    && apt install -y openjdk-8-jdk nodejs npm \
    && rm -rf /var/lib/apt/lists/*
RUN curl -s "https://get.sdkman.io" | bash \
    && bash -c "source \"/root/.sdkman/bin/sdkman-init.sh\" && sdk install gradle"

ADD src /app/src
ADD frontend/ /app/frontend
ADD build.gradle /app/build.gradle
ADD src/main/resources/application.properties.docker /app/src/main/resources/application.properties

RUN cd /app/frontend \
    && mkdir /app/src/main/resources/static \
    && npm install && npm link @angular/cli \
    && ng build --prod \
    && mv /app/frontend/dist/ServiceMonitorFrontend/* /app/src/main/resources/static\
    && cd .. && rm frontend -rf
RUN bash -c "source \"/root/.sdkman/bin/sdkman-init.sh\" && gradle assemble"

CMD ["java", "-Xmx64M", "-Xms64M", "-jar", "/home/gradle/app/build/libs/app-0.0.1-SNAPSHOT.jar"]
