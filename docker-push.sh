#1/usr/bin/env bash

#####
# push image
#####

docker tag pkiraly/mqaf-ws:latest pkiraly/mqaf-ws:latest
docker login
docker push pkiraly/mqaf-ws:latest
