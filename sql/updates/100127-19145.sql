ALTER TABLE `auction`  DROP COLUMN `itemType`,  DROP COLUMN `itemId`,  DROP COLUMN `itemObjectId`,  DROP COLUMN `itemQuantity`;
UPDATE `clanhall` SET `price` = 100000000 WHERE `Grade` = 1 AND `price` > 0;
UPDATE `clanhall` SET `price` = 500000000 WHERE `Grade` = 2 AND `price` > 0;
UPDATE `clanhall` SET `price` = 1000000000 WHERE `Grade` = 3 AND `price` > 0;