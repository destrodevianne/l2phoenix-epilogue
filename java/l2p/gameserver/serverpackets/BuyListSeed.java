package l2p.gameserver.serverpackets;

import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.model.TradeItem;
import l2p.util.GArray;

/**
 * Format: c ddh[hdddhhd]
 * c - id (0xE8)
 *
 * d - money
 * d - manor id
 * h - size
 * [
 * h - item type 1
 * d - object id
 * d - item id
 * d - count
 * h - item type 2
 * h
 * d - price
 * ]
 */
public final class BuyListSeed extends L2GameServerPacket
{
	private int _manorId;
	private GArray<TradeItem> _list = new GArray<TradeItem>();
	private long _money;

	public BuyListSeed(NpcTradeList list, int manorId, long currentMoney)
	{
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xe9);

		writeQ(_money); // current money
		writeD(_manorId); // manor id

		writeH(_list.size()); // list length

		for(TradeItem item : _list)
		{
			writeH(0x04); // item->type1
			writeD(0x00); // objectId
			writeD(item.getItemId()); // item id
			writeQ(item.getCount());
			writeH(0x04); // item->type2
			writeH(0x00); // size of [dhhh]
			writeQ(item.getOwnersPrice());
		}
	}
}