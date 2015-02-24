package l2p.gameserver.clientpackets;

import l2p.extensions.multilang.CustomMessage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.Reflection;
import l2p.gameserver.model.entity.DimensionalRift;

public class RequestWithDrawalParty extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isInParty())
		{
			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("Вы не можете сейчас выйти из группы.");
				return;
			}
			Reflection r = activeChar.getParty().getReflection();
			if(r != null && r instanceof DimensionalRift && activeChar.getReflection().equals(r))
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestWithDrawalParty.Rift", activeChar));
			else if(r != null && activeChar.isInCombat())
				activeChar.sendMessage("Вы не можете сейчас выйти из группы.");
			else
				activeChar.getParty().oustPartyMember(activeChar);
		}
	}
}