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
-- Table structure for table `opening_hours_weekly`
--

DROP TABLE IF EXISTS `opening_hours_weekly`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opening_hours_weekly` (
  `dayOfWeek` tinyint NOT NULL,
  `openTime` time DEFAULT NULL,
  `closeTime` time DEFAULT NULL,
  `isClosed` tinyint NOT NULL DEFAULT '0',
  `updatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updatedBy` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`dayOfWeek`),
  CONSTRAINT `opening_hours_weekly_chk_1` CHECK ((`dayOfWeek` between 1 and 7)),
  CONSTRAINT `opening_hours_weekly_chk_2` CHECK ((((`isClosed` = 1) and (`openTime` is null) and (`closeTime` is null)) or ((`isClosed` = 0) and (`openTime` is not null) and (`closeTime` is not null) and (`openTime` < `closeTime`))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opening_hours_weekly`
--

LOCK TABLES `opening_hours_weekly` WRITE;
/*!40000 ALTER TABLE `opening_hours_weekly` DISABLE KEYS */;
INSERT INTO `opening_hours_weekly` VALUES (1,'12:00:00','23:00:00',0,'2026-01-09 19:15:09',NULL),(2,'12:00:00','23:00:00',0,'2026-01-09 19:15:09',NULL),(3,'09:00:00','23:00:00',0,'2026-01-13 09:19:29',NULL),(4,'12:00:00','23:00:00',0,'2026-01-09 19:15:09',NULL),(5,'12:00:00','23:00:00',0,'2026-01-09 19:15:09',NULL),(6,'12:00:00','23:00:00',0,'2026-01-09 19:15:09',NULL),(7,'12:00:00','22:00:00',0,'2026-01-11 17:32:18',NULL);
/*!40000 ALTER TABLE `opening_hours_weekly` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-17 12:48:17
