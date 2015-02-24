ALTER TABLE `character_skills`
 CHANGE COLUMN `skill_id` `skill_id` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' AFTER `char_obj_id`,
 CHANGE COLUMN `skill_level` `skill_level` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' AFTER `skill_id`,
 CHANGE COLUMN `class_index` `class_index` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' AFTER `skill_name`;