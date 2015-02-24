package l2p.gameserver.skills.skillclasses;

import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Playable;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.instances.L2DecoyInstance;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.templates.StatsSet;
import l2p.util.GArray;

public class Aggression extends L2Skill
{
	private final boolean _unaggring;
	private final boolean _silent;

	public Aggression(StatsSet set)
	{
		super(set);
		_unaggring = set.getBool("unaggroing", false);
		_silent = set.getBool("silent", false);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		int effect = _effectPoint;

		if(isSSPossible() && (activeChar.getChargedSoulShot() || activeChar.getChargedSpiritShot() > 0))
			effect *= 2;

		for(L2Character target : targets)
			if(target != null)
			{
				if(!target.isNpc() && !target.isPlayable())
					continue;

				if(_unaggring)
				{
					if(target.isNpc() && activeChar.isPlayable())
						((L2Playable) activeChar).addDamageHate((L2NpcInstance) target, 0, -effect);
				}
				else if(activeChar instanceof L2DecoyInstance)
				{
					if(((L2DecoyInstance) activeChar).getPlayer() == target)
						return;
					target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
				}
				else
				{
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, effect);
					if(!_silent && target.isNpc())
						((L2NpcInstance) target).callFriends(activeChar);
				}

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}