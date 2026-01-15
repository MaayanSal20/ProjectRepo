-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: schema_for_project
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
) ENGINE=InnoDB AUTO_INCREMENT=231 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `costumer`
--

LOCK TABLES `costumer` WRITE;
/*!40000 ALTER TABLE `costumer` DISABLE KEYS */;
INSERT INTO `costumer` VALUES ('0500000000','agent@test.com',1),('0500000001','manager@test.com',2),('0543493399','hala@gmail.com',3),('0543602520','halaabo11@gmail.com',4),('0512345678','sw@gmail.com',6),('0512345679','saraa@gmail.com',8),('0543493322','bla@braude.ac.il',9),('0512345670','hal@gmail.com',10),('0543493333','haha@gm.com',14),('0511111111','sara@gmail.com',15),('0522222222','hi@gmail.com',20),('0511111112','he@g.com',21),('0500000003','fi@g.com',22),('0533333334','ji@g.com',23),('0587654321','oi@gm.com',24),('0585274163','q@g.com',28),('0512365487','r@g.com',29),('0543493320','hala.aboresh19@gmail.com',30),('0549273060','saraaboreesh1@gmail.com',31),('0544048112','gwdbwrys9@gmail.com',32),('0514736258','d@g.com',33),('0512365488','t@g.com',34),('0524242424','aboreshhala@gmail.com',42),('0545859439','rona.aboabla@gmail.com',43),('0545859437','rona.aboabla12@gmail.com',44),('0543728946','ayash532003@gmail.com',46),('0583628442','katreengo176@gmail.com',47),('0589898989','ha@g.com',48),('0555555555','s@g.com',49),('0544444444','sw@g.com',50),('0548888999','po@g.il',51),('0522654686','hh@g.il',52),('0512121212','d@g.com',55),('0568245455','hr@g.com',56),('0511425562','re@g.il',57),('0547639128','jo@g.co',58),('0547639129','jo@g.il',59),('0546244878','aboreshhala@gmail.com',60),('0586696515','hala.aboresh19@gmail.com',61),('0526762845','katreengo176@gmail.com',62),('0523161436','lamaeb14@gmail.com',63),('','katreengo176@gmail.com',64),('0536249552','h@gmail.com',65),('0542525252','h@g.com',68),('0523658999','g@g.il',69),('0563524169','Ha@gml.com',72),('0549685743','r@g.co',73),('0544444444','j@g.com',74),('056987412','in@g.com',75),('0546392845','insh@g.com',76),('0555555519','unsh@g.com',77),('0543698526','insh@g.com',78),('052356362','insh@g.il',79),('0547896325','h@g.il',80),('0563214789','insh@gm.com',81),('0543265549','in@g.com',82),('0523568956','ha@g.com',83),('0500000001','customer1@mail.com',84),('0500000002','customer2@mail.com',85),('0500000003','customer3@mail.com',86),('0500000004','customer4@mail.com',87),('0500000005','customer5@mail.com',88),('0500000006','customer6@mail.com',89),('0500000007','customer7@mail.com',90),('0500000008','customer8@mail.com',91),('0500000009','customer9@mail.com',92),('0500000010','customer10@mail.com',93),('0500000011','customer11@mail.com',94),('0500000012','customer12@mail.com',95),('0500000013','customer13@mail.com',96),('0500000014','customer14@mail.com',97),('0500000015','customer15@mail.com',98),('0500000016','customer16@mail.com',99),('0500000017','customer17@mail.com',100),('0500000018','customer18@mail.com',101),('0500000019','customer19@mail.com',102),('0500000020','customer20@mail.com',103),('0520000001','newcust520000001@mail.com',104),('0520000002','newcust520000002@mail.com',105),('0520000003','newcust520000003@mail.com',106),('0520000004','newcust520000004@mail.com',107),('0520000005','newcust520000005@mail.com',108),('0520000006','newcust520000006@mail.com',109),('0520000007','newcust520000007@mail.com',110),('0520000008','newcust520000008@mail.com',111),('0520000009','newcust520000009@mail.com',112),('0520000010','newcust520000010@mail.com',113),('0520000011','newcust520000011@mail.com',114),('0520000012','newcust520000012@mail.com',115),('0520000013','newcust520000013@mail.com',116),('0520000014','newcust520000014@mail.com',117),('0520000015','newcust520000015@mail.com',118),('0520000016','newcust520000016@mail.com',119),('0520000017','newcust520000017@mail.com',120),('0520000018','newcust520000018@mail.com',121),('0520000019','newcust520000019@mail.com',122),('0520000020','newcust520000020@mail.com',123),('0520000021','newcust520000021@mail.com',124),('0520000022','newcust520000022@mail.com',125),('0520000023','newcust520000023@mail.com',126),('0520000024','newcust520000024@mail.com',127),('0520000025','newcust520000025@mail.com',128),('0520000026','newcust520000026@mail.com',129),('0520000027','newcust520000027@mail.com',130),('0520000028','newcust520000028@mail.com',131),('0520000029','newcust520000029@mail.com',132),('0520000030','newcust520000030@mail.com',133),('0520000031','newcust520000031@mail.com',134),('0520000032','newcust520000032@mail.com',135),('0520000033','newcust520000033@mail.com',136),('0520000034','newcust520000034@mail.com',137),('0520000035','newcust520000035@mail.com',138),('0520000036','newcust520000036@mail.com',139),('0520000037','newcust520000037@mail.com',140),('0520000038','newcust520000038@mail.com',141),('0520000039','newcust520000039@mail.com',142),('0520000040','newcust520000040@mail.com',143),('0520000041','newcust520000041@mail.com',144),('0520000042','newcust520000042@mail.com',145),('0520000043','newcust520000043@mail.com',146),('0520000044','newcust520000044@mail.com',147),('0520000045','newcust520000045@mail.com',148),('0520000046','newcust520000046@mail.com',149),('0520000047','newcust520000047@mail.com',150),('0520000048','newcust520000048@mail.com',151),('0520000049','newcust520000049@mail.com',152),('0520000050','newcust520000050@mail.com',153),('0520000051','newcust520000051@mail.com',154),('0520000052','newcust520000052@mail.com',155),('0520000053','newcust520000053@mail.com',156),('0520000054','newcust520000054@mail.com',157),('0520000055','newcust520000055@mail.com',158),('0520000056','newcust520000056@mail.com',159),('0520000057','newcust520000057@mail.com',160),('0520000058','newcust520000058@mail.com',161),('0520000059','newcust520000059@mail.com',162),('0520000060','newcust520000060@mail.com',163),('0520000061','newcust520000061@mail.com',164),('0520000062','newcust520000062@mail.com',165),('0520000063','newcust520000063@mail.com',166),('0520000064','newcust520000064@mail.com',167),('0520000065','newcust520000065@mail.com',168),('0520000066','newcust520000066@mail.com',169),('0520000067','newcust520000067@mail.com',170),('0520000068','newcust520000068@mail.com',171),('0520000069','newcust520000069@mail.com',172),('0520000070','newcust520000070@mail.com',173),('0520000071','newcust520000071@mail.com',174),('0520000072','newcust520000072@mail.com',175),('0520000073','newcust520000073@mail.com',176),('0520000074','newcust520000074@mail.com',177),('0520000075','newcust520000075@mail.com',178),('0520000076','newcust520000076@mail.com',179),('0520000077','newcust520000077@mail.com',180),('0520000078','newcust520000078@mail.com',181),('0520000079','newcust520000079@mail.com',182),('0520000080','newcust520000080@mail.com',183),('0520000081','newcust520000081@mail.com',184),('0520000082','newcust520000082@mail.com',185),('0520000083','newcust520000083@mail.com',186),('0520000084','newcust520000084@mail.com',187),('0520000085','newcust520000085@mail.com',188),('0520000086','newcust520000086@mail.com',189),('0520000087','newcust520000087@mail.com',190),('0520000088','newcust520000088@mail.com',191),('0520000089','newcust520000089@mail.com',192),('0520000090','newcust520000090@mail.com',193),('0520000091','newcust520000091@mail.com',194),('0520000092','newcust520000092@mail.com',195),('0520000093','newcust520000093@mail.com',196),('0520000094','newcust520000094@mail.com',197),('0520000095','newcust520000095@mail.com',198),('0520000096','newcust520000096@mail.com',199),('0520000097','newcust520000097@mail.com',200),('0520000098','newcust520000098@mail.com',201),('0520000099','newcust520000099@mail.com',202),('0520000100','newcust520000100@mail.com',203);
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

-- Dump completed on 2026-01-16  0:18:55
