package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestObserverEnd extends L2GameClientPacket
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
		if(activeChar.inObserverMode())
			if(activeChar.getOlympiadObserveId() > 0)
				activeChar.leaveOlympiadObserverMode();
			else
				activeChar.leaveObserverMode();
	}
}