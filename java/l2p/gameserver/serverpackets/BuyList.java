package l2p.gameserver.serverpackets;

import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.util.GArray;

@Deprecated
public class BuyList extends L2GameServerPacket
{
	private int _listId;
	private final GArray<TradeItem> _Buylist, _SellList, _RefundList;
	private long _money;
	private double _TaxRate = 0;

	public BuyList(NpcTradeList Buylist, L2Player activeChar)
	{
		_listId = Buylist.getListId();
		_money = activeChar.getAdena();
		activeChar.setBuyListId(_listId);

		_Buylist = cloneAndFilter(Buylist.getItems());
		_SellList = cloneAndFilter(null);
		_RefundList = cloneAndFilter(null);
	}

	public BuyList(NpcTradeList Buylist, L2Player activeChar, double taxRate)
	{
		_listId = Buylist.getListId();
		_money = activeChar.getAdena();
		_TaxRate = taxRate;
		activeChar.setBuyListId(_listId);

		_Buylist = cloneAndFilter(Buylist.getItems());
		_SellList = cloneAndFilter(null);
		_RefundList = cloneAndFilter(null);
	}

	protected static GArray<TradeItem> cloneAndFilter(GArray<TradeItem> list)
	{
		if(list == null)
			return null;

		GArray<TradeItem> ret = new GArray<TradeItem>(list.size());

		for(TradeItem item : list)
		{
			// А не пора ли обновить количество лимитированных предметов в трейд листе?
			if(item.getCurrentValue() < item.getCount() && item.getLastRechargeTime() + item.getRechargeTime() <= System.currentTimeMillis() / 60000)
			{
				item.setLastRechargeTime(item.getLastRechargeTime() + item.getRechargeTime());
				item.setCurrentValue(item.getCount());
			}

			if(item.getCurrentValue() == 0 && item.getCount() != 0)
				continue;

			ret.add(item);
		}

		return ret;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x07);
		writeQ(_money); // current money
		writeD(_listId);

		if(_Buylist == null)
			writeH(0);
		else
		{
			writeH(_Buylist.size());
			for(TradeItem item : _Buylist)
			{
				writeH(item.getItem().getType1()); // item type1
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeQ(item.getCurrentValue()); // max amount of items that a player can buy at a time (with this itemid)
				writeH(item.getItem().getType2ForPackets()); // item type2
				writeH(0); // getCustomType1?
				writeD(item.getItem().getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
				writeH(item.getEnchantLevel()); // enchant level
				writeH(0); // getCustomType1?
				writeH(0); // unknown
				writeQ((long) (item.getOwnersPrice() * (1 + _TaxRate)));
				writeItemElements(item);
			}
		}

		if(_SellList == null)
			writeH(0);
		else
		{
			writeH(_SellList.size());
			for(TradeItem item : _SellList)
			{
				writeH(item.getItem().getType1()); // item type1
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeQ(item.getCurrentValue()); // max amount of items that a player can buy at a time (with this itemid)
				writeH(item.getItem().getType2ForPackets()); // item type2
				writeH(0); // getCustomType1?
				writeD(item.getItem().getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
				writeH(item.getEnchantLevel()); // enchant level
				writeH(0); // getCustomType1?
				writeH(0); // unknown
				writeQ((long) (item.getOwnersPrice() * (1 + _TaxRate)));
				writeItemElements(item);
			}
		}

		if(_RefundList == null)
			writeH(0);
		else
		{
			writeH(_RefundList.size());
			for(TradeItem item : _RefundList)
			{
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeQ(item.getCurrentValue()); // max amount of items that a player can buy at a time (with this itemid)

				writeH(item.getItem().getType2ForPackets()); // item type2
				writeH(0); // getCustomType1?
				writeH(item.getEnchantLevel()); // enchant level
				writeH(0); // getCustomType1?
				//writeH(0); // unknown //TODO возможно нужно убирать какойто другой H

				writeQ((long) (item.getOwnersPrice() * (1 + _TaxRate)));
				writeItemElements(item);
			}
		}
	}
}