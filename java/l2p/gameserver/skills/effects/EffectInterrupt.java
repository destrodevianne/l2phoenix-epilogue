package l2p.gameserver.skills.effects;

import l2p.gameserver.model.L2Effect;
import l2p.gameserver.skills.Env;

public class EffectInterrupt extends L2Effect
{
	public EffectInterrupt(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!getEffected().isRaid())
			getEffected().abortCast(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}