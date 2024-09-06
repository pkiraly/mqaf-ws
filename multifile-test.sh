#!/usr/bin/env bash

TYPE=$1

if [[ "$TYPE" = "aqinda" ]]; then
  PORT=9090
  INPUT_FILE1=dc7d4e47-e1b1-4ea3-93b1-e8fd9bacc14c_LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml
  INPUT_FILE2=dc7d4e47-e1b1-4ea3-93b1-e8fd9bacc14c_LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml
else
  PORT=8080
  INPUT_FILE1=LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml
  INPUT_FILE2=LIDO-v1.1-Example_FMobj20344012-Fontana_del_Moro.xml
fi

echo "with content"
curl -X POST \
     -w '\n' \
     -F 'schemaContent={"format":"XML","fields":[{"name":"id","path":"lido:lido/lido:lidoRecID","extractable":true,"identifierField":true,"rules":[{"id":"idPattern","description":"The record ID should fit to a pattern","pattern":"^DE-Mb[0-9]+/lido-obj.*$"}]},{"name":"source","path":"lido:lido/lido:lidoRecID/@lido:source","extractable":true,"rules":[{"id":"proveninance","description":"The record should come from Foto Marburg","pattern":"^Deutsches Dokumentationszentrum für Kunstgeschichte - Bildarchiv Foto Marburg$"}]}],"namespaces":{"owl":"http://www.w3.org/2002/07/owl#","rdf":"http://www.w3.org/1999/02/22-rdf-syntax-ns#","gml":"http://www.opengis.net/gml","doc":"http://www.mda.org.uk/spectrumXML/Documentation","sch":"http://purl.oclc.org/dsdl/schematron","skos":"http://www.w3.org/2004/02/skos/core#","tei":"http://www.tei-c.org/ns/1.0","lido":"http://www.lido-schema.org","xlink":"http://www.w3.org/1999/xlink","smil20lang":"http://www.w3.org/2001/SMIL20/Language"}}' \
     -F 'schemaFormat=yaml' \
     -F 'measurementsContent={"fieldExtractorEnabled":true,"fieldExistenceMeasurementEnabled":false,"fieldCardinalityMeasurementEnabled":false,"completenessMeasurementEnabled":false,"tfIdfMeasurementEnabled":false,"problemCatalogMeasurementEnabled":false,"ruleCatalogMeasurementEnabled":true,"languageMeasurementEnabled":false,"multilingualSaturationMeasurementEnabled":false,"collectTfIdfTerms":false,"uniquenessMeasurementEnabled":false,"completenessCollectFields":false,"saturationExtendedResult":false,"checkSkippableCollections":false,"onlyIdInHeader":true,"ruleCheckingOutputType":"BOTH"}' \
     -F 'measurementsFormat=json' \
     -F 'gzip=false' \
     -F 'outputFormat=CSV' \
     -F "output=output.csv" \
     -F "inputFile=${INPUT_FILE1}" \
     -F "inputFile=${INPUT_FILE2}" \
     -F 'recordAddress=lido:lido' \
     -F 'inputFormat=xml' \
     -F 'sessionId=abc' \
     -F 'reportId=123' \
     http://localhost:${PORT}/mqaf-ws/validate

exit

sleep 1

echo "with stream"
DIR=/home/pkiraly/git/aqinda/constrainify/test-data/aqinda/config
curl -X POST \
     -w '\n' \
     -F "schemaStream=@${DIR}/schema.yaml" \
     -F 'schemaFormat=yaml' \
     -F "measurementsStream=@${DIR}/measurements.json" \
     -F 'measurementsFormat=json' \
     -F 'gzip=false' \
     -F 'outputFormat=CSV' \
     -F "output=output.csv" \
     -F "inputFile=${INPUT_FILE1}" \
     -F "inputFile=${INPUT_FILE2}" \
     -F 'recordAddress=lido:lido' \
     -F 'inputFormat=xml' \
     -F 'sessionId=abc' \
     -F 'reportId=123' \
     http://localhost:${PORT}/mqaf-ws/validate
