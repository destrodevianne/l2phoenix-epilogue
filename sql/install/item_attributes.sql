CREATE TABLE IF NOT EXISTS `item_attributes` (
  `itemId` int NOT NULL DEFAULT 0,
  `augAttributes` int NOT NULL DEFAULT -1,
  `augSkillId` int NOT NULL DEFAULT -1,
  `augSkillLevel` int NOT NULL DEFAULT -1,
  `elemType` tinyint NOT NULL DEFAULT -1,
  `elemValue` int NOT NULL DEFAULT -1,
  PRIMARY KEY (`itemId`)
) TYPE=MyISAM;