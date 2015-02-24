ALTER TABLE `clanhall`  CHANGE COLUMN `price` `price` BIGINT(20) NOT NULL DEFAULT '0' AFTER `Grade`;
ALTER TABLE `auction`  CHANGE COLUMN `itemQuantity` `itemQuantity` BIGINT(20) NOT NULL DEFAULT '0' AFTER `itemName`;
ALTER TABLE `auction`  CHANGE COLUMN `startingBid` `startingBid` BIGINT(20) NOT NULL DEFAULT '0' AFTER `itemQuantity`;
ALTER TABLE `auction`  CHANGE COLUMN `currentBid` `currentBid` BIGINT(20) NOT NULL DEFAULT '0' AFTER `startingBid`;