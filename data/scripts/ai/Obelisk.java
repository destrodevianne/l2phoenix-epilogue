package ai;

import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.Reflection;
import l2p.gameserver.model.instances.L2MonsterInstance;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.serverpackets.ExShowScreenMessage;
import l2p.gameserver.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2p.gameserver.tables.NpcTable;
import l2p.util.Rnd;

/**
 * AI Obelisk в Seed of Destruction:
 * - при смерти отображает сообщение и открывает двери
 * - при атаке спаунит толпу мобов вокруг себя
 *
 * @author SYS
 */
public class Obelisk extends DefaultAI
{
	private static final int[] DOORS = { 12240003, 12240004, 12240011, 12240012, 12240019, 12240020 };
	private static final int[] MOBS = { 22541, 22544, 22543 };

	private boolean _firstTimeAttacked = true;

	public Obelisk(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_firstTimeAttacked = true;
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		// ru: Обелиск поражен. Вражеские войска, прекратите атаку!!!
		actor.broadcastPacket(new ExShowScreenMessage("Obelisk has collapsed. Don't let the enemies jump around wildly anymore!!!!", 3000, ScreenMessageAlign.MIDDLE_CENTER, false));
		for(int doorId : DOORS)
			actor.getReflection().openDoor(doorId);

		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;

			Reflection r = actor.getReflection();
			for(int i = 0; i < 8; i++)
				for(int mobId : MOBS)
				{
					L2MonsterInstance npc = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(mobId));
					npc.setSpawnedLoc(Rnd.coordsRandomize(actor.getLoc(), 400, 1000));
					npc.setReflection(actor.getReflection());
					npc.onSpawn();
					npc.spawnMe(npc.getSpawnedLoc());
					r.addSpawn(npc.getSpawn());
					L2Character randomHated = actor.getRandomHated();
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, randomHated != null ? randomHated : attacker, Rnd.get(1, 100));
				}
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}