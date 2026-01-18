-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: schema_for_project
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `subscriber`
--

DROP TABLE IF EXISTS `subscriber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriber` (
  `Personalinfo` varchar(45) NOT NULL,
  `Name` varchar(45) NOT NULL,
  `subscriberId` int NOT NULL AUTO_INCREMENT,
  `CostumerId` int DEFAULT NULL,
  `ScanCode` varchar(8) DEFAULT NULL,
  PRIMARY KEY (`subscriberId`),
  UNIQUE KEY `uk_subscriber_scancode` (`ScanCode`),
  KEY `fk_subscriber_costumer` (`CostumerId`),
  CONSTRAINT `fk_subscriber_costumer` FOREIGN KEY (`CostumerId`) REFERENCES `costumer` (`CostumerId`)
) ENGINE=InnoDB AUTO_INCREMENT=2019 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriber`
--

LOCK TABLES `subscriber` WRITE;
/*!40000 ALTER TABLE `subscriber` DISABLE KEYS */;
INSERT INTO `subscriber` VALUES ('0530000003','Lama',2001,3,'00002001'),('0530000004','Hala',2002,4,'00002002'),('0530000005','Sara',2003,5,'00002003'),('0530000006','Rona',2004,6,'00002004'),('0530000007','Aya',2005,7,'00002005'),('0530000008','Katreen',2006,8,'00002006'),('0530000009','Noam',2007,9,'00002007'),('0530000010','Omer',2008,10,'00002008'),('0530000011','Maya',2009,11,'00002009'),('0530000012','Yossi',2010,12,'00002010'),('0530000013','Dana',2011,13,'00002011'),('0530000014','Nour',2012,14,'00002012'),('0530000015','Amir',2013,15,'00002013'),('0530000016','Lior',2014,16,'00002014'),('0530000017','Yara',2015,17,'00002015'),('0530000018','Adam',2016,18,'00002016'),('0530000019','Moran',2017,19,'00002017'),('0530000020','Shani',2018,20,'00002018');
/*!40000 ALTER TABLE `subscriber` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-18  2:35:03
