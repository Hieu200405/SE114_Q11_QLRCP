-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
-- Host: 127.0.0.1    Database: filmmanagement
-- ------------------------------------------------------
-- Server version       9.3.0

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
-- Table structure for table `cinema`
--
DROP TABLE IF EXISTS `cinema`;
CREATE TABLE `cinema` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `address` varchar(500) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `description` text,
  `is_delete` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `cinema`
--
LOCK TABLES `cinema` WRITE;
INSERT INTO `cinema` VALUES 
(1,'CGV Vincom Đồng Khởi','72 Lê Thánh Tôn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh',10.776889,106.701686,'1900 6017','https://www.cgv.vn/media/site/cache/3/980x415/b58515f018eb873dafa430b6f9ae0c1e/c/i/cinema_vincom_dongkhoi_h1_0.jpg','Rạp chiếu phim CGV tại Vincom Center Đồng Khởi - Trung tâm Quận 1, TP.HCM',0),
(2,'CGV Aeon Mall Tân Phú','30 Bờ Bao Tân Thắng, Phường Sơn Kỳ, Quận Tân Phú, TP. Hồ Chí Minh',10.801973,106.618227,'1900 6017','https://www.cgv.vn/media/site/cache/3/980x415/b58515f018eb873dafa430b6f9ae0c1e/a/e/aeon_tanphu_cinema_2.jpg','Rạp chiếu phim CGV tại Aeon Mall Tân Phú',0),
(3,'Galaxy Nguyễn Du','116 Nguyễn Du, Phường Bến Thành, Quận 1, TP. Hồ Chí Minh',10.772321,106.693084,'1900 2224','https://www.galaxycine.vn/media/2020/5/6/nguyen-du-1_1588758449046.jpg','Rạp chiếu phim Galaxy Cinema Nguyễn Du - Trung tâm Quận 1',0),
(4,'Lotte Cinema Nam Sài Gòn','469 Nguyễn Hữu Thọ, Phường Tân Hưng, Quận 7, TP. Hồ Chí Minh',10.722065,106.699074,'1900 6017','https://www.lottecinemavn.com/LCHS/Image/Theater/2016/08/17/Nam%20Sai%20Gon_1.jpg','Rạp chiếu phim Lotte Cinema tại Lotte Mart Nam Sài Gòn',0),
(5,'BHD Star Vincom Thảo Điền','159 Xa Lộ Hà Nội, Phường Thảo Điền, Quận 2, TP. Hồ Chí Minh',10.803100,106.740677,'1900 2099','https://bhdstar.vn/wp-content/uploads/2019/09/thao-dien-2.jpg','Rạp chiếu phim BHD Star tại Vincom Mega Mall Thảo Điền',0);
UNLOCK TABLES;

--
-- Table structure for table `room` (updated)
--
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `seats` int DEFAULT NULL,
  `is_delete` tinyint(1) DEFAULT NULL,
  `cinema_id` int DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `cinema_id` (`cinema_id`),
  CONSTRAINT `room_ibfk_1` FOREIGN KEY (`cinema_id`) REFERENCES `cinema` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `room` (cinema_id set NULL for old data)
--
LOCK TABLES `room` WRITE;
INSERT INTO `room` VALUES (1,'Room 55',25,1,NULL),(2,'Room 8',35,0,NULL),(3,'Room 1',26,0,NULL),(4,'Room 22',30,1,NULL),(5,'yasua',40,1,NULL),(6,'leesin',25,1,NULL),(7,'huypro',35,0,NULL),(8,'room 12',45,0,NULL),(9,'room 6',30,0,NULL),(10,'room 7',38,0,NULL),(11,'room 13',70,0,NULL);
UNLOCK TABLES;

--
-- (Các bảng khác giữ nguyên như cũ, chỉ cần copy từ file backup_data.sql vào sau đoạn này)
