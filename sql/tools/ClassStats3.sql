--ALTER TABLE `character_subclasses`  ADD INDEX `level` (`level`),  ADD INDEX `class_id` (`class_id`);
-- Выполняется долго, на живом сервере не запускать!

SELECT CONCAT(ct2.ClassName,'/',ct1.ClassName) AS name,
(SELECT COUNT(IF(class_id=ct1.parent,1,null)) AS p1 FROM character_subclasses WHERE level>=60)+(SELECT COUNT(IF(class_id=ct1.ClassId,1,null)) AS p1 FROM character_subclasses WHERE level>=60) AS lvl60,
(SELECT COUNT(IF(class_id=ct1.parent,1,null)) AS p1 FROM character_subclasses WHERE level>=70)+(SELECT COUNT(IF(class_id=ct1.ClassId,1,null)) AS p1 FROM character_subclasses WHERE level>=70) AS lvl70,
(SELECT COUNT(IF(class_id=ct1.parent,1,null)) AS p1 FROM character_subclasses WHERE level>=76)+(SELECT COUNT(IF(class_id=ct1.ClassId,1,null)) AS p1 FROM character_subclasses WHERE level>=76) AS lvl76,
(SELECT COUNT(IF(class_id=ct1.parent,1,null)) AS p1 FROM character_subclasses WHERE level>=80)+(SELECT COUNT(IF(class_id=ct1.ClassId,1,null)) AS p1 FROM character_subclasses WHERE level>=80) AS lvl80,
(SELECT COUNT(IF(class_id=ct1.parent,1,null)) AS p1 FROM character_subclasses WHERE level>=83)+(SELECT COUNT(IF(class_id=ct1.ClassId,1,null)) AS p1 FROM character_subclasses WHERE level>=83) AS lvl83,
(SELECT COUNT(IF(class_id=ct1.parent,1,null)) AS p1 FROM character_subclasses WHERE level>=85)+(SELECT COUNT(IF(class_id=ct1.ClassId,1,null)) AS p1 FROM character_subclasses WHERE level>=85) AS lvl85
FROM char_templates ct1
LEFT JOIN char_templates AS ct2 ON ct2.ClassId=ct1.parent
WHERE ct1.level=3;