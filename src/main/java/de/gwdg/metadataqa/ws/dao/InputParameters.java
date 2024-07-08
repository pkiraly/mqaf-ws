package de.gwdg.metadataqa.ws.dao;

import de.gwdg.metadataqa.api.configuration.ConfigurationReader;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.configuration.SchemaConfiguration;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.ws.MqafConfiguration;
import de.gwdg.metadataqa.ws.Utils;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import static de.gwdg.metadataqa.api.cli.App.JSON;
import static de.gwdg.metadataqa.api.cli.App.YAML;

public class InputParameters {
  private static final Logger logger = Logger.getLogger(InputParameters.class.getCanonicalName());

  private final MqafConfiguration mqafConfiguration;

  private Schema schema;
  private MeasurementConfiguration measurementConfig;
  private String outputFilePath;

  public InputParameters(MqafConfiguration mqafConfiguration) {
    this.mqafConfiguration = mqafConfiguration;
  }

  public String getOutputDir() {
    return mqafConfiguration.getOutputDir();
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

  public Schema createSchema(String schemaContent, String schemaFile, String schemaFormat) throws FileNotFoundException {
    try {
      if (schemaFile != null && !schemaFile.isEmpty()) {
        switch (schemaFormat) {
          case YAML: schema = ConfigurationReader.readSchemaYaml(schemaFile).asSchema(); break;
          case JSON:
          default:   schema = ConfigurationReader.readSchemaYaml(schemaFile).asSchema();
        }
      } else {
        switch (schemaFormat) {
          case YAML: schema = Utils.loadYaml(schemaContent, SchemaConfiguration.class).asSchema(); break;
          case JSON:
          default:   schema = Utils.loadJson(schemaContent, SchemaConfiguration.class).asSchema();
        }
      }
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return schema;
  }

  public MeasurementConfiguration createMeasurementConfiguration(String measurementsContent,
                                                                      String measurementsFile,
                                                                      String measurementsFormat)
    throws FileNotFoundException {
    if (measurementsFile != null && !measurementsFile.isEmpty()) {
      logger.info("Read MeasurementConfiguration from file: " + measurementsFile);
      logger.info("measurementsFormat: " + measurementsFormat);
      switch (measurementsFormat) {
        case YAML: measurementConfig = ConfigurationReader.readMeasurementJson(measurementsFile); break;
        case JSON:
        default:   measurementConfig = ConfigurationReader.readMeasurementYaml(measurementsFile);
      }
    } else {
      switch (measurementsFormat) {
        case YAML: measurementConfig = Utils.loadYaml(measurementsContent, MeasurementConfiguration.class); break;
        case JSON:
        default:   measurementConfig = Utils.loadJson(measurementsContent, MeasurementConfiguration.class);
      }
    }
    return measurementConfig;
  }

  public void setOutputFilePath(String outputFilePath) {
    this.outputFilePath = outputFilePath;
  }
}
