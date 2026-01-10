-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: schema_for_project
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `makeres`
--

DROP TABLE IF EXISTS `makeres`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `makeres` (
  `subscriberId` int DEFAULT NULL,
  `ConfCode` int NOT NULL,
  `CostumerId` int NOT NULL,
  `ResId` int NOT NULL,
  PRIMARY KEY (`ResId`),
  KEY `makeres_ibfk_2` (`subscriberId`),
  KEY `CostumerId` (`CostumerId`),
  CONSTRAINT `CostumerId` FOREIGN KEY (`CostumerId`) REFERENCES `costumer` (`CostumerId`),
  CONSTRAINT `fk_makeres_res` FOREIGN KEY (`ResId`) REFERENCES `reservation` (`ResId`),
  CONSTRAINT `makeres_ibfk_2` FOREIGN KEY (`subscriberId`) REFERENCES `subscriber` (`subscriberId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `makeres`
--

LOCK TABLES `makeres` WRITE;
/*!40000 ALTER TABLE `makeres` DISABLE KEYS */;
INSERT INTO `makeres` VALUES (1002,100002,4,2),(1001,100003,3,3),(1001,100004,3,4),(1037,100006,42,6),(NULL,100007,55,7);
/*!40000 ALTER TABLE `makeres` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-11  0:27:00
