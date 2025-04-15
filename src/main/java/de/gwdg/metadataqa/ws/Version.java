package de.gwdg.metadataqa.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {
  private static String version;

  public static void main(String[] args) {
    System.err.println(de.gwdg.metadataqa.ws.Version.getVersion());
  }

  public static String getVersion() {
    if (version == null) {
      initialize();
    }
    return version;
  }

  private static void initialize() {
    String versionCandidate = de.gwdg.metadataqa.ws.Version.class.getPackage().getImplementationVersion();
    if (versionCandidate != null)
      version = versionCandidate;
    else {
      version = readVersionFromPropertyFile();
    }
  }

  public static String readVersionFromPropertyFile() {
    String path = "/version.prop";
    InputStream stream = de.gwdg.metadataqa.ws.Version.class.getResourceAsStream(path);
    if (stream == null)
      return "UNKNOWN";
    Properties props = new Properties();
    try {
      props.load(stream);
      stream.close();
      return (String) props.get("version");
    } catch (IOException e) {
      return "UNKNOWN";
    }
  }
}
