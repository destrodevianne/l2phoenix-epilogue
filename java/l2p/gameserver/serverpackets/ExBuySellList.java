package l2p.gameserver.serverpackets;

import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.util.GArray;

public class ExBuySellList extends L2GameServerPacket
{
	private int _listId, _done;
	private final GArray<TradeItem> _Buylist;
	private final TreeSet<L2ItemInstance> _SellList;
	private final ConcurrentLinkedQueue<L2ItemInstance> _RefundList;
	private long _money;
	private double _TaxRate = 0;

	public ExBuySellList(NpcTradeList Buylist, L2Player activeChar, double taxRate)
	{
		if(Buylist != null)
		{
			_listId = Buylist.getListId();
			_Buylist = cloneAndFilter(Buylist.getItems());
			activeChar.setBuyListId(_listId);
		}
		else
			_Buylist = null;

		_money = activeChar.getAdena();
		_TaxRate = taxRate;
		_RefundList = activeChar.getInventory().getRefundItemsList();

		_SellList = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);

		for(L2ItemInstance item : activeChar.getInventory().getItemsList())
			if(item.getItem().isSellable() && item.canBeTraded(activeChar) && item.getReferencePrice() > 0)
				_SellList.add(item);
	}

	public ExBuySellList done()
	{
		_done = 1;
		return this;
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
		writeC(EXTENDED_PACKET);
		writeH(0xB7);

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
				writeItemRev152(item);
			}
		}

		if(_SellList == null)
			writeH(0);
		else
		{
			writeH(_SellList.size());
			for(L2ItemInstance item : _SellList)
			{
				writeH(item.getItem().getType1());
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeQ(item.getCount());
				writeH(item.getItem().getType2ForPackets());
				writeH(item.getCustomType1());
				writeD(item.getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(item.getCustomType2());
				writeH(0x00); // unknown
				writeQ(item.getItem().getReferencePrice() / 2);
				writeItemElements(item);
				writeItemRev152(item);
			}
		}

		if(_RefundList == null)
			writeH(0);
		else
		{
			writeH(_RefundList.size());
			//hx[ddQhhhhQhhhhhhhh h]
			for(L2ItemInstance item : _RefundList)
			{
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeQ(item.getCount());
				writeH(item.getItem().getType2ForPackets()); // checked
				writeH(item.getCustomType1()); // unknown				
				writeH(item.getEnchantLevel()); // ?
				writeH(item.getCustomType2()); // unknown				
				writeQ(item.getItem().getReferencePrice() / 2);
				writeItemElements(item);
				writeItemRev152(item);
			}
		}

		writeC(_done);
	}
}