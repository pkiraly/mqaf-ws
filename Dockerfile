FROM tomcat:10.0.11-jdk17-openjdk-slim

RUN  mkdir -p /opt/metadata-qa/input \
  && mkdir -p /opt/metadata-qa/output \
  && mkdir -p /opt/metadata-qa/scripts \
  && chmod 777 /opt/metadata-qa/output

ENV TZ=Etc/UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
 && echo $TZ > /etc/timezone

RUN apt-get update \
 # Install add-apt-repository command
 && apt-get install -y --no-install-recommends software-properties-common gnupg2 \
 # add PPA with pre-compiled cran packages
 && add-apt-repository -y ppa:openjdk-r/ppa \
 && echo "deb https://cloud.r-project.org/bin/linux/ubuntu focal-cran40/" > /etc/apt/sources.list.d/cran.list \
 && apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E298A3A825C0D65DFD57CBB651716619E084DAB9 \
 && apt-get install -y --no-install-recommends \
      # install basic OS tools
      apt-utils \
      nano \
      jq \
      curl \
      wget \
      openssl \
      git \
      # Install R
      r-base \
      # Install R packages from ppa:marutter
      r-cran-tidyverse \
      r-cran-stringr \
      r-cran-gridextra \
      r-cran-rsqlite \
      r-cran-httr \
      sqlite3 \
      less \
      # for Apache Solr
      lsof \
      # php \
      locales \
      php \
      php-sqlite3 \
      php-curl \
      php-yaml \
      php-intl \
      php-dom \
 && apt-get --assume-yes autoremove \
 && rm -rf /var/lib/apt/lists/*

COPY target/mqaf-ws.war /usr/local/tomcat/webapps/
COPY scripts/* /opt/metadata-qa/scripts

EXPOSE 8080

CMD ["catalina.sh", "run"]