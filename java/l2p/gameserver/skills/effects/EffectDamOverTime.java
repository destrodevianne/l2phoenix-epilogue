package l2p.gameserver.skills.effects;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.skills.Env;
import l2p.gameserver.skills.Stats;

public class EffectDamOverTime extends L2Effect
{
	// TODO уточнить уровни 1, 2, 9, 10, 11, 12
	private static int[] bleed = new int[] { 12, 17, 25, 34, 44, 54, 62, 67, 72, 77, 82, 87 };
	private static int[] poison = new int[] { 11, 16, 24, 32, 41, 50, 58, 63, 68, 72, 77, 82 };

	public EffectDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;

		double damage = calc();

		if(damage < 2)
			switch(getEffectType())
			{
				case Poison:
					damage = poison[getStackOrder() - 1] * getPeriod() / 1000;
					break;
				case Bleed:
					damage = bleed[getStackOrder() - 1] * getPeriod() / 1000;
					break;
			}

		damage = _effector.calcStat(getSkill().isMagic() ? Stats.MAGIC_DAMAGE : Stats.PHYSICAL_DAMAGE, damage, _effected, getSkill());

		if(damage > _effected.getCurrentHp() - 1 && !_effected.isNpc())
		{
			if(!getSkill().isOffensive())
				_effected.sendPacket(Msg.NOT_ENOUGH_HP);
			return false;
		}

		if(getSkill().getAbsorbPart() > 0 && !_effected.isDoor() && !_effector.isHealBlocked(false))
			_effector.setCurrentHp(getSkill().getAbsorbPart() * Math.min(_effected.getCurrentHp(), damage) + _effector.getCurrentHp(), false);

		_effected.reduceCurrentHp(damage, _effected.isPlayer() ? _effected : _effector, getSkill(), !_effected.isNpc() && _effected != _effector, _effected != _effector, _effector.isNpc() || getSkill().isToggle() || _effected == _effector, false);

		return true;
	}
}