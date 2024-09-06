package de.gwdg.metadataqa.ws;

/**
 * Utility functions for integration tests
 */
public class UtilIT {

  static String getRestAssuredBaseUri() {
    String restAssuredBaseUri = "http://localhost:8080";
    String specifiedUri = System.getProperty("mqaf-ws.test.baseurl");
    if (specifiedUri != null) {
      restAssuredBaseUri = specifiedUri;
    }
    return restAssuredBaseUri;
  }
}
