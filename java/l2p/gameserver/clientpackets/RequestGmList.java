package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.tables.GmListTable;
import l2p.util.Log;

public class RequestGmList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			GmListTable.sendListToPlayer(activeChar);
			Log.LogCommand(activeChar, 2, getType(), 1);
		}
	}
}