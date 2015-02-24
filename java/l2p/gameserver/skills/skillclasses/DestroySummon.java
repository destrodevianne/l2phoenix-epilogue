package l2p.gameserver.skills.skillclasses;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2Summon;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.skills.Formulas;
import l2p.gameserver.templates.StatsSet;
import l2p.util.GArray;

public class DestroySummon extends L2Skill
{
	public DestroySummon(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{

				if(getActivateRate() > 0 && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(target.getName()).addSkillName(getId(), getLevel()));
					continue;
				}

				if(target.isSummon())
				{
					((L2Summon) target).unSummon();
					getEffects(activeChar, target, getActivateRate() > 0, false);
				}
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}