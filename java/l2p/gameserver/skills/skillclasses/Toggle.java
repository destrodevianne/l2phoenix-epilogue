package l2p.gameserver.skills.skillclasses;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.templates.StatsSet;
import l2p.util.GArray;

public class Toggle extends L2Skill
{
	public Toggle(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		if(activeChar.getEffectList().getEffectsBySkillId(_id) != null)
		{
			activeChar.getEffectList().stopEffect(_id);
			activeChar.sendActionFailed();
			return;
		}

		getEffects(activeChar, activeChar, getActivateRate() > 0, false);
	}
}
