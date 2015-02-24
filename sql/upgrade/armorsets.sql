DROP TABLE IF EXISTS armorsets;
CREATE TABLE armorsets (
 id int(3) NOT NULL auto_increment,
 chest smallint(4) NOT NULL default '0',
 legs smallint(4) NOT NULL default '0',
 head smallint(4) NOT NULL default '0',
 gloves smallint(4) NOT NULL default '0',
 feet smallint(4) NOT NULL default '0',
 skill varchar(8) NOT NULL default '0;0',
 shield smallint(4) NOT NULL default '0',
 shield_skill varchar(8) NOT NULL default '0;0',
 enchant6skill varchar(8) NOT NULL default '0;0',
 PRIMARY KEY (id,chest)
) ENGINE=MyISAM;

-- ------------------------------                 NO GRADE              -------------------------------------------
-- ########################  id  chest legs  head  gloves feet skill     shield sh_skill enchant6
INSERT INTO armorsets VALUES(0,  23,   2386, 43,   0,     0,   '3500;1', 0,     '0;0',   '0;0');            -- Wooden Breastplate set (light)
INSERT INTO armorsets VALUES(1,  1101, 1104, 44,   0,     0,   '3501;1', 0,     '0;0',   '0;0');            -- Devotion robe set (robe)
INSERT INTO armorsets VALUES(2,  9670, 9671, 9669, 0,     0,   '3359;1', 0,     '0;0',   '0;0');            -- Native set (heavy)


-- ------------------------------                 D GRADE              -------------------------------------------
-- ########################  id  chest  legs  head  gloves feet  skill     shield sh_skill  enchant6
INSERT INTO armorsets VALUES(3,  58,    59,   47,   0,     0,    '3502;1', 628,   '3543;1', '3611;1');      -- Mithril Breastplate set(heavy)
INSERT INTO armorsets VALUES(4,  352,   2378, 2411, 0,     0,    '3506;1', 2493,  '3544;1', '3611;1');      -- Brigandine Armor set

INSERT INTO armorsets VALUES(5,  394,   416,  0,    0,     2422, '3503;1', 0,     '0;0',    '3612;1');      -- Reinforced leather set
INSERT INTO armorsets VALUES(6,  395,   417,  0,    0,     2424, '3505;1', 0,     '0;0',    '3612;1');      -- Manticore skin set

INSERT INTO armorsets VALUES(7,  436,   469,  0,    2447,  0,    '3504;1', 0,     '0;0',    '3613;1');      -- Tunic of knowledge set
INSERT INTO armorsets VALUES(8,  437,   470,  0,    2450,  0,    '3507;1', 0,     '0;0',    '3613;1');      -- Mithril Tunic

-- ------------------------------                 C GRADE              -------------------------------------------
-- ########################  id  chest legs  head  gloves feet  skill     shield sh_skill  enchant6
INSERT INTO armorsets VALUES(9,  354,  381,  2413, 0,     0,    '3509;1', 2495,  '3545;1', '3614;1');       -- Chain Mail Shirt set
INSERT INTO armorsets VALUES(10, 60,   0,    517,  0,     0,    '3512;1', 107,   '3546;1', '3614;1');       -- Composite Armor set
INSERT INTO armorsets VALUES(11, 356,  0,    2414, 0,     0,    '3516;1', 2497,  '3547;1', '3614;1');       -- Full Plate Armor set

INSERT INTO armorsets VALUES(12, 397,  2387, 0,    0,     62,   '3508;1', 0,     '0;0',    '3615;1');       -- Mithrill shirt set
INSERT INTO armorsets VALUES(13, 398,  418,  0,    0,     2431, '3511;1', 0,     '0;0',    '3615;1');       -- Plated leather set
INSERT INTO armorsets VALUES(14, 400,  420,  0,    0,     2436, '3514;1', 0,     '0;0',    '3615;1');       -- Theca leather set
INSERT INTO armorsets VALUES(15, 401,  0,    0,    0,     2437, '3515;1', 0,     '0;0',    '3615;1');       -- Drake leather set

INSERT INTO armorsets VALUES(16, 439,  471,  0,    2454,  0,    '3510;1', 0,     '0;0',    '3616;1');       -- Karmian robe set
INSERT INTO armorsets VALUES(17, 441,  472,  0,    2459,  0,    '3513;1', 0,     '0;0',    '3616;1');       -- Demon robe set
INSERT INTO armorsets VALUES(18, 442,  473,  0,    2463,  0,    '3517;1', 0,     '0;0',    '3616;1');       -- Divine robe set

-- ------------------------------                 B GRADE              -------------------------------------------
-- ########################  id  chest legs  head  gloves feet    skill      shield sh_skill  enchant6
INSERT INTO armorsets VALUES(19, 357,  383,  503,  5710,  5726,   '3518;1',  0,     '0;0',    '3617;1');    -- Zubei's Breastplate set
INSERT INTO armorsets VALUES(20, 2384, 2388, 503,  5711,  5727,   '3520;1',  0,     '0;0',    '3618;1');    -- Zubei's leather set
INSERT INTO armorsets VALUES(21, 2397, 2402, 503,  5712,  5728,   '3522;1',  0,     '0;0',    '3619;1');    -- Zubei robe set

INSERT INTO armorsets VALUES(22, 2376, 2379, 2415, 5714,  5730,   '3519;1',  673,   '3548;1', '3617;1');    -- Avadon heavy set
INSERT INTO armorsets VALUES(23, 2390, 0,    2415, 5715,  5731,   '3521;1',  0,     '0;0',    '3618;1');    -- Avadon leather set
INSERT INTO armorsets VALUES(24, 2406, 0,    2415, 5716,  5732,   '3523;1',  0,     '0;0',    '3619;1');    -- Avadon robe set

INSERT INTO armorsets VALUES(25, 358,  2380, 2416, 5718,  5734,   '3524;1',  0,     '0;0',    '3617;1');    -- Blue Wolf's Breastplate set
INSERT INTO armorsets VALUES(26, 2391, 0,    2416, 5719,  5735,   '3526;1',  0,     '0;0',    '3618;1');    -- Blue wolf leather set
INSERT INTO armorsets VALUES(27, 2398, 2403, 2416, 5720,  5736,   '3528;1',  0,     '0;0',    '3619;1');    -- Blue Wolf robe set

INSERT INTO armorsets VALUES(28, 2381, 0,    2417, 5722,  5738,   '3525;1',  110,   '3549;1', '3617;1');    -- Doom plate heavy set
INSERT INTO armorsets VALUES(29, 2392, 0,    2417, 5723,  5739,   '3527;1',  0,     '0;0',    '3618;1');    -- Doom leather set
INSERT INTO armorsets VALUES(30, 2399, 2404, 2417, 5724,  5740,   '3529;1',  0,     '0;0',    '3619;1');    -- Doom robe set

-- ------------------------------                 A GRADE              -------------------------------------------
-- ########################  id  chest  legs  head  gloves feet  skill     shield sh_skill  enchant6
INSERT INTO armorsets VALUES(31, 365,   388,  512,  5765,  5777, '3530;1', 641,   '3550;1', '3620;1');      -- Dark Crystal Breastplate set
INSERT INTO armorsets VALUES(32, 2385,  2389, 512,  5766,  5778, '3532;1', 0,     '0;0',    '3621;1');      -- Dark Crystal leather set
INSERT INTO armorsets VALUES(33, 2407,  0,    512,  5767,  5779, '3535;1', 0,     '0;0',    '3622;1');      -- Dark Crystal robe set

INSERT INTO armorsets VALUES(34, 2382,  0,    547,  5768,  5780, '3531;1', 0,     '0;0',    '3620;1');      -- Tallum plate heavy set
INSERT INTO armorsets VALUES(35, 2393,  0,    547,  5769,  5781, '3533;1', 0,     '0;0',    '3621;1');      -- Tallum leather set
INSERT INTO armorsets VALUES(36, 2400,  2405, 547,  5770,  5782, '3534;1', 0,     '0;0',    '3622;1');      -- Tallum robe set

INSERT INTO armorsets VALUES(37, 374,   0,    2418, 5771,  5783, '3536;1', 2498,  '3551;1', '3620;1');      -- Nightmare heavy set
INSERT INTO armorsets VALUES(38, 10793, 0,    2418, 5771,  5783, '8193;1', 2498,  '3551;1', '3620;1');      -- Armor of Nightmare - PvP heavy set
INSERT INTO armorsets VALUES(39, 2394,  0,    2418, 5772,  5784, '3538;1', 0,     '0;0',    '3621;1');      -- Nightmare leather set
INSERT INTO armorsets VALUES(40, 10795, 0,    2418, 5772,  5784, '8195;1', 0,     '0;0',    '3621;1');      -- Leather Armor of Nightmare - PvP leather set
INSERT INTO armorsets VALUES(41, 2408,  0,    2418, 5773,  5785, '3540;1', 0,     '0;0',    '3622;1');      -- Robe of nightmare set
INSERT INTO armorsets VALUES(42, 10797, 0,    2418, 5773,  5785, '8197;1', 0,     '0;0',    '3622;1');      -- Robe of Nightmare - PvP set

INSERT INTO armorsets VALUES(43, 2383,  0,    2419, 5774,  5786, '3537;1', 0,     '0;0',    '3620;1');      -- Majestic plate heavy set
INSERT INTO armorsets VALUES(44, 10794, 0,    2419, 5774,  5786, '8194;1', 0,     '0;0',    '3620;1');      -- Majestic Plate Armor - PvP heavy set
INSERT INTO armorsets VALUES(45, 2395,  0,    2419, 5775,  5787, '3539;1', 0,     '0;0',    '3621;1');      -- Majestic leather set
INSERT INTO armorsets VALUES(46, 10796, 0,    2419, 5775,  5787, '8196;1', 0,     '0;0',    '3621;1');      -- Majestic Leather Mail - PvP leather set
INSERT INTO armorsets VALUES(47, 2409,  0,    2419, 5776,  5788, '3541;1', 0,     '0;0',    '3622;1');      -- Majestic robe set
INSERT INTO armorsets VALUES(48, 10798, 0,    2419, 5776,  5788, '8198;1', 0,     '0;0',    '3622;1');      -- Majestic Robe - PvP set

-- ------------------------------                 S GRADE              -------------------------------------------
-- ########################  id  chest  legs  head  gloves feet  skill     shield sh_skill  enchant6
INSERT INTO armorsets VALUES(49, 6373,  6374, 6378, 6375,  6376, '3553;1', 6377,  '3554;1', '3623;1');      -- Imperial crusader set
INSERT INTO armorsets VALUES(50, 10799, 6374, 6378, 6375,  6376, '8199;1', 6377,  '3554;1', '3623;1');      -- Imperial Crusader Breastplate - PvP set
INSERT INTO armorsets VALUES(51, 6379,  0,    6382, 6380,  6381, '3555;1', 0,     '0;0',    '3624;1');      -- Draconic leather set
INSERT INTO armorsets VALUES(52, 10800, 0,    6382, 6380,  6381, '8200;1', 0,     '0;0',    '3624;1');      -- Draconic Leather Armor - PvP set
INSERT INTO armorsets VALUES(53, 6383,  0,    6386, 6384,  6385, '3556;1', 0,     '0;0',    '3625;1');      -- Major arcana robe set
INSERT INTO armorsets VALUES(54, 10801, 0,    6386, 6384,  6385, '8201;1', 0,     '0;0',    '3625;1');      -- Major Arcana Robe - PvP set

-- ------------------------------                Clan Sets              -------------------------------------------
-- ########################  id  chest legs  head  gloves feet  skill     shield sh_skill enchant6
INSERT INTO armorsets VALUES(55, 7851, 0,    7850, 7852,  7853, '3605;1', 0,     '0;0',   '3611;1');        -- Clan oath Armor set (heavy)
INSERT INTO armorsets VALUES(56, 7854, 0,    7850, 7855,  7856, '3606;1', 0,     '0;0',   '3612;1');        -- Clan Oath Brigandine set (light)
INSERT INTO armorsets VALUES(57, 7857, 0,    7850, 7858,  7859, '3607;1', 0,     '0;0',   '3613;1');        -- Clan Oath Aketon set (robe)

INSERT INTO armorsets VALUES(58, 9821, 0,    9820, 9822,  9823, '3605;1', 0,     '0;0',   '3611;1');        -- Shadow Item: Clan oath Armor set (heavy)
INSERT INTO armorsets VALUES(59, 9824, 0,    9820, 9825,  9826, '3606;1', 0,     '0;0',   '3612;1');        -- Shadow Item: Clan Oath Brigandine set (light)
INSERT INTO armorsets VALUES(60, 9827, 0,    9820, 9828,  9829, '3607;1', 0,     '0;0',   '3613;1');        -- Shadow Item: Clan Oath Aketon set (robe)

INSERT INTO armorsets VALUES(61, 7861, 0,    7860, 7862,  7863, '3608;1', 0,     '0;0',   '3620;1');        -- Apella plate armor set (heavy)
INSERT INTO armorsets VALUES(62, 7864, 0,    7860, 7865,  7866, '3609;1', 0,     '0;0',   '3621;1');        -- Apella Brigandine set (light)
INSERT INTO armorsets VALUES(63, 7867, 0,    7860, 7868,  7869, '3610;1', 0,     '0;0',   '3622;1');        -- Apella Doublet set (robe)

INSERT INTO armorsets VALUES(64, 9831, 0,    9830, 9832,  9833, '3608;2', 0,     '0;0',   '3620;1');        -- Improved Apella Plate Armor set (heavy)
INSERT INTO armorsets VALUES(65, 9834, 0,    9830, 9835,  9836, '3609;2', 0,     '0;0',   '3621;1');        -- Improved Apella Brigandine set (light)
INSERT INTO armorsets VALUES(66, 9837, 0,    9830, 9838,  9839, '3610;2', 0,     '0;0',   '3622;1');        -- Improved Apella Doublet set (robe)

--                Dynasty Sets              -------------
-- ########################  id  chest  legs  head  gloves feet  skill     shield sh_skill  enchant6
INSERT INTO armorsets VALUES(67, 9432,  9437, 9438, 9439,  9440, '3416;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic set
INSERT INTO armorsets VALUES(68, 10810, 9437, 9438, 9439,  9440, '8210;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic - PvP set
INSERT INTO armorsets VALUES(69, 9433,  9437, 9438, 9439,  9440, '3354;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic Healer set
INSERT INTO armorsets VALUES(70, 10811, 9437, 9438, 9439,  9440, '8211;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic - PvP	Healer set
INSERT INTO armorsets VALUES(71, 9434,  9437, 9438, 9439,  9440, '3355;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic Enchanter set
INSERT INTO armorsets VALUES(72, 10812, 9437, 9438, 9439,  9440, '8212;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic - PvP	Enchanter set
INSERT INTO armorsets VALUES(73, 9435,  9437, 9438, 9439,  9440, '3356;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic Summoner set
INSERT INTO armorsets VALUES(74, 10813, 9437, 9438, 9439,  9440, '8213;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic - PvP	Summoner set
INSERT INTO armorsets VALUES(75, 9436,  9437, 9438, 9439,  9440, '3357;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic Wizard set
INSERT INTO armorsets VALUES(76, 10814, 9437, 9438, 9439,  9440, '8214;1', 0,     '0;0',    '3625;1');      -- Dynasty Tunic - PvP	Wizard set

INSERT INTO armorsets VALUES(77, 9416,  9421, 9422, 9423,  9424, '3412;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breast Plate set
INSERT INTO armorsets VALUES(78, 10802, 9421, 9422, 9423,  9424, '8202;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breastplate - PvP set
INSERT INTO armorsets VALUES(79, 9417,  9421, 9422, 9423,  9424, '3348;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breast Plate Shield Master set
INSERT INTO armorsets VALUES(80, 10803, 9421, 9422, 9423,  9424, '8203;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breastplate - PvP	Shield Master set
INSERT INTO armorsets VALUES(81, 9418,  9421, 9422, 9423,  9424, '3349;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breast Plate Weapon Master set
INSERT INTO armorsets VALUES(82, 10804, 9421, 9422, 9423,  9424, '8204;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breastplate - PvP	Weapon Master set
INSERT INTO armorsets VALUES(83, 9419,  9421, 9422, 9423,  9424, '3350;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breast Plate Force Master set
INSERT INTO armorsets VALUES(84, 10805, 9421, 9422, 9423,  9424, '8205;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breastplate - PvP	Force Master set
INSERT INTO armorsets VALUES(85, 9420,  9421, 9422, 9423,  9424, '3351;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breast Plate Bard set
INSERT INTO armorsets VALUES(86, 10806, 9421, 9422, 9423,  9424, '8206;1', 9441,  '3550;1', '3623;1');      -- Dynasty Breastplate - PvP	Bard set

INSERT INTO armorsets VALUES(87,  9425,  9428, 9429, 9430, 9431, '3413;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor set
INSERT INTO armorsets VALUES(88,  10807, 9428, 9429, 9430, 9431, '8207;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor - PvP set
INSERT INTO armorsets VALUES(89,  9426,  9428, 9429, 9430, 9431, '3352;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor Dagger Master set
INSERT INTO armorsets VALUES(90,  10808, 9428, 9429, 9430, 9431, '8208;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor - PvP	Dagger Master set
INSERT INTO armorsets VALUES(91,  9427,  9428, 9429, 9430, 9431, '3353;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor Bow Master set
INSERT INTO armorsets VALUES(92,  10809, 9428, 9429, 9430, 9431, '8209;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor - PvP	Bow Master set
INSERT INTO armorsets VALUES(93,  10126, 9428, 9429, 9430, 9431, '3414;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor Force Master set
INSERT INTO armorsets VALUES(94,  10815, 9428, 9429, 9430, 9431, '8215;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor - PvP	Force Master set
INSERT INTO armorsets VALUES(95,  10127, 9428, 9429, 9430, 9431, '3415;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor Weapon Master set
INSERT INTO armorsets VALUES(96,  10816, 9428, 9429, 9430, 9431, '8216;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor - PvP	Weapon Master set
INSERT INTO armorsets VALUES(97,  10168, 9428, 9429, 9430, 9431, '3355;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor Enchanter set
INSERT INTO armorsets VALUES(98,  10817, 9428, 9429, 9430, 9431, '8217;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor - PvP	Enchanter set
INSERT INTO armorsets VALUES(99,  10214, 9428, 9429, 9430, 9431, '3420;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor Summoner
INSERT INTO armorsets VALUES(100, 10818, 9428, 9429, 9430, 9431, '8218;1', 0,    '0;0',    '3624;1');       -- Dynasty Leather Armor - PvP	Summoner set

INSERT INTO armorsets VALUES(101, 10236, 9437, 9438, 9439, 9440, '3645;1', 0,    '0;0',     '3625;1');      -- Dynasty Silver Satin Tunic - Healer set
INSERT INTO armorsets VALUES(102, 10828, 9437, 9438, 9439, 9440, '8229;1', 0,    '0;0',     '3625;1');      -- Dynasty Silver Satin Tunic - PvP Healer set
INSERT INTO armorsets VALUES(103, 10237, 9437, 9438, 9439, 9440, '3646;1', 0,    '0;0',     '3625;1');      -- Dynasty Silver Satin Tunic - Enchanter set
INSERT INTO armorsets VALUES(104, 10829, 9437, 9438, 9439, 9440, '8230;1', 0,    '0;0',     '3625;1');      -- Dynasty Silver Satin Tunic - PvP Enchanter set
INSERT INTO armorsets VALUES(105, 10238, 9437, 9438, 9439, 9440, '3647;1', 0,    '0;0',     '3625;1');      -- Dynasty Silver Satin Tunic - Summoner set
INSERT INTO armorsets VALUES(106, 10830, 9437, 9438, 9439, 9440, '8231;1', 0,    '0;0',     '3625;1');      -- Dynasty Silver Satin Tunic - PvP Summoner set
INSERT INTO armorsets VALUES(107, 10239, 9437, 9438, 9439, 9440, '3648;1', 0,    '0;0',     '3625;1');      -- Dynasty Silver Satin Tunic - Wizard set
INSERT INTO armorsets VALUES(108, 10831, 9437, 9438, 9439, 9440, '8232;1', 0,    '0;0',     '3625;1');      -- Dynasty Silver Satin Tunic - PvP Wizard set

INSERT INTO armorsets VALUES(109, 10228, 9421, 9422, 9423, 9424, '3636;1', 9441, '3550;1', '3623;1');       -- Dynasty Platinum Plate Shield Master set
INSERT INTO armorsets VALUES(110, 10820, 9421, 9422, 9423, 9424, '8219;1', 9441, '3550;1', '3623;1');       -- Dynasty Platinum Breastplate - PvP Shield Master set
INSERT INTO armorsets VALUES(111, 10229, 9421, 9422, 9423, 9424, '3637;1', 9441, '3550;1', '3623;1');       -- Dynasty Platinum Plate Weapon Master set
INSERT INTO armorsets VALUES(112, 10821, 9421, 9422, 9423, 9424, '8220;1', 9441, '3550;1', '3623;1');       -- Dynasty Platinum Breastplate - PvP Weapon Master set
INSERT INTO armorsets VALUES(113, 10230, 9421, 9422, 9423, 9424, '3639;1', 9441, '3550;1', '3623;1');       -- Dynasty Platinum Plate Force Master set
INSERT INTO armorsets VALUES(114, 10822, 9421, 9422, 9423, 9424, '8221;1', 9441, '3550;1', '3623;1');       -- Dynasty Platinum Breastplate - PvP Force Master set
INSERT INTO armorsets VALUES(115, 10231, 9421, 9422, 9423, 9424, '3638;1', 9441, '3550;1', '3623;1');       -- Dynasty Platinum Plate Bard set
INSERT INTO armorsets VALUES(116, 10823, 9421, 9422, 9423, 9424, '8222;1', 9441, '3550;1', '3623;1');       -- Dynasty Platinum Breastplate - PvP Bard set

INSERT INTO armorsets VALUES(117, 10233, 9428, 9429, 9430, 9431, '3640;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Mail - Dagger Master set
INSERT INTO armorsets VALUES(118, 10825, 9428, 9429, 9430, 9431, '8223;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Armor - PvP Dagger Master set
INSERT INTO armorsets VALUES(119, 10234, 9428, 9429, 9430, 9431, '3641;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Mail - Bow Master set
INSERT INTO armorsets VALUES(120, 10826, 9428, 9429, 9430, 9431, '8224;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Armor - PvP Bow Master set
INSERT INTO armorsets VALUES(121, 10487, 9428, 9429, 9430, 9431, '3642;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Mail - Force Master set
INSERT INTO armorsets VALUES(122, 10832, 9428, 9429, 9430, 9431, '8225;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Armor - PvP Force Master set
INSERT INTO armorsets VALUES(123, 10488, 9428, 9429, 9430, 9431, '3643;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Mail - Weapon Master set
INSERT INTO armorsets VALUES(124, 10833, 9428, 9429, 9430, 9431, '8226;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Armor - PvP Weapon Master set
INSERT INTO armorsets VALUES(125, 10489, 9428, 9429, 9430, 9431, '3644;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Mail - Enchanter set
INSERT INTO armorsets VALUES(126, 10834, 9428, 9429, 9430, 9431, '8227;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Armor - PvP Enchanter set
INSERT INTO armorsets VALUES(127, 10490, 9428, 9429, 9430, 9431, '3805;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Mail - Summoner set
INSERT INTO armorsets VALUES(128, 10835, 9428, 9429, 9430, 9431, '8228;1', 0,    '0;0',    '3624;1');       -- Dynasty Jewel Leather Armor - PvP Summoner set

--                Vesper Sets              -------------
-- ########################  id  chest   legs   head   gloves  feet   skill     shield sh_skill  enchant6
INSERT INTO armorsets VALUES(129, 13432, 13438, 13137, 13439,  13440, '8283;1', 13471, '3547;1', '3623;1');      -- Vesper Breastplate set
INSERT INTO armorsets VALUES(130, 13433, 13441, 13138, 13442,  13443, '8285;1', 0,     '0;0',    '3624;1');      -- Vesper Leather Breastplate set
INSERT INTO armorsets VALUES(131, 13434, 13444, 13139, 13445,  13446, '8287;1', 0,     '0;0',    '3625;1');      -- Vesper Tunic set
INSERT INTO armorsets VALUES(132, 13435, 13448, 13140, 13449,  13450, '8284;1', 13471, '3547;1', '3623;1');      -- Vesper Noble Breastplate set
INSERT INTO armorsets VALUES(133, 13436, 13451, 13141, 13452,  13453, '8286;1', 0,     '0;0',    '3624;1');      -- Vesper Noble Leather Breastplate set
INSERT INTO armorsets VALUES(134, 13437, 13454, 13142, 13455,  13456, '8288;1', 0,     '0;0',    '3625;1');      -- Vesper Noble Tunic set

INSERT INTO armorsets VALUES(135, 14520, 13438, 13137, 13439,  13440, '8301;1', 13471, '3547;1', '3623;1');      -- Vesper Breastplate (PvP) set
INSERT INTO armorsets VALUES(136, 14521, 13441, 13138, 13442,  13443, '8303;1', 0,     '0;0',    '3624;1');      -- Vesper Leather Breastplate (PvP) set
INSERT INTO armorsets VALUES(137, 14522, 13444, 13139, 13445,  13446, '8305;1', 0,     '0;0',    '3625;1');      -- Vesper Tunic (PvP) set
INSERT INTO armorsets VALUES(138, 14523, 13448, 13140, 13449,  13450, '8302;1', 13471, '3547;1', '3623;1');      -- Vesper Noble Breastplate (PvP) set
INSERT INTO armorsets VALUES(139, 14524, 13451, 13141, 13452,  13453, '8304;1', 0,     '0;0',    '3624;1');      -- Vesper Noble Leather Breastplate (PvP) set
INSERT INTO armorsets VALUES(140, 14525, 13454, 13142, 13455,  13456, '8306;1', 0,     '0;0',    '3625;1');      -- Vesper Noble Tunic (PvP) set