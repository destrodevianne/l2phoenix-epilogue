package l2p.gameserver.skills.effects;

import l2p.gameserver.model.L2Effect;
import l2p.gameserver.skills.Env;

public final class EffectHealBlock extends L2Effect
{
	public EffectHealBlock(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		if(_effected.isHealBlocked(true))
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.setHealBlocked(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.setHealBlocked(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}