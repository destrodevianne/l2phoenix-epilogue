package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.util.Log;

public class RequestPetitionCancel extends L2GameClientPacket
{
	private String _text;

	@Override
	public void readImpl()
	{
		_text = readS(4096);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		Log.LogPetition(activeChar, 0, "Cancel: " + _text);
	}
}