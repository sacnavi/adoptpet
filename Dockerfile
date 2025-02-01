## use a java 17 image
FROM openjdk:17-oracle

LABEL maintainer="Ivan Castillo <sacnavi@outlook.com>"

## set app db credentials (default to cloud database on Atlas)
ENV APP_DB_URL=mongodb://localhost:27017/db

## define container port
EXPOSE 8079
## copy runnable jar and run it
COPY target/adoptpet.jar adoptpet.jar
CMD ["java", "-jar", "/adoptpet.jar"]
