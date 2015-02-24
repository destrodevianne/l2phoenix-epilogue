package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExJumpToLocation;

public class RequestExJump extends L2GameClientPacket
{
	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		sendPacket(new ExJumpToLocation(activeChar.getObjectId(), activeChar.getLoc(), activeChar.getLoc()));
		System.out.println(getType());
	}

	@Override
	public void readImpl()
	{}
}