package l2p.gameserver.skills.conditions;

import l2p.gameserver.skills.Env;
import l2p.util.ArrayMap;

public class ConditionFirstEffectSuccess extends Condition
{
	boolean _param;

	public ConditionFirstEffectSuccess(boolean param)
	{
		_param = param;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return _param == (ArrayMap.get(env.arraymap, Env.FirstEffectSuccess) == Integer.MAX_VALUE);
	}
}