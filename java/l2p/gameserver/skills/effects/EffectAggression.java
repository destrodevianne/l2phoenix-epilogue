package l2p.gameserver.skills.effects;

import l2p.gameserver.ai.L2PlayableAI;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.skills.Env;

public class EffectAggression extends L2Effect
{
	public EffectAggression(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isPlayable())
			((L2PlayableAI) _effected.getAI()).lockTarget(_effector);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isPlayable())
			((L2PlayableAI) _effected.getAI()).lockTarget(null);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}