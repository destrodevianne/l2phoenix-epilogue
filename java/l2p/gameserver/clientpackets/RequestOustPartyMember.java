package l2p.gameserver.clientpackets;

import l2p.Config;
import l2p.extensions.multilang.CustomMessage;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.Reflection;
import l2p.gameserver.model.entity.DimensionalRift;

public class RequestOustPartyMember extends L2GameClientPacket
{
	//Format: cS
	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Party party = activeChar.getParty();
		if(party != null && party.isLeader(activeChar))
		{
			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("Вы не можете сейчас выйти из группы.");
				return;
			}
			Reflection r = party.getReflection();
			L2Player oustPlayer = party.getPlayerByName(_name);
			if(r != null && r instanceof DimensionalRift && oustPlayer != null && oustPlayer.getReflection().equals(r))
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustPartyMember.CantOustInRift", activeChar));
			else if(r != null && !(r instanceof DimensionalRift))
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustPartyMember.CantOustInDungeon", activeChar));
			else
				party.oustPartyMember(_name);
		}
	}
}