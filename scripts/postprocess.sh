#!/usr/bin/env bash

#### - - - - - - - - - - -
# Run post processing steps on the output of the MQAF REST API
#- - - - - - - - - - - - -

ME=$(basename $0)

show_usage() { # display help message
  cat <<EOF
Run post processing steps on the output of the MQAF REST API

usage:
 ${ME} [options]

options:
 -f, --outputFilePath <arg>  output file path
 -d, --outputDir <arg>  output directory
 -h, --help                  display help
EOF
  exit 1
}

REPOSITORY=dockerhub
SHORT_OPTIONS="f:d:i:h"
LONG_OPTIONS="outputFilePath:,outputDir:,inputDir:,help"

GETOPT=$(getopt \
  -o ${SHORT_OPTIONS} \
  --long ${LONG_OPTIONS} \
  -n ${ME} -- "$@")
eval set -- "${GETOPT}"

OUTPUT_FILE_PATH="output.csv"
OUTPUT_DIR="/opt/metadata-qa/output"
INPUT_DIR="/opt/metadata-qa/input"
HELP=0
while true ; do
  case "$1" in
    -f|--outputFilePath)  OUTPUT_FILE_PATH=$2 ; shift 2 ;;
    -d|--outputDir)       OUTPUT_DIR=$2       ; shift 2 ;;
    -i|--inputDir)        INPUT_DIR=$2        ; shift 2 ;;
    -h|--help)            HELP=1              ; shift   ;;
    --) shift ; break ;;
    *) echo "Internal error!: $1" ; exit 1 ;;
  esac
done

php csv2sql.php ${OUTPUT_FILE_PATH} 'output' > ${OUTPUT_DIR}/output.sql
hello-world.sh > ${OUTPUT_DIR}/hello-world.txt
chmod 644 -R ${OUTPUT_DIR}
chmod 644 -R ${INPUT_DIR}
