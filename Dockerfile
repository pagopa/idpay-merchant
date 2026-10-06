#
# Build
#
FROM maven:3.9.16-amazoncorretto-25-alpine@sha256:a80b0474d68f8ebc05f6bb09e738102d2961e2d47b30b661b099d08968a40f5a AS buildtime

WORKDIR /build
COPY . .

RUN mvn clean package -DskipTests -Dtomcat.version=11.0.22

#
# Docker RUNTIME
#
FROM amazoncorretto:25-alpine3.24@sha256:2ad5f5cf03a3970f2478b130dc28f51b179ce13c58154fe3ec1a6fdeb3b86e3a AS runtime

RUN apk --no-cache upgrade \
&& apk --no-cache add shadow \
&& useradd --uid 10000 runner

VOLUME /tmp
WORKDIR /app

COPY --from=buildtime /build/target/*.jar /app/app.jar
# The agent is enabled at runtime via JAVA_TOOL_OPTIONS.
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.7.9/applicationinsights-agent-3.7.9.jar /app/applicationinsights-agent.jar

RUN chown -R runner:runner /app

USER 10000

ENTRYPOINT ["java","-jar","/app/app.jar"]
