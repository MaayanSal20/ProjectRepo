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
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `costumer`
--

LOCK TABLES `costumer` WRITE;
/*!40000 ALTER TABLE `costumer` DISABLE KEYS */;
INSERT INTO `costumer` VALUES ('0500000000','agent@test.com',1),('0500000001','manager@test.com',2),('0543493399','hala@gmail.com',3),('0543602520','halaabo11@gmail.com',4),('0512345678','sw@gmail.com',6),('0512345679','saraa@gmail.com',8),('0543493322','bla@braude.ac.il',9),('0512345670','hal@gmail.com',10),('0543493333','haha@gm.com',14),('0511111111','sara@gmail.com',15),('0522222222','hi@gmail.com',20),('0511111112','he@g.com',21),('0500000003','fi@g.com',22),('0533333334','ji@g.com',23),('0587654321','oi@gm.com',24),('0585274163','q@g.com',28),('0512365487','r@g.com',29),('0543493320','hala.aboresh19@gmail.com',30),('0549273060','saraaboreesh1@gmail.com',31),('0544048112','gwdbwrys9@gmail.com',32),('0514736258','d@g.com',33),('0512365488','t@g.com',34),('0524242424','aboreshhala@gmail.com',42),('0545859439','rona.aboabla@gmail.com',43),('0545859437','rona.aboabla12@gmail.com',44),('0543728946','ayash532003@gmail.com',46),('0583628442','katreengo176@gmail.com',47),('0589898989','ha@g.com',48),('0555555555','s@g.com',49),('0544444444','sw@g.com',50),('0548888999','po@g.il',51),('0522654686','hh@g.il',52),('0512121212','d@g.com',55),('0568245455','hr@g.com',56),('0511425562','re@g.il',57),('0547639128','jo@g.co',58),('0547639129','jo@g.il',59),('0546244878','aboreshhala@gmail.com',60),('0586696515','hala.aboresh19@gmail.com',61);
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

-- Dump completed on 2026-01-12  1:58:58
