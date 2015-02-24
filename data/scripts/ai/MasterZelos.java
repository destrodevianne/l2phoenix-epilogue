package ai;

import l2p.common.ThreadPoolManager;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.L2Zone.ZoneType;
import l2p.util.Location;

/**
 * Через 10 сек после смерти активирует зону перехода на следующий этаж Tylly's Workshop
 * @author SYS
 */
public class MasterZelos extends Fighter
{
	private static Location TullyFloor2LocationPoint = new Location(-14180, 273060, -13600);
	private static L2Zone _zone;

	public MasterZelos(L2Character actor)
	{
		super(actor);
		_zone = ZoneManager.getInstance().getZoneById(ZoneType.dummy, 704009, true);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		super.onEvtDead(killer);
		ThreadPoolManager.getInstance().scheduleAi(new TeleportTask(), 10000, true);
	}

	public class TeleportTask implements Runnable
	{
		public void run()
		{
			for(L2Player p : _zone.getInsidePlayersIncludeZ())
				if(p != null)
					p.teleToLocation(TullyFloor2LocationPoint);
		}
	}
}