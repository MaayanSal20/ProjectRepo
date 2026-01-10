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
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
  `reservationTime` datetime NOT NULL,
  `NumOfDin` int NOT NULL,
  `Status` varchar(45) NOT NULL,
  `ResId` int NOT NULL AUTO_INCREMENT,
  `CustomerId` int NOT NULL,
  `arrivalTime` datetime DEFAULT NULL,
  `leaveTime` datetime DEFAULT NULL,
  `createdAt` datetime NOT NULL,
  `source` varchar(20) NOT NULL DEFAULT 'REGULAR',
  `ConfCode` int DEFAULT NULL,
  `TableNum` int DEFAULT NULL,
  PRIMARY KEY (`ResId`),
  KEY `fk_reservation_costumer` (`CustomerId`),
  KEY `idx_res_table_time` (`TableNum`,`reservationTime`),
  CONSTRAINT `fk_res_table` FOREIGN KEY (`TableNum`) REFERENCES `table` (`TableNum`),
  CONSTRAINT `fk_reservation_costumer` FOREIGN KEY (`CustomerId`) REFERENCES `costumer` (`CostumerId`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
INSERT INTO `reservation` VALUES ('2026-01-05 19:30:00',2,'ACTIVE',2,4,'2026-01-05 19:35:00',NULL,'2026-01-05 12:26:00','REGULAR',100002,1),('2026-01-04 20:00:00',4,'DONE',3,3,'2026-01-04 20:05:00','2026-01-04 21:10:00','2026-01-01 12:26:00','REGULAR',100003,NULL),('2026-01-05 18:00:00',3,'CANCELED',4,3,NULL,NULL,'2026-01-05 12:26:00','REGULAR',100004,5),('2026-05-05 18:00:00',5,'ACTIVE',6,42,NULL,NULL,'2026-05-01 18:00:00','REGULAR',100006,6),('2026-01-04 20:00:00',2,'ACTIVE',7,55,NULL,NULL,'2026-01-03 15:22:00','REGULAR',100007,7),('2026-01-05 19:00:00',6,'ACTIVE',8,3,'2026-01-05 19:30:00',NULL,'0226-01-01 00:00:00','REGULAR',100008,8);
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-11  0:26:59
