package npc.model;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.instances.L2MonsterInstance;
import l2p.gameserver.templates.L2NpcTemplate;

public class HellboundRemnantInstance extends L2MonsterInstance
{
	public HellboundRemnantInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{
		super.reduceCurrentHp(Math.min(i, getCurrentHp() - 1), attacker, skill, awake, standUp, directHp, canReflect);
	}

	public void onUseHolyWater(L2Character user)
	{
		if(getCurrentHp() < 100)
			doDie(user);
	}
}