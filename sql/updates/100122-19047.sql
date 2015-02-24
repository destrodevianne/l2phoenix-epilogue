truncate table forums;
truncate table topic;
truncate table posts;

ALTER TABLE `forums`  ADD INDEX `forum_id_parent` (`forum_id`, `forum_parent`);
ALTER TABLE `topic`  ADD INDEX `topic_forum_id` (`topic_forum_id`);

INSERT IGNORE INTO `forums` VALUES (1, 'NormalRoot', 0, 0, 0, 1, 0);
INSERT IGNORE INTO `forums` VALUES (2, 'ClanRoot', 0, 0, 0, 0, 0);
INSERT IGNORE INTO `forums` VALUES (3, 'MemoRoot', 0, 0, 0, 0, 0);
INSERT IGNORE INTO `forums` VALUES (4, 'MailRoot', 0, 0, 0, 0, 0);