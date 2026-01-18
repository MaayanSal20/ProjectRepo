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
-- Table structure for table `costumer`
--

DROP TABLE IF EXISTS `costumer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `costumer` (
  `PhoneNum` varchar(10) NOT NULL,
  `Email` varchar(45) NOT NULL,
  `CostumerId` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`CostumerId`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `costumer`
--

LOCK TABLES `costumer` WRITE;
/*!40000 ALTER TABLE `costumer` DISABLE KEYS */;
INSERT INTO `costumer` VALUES ('0500000000','rep1@bistro.example',1),('0500000001','manager@bistro.example',2),('0530000003','lama3@example.com',3),('0530000004','hala4@example.com',4),('0530000005','sara5@example.com',5),('0530000006','rona6@example.com',6),('0530000007','aya7@example.com',7),('0530000008','katreen8@example.com',8),('0530000009','noam9@example.com',9),('0530000010','omer10@example.com',10),('0530000011','maya11@example.com',11),('0530000012','yossi12@example.com',12),('0530000013','dana13@example.com',13),('0530000014','nour14@example.com',14),('0530000015','amir15@example.com',15),('0530000016','lior16@example.com',16),('0530000017','yara17@example.com',17),('0530000018','adam18@example.com',18),('0530000019','moran19@example.com',19),('0530000020','shani20@example.com',20),('0530000021','eden21@example.com',21),('0530000022','itay22@example.com',22),('0530000023','rami23@example.com',23),('0530000024','sahar24@example.com',24),('0530000025','lina25@example.com',25),('0530000026','tamar26@example.com',26),('0530000027','ofir27@example.com',27),('0530000028','yael28@example.com',28),('0530000029','nadav29@example.com',29),('0530000030','gal30@example.com',30),('0530000031','yasmin31@example.com',31),('0530000032','roi32@example.com',32),('0548120148','',34);
/*!40000 ALTER TABLE `costumer` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-18  2:35:04
