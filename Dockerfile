#
# Build
#
FROM maven:3.9.16-amazoncorretto-25-alpine@sha256:a80b0474d68f8ebc05f6bb09e738102d2961e2d47b30b661b099d08968a40f5a AS buildtime

WORKDIR /build
COPY . .

RUN mvn clean package -DskipTests

#
# Docker RUNTIME
#
FROM amazoncorretto:25-alpine3.22@sha256:7d93179da1c00e18ac3760c90f4ad1b7fc053cf358a6db29028fa5bb022d3043 AS runtime

RUN apk --no-cache add shadow \
&& useradd --uid 10000 runner

VOLUME /tmp
WORKDIR /app

COPY --from=buildtime /build/target/*.jar /app/app.jar
# The agent is enabled at runtime via JAVA_TOOL_OPTIONS.
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.7.7/applicationinsights-agent-3.7.7.jar /app/applicationinsights-agent.jar

RUN chown -R runner:runner /app

USER 10000

ENTRYPOINT ["java","-jar","/app/app.jar"]
