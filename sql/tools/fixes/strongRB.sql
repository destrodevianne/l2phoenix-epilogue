update npc set
patk=patk*2, matk=matk*2, pdef=2*pdef, mdef=2*mdef
where type IN ("L2RaidBoss", "L2Boss");