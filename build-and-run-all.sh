#!/bin/bash

# Build libraries
cd ./java
mvn clean install
cd ./../python
docker build -t filipklimes-diploma/python .
cd ../
docker build -f nodejs/Dockerfile -t filipklimes-diploma/nodejs .

# Build & Run example system
cd ./example
docker-compose down # cleanup
cd ./billing
mvn clean package dockerfile:build
cd ../shipping
mvn clean package dockerfile:build
cd ../order
mvn clean package dockerfile:build
cd ../ui
mvn clean package dockerfile:build
cd ../product
docker build -t filipklimes-diploma/example-product-service .
cd ../user
docker build -t filipklimes-diploma/example-user-service .
cd ../
docker-compose up -d