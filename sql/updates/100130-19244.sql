ALTER TABLE `seven_signs_status` CHANGE `dawn_stone_score` `dawn_stone_score` BIGINT NOT NULL default '0';
ALTER TABLE `seven_signs_status` CHANGE `dawn_festival_score` `dawn_festival_score` BIGINT NOT NULL default '0';
ALTER TABLE `seven_signs_status` CHANGE `dusk_stone_score` `dusk_stone_score` BIGINT NOT NULL default '0';
ALTER TABLE `seven_signs_status` CHANGE `dusk_festival_score` `dusk_festival_score` BIGINT NOT NULL default '0';

ALTER TABLE `character_effects_save` DROP INDEX `order`;