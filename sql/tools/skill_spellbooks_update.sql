update skill_spellbooks set item_name = (select name from etcitem where item_id = skill_spellbooks.item_id);
update skill_spellbooks set min_level = (select MIN(min_level) from skill_trees where skill_id = skill_spellbooks.skill_id and level = skill_spellbooks.level group by skill_id);