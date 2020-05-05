FROM adoptopenjdk/openjdk11

ENV WORKDIR /opt/motionms

COPY ./build/libs/*.jar ${WORKDIR}/motionms.jar

WORKDIR ${WORKDIR}

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "motionms.jar"]
