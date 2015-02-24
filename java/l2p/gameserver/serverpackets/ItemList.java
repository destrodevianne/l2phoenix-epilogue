package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.L2ItemInstance;

public class ItemList extends L2GameServerPacket
{
	private final L2ItemInstance[] _items;
	private final boolean _showWindow;

	public ItemList(L2Player cha, boolean showWindow)
	{
		_items = cha.getInventory().getItems();
		_showWindow = showWindow;
	}

	public ItemList(L2ItemInstance[] items, boolean showWindow)
	{
		_items = items;
		_showWindow = showWindow;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x11);
		writeH(_showWindow ? 1 : 0);

		writeH(_items.length);
		for(L2ItemInstance temp : _items)
		{
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getEquipSlot()); //order
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2ForPackets()); // item type2
			writeH(temp.getCustomType1()); // item type3
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel()); // enchant level
			writeH(temp.getCustomType2()); // item type3
			writeD(temp.getAugmentationId());
			writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
			writeItemElements(temp);
			writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0x00); // limited time item life remaining
			writeItemRev152(temp);
		}

		//?? GraciaEpilogue
		short some_count = 0; // кол-во хз чего
		writeH(some_count);
		if(some_count > 0)
		{
			writeC(0); //?
			for(int i = 0; i < some_count; i++)
				writeD(0); //?
		}
	}
}