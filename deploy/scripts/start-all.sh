#!/bin/bash
cd ..
docker-compose -f docker-compose.yml -f docker-compose.storage.yml \
  -f docker-compose.monitoring.yml --profile storage --profile monitoring up -d