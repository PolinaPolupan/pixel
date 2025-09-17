#!/bin/bash
set -e

cd "$(dirname "$0")/../../node"

set -a
source .env
set +a

source .venv/bin/activate

rm -rf dist/ build/ *.egg-info
python3 -m build

export TWINE_PASSWORD="$TWINE_PASSWORD"
twine upload --repository-url https://upload.pypi.org/legacy/ dist/*
