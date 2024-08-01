#1/usr/bin/env bash

#### - - - - - - - - - - -
# run Europeana validation
# with building image locally
#- - - - - - - - - - - - -


echo "remove container and image"
docker stop $(docker ps -a -q)
docker rm mqaf-ws
docker rmi $(docker images pkiraly/mqaf-ws -q)

echo "build image"
docker compose -f docker-compose.yml build app

echo "run container"
if [[ ! -d test-europeana/output ]]; then
  mkdir test-europeana/output
fi
docker run -d -p 8080:8080 \
  -v ./test-europeana:/opt/metadata-qa/input \
  -v ./test-europeana/output:/opt/metadata-qa/output \
  --name mqaf-ws pkiraly/mqaf-ws

sleep 3

echo "call the API"
curl -X POST \
     -F "schemaFile=schema.yaml" \
     -F 'schemaFormat=yaml' \
     -F "measurementsFile=measurements.json" \
     -F 'measurementsFormat=json' \
     -F "inputFile=combined_records.json" \
     -F "inputFormat=json-array" \
     -F 'gzip=false' \
     -F 'outputFormat=CSV' \
     -F "output=output.csv" \
     http://localhost:8080/mqaf-ws/validate
