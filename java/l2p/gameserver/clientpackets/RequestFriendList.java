package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.L2FriendList;

public class RequestFriendList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		sendPacket(new L2FriendList(getClient().getActiveChar()));
	}
}