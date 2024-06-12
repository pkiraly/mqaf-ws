FROM tomcat:10.0.11-jdk17-openjdk-slim

RUN  mkdir -p /opt/metadata-qa/input \
  && mkdir -p /opt/metadata-qa/output

COPY target/mqaf-ws.war /usr/local/tomcat/webapps/

EXPOSE 8080

CMD ["catalina.sh", "run"]