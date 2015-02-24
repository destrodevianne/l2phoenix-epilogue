package l2p.gameserver.skills.effects;

import l2p.gameserver.model.L2Effect;
import l2p.gameserver.skills.Env;

public class EffectNegateMusic extends L2Effect
{
	public EffectNegateMusic(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
		for(L2Effect e : _effected.getEffectList().getAllEffects())
			if(e.getSkill().isMusic())
				e.exit();
		return false;
	}
}