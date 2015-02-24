package l2p.gameserver.skills.effects;

import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2Skill.SkillType;
import l2p.gameserver.skills.Env;

public final class EffectInvulnerable extends L2Effect
{
	public EffectInvulnerable(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isInvul())
			return false;
		L2Skill skill = _effected.getCastingSkill();
		if(skill != null && (skill.getSkillType() == SkillType.TAKECASTLE))
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.setIsInvul(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.setIsInvul(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}