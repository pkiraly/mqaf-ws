CREATE DATABASE IF NOT EXISTS mqaf CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'mqaf'@'%' IDENTIFIED BY 'mqaf';
GRANT ALL PRIVILEGES ON *.* TO 'mqaf'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

USE mqaf;

DROP TABLE IF EXISTS file;
CREATE TABLE file (
  file TEXT,
  metadata_schema VARCHAR(20),
  provider_id VARCHAR(50),
  provider_name VARCHAR(200),
  set_id VARCHAR(50),
  set_name VARCHAR(200),
  datum VARCHAR(20),
  size INTEGER
);
CREATE INDEX f_file_idx ON file (file(500));
CREATE INDEX f_schema_idx ON file (metadata_schema);
CREATE INDEX f_set_id_idx ON file (set_id);
CREATE INDEX f_provider_id_idx ON file (provider_id);

DROP TABLE IF EXISTS variability;
CREATE TABLE variability (
  field VARCHAR(20),
  number_of_values INTEGER,
  metadata_schema VARCHAR(20),
  set_id VARCHAR(50),
  provider_id VARCHAR(50)
);

DROP TABLE IF EXISTS frequency;
CREATE TABLE frequency (
  field VARCHAR(20),
  value VARCHAR(20),
  frequency INTEGER,
  metadata_schema VARCHAR(20),
  set_id VARCHAR(50),
  provider_id VARCHAR(50)
);

DROP TABLE IF EXISTS count;
CREATE TABLE count (
  metadata_schema VARCHAR(20),
  set_id VARCHAR(50),
  provider_id VARCHAR(50),
  count INTEGER
);