#!/bin/bash

cd ..

docker-compose -f docker-compose.yml -f docker-compose.storage.yml up -d

echo "Application started with PostgreSQL database (storage profile) and Redis cache"