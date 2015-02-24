package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.network.L2GameClient.GameClientState;
import l2p.gameserver.serverpackets.CharSelected;
import l2p.util.AutoBan;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;

	/**
	 * Format: cdhddd
	 */
	@Override
	public void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	public void runImpl()
	{
		L2GameClient client = getClient();

		if(client.getActiveChar() != null)
			return;

		L2Player activeChar = client.loadCharFromDisk(_charSlot);
		if(activeChar == null)
			return;

		if(AutoBan.isBanned(activeChar.getObjectId()))
		{
			activeChar.setAccessLevel(-100);
			activeChar.logout(false, false, true, true);
			return;
		}

		if(activeChar.getAccessLevel() < 0)
			activeChar.setAccessLevel(0);

		client.setState(GameClientState.IN_GAME);

		sendPacket(new CharSelected(activeChar, client.getSessionId().playOkID1));
	}
}