package de.gwdg.metadataqa.ws;

import io.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.response.Response;

/**
 * Testing the web service calls
 */
class MqafControllerIT {

  @BeforeClass
  public static void setUpClass() {
    RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
  }

  @Test
  void indexPage() {
    Response response = given().get("/mqaf-ws/");
    assertEquals("This is Metadata Quality Assessment Framework", response.getBody().asPrettyString());
  }

  @Test
  void getClasspath() {
    Response response = given().get("/mqaf-ws/classpath");
    assertEquals("/usr/local/tomcat/bin/bootstrap.jar:/usr/local/tomcat/bin/tomcat-juli.jar",
      response.getBody().asPrettyString());
  }

  @Test
  void validation_minimal_usage() {
    given()
      .params(
        "schemaContent", "{\"format\":\"XML\",\"fields\":[{\"name\":\"id\",\"path\":\"lido:lido/lido:lidoRecID\",\"extractable\":true,\"identifierField\":true,\"rules\":[{\"id\":\"idPattern\",\"description\":\"The record ID should fit to a pattern\",\"pattern\":\"^DE-Mb[0-9]+/lido-obj.*$\"}]},{\"name\":\"source\",\"path\":\"lido:lido/lido:lidoRecID/@lido:source\",\"extractable\":true,\"rules\":[{\"id\":\"proveninance\",\"description\":\"The record should come from Foto Marburg\",\"pattern\":\"^Deutsches Dokumentationszentrum für Kunstgeschichte - Bildarchiv Foto Marburg$\"}]}],\"namespaces\":{\"owl\":\"http://www.w3.org/2002/07/owl#\",\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"gml\":\"http://www.opengis.net/gml\",\"doc\":\"http://www.mda.org.uk/spectrumXML/Documentation\",\"sch\":\"http://purl.oclc.org/dsdl/schematron\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"tei\":\"http://www.tei-c.org/ns/1.0\",\"lido\":\"http://www.lido-schema.org\",\"xlink\":\"http://www.w3.org/1999/xlink\",\"smil20lang\":\"http://www.w3.org/2001/SMIL20/Language\"}}",
        "inputFile[]", "LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml",
        "inputFile[]", "LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml",
        "recordAddress", "lido:lido"
      )
      .when()
      .post("/mqaf-ws/validate")
      .then()
        .body("report", equalTo("http://localhost:90/"))
        .body("success", equalTo(true))
      ;
  }

  @Test
  void validation_two_files() {
    given()
      .params(
        "schemaContent", "{\"format\":\"XML\",\"fields\":[{\"name\":\"id\",\"path\":\"lido:lido/lido:lidoRecID\",\"extractable\":true,\"identifierField\":true,\"rules\":[{\"id\":\"idPattern\",\"description\":\"The record ID should fit to a pattern\",\"pattern\":\"^DE-Mb[0-9]+/lido-obj.*$\"}]},{\"name\":\"source\",\"path\":\"lido:lido/lido:lidoRecID/@lido:source\",\"extractable\":true,\"rules\":[{\"id\":\"proveninance\",\"description\":\"The record should come from Foto Marburg\",\"pattern\":\"^Deutsches Dokumentationszentrum für Kunstgeschichte - Bildarchiv Foto Marburg$\"}]}],\"namespaces\":{\"owl\":\"http://www.w3.org/2002/07/owl#\",\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"gml\":\"http://www.opengis.net/gml\",\"doc\":\"http://www.mda.org.uk/spectrumXML/Documentation\",\"sch\":\"http://purl.oclc.org/dsdl/schematron\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"tei\":\"http://www.tei-c.org/ns/1.0\",\"lido\":\"http://www.lido-schema.org\",\"xlink\":\"http://www.w3.org/1999/xlink\",\"smil20lang\":\"http://www.w3.org/2001/SMIL20/Language\"}}",
        "inputFile", List.of("LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml", "LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml"),
        "recordAddress", "lido:lido"
      )
      .when()
      .post("/mqaf-ws/validate")
      .then()
        .body("report", equalTo("http://localhost:90/"))
        .body("success", equalTo(true))
      ;
  }

  @Test
  void validation_session() {
    given()
      .params(
        "schemaContent", "{\"format\":\"XML\",\"fields\":[{\"name\":\"id\",\"path\":\"lido:lido/lido:lidoRecID\",\"extractable\":true,\"identifierField\":true,\"rules\":[{\"id\":\"idPattern\",\"description\":\"The record ID should fit to a pattern\",\"pattern\":\"^DE-Mb[0-9]+/lido-obj.*$\"}]},{\"name\":\"source\",\"path\":\"lido:lido/lido:lidoRecID/@lido:source\",\"extractable\":true,\"rules\":[{\"id\":\"proveninance\",\"description\":\"The record should come from Foto Marburg\",\"pattern\":\"^Deutsches Dokumentationszentrum für Kunstgeschichte - Bildarchiv Foto Marburg$\"}]}],\"namespaces\":{\"owl\":\"http://www.w3.org/2002/07/owl#\",\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"gml\":\"http://www.opengis.net/gml\",\"doc\":\"http://www.mda.org.uk/spectrumXML/Documentation\",\"sch\":\"http://purl.oclc.org/dsdl/schematron\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"tei\":\"http://www.tei-c.org/ns/1.0\",\"lido\":\"http://www.lido-schema.org\",\"xlink\":\"http://www.w3.org/1999/xlink\",\"smil20lang\":\"http://www.w3.org/2001/SMIL20/Language\"}}",
        "inputFile[]", "LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml",
        "inputFile[]", "LIDO-v1.1-Example_FMobj00154983-LaPrimavera.xml",
        "recordAddress", "lido:lido",
        "sessionId", "test-session",
        "reportId", "1"
      )
      .when()
      .post("/mqaf-ws/validate")
      .then()
      .body("report", equalTo("http://localhost:90/test-session/1"))
      .body("success", equalTo(true))
    ;
  }
}