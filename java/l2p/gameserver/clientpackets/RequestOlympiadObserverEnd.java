package l2p.gameserver.clientpackets;

import l2p.Config;
import l2p.gameserver.model.L2Player;

/**
 * format ch
 * c: (id) 0xD0
 * h: (subid) 0x29
 *
 */
public class RequestOlympiadObserverEnd extends L2GameClientPacket
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
		if(Config.ENABLE_OLYMPIAD && activeChar.inObserverMode())
			activeChar.leaveOlympiadObserverMode();
	}
}