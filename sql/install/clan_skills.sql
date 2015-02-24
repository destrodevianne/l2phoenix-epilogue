CREATE TABLE IF NOT EXISTS `clan_skills` (
  `clan_id` int NOT NULL default 0,
  `skill_id` smallint unsigned NOT NULL default 0,
  `skill_level` tinyint unsigned NOT NULL default 0,
  `skill_name` varchar(26) default NULL,
  PRIMARY KEY  (`clan_id`,`skill_id`)
) TYPE=MyISAM;
