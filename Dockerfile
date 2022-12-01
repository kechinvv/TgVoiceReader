FROM gradle:7.6-jdk8 as builder
USER root
WORKDIR /builder
ADD . /builder
RUN ["gradle", "clean","MyFatJar"]

FROM openjdk:8-jre-alpine
WORKDIR /bot
COPY --from=builder /builder/build/libs/TgVoiceReader-1.0.jar .
ENTRYPOINT ["java", "-jar", "TgVoiceReader-1.0.jar"]
