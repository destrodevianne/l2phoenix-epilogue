package l2p.gameserver.serverpackets;

import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.templates.L2Item;
import l2p.util.GArray;

public class ShopPreviewList extends L2GameServerPacket
{
	private int _listId;
	private TradeItem[] _list;
	private long _money;
	private int _expertise;

	public ShopPreviewList(NpcTradeList list, long currentMoney, int expertiseIndex)
	{
		_listId = list.getListId();
		GArray<TradeItem> lst = list.getItems();
		_list = lst.toArray(new TradeItem[lst.size()]);
		_money = currentMoney;
		_expertise = expertiseIndex;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xf5);

		writeD(0x13c0); //?
		writeQ(_money);
		writeD(_listId);

		int newlength = 0;
		for(TradeItem item : _list)
			if(item.getItem().getCrystalType().ordinal() <= _expertise && item.getItem().isEquipable())
				newlength++;
		writeH(newlength);

		for(TradeItem item : _list)
			if(item.getItem().getCrystalType().ordinal() <= _expertise && item.getItem().isEquipable())
			{
				writeD(item.getItemId());
				writeH(item.getItem().getType2ForPackets()); // item type2
				writeH(item.getItem().getType1() == L2Item.TYPE1_ITEM_QUESTITEM_ADENA ? 0x00 : item.getItem().getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
				writeQ(10);
			}
	}
}