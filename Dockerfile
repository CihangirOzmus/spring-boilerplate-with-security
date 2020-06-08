FROM adoptopenjdk/openjdk11 as build
WORKDIR /home/security-server
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
RUN ./gradlew dependencies
COPY src src
RUN ./gradlew build -x test
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

FROM openjdk:11-jre-slim
MAINTAINER 'Cihangir Ozmus "cihangir@thinkerfox.com"'
LABEL description="Image for spring-boilerplate-with-security java11"
VOLUME /tmp
ARG DEPENDENCY=/home/security-server/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /home/security-server/lib
COPY --from=build ${DEPENDENCY}/META-INF /home/security-server/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /home/security-server
RUN mkdir -p home/uploads
EXPOSE 8080
ENTRYPOINT ["java","-cp","home/security-server:home/security-server/lib/*","com.cigi.iems.IemsApplication"]