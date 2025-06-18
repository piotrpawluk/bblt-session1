#!/bin/bash

# Run the Spring Boot web application without CLI mode
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8911

echo "Starting AWS Bedrock Spring AI Demo Web Application on port 8911..."
mvn spring-boot:run -Dspring.profiles.active=prod -Dserver.port=8911