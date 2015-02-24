//
// Suspicious Merchant - Narsell Fortress (35828).
//
package ai;

import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.util.Location;
import l2p.util.Rnd;

public class SuspiciousMerchantNarsell extends DefaultAI
{
	static final Location[] points = { new Location(159377, 52403, -3312), new Location(161177, 54083, -3560),
			new Location(162152, 54365, -3632), new Location(162703, 55840, -3696), new Location(162370, 58534, -3504),
			new Location(160099, 60034, -3224), new Location(158048, 62696, -3464), new Location(157220, 63450, -3520),
			new Location(155076, 63731, -3544), new Location(153893, 64441, -3656), new Location(153085, 62948, -3680),
			new Location(150866, 58737, -3432), new Location(153085, 62948, -3680), new Location(153893, 64441, -3656),
			new Location(155076, 63731, -3544), new Location(157220, 63450, -3520), new Location(158048, 62696, -3464),
			new Location(160099, 60034, -3224), new Location(162370, 58534, -3504), new Location(162703, 55840, -3696),
			new Location(162152, 54365, -3632), new Location(161177, 54083, -3560), new Location(159377, 52403, -3312) };

	private int current_point = -1;
	private long wait_timeout = 0;
	private boolean wait = false;

	public SuspiciousMerchantNarsell(L2Character actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;

		if(_def_think)
		{
			doTask();
			return true;
		}

		if(System.currentTimeMillis() > wait_timeout && (current_point > -1 || Rnd.chance(5)))
		{
			if(!wait)
				switch(current_point)
				{
					case 0:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
					case 12:
						wait_timeout = System.currentTimeMillis() + 60000;
						wait = true;
						return true;
					case 21:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
				}

			wait_timeout = 0;
			wait = false;
			current_point++;

			if(current_point >= points.length)
				current_point = 0;

			addTaskMove(points[current_point], false);
			doTask();
			return true;
		}

		if(randomAnimation())
			return true;

		return false;
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{}
}