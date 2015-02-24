package ai;

import l2p.gameserver.ai.Fighter;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.util.Rnd;

/**
 * @author Diamond
 */
public class EvasGiftBox extends Fighter
{
	private static final int[] KISS_OF_EVA = new int[] { 1073, 3141, 3252 };

	private static final int Red_Coral = 9692;
	private static final int Crystal_Fragment = 9693;

	public EvasGiftBox(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor != null && killer != null)
		{
			L2Player player = killer.getPlayer();
			if(player != null && player.getEffectList().containEffectFromSkills(KISS_OF_EVA))
				actor.dropItem(player, Rnd.chance(50) ? Red_Coral : Crystal_Fragment, 1);
		}
		super.onEvtDead(killer);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}