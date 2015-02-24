-- строим сводные таблицы

DROP TABLE IF EXISTS prices;
CREATE TABLE prices SELECT item_id, name, '' AS additional_name, price, weight, crystal_type, crystal_count, crystallizable, item_type, consume_type, class, icon  FROM etcitem;
ALTER TABLE `prices` CHANGE `additional_name` `additional_name` VARCHAR(40) NOT NULL;
ALTER TABLE `prices` ENGINE = MyISAM;
ALTER TABLE `prices` ADD PRIMARY KEY (`item_id`), ADD KEY (`name`), ADD KEY(`price`), ADD KEY(`crystal_type`), ADD KEY(`item_type`), ADD INDEX (`class`);
INSERT INTO prices SELECT item_id, name, additional_name, price, weight, crystal_type, crystal_count, crystallizable, 'weapon', 'normal', 'EQUIPMENT', icon FROM weapon;
INSERT INTO prices SELECT item_id, name, additional_name, price, weight, crystal_type, crystal_count, crystallizable, 'armor', 'normal', 'EQUIPMENT', icon FROM armor;
UPDATE `prices` SET `item_type` = 'none' WHERE `item_type` = '';
ALTER TABLE `prices` CHANGE `item_type` `item_type` ENUM( 'armor', 'arrow', 'bait', 'bolt', 'dye', 'harvest', 'herb', 'lotto', 'material', 'mticket', 'none', 'pet_collar', 'potion', 'quest', 'race_ticket', 'recipe', 'scroll', 'seed', 'shot', 'spellbook', 'weapon' ) NOT NULL;
ALTER TABLE `prices` CHANGE `consume_type` `consume_type` ENUM( 'asset','normal','stackable' ) NOT NULL;
ALTER TABLE `prices` CHANGE `icon` `icon` VARCHAR(64) NOT NULL;
ALTER TABLE `prices` CHANGE `name` `name` VARCHAR(80) NOT NULL;
ALTER TABLE `prices` CHANGE `weight` `weight` SMALLINT(5) UNSIGNED NOT NULL;
ALTER TABLE `prices` ORDER BY `item_id`;

-- апдейты, которые можно применять многократно без побочных действий

UPDATE IGNORE `character_variables` SET `name` = 'KamalokaHall' WHERE `name` = 'LastEnterInstance';
DELETE FROM `character_variables` WHERE `name` = 'LastEnterInstance';
DELETE FROM `character_variables` WHERE `name` = 'HellboundConfidence';
DELETE FROM `character_quests` WHERE `name`='_1003_Valakas';

DELETE FROM `character_variables` WHERE `name` IN ('q211','q212','q213','q214','q215','q216','q217','q218','q219','q220','q221','q222','q223','q224','q225','q226','q227','q228','q229','q230','q231','q232','q233','q281','dd');

UPDATE `items` SET `item_id`=12602 WHERE `item_id`=12609;

DROP TABLE IF EXISTS zone;