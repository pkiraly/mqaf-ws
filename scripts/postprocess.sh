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
 -d, --outputDir <arg>       output directory
 -i, --inputDir <arg>        input directory
 -r, --ruleColumns <arg>     the columns in the output that contain rules
 -h, --help                  display help
EOF
  exit 1
}

REPOSITORY=dockerhub
SHORT_OPTIONS="f:d:i:r:h"
LONG_OPTIONS="outputFilePath:,outputDir:,inputDir:,ruleColumns:,help"

GETOPT=$(getopt \
  -o ${SHORT_OPTIONS} \
  --long ${LONG_OPTIONS} \
  -n ${ME} -- "$@")
eval set -- "${GETOPT}"

OUTPUT_FILE_PATH="output.csv"
OUTPUT_DIR="/opt/metadata-qa/output"
INPUT_DIR="/opt/metadata-qa/input"
RULE_COLUMNS="rulecatalog_score"
HELP=0
while true ; do
  case "$1" in
    -f|--outputFilePath)  OUTPUT_FILE_PATH=$2 ; shift 2 ;;
    -d|--outputDir)       OUTPUT_DIR=$2       ; shift 2 ;;
    -i|--inputDir)        INPUT_DIR=$2        ; shift 2 ;;
    -r|--ruleColumns)     RULE_COLUMNS=$2     ; shift 2 ;;
    -h|--help)            HELP=1              ; shift   ;;
    --) shift ; break ;;
    *) echo "Internal error!: $1" ; exit 1 ;;
  esac
done

DIR=$(dirname $0)
cd ${DIR}
./hello-world.sh > ${OUTPUT_DIR}/hello-world.txt
php csv2sql.php ${OUTPUT_FILE_PATH} 'output' > ${OUTPUT_DIR}/output.sql
Rscript analyse-output.R --csv ${OUTPUT_FILE_PATH} \
                         --outputDir ${OUTPUT_DIR} \
                         --fields ${RULE_COLUMNS} \
                         -v
chmod 755 -R ${OUTPUT_DIR}
chown www-data -R ${OUTPUT_DIR}
chmod 755 -R ${INPUT_DIR}
