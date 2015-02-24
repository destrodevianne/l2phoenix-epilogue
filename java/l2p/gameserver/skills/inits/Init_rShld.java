package l2p.gameserver.skills.inits;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.skills.Env;

public final class Init_rShld extends InitFunc
{
	@Override
	public void calc(Env env)
	{
		L2Character cha = env.character;
		if(cha == null || cha.isPlayer())
			env.value = 0.;
		else
			env.value = cha.getTemplate().baseShldRate;
	}
}