#!/usr/bin/env bash

#### - - - - - - - - - - -
# run Aqinda validation
# with building image locally
#- - - - - - - - - - - - -

WORKING_DIR=test-aqinda

echo "remove container and image"
docker stop $(docker ps -a -q)
docker rm mqaf-ws
docker rmi $(docker images pkiraly/mqaf-ws -q)

echo "build image"
docker compose -f docker-compose.yml build app

echo "run container"
if [[ ! -d ${WORKING_DIR}/output ]]; then
  mkdir ${WORKING_DIR}/output
fi

docker run -d -p 8080:8080 \
  -v ./${WORKING_DIR}/input:/opt/metadata-qa/input \
  -v ./${WORKING_DIR}/output:/opt/metadata-qa/output \
  --name mqaf-ws \
  pkiraly/mqaf-ws

sleep 3

echo "call the API"
curl -X POST \
     -w '\n' \
     -F "schemaFile=schema.yaml" \
     -F 'schemaFormat=yaml' \
     -F "measurementsFile=measurements.json" \
     -F 'measurementsFormat=json' \
     -F "inputFile=LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml" \
     -F "recordAddress=lido:lido" \
     -F "inputFormat=xml" \
     -F 'gzip=false' \
     -F 'outputFormat=CSV' \
     -F "output=output.csv" \
     http://localhost:8080/mqaf-ws/validate
