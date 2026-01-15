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
  PRIMARY KEY (`subscriberId`),
  KEY `fk_subscriber_costumer` (`CostumerId`),
  CONSTRAINT `fk_subscriber_costumer` FOREIGN KEY (`CostumerId`) REFERENCES `costumer` (`CostumerId`)
) ENGINE=InnoDB AUTO_INCREMENT=1050 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriber`
--

LOCK TABLES `subscriber` WRITE;
/*!40000 ALTER TABLE `subscriber` DISABLE KEYS */;
INSERT INTO `subscriber` VALUES ('0543493399','hala',1001,3),('0543602520','hala',1002,4),('0512345678','swsan',1004,6),('0512345679','saraa',1006,8),('0543493322','bla',1007,9),('0512345670','hal',1008,10),('0543493333','haha',1009,14),('0511111111','sara',1010,15),('0522222222','hi',1015,20),('0511111112','he',1016,21),('0500000003','fi',1017,22),('0533333334','ji',1018,23),('0587654321','oi',1019,24),('0585274163','qq',1023,28),('0512365487','rr',1024,29),('0543493320','hala',1025,30),('0549273060','saraa',1026,31),('0544048112','jawad',1027,32),('0514736258','dd',1028,33),('0512365488','ee',1029,34),('0524242424','hala',1037,42),('0545859439','rona',1038,43),('0545859437','rona',1039,44),('0543728946','aya',1041,46),('0583628442','katreen',1042,47),('0589898989','ha',1043,48),('0555555555','sa',1044,49),('0544444444','sw',1045,50),('0548888999','po',1046,51),('0522654686','hh',1047,52),('0568245455','hr',1048,56),('0511425562','re',1049,57);
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

-- Dump completed on 2026-01-15 17:15:28
