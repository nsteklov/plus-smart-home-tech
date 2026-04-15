FROM amazoncorretto:21
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
ENV LATER_IMAGE_DIRECTORY="images"