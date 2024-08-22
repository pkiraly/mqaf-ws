package de.gwdg.metadataqa.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
  public static <T> T loadJson(String content, Class<T> clazz) {
    var objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(content, clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T loadYaml(String content, Class<T> clazz) {
    var yaml = new Yaml(new Constructor(clazz, new LoaderOptions()));
    InputStream inputStream = IOUtils.toInputStream(content);
    return yaml.load(inputStream);
  }

  public static String toJson(Object data) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
