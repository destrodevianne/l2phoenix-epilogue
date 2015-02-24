package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestTeleportBookMark extends L2GameClientPacket
{
	private int slot;

	@Override
	public void readImpl()
	{
		slot = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
			activeChar.bookmarks.tryTeleport(slot);
	}
}