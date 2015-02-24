UPDATE characters c
LEFT JOIN items i ON i.item_id=8181 AND c.obj_Id = i.owner_id
SET c.lvl_joined_academy=39
WHERE i.object_id IS NOT NULL;