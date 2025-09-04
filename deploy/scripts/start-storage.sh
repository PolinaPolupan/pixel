#!/bin/bash

cd ..

export SPRING_PROFILES_ACTIVE=db,cache
docker-compose -f docker-compose.yml -f docker-compose.cache.yml -f docker-compose.db.yml up -d

echo "Application started with PostgreSQL database (storage profile) and Redis cache"