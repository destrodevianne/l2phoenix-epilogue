package l2p.gameserver.serverpackets;

import java.util.TreeSet;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.model.items.Warehouse.WarehouseType;
import l2p.gameserver.templates.L2Item;

public class WareHouseDepositList extends L2GameServerPacket
{
	private int _whtype;
	private long char_adena;
	private TreeSet<L2ItemInstance> _itemslist = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);

	public WareHouseDepositList(L2Player cha, WarehouseType whtype)
	{
		cha.setUsingWarehouseType(whtype);
		_whtype = whtype.getPacketValue();
		char_adena = cha.getAdena();
		for(L2ItemInstance item : cha.getInventory().getItems())
			if(item != null && item.canBeStored(cha, _whtype == 1))
				_itemslist.add(item);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		writeH(_whtype);
		writeQ(char_adena);
		writeH(_itemslist.size());
		for(L2ItemInstance temp : _itemslist)
		{
			L2Item item = temp.getItem();
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeQ(temp.getCount());
			writeH(item.getType2ForPackets());
			writeH(temp.getCustomType1());
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(0x00); // ? 200
			writeD(temp.getObjectId()); // return value for define item (object_id)
			writeD(temp.getAugmentationId() & 0x0000FFFF);
			writeD(temp.getAugmentationId() >> 16);
			writeItemElements(temp);
			writeD(temp.getLifeTimeRemaining());
			writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0x00); // limited time item life remaining
			writeItemRev152(temp);
		}
	}
}