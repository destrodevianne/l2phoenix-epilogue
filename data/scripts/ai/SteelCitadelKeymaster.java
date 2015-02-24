package ai;

import l2p.extensions.scripts.Functions;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2World;
import l2p.gameserver.model.instances.L2NpcInstance;

/**
 * AI Steel Citadel Keymaster в городе-инстанте на Hellbound<br>
 * - кричит когда его атакуют первый раз
 * - портает к себе Amaskari, если был атакован
 * - не использует random walk
 * 
 * @author SYS
 */
public class SteelCitadelKeymaster extends Fighter
{
	private boolean _firstTimeAttacked = true;
	private static final int AMASKARI_ID = 22449;

	public SteelCitadelKeymaster(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return;

		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			Functions.npcSay(actor, "You have done well in finding me, but I cannot just hand you the key!");
			for(L2NpcInstance npc : L2World.getAroundNpc(actor))
				if(npc.getNpcId() == AMASKARI_ID && npc.getReflection().getId() == actor.getReflection().getId() && !npc.isDead())
				{
					npc.teleToLocation(GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 150, 180, actor.getReflection().getGeoIndex()));
					break;
				}
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}