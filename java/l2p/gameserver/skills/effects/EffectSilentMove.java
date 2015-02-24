package l2p.gameserver.skills.effects;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Playable;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.skills.Env;
import l2p.gameserver.skills.Stats;

public final class EffectSilentMove extends L2Effect
{
	public EffectSilentMove(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isPlayable())
			((L2Playable) _effected).setSilentMoving(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isPlayable())
			((L2Playable) _effected).setSilentMoving(false);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;

		if(!getSkill().isToggle())
			return false;

		double manaDam = calc();
		if(getSkill().isMagic())
			manaDam = _effected.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, manaDam, null, getSkill());
		else
			manaDam = _effected.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, manaDam, null, getSkill());

		if(manaDam > _effected.getCurrentMp())
		{
			_effected.sendPacket(Msg.NOT_ENOUGH_MP);
			_effected.sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}

		_effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}