package ai;

import l2p.extensions.scripts.Functions;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.instances.L2MonsterInstance;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.tables.NpcTable;
import l2p.util.Rnd;

/**
 * AI для Timak Orc Troop Leader ID: 20767, кричащего и призывающего братьев по клану при ударе.
 *
 * @author SYS
 */
public class TimakOrcTroopLeader extends Fighter
{
	private static final int[] BROTHERS = { 20768, // Timak Orc Troop Shaman
			20769, // Timak Orc Troop Warrior
			20770 // Timak Orc Troop Archer
	};

	private boolean _firstTimeAttacked = true;

	public TimakOrcTroopLeader(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(!actor.isDead() && _firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			Functions.npcSay(actor, "Show yourselves!");
			for(int bro : BROTHERS)
				try
				{
					L2NpcInstance npc = NpcTable.getTemplate(bro).getNewInstance();
					npc.setSpawnedLoc(((L2MonsterInstance) actor).getMinionPosition());
					npc.setReflection(actor.getReflection());
					npc.onSpawn();
					npc.spawnMe(npc.getSpawnedLoc());
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
				}
				catch(Exception e)
				{
					e.printStackTrace();
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
}