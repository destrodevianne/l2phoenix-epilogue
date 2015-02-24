SELECT `n`.`name`, `n`.`level`, SUM(`k`.`count`)
FROM `killcount` `k`
LEFT JOIN `npc` `n` ON `n`.`id`=`k`.`npc_id`
GROUP BY `n`.`name`
ORDER BY SUM(`k`.`count`) DESC
LIMIT 20;