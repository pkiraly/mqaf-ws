package de.gwdg.metadataqa.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:mqaf.properties")
public class MqafConfiguration {

  @Autowired
  Environment env;

  @Bean
  public String getInputDir() {
    return env.getProperty("INPUT_DIR");
  }

  @Bean
  public String getOutputDir() {
    return env.getProperty("OUTPUT_DIR");
  }
}
