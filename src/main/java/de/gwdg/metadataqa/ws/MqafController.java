package de.gwdg.metadataqa.ws;

import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.cli.InputFormat;
import de.gwdg.metadataqa.api.cli.RecordFactory;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.configuration.schema.Rule;
import de.gwdg.metadataqa.api.interfaces.MetricResult;
import de.gwdg.metadataqa.api.io.reader.RecordReader;
import de.gwdg.metadataqa.api.io.reader.XMLRecordReader;
import de.gwdg.metadataqa.api.io.writer.ResultWriter;
import de.gwdg.metadataqa.api.json.DataElement;
import de.gwdg.metadataqa.api.rule.RuleCheckingOutputType;
import de.gwdg.metadataqa.api.schema.Schema;

import de.gwdg.metadataqa.ws.dao.InputParameters;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.awt.SystemColor.text;

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
    @RequestParam(value = "inputFormat", defaultValue = "") String inputFormat,
    @RequestParam(value = "gzip", defaultValue = "false") boolean gzip,
    @RequestParam(value = "outputFormat", defaultValue = "ndjson") String outputFormat,
    @RequestParam(value = "output", defaultValue = "") String outputFile,
    @RequestParam(value = "recordAddress", defaultValue = "") String recordAddress,
    Model model
  ) {
    try {
      InputParameters inputParameters = new InputParameters(mqafConfiguration);
      logger.info(String.format("gzip: %s", gzip));

      Schema schema = inputParameters.createSchema(schemaContent, getInputFilePath(schemaFile), schemaFormat);

      // initialize config
      MeasurementConfiguration measurementConfig = inputParameters.createMeasurementConfiguration(
        measurementsContent, getInputFilePath(measurementsFile), measurementsFormat);
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
      InputFormat inputFormatEnum = InputFormat.byCode(inputFormat);
      RecordReader inputReader = RecordFactory.getRecordReader(getInputFilePath(inputFile), calculator, gzip, inputFormatEnum);

      // initialize output
      // String outFormat = cmd.getOptionValue(OUTPUT_FORMAT, NDJSON);
      // write to std out if no file was given
      outputFile = getOutputFilePath(outputFile);
      inputParameters.setOutputFilePath(outputFile);
      inputParameters.saveInputParameters();

      ResultWriter outputWriter = !outputFile.equals("")
        ? RecordFactory.getResultWriter(outputFormat, outputFile)
        : RecordFactory.getResultWriter(outputFormat);

      if (recordAddress.equals(""))
        recordAddress = null;
      if (inputReader instanceof XMLRecordReader && recordAddress != null)
        ((XMLRecordReader)inputReader).setRecordAddress(recordAddress);

      long counter = 0;
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

      postProcess(inputParameters);

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.set("Content-Type", MediaType.APPLICATION_JSON.toString());

      return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .headers(responseHeaders)
        .body("{result: 1}");

    } catch (Exception e) {
      e.printStackTrace();
      // logger.severe(String.format("Assessment failed with %s records. ", counter));
      logger.severe(String.format("Assessment failed"));
      logger.severe(e.getClass() + " " + e.getMessage());

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.set("Content-Type", MediaType.APPLICATION_JSON.toString());

      return ResponseEntity.internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .headers(responseHeaders)
        .body(String.format("{result: %s}", e.getMessage()));
    }
  }

  private void postProcess(InputParameters inputParameters) throws IOException {
    logger.info("postProcess()");
    createDatabaseDefinition(inputParameters);
    List<String> commands = List.of(
      String.format("php /opt/metadata-qa/scripts/csv2sql.php %s %s > %s/%s",
        inputParameters.getOutputFilePath(), "output", inputParameters.getOutputDir(), "output.sql"),
      String.format("/opt/metadata-qa/scripts/hello-world.sh > %s/hello-world.txt", inputParameters.getOutputDir()),
      String.format("chmod 644 -R %s", inputParameters.getOutputDir()),
      String.format("chmod 644 -R %s", inputParameters.getInputDir())
    );
    Process process = null;
    try {
      for (String command : commands) {
        process = Runtime.getRuntime().exec(command);
        TimeUnit.SECONDS.sleep(1);
        if (process.exitValue() != 0) {
          logger.info(command);
          System.err.println(process.getErrorStream().transferTo(System.out));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static void createDatabaseDefinition(InputParameters inputParameters) {
    RuleCheckingOutputType outPutType = inputParameters.getMeasurementConfig().getRuleCheckingOutputType();
    logger.info("outPutType: " + outPutType.toString());
    Map<String, String> mapping = new LinkedHashMap<>();
    // List<String> fields = new ArrayList<>();
    for (DataElement dataElement : inputParameters.getSchema().getPaths()) {
      if (dataElement.isExtractable()) {
        mapping.put(dataElement.getLabel(), "VARCHAR(255)");
        // logger.info(dataElement.getLabel());
      }
      List<Rule> rules = dataElement.getRules();
      if (rules != null) {
        for (Rule rule : rules) {
          if (outPutType.equals(RuleCheckingOutputType.STATUS)) {
            mapping.put(rule.getId() + ":status", "BOOLEAN");
          } else if (outPutType.equals(RuleCheckingOutputType.SCORE)) {
            mapping.put(rule.getId() + ":score", "INTEGER");
          } else {
            mapping.put(rule.getId() + ":status", "BOOLEAN");
            mapping.put(rule.getId() + ":score", "INTEGER");
          }
          logger.info(rule.getId());
        }
      }
    }
    mapping.put("rulecatalog_score", "INTEGER");

    try (PrintWriter out = new PrintWriter(inputParameters.getOutputDir() + "/output-definition.sql")) {
      out.println(createDatabaseDefinitionSQL(mapping));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private static String createDatabaseDefinitionSQL(Map<String, String> mapping) {
    List<String> lines = new ArrayList<>();
    lines.add("CREATE TABLE %s (");
    for (Map.Entry<String, String> entry : mapping.entrySet())
      lines.add(String.format("  \"%s\" %s,", entry.getKey(), entry.getValue()));
    lines.add(");");
    return StringUtils.join(lines, "\n");
  }

  private String getInputFilePath(String inputFile) {
    return getPath(mqafConfiguration.getInputDir(), inputFile);
  }

  private String getOutputFilePath(String outputFile) {
    return getPath(mqafConfiguration.getOutputDir(), outputFile);
  }

  private static String getPath(String dir, String file) {
    if (StringUtils.isNoneBlank(dir) && (new File(dir)).exists()) {
      String separator = (dir.endsWith("/")) ? "" : "/";
      file = dir + separator + file;
    }
    return file;
  }

}
