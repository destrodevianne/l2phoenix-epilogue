SET @min=60;

DROP TABLE IF EXISTS temp1;
DROP TABLE IF EXISTS temp2;
DROP TABLE IF EXISTS temp;

CREATE TEMPORARY TABLE `temp`
SELECT `t2`.`ClassId` AS `id`, `t`.`ClassId` AS `id2`, CONCAT(`t2`.`ClassName`,'/',`t`.`ClassName`) AS `name`, 0 AS `num`
FROM `char_templates` `t`
LEFT JOIN `char_templates` `t2` ON `t2`.`ClassId`=`t`.`parent`
WHERE `t`.`level`='3';

CREATE TEMPORARY TABLE temp1
SELECT `t`.`ClassId` AS `id`, count( * ) AS `num`
FROM `character_subclasses` `s`
LEFT JOIN `char_templates` `t` ON `t`.`ClassId` = `s`.`class_id`
WHERE `s`.`level` >= @min AND `t`.`level`='2'
GROUP BY `s`.`class_id`;

UPDATE `temp`
LEFT JOIN `temp1` ON `temp`.`id`=`temp1`.`id`
SET `temp`.`num`=`temp1`.`num`+`temp`.`num`
WHERE `temp1`.`id` IS NOT NULL;

DROP TABLE IF EXISTS temp1;

CREATE TEMPORARY TABLE temp1
SELECT `t`.`ClassId` AS `id`, count( * ) AS `num`
FROM `character_subclasses` `s`
LEFT JOIN `char_templates` `t` ON `t`.`ClassId` = `s`.`class_id`
WHERE `s`.`level` >= @min AND `t`.`level`='3'
GROUP BY `s`.`class_id`;

UPDATE `temp`
LEFT JOIN `temp1` ON `temp`.`id2`=`temp1`.`id`
SET `temp`.`num`=`temp1`.`num`+`temp`.`num`
WHERE `temp1`.`id` IS NOT NULL;

DROP TABLE IF EXISTS temp1;

SELECT `name`,`num` FROM `temp` ORDER BY `num` DESC;