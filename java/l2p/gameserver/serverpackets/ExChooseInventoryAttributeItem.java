package l2p.gameserver.serverpackets;

public class ExChooseInventoryAttributeItem extends L2GameServerPacket
{
	private int _itemId;

	public ExChooseInventoryAttributeItem(int itemId)
	{
		_itemId = itemId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x62);
		writeD(_itemId);
	}
}