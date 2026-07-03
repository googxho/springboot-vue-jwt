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

 Date: 03/07/2026 12:11:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `desc` varchar(255) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of book
-- ----------------------------
BEGIN;
INSERT INTO `book` (`id`, `title`, `desc`, `price`) VALUES (1, '《西游记》', '西游记以民间传说的唐僧取经的故事和有关话本及杂剧（元末明初杨讷作）基础上创作而成。', 100.00);
INSERT INTO `book` (`id`, `title`, `desc`, `price`) VALUES (2, '《红楼梦》', '《红楼梦》是一部章回体长篇小说。早期仅有前八十回抄本流传，八十回后部分未完成且原稿佚失。原名《脂砚斋重评石头记》。程伟元邀请高鹗协同整理出版百二十回全本，定名《红楼梦》。亦有版本作《金玉缘》。', 120.00);
INSERT INTO `book` (`id`, `title`, `desc`, `price`) VALUES (3, '《水浒传》', '《水浒传》的故事源起于北宋宣和年间，出现了话本《大宋宣和遗事》描述了宋江、吴加亮（吴用）、晁盖等36人起义造反的故事，成为《水浒传》的蓝本。', 100.00);
INSERT INTO `book` (`id`, `title`, `desc`, `price`) VALUES (4, '《三国演义》', '《三国演义》是综合民间传说和戏曲、话本，结合陈寿的《三国志》、范晔《后汉书》、元代《三国志平话》、和裴松之注的史料，以及作者个人对社会人生的体悟写成。现所见刊本以明嘉靖本最早，分24卷，240则。清初毛宗岗父子又做了一些修改，并成为现在最常见的120回本。', 120.00);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
