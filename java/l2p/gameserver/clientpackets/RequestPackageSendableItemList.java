package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.PackageSendableList;

/**
 * Format: cd
 * @author SYS
 */
public class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int _characterObjectId;

	@Override
	public void readImpl()
	{
		_characterObjectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || !activeChar.getPlayerAccess().UseWarehouse)
			return;
		activeChar.tempInventoryDisable();
		activeChar.sendPacket(new PackageSendableList(activeChar, _characterObjectId));
	}
}