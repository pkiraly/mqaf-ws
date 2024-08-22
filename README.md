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

* `schemaFile` (optional, String, default: "") The schema configuration file. The file should be available in 
   the container's `/opt/metadata-qa/config` directory.
* `schemaContent` (optional, String, default: "") The content of schema configuration file. If you use curl
  you can use the `-F measurementsContent=@filename` syntax to pass the content.
* `schemaFormat` (optional, String, default: "yaml") The format of the Schema file (`yaml` or `json`)
* `measurementsFile` (optional, String, default: "") The measurement configuration file that describes what kind of 
   quality measurements should be run. The file should be available in the container's `/opt/metadata-qa/config` directory.
   See [details](https://github.com/pkiraly/metadata-qa-api?tab=readme-ov-file#defining-measurementconfiguration-with-a-configuration-file) 
* `measurementsContent` (optional, String, default: "") The content of a schema file. If you use curl 
   you can use the `-F measurementsContent=@filename` syntax to pass the content.
* `measurementsFormat` (optional, String, default: "yaml") The format of the Schema file (`yaml` or `json`)
* `inputFile` (optional, String) The input file
* `inputFormat` (optional, String) The format of input file if it is a JSON (if the file extension is 
  XML or CSV you do not have to specify)
  * `ndjson`: line delimited JSON in which every line is a new record (the default)
  * `json-array`: JSON file that contains an array of objects
* `gzip` (optional, boolean) A flag to denote if the input file is gzipped
* `recordAddress` (optional, String) The XPath expression that separates individual records within an XML file 
    (it can be used if the `inputFile` is an XML file)
* `output` (mandatory, String) The output file name
* `outputFormat` (optional, String, defaultValue = "csv") The output format
* `headers` (optional, String) A comma spearated string denoting the header of the input CSV 
   (if the input is a CSV file without header line)
* `sessionId` (optional, String) A string for a session identifier (that identifies a user session in an external system)
* `reportId` (optional, String) A string for report identifier (that identifies an analysis workflow in an external system)

Validate a binary marc file in pure MARC21 schema:
```
DIR=/path/to/files
curl -X POST \
     -F 'schemaContent=@${DIR}/dc-schema.yaml' \
     -F 'schemaFormat=yaml' \
     -F 'measurementsContent=@${DIR}/measurement.json' \
     -F 'measurementsFormat=json' \
     -F 'inputFile=${DIR}/UB_W-rzburg_Texte.xml' \
     -F 'recordAddress=//oai:record' \
     -F 'gzip=false' \
     -F 'outputFormat=CSV' \
     -F 'output=${DIR}/output.csv' \
     http://localhost:8080/mqaf-ws/validate
```

If it was successfull, the API returns a simple JSON report:

```JSON
{result: 1}
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