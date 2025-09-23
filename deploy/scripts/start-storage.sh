#!/bin/bash

cd ..

export SPRING_PROFILES_ACTIVE=postgres,redis
docker-compose -f docker-compose.yml -f docker-compose.redis.yml -f docker-compose.postgres.yml up -d --build

echo "Application started with PostgreSQL database (storage profile) and Redis cache"