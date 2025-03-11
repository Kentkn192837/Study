-- ------------------------------------
-- データベース名はtestdbとする
-- ------------------------------------

DROP DATABASE IF EXISTS testdb;

CREATE DATABASE testdb;

USE testdb;

-- ------------------------------------
-- departmentテーブル：章全体で使用
-- ------------------------------------

DROP TABLE IF EXISTS department;

CREATE TABLE department (
  dept_code INT(11) NOT NULL,
  dept_name VARCHAR(20) NOT NULL,
  dept_address VARCHAR(40) NOT NULL,
  pilot_number VARCHAR(20) DEFAULT NULL,
  PRIMARY KEY  (dept_code)
);

LOCK TABLES department WRITE;
INSERT INTO department VALUES(1,'Sales','Tokyo', '03-3333-xxxx');
INSERT INTO department VALUES(2,'Engineering','Yokohama', '045-444-xxxx');
INSERT INTO department VALUES(3,'Development','Osaka', null);
INSERT INTO department VALUES(4,'Marketing','Fukuoka', '092-222-xxxx');
INSERT INTO department VALUES(5,'Education','Tokyo', null);
UNLOCK TABLES;

-- ------------------------------------
-- mytableAテーブル：●●2.javaで使用
-- ------------------------------------

DROP TABLE IF EXISTS mytableA;

CREATE TABLE mytableA (
  field1 DATE NOT NULL,
  field2 TIME NOT NULL,
  field3 TIMESTAMP NOT NULL
);
LOCK TABLES mytableA WRITE;
INSERT INTO mytableA VALUES('2020-10-30', '12:40', '2020-10-30T12:40');
UNLOCK TABLES;
-- ------------------------------------
-- mytableBテーブル：●●.javaで使用
-- ------------------------------------

DROP TABLE IF EXISTS mytableB;

CREATE TABLE mytableB (
  field1 INT(10) NOT NULL,
  field2 INT(20) NOT NULL,
  field3 VARCHAR(20) NOT NULL
);

LOCK TABLES mytableB WRITE;
INSERT INTO mytableB VALUES(1, 100000, 'TESTA');
INSERT INTO mytableB VALUES(2, 110000, 'TESTB');
INSERT INTO mytableB VALUES(3, 120000, 'TESTC');
INSERT INTO mytableB VALUES(4, 130000, 'TESTD');
UNLOCK TABLES;


-- ------------------------------------
-- ストアドプロシージャ：●●.javaで使用
-- TODO: ここは修正する
-- ------------------------------------
DROP PROCEDURE IF EXISTS myprocedure;
CREATE PROCEDURE myprocedure (IN value INT, OUT total INT)
BEGIN
  SELECT SUM(field2) INTO total FROM mytableB WHERE field2 > value;
END;
