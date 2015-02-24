package l2p.gameserver.clientpackets;

import l2p.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.tables.FriendsTable;

public class RequestFriendDel extends L2GameClientPacket
{
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
		FriendsTable.getInstance().TryFriendDelete(activeChar, _name);
	}
}