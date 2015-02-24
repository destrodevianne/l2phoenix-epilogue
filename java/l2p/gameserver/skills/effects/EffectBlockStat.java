package l2p.gameserver.skills.effects;

import l2p.gameserver.model.L2Effect;
import l2p.gameserver.skills.Env;
import l2p.gameserver.skills.skillclasses.NegateStats;

public class EffectBlockStat extends L2Effect
{
	public EffectBlockStat(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.addBlockStats(((NegateStats) _skill).getNegateStats());
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.removeBlockStats(((NegateStats) _skill).getNegateStats());
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}