-- MySQL dump 10.13  Distrib 8.0.31, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: friend
-- ------------------------------------------------------
-- Server version	8.0.31

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `postId` bigint NOT NULL COMMENT '帖子id',
  `createUserId` bigint NOT NULL COMMENT '创建人id',
  `content` varchar(512) DEFAULT NULL COMMENT '评论内容',
  `hot` tinyint NOT NULL DEFAULT '0' COMMENT '热度 0-普通 1-热评',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (1,4,2,'这是本网站的第一条评论',0,'2024-09-08 09:19:30','2024-09-08 09:19:30',0),(4,7,2,'一楼是我的',0,'2024-10-22 19:36:18','2024-10-22 19:36:18',0),(5,4,2,'哈哈哈哈哈，我是哈里斯',0,'2024-10-22 19:37:36','2024-10-22 19:37:36',0),(6,7,2,'哈哈哈哈哈',0,'2024-10-22 20:30:02','2024-10-22 20:30:02',0);
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_answer`
--

DROP TABLE IF EXISTS `comment_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `commentId` bigint NOT NULL COMMENT '评论id',
  `createUserId` bigint NOT NULL COMMENT '创建人id',
  `content` varchar(512) DEFAULT NULL COMMENT '回复内容',
  `hot` tinyint NOT NULL DEFAULT '0' COMMENT '热度 0-普通 1-热评',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '回复类型 0-普通 1-二级回复',
  `toAnswerId` bigint DEFAULT NULL COMMENT '如果是二级回复， 回复的answerId',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论回复表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_answer`
--

LOCK TABLES `comment_answer` WRITE;
/*!40000 ALTER TABLE `comment_answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(128) DEFAULT NULL COMMENT '帖子标题',
  `content` varchar(2048) DEFAULT NULL COMMENT '帖子内容',
  `category` int DEFAULT '0' COMMENT '帖子分类',
  `createUserId` bigint NOT NULL COMMENT '创建人id',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `likeCount` int NOT NULL DEFAULT '0',
  `commentCount` int NOT NULL DEFAULT '0',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post`
--

LOCK TABLES `post` WRITE;
/*!40000 ALTER TABLE `post` DISABLE KEYS */;
INSERT INTO `post` VALUES (4,NULL,'这是本网站的第一个帖子',0,2,'2024-09-08 09:19:05','2024-10-22 20:05:41',1,1,0),(5,NULL,'震惊！某东大2150杨某竟干出这种事，这是人性的扭曲还是道德的沦丧？',0,602015,'2024-09-08 09:20:42','2024-10-22 20:05:41',0,2,0),(7,NULL,'胆大党是十月唯一真神',0,2,'2024-10-22 15:35:47','2024-10-22 15:35:47',1,1,0);
/*!40000 ALTER TABLE `post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_thumb`
--

DROP TABLE IF EXISTS `post_thumb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_thumb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint DEFAULT NULL COMMENT '用户id',
  `postId` bigint DEFAULT NULL COMMENT '帖子id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1092 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_thumb`
--

LOCK TABLES `post_thumb` WRITE;
/*!40000 ALTER TABLE `post_thumb` DISABLE KEYS */;
INSERT INTO `post_thumb` VALUES (1090,2,4),(1091,2,7);
/*!40000 ALTER TABLE `post_thumb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(256) NOT NULL COMMENT '队伍名称',
  `description` varchar(1024) DEFAULT NULL COMMENT '队伍描述',
  `maxNum` int DEFAULT '1' COMMENT '队伍的最大人数',
  `userId` bigint NOT NULL COMMENT '创建人id',
  `expireTime` datetime DEFAULT NULL COMMENT '队伍过期时间',
  `status` int DEFAULT '0' COMMENT '队伍类型 0 - 公开， 1 - 私密， 2 - 加密',
  `password` varchar(512) DEFAULT NULL COMMENT '加密队伍的密码',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='队伍';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team`
--

LOCK TABLES `team` WRITE;
/*!40000 ALTER TABLE `team` DISABLE KEYS */;
INSERT INTO `team` VALUES (2,'蓝桥杯小队','想要参加蓝桥杯的都进来',6,2,NULL,0,'','2024-09-05 14:42:39','2024-09-05 14:42:39',0),(3,'蓝桥杯小队','想要参加蓝桥杯的都进来',6,2,NULL,0,'','2024-09-08 19:44:10','2024-09-08 19:44:22',1),(4,'蓝桥杯小队','想要参加蓝桥杯的都进来',5,602015,NULL,0,'','2024-10-24 12:11:38','2024-10-24 12:11:38',0),(5,'acm 小队','想要学习算法就进来',2,602015,NULL,0,'','2024-10-24 12:12:04','2024-10-24 12:12:04',0);
/*!40000 ALTER TABLE `team` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(256) DEFAULT NULL COMMENT '用户昵称',
  `userDescription` varchar(512) DEFAULT NULL COMMENT '用户描述',
  `userAccount` varchar(256) DEFAULT NULL COMMENT '账号',
  `avatarUrl` varchar(1024) DEFAULT NULL COMMENT '用户头像',
  `gender` tinyint DEFAULT NULL COMMENT ' 性别 0 - 男  1 - 女',
  `secretKey` varchar(256) NOT NULL,
  `userPassword` varchar(512) NOT NULL COMMENT '密码',
  `qq` varchar(64) DEFAULT NULL COMMENT 'qq号',
  `phone` varchar(128) DEFAULT NULL COMMENT '电话',
  `email` varchar(512) DEFAULT NULL COMMENT '邮箱',
  `userStatus` int NOT NULL DEFAULT '0' COMMENT '状态 0 - 正常',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  `userRole` int NOT NULL DEFAULT '0' COMMENT '用户角色 0 - 普通用户 1 - 管理员',
  `tags` varchar(1024) DEFAULT NULL COMMENT '标签 json 数组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=602016 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'yupi1234','我爱吃鱼，所以我叫鱼皮','yupi','https://web-friend-sheephappy.oss-cn-hangzhou.aliyuncs.com/a36e4121-6574-4014-a04c-ed6c1b556fd7_20221114215948_1cf96.thumb.400_0.jpeg',0,'','b0dd3697a192885d7c055db46155b26a','12345678','13180148958','2870@yupi.com',0,'2024-07-26 10:51:35','2024-08-17 23:10:02',0,0,'[\"movies\",\"男\",\"java\"]'),(2,'sheephappy01','我要看一辈子的少女乐队动画','sheephappy','http://web-friend-sheephappy.oss-cn-hangzhou.aliyuncs.com/968c6fd4-30d7-4061-abb3-0fec7f94ffc2_%E5%B0%8F%E6%B3%A2%E5%A5%87.jpg',0,'79518a1cc45ec8aa420d2b694cbcda3ada15be76d5078c67812af0b9c48efb8212a44b19541d3c824eb9a0d4f141c4d9e16bc0d7531daf2e577654db40404997','da34826c9b682c5cf480c2d8086f5d73','2870839006','',NULL,0,'2024-08-13 17:35:12','2024-10-22 15:24:53',0,0,'[\"python\",\"movies\",\"art\",\"gardening\"]'),(3,'Alice','Loves music and traveling.','alice123','https://picture.gptkong.com/20240729/20585eb5ece1594408bd6526c5004906bb.jpeg',1,'','b0dd3697a192885d7c055db46155b26a',NULL,'+1234567890','alice@example.com',0,'2024-07-29 20:43:35','2024-08-05 17:11:23',0,0,'[\"sports\", \"movies\", \"男\"]'),(4,'Bob','Enjoys playing sports and watching movies.','bob123','https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0',0,'','b0dd3697a192885d7c055db46155b26a',NULL,'+0987654321','bob@example.com',0,'2024-07-29 20:43:35','2024-08-05 17:04:20',0,0,'[\"sports\", \"java\", \"男\"]'),(5,'Charlie567','Likes reading books and taking photos.','charlie123','https://picture.gptkong.com/20240729/20585eb5ece1594408bd6526c5004906bb.jpeg',1,'79518a1cc45ec8aa420d2b694cbcda3ada15be76d5078c67812af0b9c48efb8212a44b19541d3c824eb9a0d4f141c4d9e16bc0d7531daf2e577654db40404997','da34826c9b682c5cf480c2d8086f5d73',NULL,'+1112223333','charlie@example.com',0,'2024-07-29 20:43:35','2024-10-18 10:05:20',0,0,'[\"reading\",\"sports\",\"java\",\"足球\"]'),(6,'Diana','Passionate about cooking and exploring nature.','diana123','https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0',0,'','b0dd3697a192885d7c055db46155b26a',NULL,'+4445556666','diana@example.com',0,'2024-07-29 20:43:35','2024-08-05 17:04:20',0,0,'[\"movies\", \"hiking\", \"男\"]'),(7,'Eve','Art enthusiast and loves gardening.','eve123','https://picture.gptkong.com/20240729/20585eb5ece1594408bd6526c5004906bb.jpeg',1,'','b0dd3697a192885d7c055db46155b26a',NULL,'+2223334444','eve@example.com',0,'2024-07-29 20:43:35','2024-07-29 21:13:47',0,0,'[\"art\", \"gardening\", \"女\"]'),(8,'fakeUser','我不是小黑子 我不是小黑子 我不是小黑子','fakeUsre1','https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0',0,'','b0dd3697a192885d7c055db46155b26a',NULL,'12345678','eve@example.com',0,'2024-07-31 17:17:48','2024-10-15 15:06:05',0,0,'[\"reading\", \"photography\", \"java\", \"女\"]'),(9,'fakeUser','我不是小黑子 我不是小黑子 我不是小黑子','fakeUsre2','https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0',0,'','b0dd3697a192885d7c055db46155b26a',NULL,'12345678','eve@example.com',0,'2024-07-31 17:17:48','2024-10-15 15:06:05',0,0,'[\"reading\", \"photography\", \"java\", \"女\"]'),(10,'fakeUser','我不是小黑子 我不是小黑子 我不是小黑子','fakeUsre','https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0',0,'','b0dd3697a192885d7c055db46155b26a',NULL,'12345678','eve@example.com',0,'2024-07-31 17:17:48','2024-10-15 15:06:05',0,0,'[\"reading\", \"photography\", \"java\", \"女\"]'),(602015,'fakeUser',NULL,'testMan','http://web-friend-sheephappy.oss-cn-hangzhou.aliyuncs.com/32e1a0bb-3d53-431e-8f50-caf68086fdc2_%E4%B8%9C%E5%8C%97%E5%A4%A7%E5%AD%A6%E6%A0%A1%E5%BE%BD.png?Expires=1729746647&OSSAccessKeyId=LTAI5tRGVySSiTwHkQE6R9ib&Signature=UWQ8lBR3HORANy%2FJVexfPCh1ReI%3D',NULL,'79518a1cc45ec8aa420d2b694cbcda3ada15be76d5078c67812af0b9c48efb8212a44b19541d3c824eb9a0d4f141c4d9e16bc0d7531daf2e577654db40404997','da34826c9b682c5cf480c2d8086f5d73',NULL,NULL,NULL,0,'2024-08-27 20:20:42','2024-10-24 12:10:47',0,0,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_team`
--

DROP TABLE IF EXISTS `user_team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_team` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint DEFAULT NULL COMMENT '用户id',
  `teamId` bigint DEFAULT NULL COMMENT '队伍id',
  `joinTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '用户加入队伍时间',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-队伍关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_team`
--

LOCK TABLES `user_team` WRITE;
/*!40000 ALTER TABLE `user_team` DISABLE KEYS */;
INSERT INTO `user_team` VALUES (1,602014,1,'2024-09-05 14:39:40','2024-09-05 14:39:40','2024-09-05 14:39:40',0),(2,2,1,'2024-09-05 14:39:56','2024-09-05 14:39:56','2024-09-05 14:39:56',0),(3,2,2,'2024-09-05 14:42:39','2024-09-05 14:42:39','2024-09-05 14:42:39',0),(4,602014,2,'2024-09-05 14:43:03','2024-09-05 14:43:03','2024-09-05 14:43:03',0),(5,2,3,'2024-09-08 19:44:10','2024-09-08 19:44:10','2024-09-08 19:44:22',1),(6,602015,2,'2024-10-24 11:44:55','2024-10-24 11:44:54','2024-10-24 11:44:54',0),(7,602015,4,'2024-10-24 12:11:38','2024-10-24 12:11:38','2024-10-24 12:11:38',0),(8,602015,5,'2024-10-24 12:12:04','2024-10-24 12:12:04','2024-10-24 12:12:04',0),(9,2,4,'2024-10-24 12:14:42','2024-10-24 12:14:41','2024-10-24 12:14:41',0);
/*!40000 ALTER TABLE `user_team` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-12-13 22:31:13
