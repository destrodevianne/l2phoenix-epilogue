package l2p.gameserver.skills.skillclasses;

import l2p.Config;
import l2p.extensions.multilang.CustomMessage;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.skills.Formulas;
import l2p.gameserver.templates.StatsSet;
import l2p.util.GArray;
import l2p.util.Rnd;

public class DeleteHateOfMe extends L2Skill
{
	private final boolean _cancelSelfTarget;

	public DeleteHateOfMe(StatsSet set)
	{
		super(set);
		_cancelSelfTarget = set.getBool("cancelSelfTarget", false);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{

				boolean success = _id == SKILL_BLUFF ? false : Rnd.chance(getActivateRate());

				if(_id != SKILL_BLUFF && Config.SKILLS_SHOW_CHANCE && activeChar.isPlayer() && !((L2Player) activeChar).getVarB("SkillsHideChance"))
					activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.Formulas.Chance", activeChar).addString(getName()).addNumber(getActivateRate()));

				if(_id == SKILL_BLUFF ? Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()) : success)
				{
					if(target.isNpc())
					{
						L2NpcInstance npc = (L2NpcInstance) target;
						activeChar.removeFromHatelist(npc, true);
						npc.getAI().clearTasks();
						npc.getAI().setAttackTarget(null);
						if(npc.isNoTarget())
						{
							npc.getAI().setGlobalAggro(System.currentTimeMillis() + 10000);
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						}
					}

					if(_cancelSelfTarget)
						activeChar.setTarget(null);
				}

				// Для Bluff шанс прохождения эффекта скила расчитывается как для шоковых атак, причем отдельно
				if(success || _id == SKILL_BLUFF)
					getEffects(activeChar, target, _id == SKILL_BLUFF, false);
			}
	}
}