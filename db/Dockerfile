## Using specific Mongo version
FROM mongo:8.0.1

LABEL maintainer="Ivan Castillo <sacnavi@outlook.com>"

## set app environment vars
## application default credentials
ENV APP_DB=adoptpetdb
ENV APP_USER=adoptpetusr
ENV APP_PASS=adoptpetpass
ENV MONGO_INITDB_DATABASE=$APP_DB

## Expose mongo default port
EXPOSE 27017

## add scripts for application user creation and cities collection loading
ADD createUser.sh /docker-entrypoint-initdb.d/
ADD loadCities.js /docker-entrypoint-initdb.d/

## add secrets files
ADD user.txt /run/secrets/
ADD pass.txt /run/secrets/

## run server binding all ips
CMD ["mongod", "--bind_ip_all"]