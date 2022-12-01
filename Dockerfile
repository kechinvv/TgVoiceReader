FROM gradle:7.6-jdk8 as builder
WORKDIR /builder
ADD . /builder
CMD ["gradlew", "shadowJar"]

FROM openjdk:8-jre-alpine
WORKDIR /bot
COPY --from=builder /builder/build/libs/TgVoiceReader-1.0-all.jar .
ENTRYPOINT ["java", "-jar", "TgVoiceReader-1.0-all.jar"]
