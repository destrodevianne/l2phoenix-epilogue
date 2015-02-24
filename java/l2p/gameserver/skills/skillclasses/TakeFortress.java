package l2p.gameserver.skills.skillclasses;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.FortressSiegeManager;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.entity.siege.SiegeClan;
import l2p.gameserver.model.entity.siege.fortress.FortressSiege;
import l2p.gameserver.model.instances.L2StaticObjectInstance;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.StatsSet;
import l2p.util.GArray;

public class TakeFortress extends L2Skill
{
	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;

		if(activeChar == null || !activeChar.isPlayer())
			return false;

		L2Player player = (L2Player) activeChar;
		if(player.getClan() == null)
			return false;

		FortressSiege siege = FortressSiegeManager.getSiege(activeChar);
		if(siege == null || !siege.getCommanders().isEmpty())
			return false;
		if(siege.getAttackerClan(player.getClan()) == null)
			return false;
		if(player.isMounted())
			return false;

		if(first)
			for(SiegeClan sc : siege.getDefenderClans().values())
			{
				L2Clan clan = sc.getClan();
				if(clan != null)
					clan.broadcastToOnlineMembers(Msg.THE_OPPONENT_CLAN_HAS_BEGUN_TO_ENGRAVE_THE_RULER);
			}

		if(!player.isInRange(target, 85))
		{
			player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		return true;
	}

	public TakeFortress(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				if(!(target instanceof L2StaticObjectInstance))
					continue;
				L2Player player = (L2Player) activeChar;
				FortressSiege siege = FortressSiegeManager.getSiege(activeChar);
				if(siege != null && siege.getCommanders().isEmpty())
				{
					siege.announceToPlayer(new SystemMessage(SystemMessage.CLAN_S1_HAS_SUCCEEDED_IN_ENGRAVING_THE_RULER).addString(player.getClan().getName()), false, true);
					siege.Engrave(player.getClan(), target.getObjectId());
				}
			}
	}
}