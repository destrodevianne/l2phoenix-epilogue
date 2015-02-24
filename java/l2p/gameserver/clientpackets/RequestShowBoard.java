package l2p.gameserver.clientpackets;

import l2p.Config;
import l2p.gameserver.communitybbs.CommunityBoard;

public class RequestShowBoard extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	public void readImpl()
	{
		_unknown = readD(); //always 1
	}

	@Override
	public void runImpl()
	{
		CommunityBoard.getInstance().handleCommands(getClient(), Config.BBS_DEFAULT);
	}
}