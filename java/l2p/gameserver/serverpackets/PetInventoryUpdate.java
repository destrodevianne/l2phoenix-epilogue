package l2p.gameserver.serverpackets;

import l2p.gameserver.model.items.L2ItemInstance;
import l2p.util.GArray;

public class PetInventoryUpdate extends L2GameServerPacket
{
	private GArray<L2ItemInstance> _items;

	public PetInventoryUpdate()
	{
		_items = new GArray<L2ItemInstance>();
	}

	public PetInventoryUpdate(GArray<L2ItemInstance> items)
	{
		_items = items;
	}

	public PetInventoryUpdate addNewItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.ADDED);
		_items.add(item);
		return this;
	}

	public PetInventoryUpdate addModifiedItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.MODIFIED);
		_items.add(item);
		return this;
	}

	public PetInventoryUpdate addRemovedItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.REMOVED);
		_items.add(item);
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xb4);
		writeH(_items.size());
		for(L2ItemInstance temp : _items)
		{
			writeH(temp.getLastChange());
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2ForPackets()); // item type2
			writeH(0x00); // ?
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart()); // rev 415   slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel()); // enchant level
			writeItemElements(temp);
			writeItemRev152(temp);
		}
	}
}