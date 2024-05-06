# mqaf-ws
REST API for the Metadata Quality Assessment Framework

## Build and deploy the tool
build
```
mvn clean package
```

deploy
- stop Tomcat if running
- copy the .war file to Tomcat
```
cp tartget/mqaf-ws.war path/to/tomcat/webapps-javaee
```
- start Tomcat

## Usage

The REST API endpoint is available at https://YOURSERVER/ws/validate

You can use the following parameters (see more details [here](https://github.com/pkiraly/metadata-qa-marc#validating-marc-records)):

    @RequestParam(value = "headers", defaultValue = "") String headers,
    @RequestParam(value = "outputFormat", defaultValue = "ndjson") String outputFormat,
    @RequestParam(value = "output", defaultValue = "") String output,
    @RequestParam(value = "recordAddress", defaultValue = "") String recordAddress

* `schemaFile` (optional, String, default: "") The schema configuration file
* `schemaFormat` (optional, String, default: "yaml") The format of the Schema file (`yaml` or `json`)
* `measurements` (optional, String, default: "") The measurement configuration file
* `measurementsFormat` (optional, String, default: "yaml") The format of the Schema file (`yaml` or `json`)
* `inputFile` (optional, String) The input file
* `gzip` (optional, boolean) A flag to denote if the input file is gzipped
* `recordAddress` (optional, String) The XPath expression that separates individual records within an XML file 
    (it can be used if the `inputFile` is an XML file)
* `output` (optional, String) The output file
* `outputFormat` (optional, String, defaultValue = "csv") The output format

Validate a binary marc file in pure MARC21 schema:
```
DIR=/path/to/files
curl -X POST \
     -F 'schemaFile=${DIR}/dc-schema.yaml' \
     -F 'schemaFormat=yaml' \
     -F 'measurementsFile=${DIR}/measurement.json' \
     -F 'measurementsFormat=json' \
     -F 'inputFile=${DIR}/UB_W-rzburg_Texte.xml' \
     -F 'recordAddress=//oai:record' \
     -F 'gzip=false' \
     -F 'outputFormat=CSV' \
     -F 'output=${DIR}/output.csv' \
     http://localhost:8080/mqaf-ws/validate
```