package l2p.gameserver.skills.effects;

import static l2p.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Summon;
import l2p.gameserver.skills.Env;

public class EffectBetray extends L2Effect
{
	public EffectBetray(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _effected;
			summon.setPossessed(true);
			summon.getAI().Attack(summon.getPlayer(), true, false);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _effected;
			summon.setPossessed(false);
			summon.getAI().setIntention(AI_INTENTION_ACTIVE);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}