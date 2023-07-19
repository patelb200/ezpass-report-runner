FROM ghcr.io/graalvm/graalvm-ce:ol9-java17-22.3.2

ENV MAVEN_HOME /usr/share/maven

COPY --from=maven ${MAVEN_HOME} ${MAVEN_HOME}

RUN ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn

ARG MAVEN_VERSION=3.9.2
ARG USER_HOME_DIR="/root"
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"