package ai;

import l2p.extensions.scripts.Functions;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.util.Rnd;

/**
 * AI монахов в Monastery of Silence<br>
 * - агрятся на чаров с оружием в руках
 * - перед тем как броситься в атаку кричат
 *
 * @author SYS
 */
public class MoSMonk extends Fighter
{
	public MoSMonk(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && Rnd.chance(50))
			Functions.npcSay(actor, "You cannot carry a weapon without authorization!");
		super.onIntentionAttack(target);
	}

	@Override
	public void checkAggression(L2Character target)
	{
		if(target.getActiveWeaponInstance() == null)
			return;
		super.checkAggression(target);
	}
}