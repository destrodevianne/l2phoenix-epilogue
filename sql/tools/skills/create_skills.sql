DROP TABLE IF EXISTS `skills`;

CREATE TABLE `skills` (
  `id` int(4) unsigned NOT NULL default '0',
  `level` int(4) unsigned NOT NULL default '0',
  `learn` smallint(6) NOT NULL default '0',
  `name` varchar(100) NOT NULL default '',
  `icon` varchar(100) NOT NULL default '',
  `description` varchar(600) NOT NULL default ' ',
  `operate_type` tinyint(1) NOT NULL default '0',
  `is_magic` tinyint(1) NOT NULL default '0',
  `mp_consume` smallint(5) unsigned NOT NULL default '0',
  `hp_consume` smallint(5) unsigned NOT NULL default '0',
  `cast_range` smallint(5) NOT NULL default '0',
  `hit_time` double unsigned NOT NULL default '0',
  `power` smallint(4) unsigned NOT NULL default '0',
  `effect` smallint(3) unsigned NOT NULL default '0',
  `enchant` varchar(30) NOT NULL default '',
  `reuse` int(10)  NOT NULL default '0',
  PRIMARY KEY  (`id`,`level`)
) ENGINE=MyISAM;

INSERT INTO skills
	SELECT
		N.id AS id,
		N.level AS level,
		'0' AS learn,
		N.name AS name,
		G.icon AS icon,
		N.desc AS description,
		G.optype AS operate_type,
		G.is_magic AS is_magic,
		G.mp AS mp_consume,
		G.hp AS hp_consume,
		G.range AS cast_range,
		G.hit_time AS hit_time,
		N.power AS power,
		N.effect AS effect,
		N.enchant AS enchant,
		0 AS reuse_final
	FROM skillname AS N	LEFT JOIN skillgrp AS G
	ON (N.id = G.id AND N.level = G.level)
	ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `icon`=VALUES(`icon`), `description`=VALUES(`description`), `operate_type`=VALUES(`operate_type`), `is_magic`=VALUES(`is_magic`), `mp_consume`=VALUES(`mp_consume`)
	, `hp_consume`=VALUES(`hp_consume`), `cast_range`=VALUES(`cast_range`), `hit_time`=VALUES(`hit_time`), `power`=VALUES(`power`), `effect`=VALUES(`effect`), `enchant`=VALUES(`enchant`);

ALTER TABLE `skills`  ORDER BY `id`, `level`;


CREATE TEMPORARY TABLE `skills_l` SELECT DISTINCT `skill_id`, `level`, `min_level` FROM `skill_trees`;
ALTER TABLE `skills_l` ORDER BY `skill_id`, `level` ASC;
ALTER TABLE `skills_l` ADD INDEX (`skill_id`), ADD INDEX (`level`);
UPDATE `skills` LEFT JOIN `skills_l` ON (`skills`.`id`=`skills_l`.`skill_id` AND `skills`.`level`=`skills_l`.`level`) SET `skills`.`learn`=`skills_l`.`min_level`;

UPDATE `skills` SET `name` = 'Reward modifier' WHERE `id` = 4417;