package l2p.gameserver.skills.funcs;

import l2p.gameserver.skills.Env;
import l2p.gameserver.skills.Stats;

public class FuncAdd extends Func
{
	public FuncAdd(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public void calc(Env env)
	{
		env.value += _value;
	}
}
