#!/bin/bash
cd ..
docker-compose -f docker-compose.yml -f docker-compose.storage.yml --profile storage up -d