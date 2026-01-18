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
-- Table structure for table `waitinglist`
--

DROP TABLE IF EXISTS `waitinglist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waitinglist` (
  `WaitId` int NOT NULL AUTO_INCREMENT,
  `ConfirmationCode` int NOT NULL,
  `timeEnterQueue` datetime NOT NULL,
  `NumberOfDiners` int NOT NULL,
  `costumerId` int NOT NULL,
  `notifiedAt` datetime DEFAULT NULL,
  `acceptedAt` datetime DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'WAITING',
  `ResId` int DEFAULT NULL,
  `priority` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`WaitId`),
  KEY `fk_waitinglist_costumer` (`costumerId`),
  KEY `fk_waitlist_res` (`ResId`),
  KEY `idx_waitinglist_confcode` (`ConfirmationCode`),
  KEY `idx_waiting_status_time` (`status`,`timeEnterQueue`),
  KEY `idx_waiting_resid` (`ResId`),
  KEY `idx_waiting_pick` (`status`,`priority`,`timeEnterQueue`,`NumberOfDiners`),
  CONSTRAINT `fk_wait_confcode` FOREIGN KEY (`ConfirmationCode`) REFERENCES `conf_codes` (`code`),
  CONSTRAINT `fk_waitinglist_costumer` FOREIGN KEY (`costumerId`) REFERENCES `costumer` (`CostumerId`),
  CONSTRAINT `fk_waitlist_res` FOREIGN KEY (`ResId`) REFERENCES `reservation` (`ResId`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waitinglist`
--

LOCK TABLES `waitinglist` WRITE;
/*!40000 ALTER TABLE `waitinglist` DISABLE KEYS */;
INSERT INTO `waitinglist` VALUES (1,600021,'2026-01-03 10:43:00',6,12,'2026-01-03 10:49:00','2026-01-03 10:59:00','ACCEPTED',20,1),(2,600046,'2026-02-14 11:37:00',4,10,'2026-02-14 11:30:00','2026-02-14 11:40:00','ACCEPTED',45,1),(3,600018,'2025-12-27 19:42:00',2,4,'2025-12-27 19:30:00','2025-12-27 19:40:00','ACCEPTED',17,1),(4,600024,'2026-01-06 14:55:00',4,23,NULL,NULL,'CANCELLED',23,0),(5,600009,'2025-12-13 15:16:00',6,1,'2025-12-13 15:39:00','2025-12-13 15:41:00','ACCEPTED',8,0),(6,600030,'2026-01-20 18:59:00',2,11,'2026-01-20 19:15:00','2026-01-20 19:17:00','ACCEPTED',29,0),(7,600047,'2026-01-14 10:55:00',6,11,NULL,NULL,'EXPIRED',NULL,0),(8,600048,'2026-01-15 08:36:00',5,5,NULL,NULL,'EXPIRED',NULL,1),(9,600049,'2025-12-29 06:02:00',8,1,NULL,NULL,'WAITING',NULL,0),(10,600050,'2026-01-03 07:15:00',3,19,NULL,NULL,'WAITING',NULL,0),(11,600051,'2026-01-01 07:01:00',2,25,NULL,NULL,'EXPIRED',NULL,0),(12,600052,'2026-01-17 08:18:00',5,4,NULL,NULL,'EXPIRED',NULL,0);
/*!40000 ALTER TABLE `waitinglist` ENABLE KEYS */;
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
