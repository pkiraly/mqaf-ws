package de.gwdg.metadataqa.ws;

import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.cli.RecordFactory;
import de.gwdg.metadataqa.api.configuration.ConfigurationReader;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.configuration.SchemaConfiguration;
import de.gwdg.metadataqa.api.interfaces.MetricResult;
import de.gwdg.metadataqa.api.io.reader.RecordReader;
import de.gwdg.metadataqa.api.io.reader.XMLRecordReader;
import de.gwdg.metadataqa.api.io.writer.ResultWriter;
import de.gwdg.metadataqa.api.schema.Schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.opencsv.exceptions.CsvValidationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static de.gwdg.metadataqa.api.cli.App.JSON;
import static de.gwdg.metadataqa.api.cli.App.YAML;

@RestController
public class MqafController {

  private static final Logger logger = Logger.getLogger(MqafController.class.getCanonicalName());

  @Value("${spring.application.name}")
  String appName;

  @Autowired
  private MqafConfiguration mqafConfiguration;

  @GetMapping("/classpath")
  public String getClassPath() {
    return System.getProperty("java.class.path");
  }

  @GetMapping("/")
  public String indexPage() {
    return String.format("This is %s", appName);
  }

  @PostMapping("/validate")
  public ResponseEntity<String> validate(
    @RequestParam(value = "schemaContent", defaultValue = "") String schemaContent,
    @RequestParam(value = "schemaFile", defaultValue = "") String schemaFile,
    @RequestParam(value = "schemaFormat", defaultValue = "") String schemaFormat,
    @RequestParam(value = "headers", defaultValue = "") String headers,
    @RequestParam(value = "measurementsContent", defaultValue = "") String measurementsContent,
    @RequestParam(value = "measurementsFile", defaultValue = "") String measurementsFile,
    @RequestParam(value = "measurementsFormat", defaultValue = "") String measurementsFormat,
    @RequestParam(value = "inputFile", defaultValue = "") String inputFile,
    @RequestParam(value = "gzip", defaultValue = "false") boolean gzip,
    @RequestParam(value = "outputFormat", defaultValue = "ndjson") String outputFormat,
    @RequestParam(value = "output", defaultValue = "") String outputFile,
    @RequestParam(value = "recordAddress", defaultValue = "") String recordAddress,
    Model model
  ) throws IOException {
    logger.info(String.format("gzip: %s", gzip));
    // String schemaFile = cmd.getOptionValue(SCHEMA_CONFIG);
    // String schemaFormat = cmd.getOptionValue(SCHEMA_FORMAT, FilenameUtils.getExtension(schemaFile));
    Schema schema = getSchema(schemaContent, getInputFile(schemaFile), schemaFormat);

    // initialize config
    MeasurementConfiguration measurementConfig = getMeasurementConfiguration(measurementsContent, getInputFile(measurementsFile), measurementsFormat);
    logger.info(String.format("isUniquenessMeasurementEnabled: %s", measurementConfig.isUniquenessMeasurementEnabled()));

    // Set the fields supplied by the command line to extractable fields
    if (!headers.equals("")) {
      String[] headersList = StringUtils.split(headers, ",");
      for (String h : headersList) {
        schema.addExtractableField(h, schema.getPathByLabel(h).getPath());
      }
    }

    // initialize calculator
    CalculatorFacade calculator = new CalculatorFacade(measurementConfig);
    // set the schema which describes the source
    calculator.setSchema(schema);

    // initialize input
    RecordReader inputReader;
    try {
      inputReader = RecordFactory.getRecordReader(getInputFile(inputFile), calculator, gzip);
    } catch (CsvValidationException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // initialize output
    // String outFormat = cmd.getOptionValue(OUTPUT_FORMAT, NDJSON);
    // write to std out if no file was given
    String outputDir = mqafConfiguration.getOutputDir();
    if (StringUtils.isNotBlank(outputFile) && StringUtils.isNoneBlank(outputDir) && (new File(outputDir)).exists()) {
      String separator = (outputDir.endsWith("/")) ? "" : "/";
      outputFile = outputDir + separator + outputFile;
    }
    ResultWriter outputWriter = !outputFile.equals("")
      ? RecordFactory.getResultWriter(outputFormat, outputFile)
      : RecordFactory.getResultWriter(outputFormat);

    if (recordAddress.equals(""))
      recordAddress = null;
    if (inputReader instanceof XMLRecordReader && recordAddress != null)
      ((XMLRecordReader)inputReader).setRecordAddress(recordAddress);

    // run
    long counter = 0;
    try {
      // print header
      List<String> header = calculator.getHeader();
      outputWriter.writeHeader(header);

      while (inputReader.hasNext()) {
        Map<String, List<MetricResult>> measurement = inputReader.next();
        outputWriter.writeResult(measurement);

        // update process
        counter++;
        if (counter % 50 == 0) {
          logger.info(String.format("Processed %s records. ", counter));
        }
      }
      logger.info(String.format("Assessment completed successfully with %s records. ", counter));
      outputWriter.close();
    } catch (IOException e) {
      logger.severe(String.format("Assessment failed with %s records. ", counter));
      logger.severe(e.getMessage());
    }

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("Content-Type", MediaType.APPLICATION_JSON.toString());

    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_JSON)
      .headers(responseHeaders)
      .body("{result: 1}");
  }

  private String getInputFile(String inputFile) {
    String inputDir = mqafConfiguration.getInputDir();
    if (StringUtils.isNoneBlank(inputDir) && (new File(inputDir)).exists()) {
      String separator = (inputDir.endsWith("/")) ? "" : "/";
      inputFile = inputDir + separator + inputFile;
    }
    return inputFile;
  }

  private static MeasurementConfiguration getMeasurementConfiguration(String measurementsContent, String measurementsFile, String measurementsFormat) throws FileNotFoundException {
    MeasurementConfiguration measurementConfig;
    if (measurementsFile != null && !measurementsFile.isEmpty()) {
      logger.info("Read MeasurementConfiguration from file: " + measurementsFile);
      switch (measurementsFormat) {
        case YAML: measurementConfig = ConfigurationReader.readMeasurementJson(measurementsFile); break;
        case JSON:
        default:   measurementConfig = ConfigurationReader.readMeasurementYaml(measurementsFile);
      }
    } else {
      switch (measurementsFormat) {
        case YAML: measurementConfig = loadYaml(measurementsContent, MeasurementConfiguration.class); break;
        case JSON:
        default:   measurementConfig = loadJson(measurementsContent, MeasurementConfiguration.class);
      }
    }
    return measurementConfig;
  }

  private static Schema getSchema(String schemaContent, String schemaFile, String schemaFormat) throws FileNotFoundException {
    Schema schema = null;
    try {
      if (schemaFile != null && !schemaFile.isEmpty()) {
        logger.info("Read Schema from file: " + schemaFile);
        logger.info("schemaFormat: " + schemaFormat);
        switch (schemaFormat) {
          case YAML: schema = ConfigurationReader.readSchemaYaml(schemaFile).asSchema(); break;
          case JSON:
          default:   schema = ConfigurationReader.readSchemaYaml(schemaFile).asSchema();
        }
      } else {
        switch (schemaFormat) {
          case YAML: schema = loadYaml(schemaContent, SchemaConfiguration.class).asSchema(); break;
          case JSON:
          default:   schema = loadJson(schemaContent, SchemaConfiguration.class).asSchema();
        }
      }
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    logger.info("schema: " + schema);
    return schema;
  }

  private static <T> T loadJson(String content, Class<T> clazz) {
    var objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(content, clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static <T> T loadYaml(String content, Class<T> clazz) {
    var yaml = new Yaml(new Constructor(clazz, new LoaderOptions()));
    InputStream inputStream = IOUtils.toInputStream(content);
    return yaml.load(inputStream);
  }
}
