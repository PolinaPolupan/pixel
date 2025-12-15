#!/bin/bash
cd ..

docker-compose -f docker-compose.yml up -d

echo "Application started in development mode with H2 database and Spring cache"