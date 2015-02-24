package l2p.gameserver.skills.effects;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Skill.AddedSkill;
import l2p.gameserver.skills.Env;
import l2p.util.GArray;

public class EffectCallSkills extends L2Effect
{
	public EffectCallSkills(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		for(AddedSkill as : getSkill().getAddedSkills())
		{
			GArray<L2Character> targets = new GArray<L2Character>();
			targets.add(getEffected());
			getEffector().callSkill(as.getSkill(), targets, false);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}