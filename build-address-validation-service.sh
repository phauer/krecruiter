#!/bin/bash

# call up front: `docker login` and insert credentials
TAG="phauer/address-validation-service-stub:latest"
docker build --tag "$TAG" local-dev/address-validation-service
docker push "$TAG"