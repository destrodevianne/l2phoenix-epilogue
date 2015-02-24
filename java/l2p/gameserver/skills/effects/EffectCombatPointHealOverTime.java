package l2p.gameserver.skills.effects;

import l2p.gameserver.model.L2Effect;
import l2p.gameserver.skills.Env;
import l2p.gameserver.skills.Stats;

public class EffectCombatPointHealOverTime extends L2Effect
{
	public EffectCombatPointHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead() || _effected.isHealBlocked(true))
			return false;
		double addToCp = Math.max(0, Math.min(calc(), _effected.calcStat(Stats.CP_LIMIT, null, null) * _effected.getMaxCp() / 100. - _effected.getCurrentCp()));
		if(addToCp > 0)
			_effected.setCurrentCp(_effected.getCurrentCp() + addToCp);
		return true;
	}
}