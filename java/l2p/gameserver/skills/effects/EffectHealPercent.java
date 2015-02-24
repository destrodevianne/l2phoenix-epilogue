package l2p.gameserver.skills.effects;

import l2p.gameserver.model.L2Effect;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.skills.Env;
import l2p.gameserver.skills.Stats;

public class EffectHealPercent extends L2Effect
{
	public EffectHealPercent(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effector.isPlayable() && _effected.isMonster())
			return false;
		return super.checkCondition();
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead() || _effected.isHealBlocked(true))
			return false;
		double base = calc() * _effected.getMaxHp() / 100;
		double newHp = base * _effected.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
		double addToHp = Math.max(0, Math.min(newHp, _effected.calcStat(Stats.HP_LIMIT, null, null) * _effected.getMaxHp() / 100. - _effected.getCurrentHp()));
		_effected.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToHp)));
		if(addToHp > 0)
			_effected.setCurrentHp(addToHp + _effected.getCurrentHp(), false);
		return false;
	}
}