#!/bin/bash

set -e

REPO_URL="https://github.com/RomanGhost/p2pStock.git"
REPO_DIR="p2pStock"
DOCKER_IMAGE="mydocker/spring-boot-app"
DOCKER_COMPOSE_FILE="compose.yaml"
NGINX_CONF_FILE="nginx.conf"

echo "Cloning repository..."
if [ -d "$REPO_DIR" ]; then
    cd "$REPO_DIR"
else
    git clone "$REPO_URL"
    cd "$REPO_DIR"
fi
echo "Checking out deploy branch..."
git checkout master --force
git pull
git checkout deploy
git merge master -m "merge branches"
git checkout --theirs README.md


echo "Building Docker image..."
docker build -t $DOCKER_IMAGE .

echo "Deploying Blue application..."
docker-compose -f $DOCKER_COMPOSE_FILE up -d postgres

# Wait for PostgreSQL container to be healthy
echo "Waiting for PostgreSQL to be ready..."
while ! docker exec -it $(docker ps -q -f name=postgres) pg_isready -U admin; do
    echo "PostgreSQL is not ready yet, waiting..."
    sleep 5
done
echo "PostgreSQL is ready."

docker-compose -f $DOCKER_COMPOSE_FILE up -d spring-boot-app-blue

# Restart nginx container if it exists, otherwise start nginx container
if [ $(docker ps -aq -f name=nginx | wc -l) -ne 0 ]; then
#    docker-compose -f $DOCKER_COMPOSE_FILE restart nginx
    echo "Nginx - healthy"
else
    docker-compose -f $DOCKER_COMPOSE_FILE up -d nginx
fi

# Wait for Blue application to be healthy
echo "Waiting for Blue application to be ready..."
while ! docker inspect --format '{{.State.Health.Status}}' $(docker ps -q -f name=spring-boot-app-blue) | grep -q "healthy"; do
    echo "Blue application is not ready yet, waiting..."
    sleep 5
done
echo "Blue application is ready."

echo "Deploying Green application..."
docker-compose -f $DOCKER_COMPOSE_FILE up -d spring-boot-app-green

# Wait for Green application to be healthy
echo "Waiting for Green application to be ready..."
while ! docker inspect --format '{{.State.Health.Status}}' $(docker ps -q -f name=spring-boot-app-green) | grep -q "healthy"; do
    echo "Green application is not ready yet, waiting..."
    sleep 5
done
echo "Green application is ready."

# Restart nginx container if it exists, otherwise start nginx container
if [ $(docker ps -aq -f name=nginx | wc -l) -ne 0 ]; then
#    docker-compose -f $DOCKER_COMPOSE_FILE restart nginx
    echo "nginx health"
else
    docker-compose -f $DOCKER_COMPOSE_FILE up -d nginx
fi

#echo "clear docker builds"
#cd ..
#sh clear-docker.sh

echo "Deployment completed successfully"
