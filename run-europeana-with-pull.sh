#1/usr/bin/env bash

#### - - - - - - - - - - -
# run Europeana validation
# with pulling image from dockerhub
#- - - - - - - - - - - - -

ME=$(basename $0)

show_usage() { # display help message
  cat <<EOF
run Europeana validation with pulling image from a docker repository

usage:
 ${ME} [options]

options:
 -r, --repository <arg>      the Docker repository where the image should be pulled from ('dockerhub' or 'github')
 -h, --help                  display help
EOF
  exit 1
}

REPOSITORY=dockerhub
SHORT_OPTIONS="r:h"
LONG_OPTIONS="repository:,help"

GETOPT=$(getopt \
  -o ${SHORT_OPTIONS} \
  --long ${LONG_OPTIONS} \
  -n ${ME} -- "$@")
eval set -- "${GETOPT}"

HELP=0
while true ; do
  case "$1" in
    -r|--repository)  REPOSITORY=$2 ; shift 2 ;;
    -h|--help)        HELP=1        ; shift   ;;
    --) shift ; break ;;
    *) echo "Internal error!: $1" ; exit 1 ;;
  esac
done

if [[ $HELP -eq 1 ]]; then
  show_usage
fi

if [[ "${REPOSITORY}" == "dockerhub" ]]; then
  IMAGE=pkiraly/mqaf-ws
elif [[ "${REPOSITORY}" == "github" ]]; then
  IMAGE=ghcr.io/pkiraly/mqaf-ws:main
else
  echo "ERROR: Not supported repository: ${REPOSITORY}"
  echo
  show_usage
  exit
fi

echo "# remove container and image"
docker stop $(docker ps -a -q)
docker rm mqaf-ws
docker rmi $(docker images $IMAGE -q)

echo "# pull image"
docker pull ${IMAGE}

echo "# run container"
if [[ ! -d test-europeana/output ]]; then
  mkdir test-europeana/output
fi
docker run -d -p 8080:8080 \
  -v ./test-europeana:/opt/metadata-qa/input \
  -v ./test-europeana/output:/opt/metadata-qa/output \
  --name mqaf-ws \
  ${IMAGE}

sleep 3

echo "# call the API"
curl -X POST \
     -w '\n' \
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
