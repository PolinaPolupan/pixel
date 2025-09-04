#!/bin/bash
cd ..

export SPRING_PROFILES_ACTIVE=db,cache,monitoring
docker-compose -f docker-compose.yml -f docker-compose.db.yml \
  -f docker-compose.monitoring.yml -f docker-compose.cache.yml up -d