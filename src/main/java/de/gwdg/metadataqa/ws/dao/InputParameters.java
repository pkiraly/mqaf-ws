package de.gwdg.metadataqa.ws.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gwdg.metadataqa.api.configuration.ConfigurationReader;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.configuration.SchemaConfiguration;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.ws.MqafConfiguration;
import de.gwdg.metadataqa.ws.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static de.gwdg.metadataqa.api.cli.App.JSON;
import static de.gwdg.metadataqa.api.cli.App.YAML;

public class InputParameters {
  private static final Logger logger = Logger.getLogger(InputParameters.class.getCanonicalName());

  private final MqafConfiguration mqafConfiguration;

  private Schema schema;
  private MeasurementConfiguration measurementConfig;
  private String outputFilePath;
  private String schemaFile;
  private String measurementFile;
  private String measurementsFormat;
  private String schemaFormat;
  private List<String> ruleColumns;
  private String sessionId;
  private String reportId;

  public InputParameters(MqafConfiguration mqafConfiguration) {
    this.mqafConfiguration = mqafConfiguration;
  }

  public String getOutputDir() {
    return mqafConfiguration.getOutputDir();
  }

  public String getInputDir() {
    return mqafConfiguration.getInputDir();
  }

  public Schema getSchema() {
    return schema;
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  public String getOutputFilePath() {
    return outputFilePath;
  }

  public MeasurementConfiguration getMeasurementConfig() {
    return measurementConfig;
  }

  public MqafConfiguration getMqafConfiguration() {
    return mqafConfiguration;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getReportId() {
    return reportId;
  }

  public void setReportId(String reportId) {
    this.reportId = reportId;
  }

  public List<String> getRuleColumns() {
    return ruleColumns;
  }

  public void setRuleColumns(List<String> ruleColumns) {
    this.ruleColumns = ruleColumns;
  }

  public Schema createSchema(String schemaContent, String schemaFile, String schemaFormat) throws FileNotFoundException {
    this.schemaFormat = schemaFormat;
    try {
      if (schemaFile != null && !schemaFile.isEmpty()) {
        this.schemaFile = schemaFile;
        switch (schemaFormat) {
          case YAML: schema = ConfigurationReader.readSchemaYaml(schemaFile).asSchema(); break;
          case JSON:
          default:   schema = ConfigurationReader.readSchemaYaml(schemaFile).asSchema();
        }
      } else {
        switch (schemaFormat) {
          case YAML:
            schema = Utils.loadYaml(schemaContent, SchemaConfiguration.class).asSchema();
            this.schemaFile = "schema.yaml";
            saveToFile(schemaContent, this.schemaFile);
            break;
          case JSON:
          default:
            schema = Utils.loadJson(schemaContent, SchemaConfiguration.class).asSchema();
            this.schemaFile = "schema.json";
            saveToFile(schemaContent, this.schemaFile);
            break;
        }
      }
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return schema;
  }

  /**
   * Save content into a file in the input directory
   * @param content
   * @param schemaFile
   * @throws IOException
   */
  private void saveToFile(String content, String schemaFile) throws IOException {
    File exportSchemaFile = new File(mqafConfiguration.getOutputDir() + "/" + schemaFile);
    FileUtils.writeStringToFile(exportSchemaFile, content, Charset.forName("UTF-8"));
  }

  public MeasurementConfiguration createMeasurementConfiguration(String measurementsContent,
                                                                      String measurementsFile,
                                                                      String measurementsFormat)
    throws IOException, FileNotFoundException {
    this.measurementsFormat = measurementsFormat;
    if (measurementsFile != null && !measurementsFile.isEmpty()) {
      logger.info("Read MeasurementConfiguration from file: " + measurementsFile);
      logger.info("measurementsFormat: " + measurementsFormat);
      this.measurementFile = measurementsFile;
      switch (measurementsFormat) {
        case YAML: measurementConfig = ConfigurationReader.readMeasurementJson(measurementsFile); break;
        case JSON:
        default:   measurementConfig = ConfigurationReader.readMeasurementYaml(measurementsFile);
      }
    } else {
      switch (measurementsFormat) {
        case YAML:
          measurementConfig = Utils.loadYaml(measurementsContent, MeasurementConfiguration.class);
          this.measurementFile = "measurement.yaml";
          saveToFile(measurementsContent, this.measurementFile);
          break;
        case JSON:
        default:
          measurementConfig = Utils.loadJson(measurementsContent, MeasurementConfiguration.class);
          this.measurementFile = "measurement.json";
          saveToFile(measurementsContent, this.measurementFile);
          break;
      }
    }
    return measurementConfig;
  }

  public void setOutputFilePath(String outputFilePath) {
    this.outputFilePath = outputFilePath;
  }

  public void saveInputParameters() {
    Map<String, String> inputParameters = Map.of(
      "measurements", this.measurementFile.replace(getInputDir(), ""),
      "measurementsFormat", this.measurementsFormat,
      "schema", this.schemaFile.replace(getInputDir(), ""),
      "schemaFormat", this.schemaFormat,
      "sessionId", this.sessionId,
      "reportId", this.reportId
    );
    try {
      saveToFile(Utils.toJson(inputParameters), "input-parameters.json");
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
