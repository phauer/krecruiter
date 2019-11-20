#!/bin/bash

# call up front: `docker login` and insert credentials
cd local-dev/address-validation-service || exit
TAG="phauer/address-validation-service-stub:latest"
docker build --tag "$TAG" .
docker push "$TAG"