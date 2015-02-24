SET @min=76;

DROP TABLE IF EXISTS cl,t1;

-- Создаем таблицу для суммы
CREATE TEMPORARY TABLE cl
SELECT ct1.parent AS p2, ct1.ClassId AS p3, CONCAT(ct2.ClassName,'/',ct1.ClassName) AS name, 0 AS p2cnt, 0 AS p3cnt
FROM char_templates ct1
LEFT JOIN char_templates AS ct2 ON ct2.ClassId=ct1.parent
WHERE ct1.level=3;

-- Считаем вторые профы
CREATE TEMPORARY TABLE t1
SELECT s.class_id AS c, COUNT(*) AS cnt
FROM character_subclasses s
LEFT JOIN cl ON cl.p2=s.class_id
WHERE s.level >= @min
GROUP BY s.class_id;
UPDATE cl
LEFT JOIN t1 ON t1.c=cl.p2
SET cl.p2cnt=t1.cnt;
DROP TABLE IF EXISTS t1;

-- Считаем третьи профы
CREATE TEMPORARY TABLE t1
SELECT s.class_id AS c, COUNT(*) AS cnt
FROM character_subclasses s
LEFT JOIN cl ON cl.p3=s.class_id
WHERE s.level >= @min
GROUP BY s.class_id;
UPDATE cl
LEFT JOIN t1 ON t1.c=cl.p3
SET cl.p3cnt=t1.cnt;
DROP TABLE IF EXISTS t1;

SELECT name, p2cnt+p3cnt AS cnt FROM cl ORDER BY p2cnt+p3cnt DESC;