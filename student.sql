/*
 Navicat Premium Dump SQL

 Source Server         : mysql8020
 Source Server Type    : MySQL
 Source Server Version : 80020 (8.0.20)
 Source Host           : localhost:3306
 Source Schema         : study

 Target Server Type    : MySQL
 Target Server Version : 80020 (8.0.20)
 File Encoding         : 65001

 Date: 03/07/2026 12:11:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `grade` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of student
-- ----------------------------
BEGIN;
INSERT INTO `student` (`id`, `name`, `sex`, `grade`) VALUES (1, '小米', '男', '5');
INSERT INTO `student` (`id`, `name`, `sex`, `grade`) VALUES (2, '小明', '男', '6');
INSERT INTO `student` (`id`, `name`, `sex`, `grade`) VALUES (3, '小红', '女', '6');
INSERT INTO `student` (`id`, `name`, `sex`, `grade`) VALUES (4, '小强', '男', '1');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
