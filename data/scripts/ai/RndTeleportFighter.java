package ai;

import l2p.Config;
import l2p.common.ThreadPoolManager;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Spawn;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.tables.TerritoryTable;
import l2p.util.Location;
import l2p.util.Rnd;

/**
 * Моб использует телепортацию вместо рандом валка.
 *
 * @author SYS
 */
public class RndTeleportFighter extends Fighter
{
	private long _lastTeleport;

	public RndTeleportFighter(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected boolean maybeMoveToHome()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || System.currentTimeMillis() - _lastTeleport < 10000)
			return false;

		boolean randomWalk = actor.hasRandomWalk();
		Location sloc = actor.getSpawnedLoc();
		if(sloc == null)
			return false;

		// Random walk or not?
		if(randomWalk && (!Config.RND_WALK || Rnd.chance(Config.RND_WALK_RATE)))
			return false;

		if(!randomWalk && actor.isInRangeZ(sloc, Config.MAX_DRIFT_RANGE))
			return false;

		int x = sloc.x + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
		int y = sloc.y + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
		int z = GeoEngine.getHeight(x, y, sloc.z, actor.getReflection().getGeoIndex());

		if(sloc.z - z > 64)
			return false;

		L2Spawn spawn = actor.getSpawn();
		if(spawn != null && spawn.getLocation() != 0 && !TerritoryTable.getInstance().getLocation(spawn.getLocation()).isInside(x, y))
			return false;

		actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 4671, 1, 500, 0));
		ThreadPoolManager.getInstance().scheduleAi(new Teleport(new Location(x, y, z)), 500, false);
		_lastTeleport = System.currentTimeMillis();

		return true;
	}
}