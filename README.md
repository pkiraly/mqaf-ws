# mqaf-ws
REST API for the Metadata Quality Assessment Framework

## Build and deploy the tool
Build (requires JDK 17):
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

You can use the following parameters for a POST request (see also [OpenAPI document](openapi.yaml)).

Schema configuration related parameters:
* `schemaContent` (optional, String, default: "") A JSON or YAML string containing the schema.
* `schemaStream` (optional, multipart/form-data, default: null) The content stream of a schema file
   encoded as multipart/form-data. It is used by web forms when you upload a file.
   If you use curl you can use the `-F measurementsContent=@filename` syntax to pass the content. 
   See [RFC 2388](https://datatracker.ietf.org/doc/html/rfc2388) and [cURL manual](https://curl.se/docs/manpage.html#-F).
* `schemaFileName` (optional, String, default: "schema.json") The schema file. The file should be available in
   the container's `/opt/metadata-qa/input` directory.
* `schemaFormat` (mandatory, String, default: "json") The format of the Schema file (`yaml` or `json`).

Measurement configuration related parameters (see 
[details](https://github.com/pkiraly/metadata-qa-api?tab=readme-ov-file#defining-measurementconfiguration-with-a-configuration-file)
about the configuration)

* `measurementsContent` (optional, String, default: "") A JSON or YAML string containing the measurement configuration.
* `measurementsStream` (optional, multipart/form-data, default: null) The content stream of a measurement configuration
   file encoded as multipart/form-data. If you use curl you can use the `-F measurementsContent=@filename` syntax to pass the content.
   See [RFC 2388](https://datatracker.ietf.org/doc/html/rfc2388) and [cURL manual](https://curl.se/docs/manpage.html#-F).
* `measurementsFileName` (optional, String, default: "measurements.json") The measurement configuration file that
   describes what kind of quality measurements should be run. The file should be available in the container's
   `/opt/metadata-qa/config` directory.
* `measurementsFormat` (optional, String, default: "json") The format of the Schema file (`yaml` or `json`)

Input related parameters
* `inputFile` (optional, String) The name of the input file. It should be available at the `/opt/metadata-qa/input` directory.
* `inputFormat` (optional, String) The format of input file if it is a JSON (if the file extension is 
  XML or CSV you do not have to specify)
  * `ndjson`: line delimited JSON in which every line is a new record (the default)
  * `json-array`: JSON file that contains an array of objects
* `gzip` (optional, String representing a boolean, default: "false") A flag to denote if the input file is gzipped
* `recordAddress` (optional, String) The XPath expression that separates individual records within an XML file 
    (it can be used if the `inputFile` is an XML file)
* `headers` (optional, String) A comma spearated string denoting the header of the input CSV 

Output related parameters:
* `output` (mandatory, String) The output file name
* `outputFormat` (optional, String, default: "csv") The output format, one of `csv`, `json`, `ndjson`, `csvjson`
   (if the input is a CSV file without header line)

Other parameters:
* `sessionId` (optional, String) A string for a session identifier (that identifies a user session in an external system)
* `reportId` (optional, String) A string for report identifier (that identifies an analysis workflow in an external system)

### Output

If it was successfull, the API returns a simple JSON report:

```JSON
{
  "success": true,
  "report": "http://localhost/mqaf-report"
}
```
Where
- `success` `true` denotes that the process were successfully finished, otherwise its value is `false`. 
- `report` displays an URL where the report can be seen (given that the API is used together 
  with the [MQAF Report](https://github.com/pkiraly/mqaf-report) tool.)
- `errorMessage` provides some information about the error (if any).

The process moreover produces output files in two steps:

1. The Java application iterates all the records of the input (based on the `recordAddress` parameter), and 
   generates a Comma Sperated Values file (the default name is `output.csv`, but that can be overwriten by the
   `output` parameter). In the file each rows represent an individual record. The columns are dependent on the 
   input schema, usually it has an identifier column and columns for each rules. If the ... parameter is `BOTH`
   there will be two columns for each rule: a `[rule-identifier]_status` that contain the values of `0` (failed),
   `1` (passed) or `NA` (the data element is not available), and `[rule-identifier]_score` that contains a
   numeric value according to the `successScore`, `failureScore` and `naScore` values associated with the rule.
2. A postprocessing method creates additional files based on the `output.csv`
   - `count.csv` records the number of records
   - `output.sql` is an SQL script that is injected to MySQL/SQLite3 database, so the values can be read from that
   - `shacl4bib-stat.csv` a CSV file that has four columns: 
     - `id`: the identifier of the rule
     - `0`: the number of records that failed the check against the rule
     - `1`: the number of records that passed the check against the rule
     - `NA`: the number of records that does not have the data element the rule checks

### Examples:

Validate a Dublin Core XML file using configuration files stored locally:
```
DIR=/path/to/files
curl -X POST \
     -F 'schemaStream=@${DIR}/dc-schema.yaml' \
     -F 'schemaFormat=yaml' \
     -F 'measurementsStream=@${DIR}/measurement.json' \
     -F 'measurementsFormat=json' \
     -F 'inputFile=${DIR}/UB_W-rzburg_Texte.xml' \
     -F 'recordAddress=//oai:record' \
     -F 'gzip=false' \
     -F 'outputFormat=CSV' \
     -F 'output=${DIR}/output.csv' \
     http://localhost:8080/mqaf-ws/validate
```

Validate a LIDO XML file using configuration strings:
```
DIR=/path/to/files
curl -X POST \
     -w '\n' \
     -F 'schemaContent={"format":"XML","fields":[{"name":"id","path":"lido:lido/lido:lidoRecID","extractable":true,"identifierField":true,"rules":[{"id":"idPattern","description":"The record ID should fit to a pattern","pattern":"^DE-Mb\\d+/lido-obj.*$"}]},{"name":"source","path":"lido:lido/lido:lidoRecID/@lido:source","extractable":true,"rules":[{"id":"proveninance","description":"The record should come from Foto Marburg","pattern":"^Deutsches Dokumentationszentrum für Kunstgeschichte - Bildarchiv Foto Marburg$"}]}],"namespaces":{"owl":"http://www.w3.org/2002/07/owl#","rdf":"http://www.w3.org/1999/02/22-rdf-syntax-ns#","gml":"http://www.opengis.net/gml","doc":"http://www.mda.org.uk/spectrumXML/Documentation","sch":"http://purl.oclc.org/dsdl/schematron","skos":"http://www.w3.org/2004/02/skos/core#","tei":"http://www.tei-c.org/ns/1.0","lido":"http://www.lido-schema.org","xlink":"http://www.w3.org/1999/xlink","smil20lang":"http://www.w3.org/2001/SMIL20/Language"}}' \
     -F 'schemaFormat=json' \
     -F 'measurementsContent={"fieldExtractorEnabled":true,"fieldExistenceMeasurementEnabled":false,"fieldCardinalityMeasurementEnabled":false,"completenessMeasurementEnabled":false,"tfIdfMeasurementEnabled":false,"problemCatalogMeasurementEnabled":false,"ruleCatalogMeasurementEnabled":true,"languageMeasurementEnabled":false,"multilingualSaturationMeasurementEnabled":false,"collectTfIdfTerms":false,"uniquenessMeasurementEnabled":false,"completenessCollectFields":false,"saturationExtendedResult":false,"checkSkippableCollections":false,"onlyIdInHeader":true,"ruleCheckingOutputType":"BOTH"}' \
     -F 'measurementsFormat=json' \
     -F 'gzip=false' \
     -F 'outputFormat=CSV' \
     -F "output=output.csv" \
     -F "inputFile=${DIR}/lido.xml" \
     -F 'recordAddress=lido:lido' \
     -F 'inputFormat=xml' \
     -F 'sessionId=abc' \
     -F 'reportId=123' \
     http://localhost:${PORT}/mqaf-ws/validate
```

## Docker

The application is available as a docker image `pkiraly/mqaf-ws:latest`. It defines the following volumes:
- `INPUT` (default: `./input`) that points to the container's `/opt/metadata-qa/input`. This is the location of the 
   input files, the user can set relative path in `inputFile`, `schemaFile` and `measurements``properties.
- `OUTPUT` (default: `./output`) that points to the container's `/opt/metadata-qa/output`. This is the directory 
   where the API puts the output file.

The continuous integration process also creates and stores an alternative Docker image at Github for every commited 
changes of the source code. It can be access as

```
docker pull ghcr.io/pkiraly/mqaf-ws:main
```

The application runs an Apache Tomcat server which is available internally on port 8080. To launch the application you 
can execute the following command:

```bash
docker run -d \
  -p 8080:8080 \
  -v ./input:/opt/metadata-qa/input \
  -v ./output:/opt/metadata-qa/output \
  --name mqaf-ws pkiraly/mqaf-ws
```
or if you use the reporitory's `docker-compose.yml` file:

```bash
docker compose up
```

### Test run

In the `test-europeana` directory there are files that one can use for testing reasons. We provided two scripts that 
utilize these files. They remove previous Docker containers and images, fetch or build the Docker image, start service 
and run the predefined REST API call.

To run Europeana validation with pulling image from dockerhub:
```
./run-europeana-with-pull.sh [options]
```

options:
* `-r <arg>`|`--repository <arg>`  the Docker repository where the image should be pulled from 
  (either `dockerhub` (a more stable version) or `github` (the current developer version))
* `-h`|`--help` display help

To run Europeana validation with building docker image locally:
```
./run-europeana-with-build.sh
```

In the `test-aqinda` directory there are files related to the Aqinda project. To run Aqinda assessment execute the 
following:

```
./run-aqinda-with-build.sh
```
