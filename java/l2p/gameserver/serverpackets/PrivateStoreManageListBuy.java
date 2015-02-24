package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;
import l2p.util.GArray;

public class PrivateStoreManageListBuy extends L2GameServerPacket
{
	private GArray<BuyItemInfo> buylist = new GArray<BuyItemInfo>();
	private int buyer_id;
	private long buyer_adena;
	private L2TradeList _list;

	/**
	 * Окно управления личным магазином продажи
	 * @param buyer
	 */
	public PrivateStoreManageListBuy(L2Player buyer)
	{
		buyer_id = buyer.getObjectId();
		buyer_adena = buyer.getAdena();

		int _id, body_part, type2;
		long count, store_price, owner_price;
		L2Item tempItem;
		for(TradeItem e : buyer.getBuyList())
		{
			_id = e.getItemId();
			if((tempItem = ItemTable.getInstance().getTemplate(_id)) == null)
				continue;

			count = e.getCount();
			store_price = e.getStorePrice();
			body_part = tempItem.getBodyPart();
			type2 = tempItem.getType2ForPackets();
			owner_price = e.getOwnersPrice();
			buylist.add(new BuyItemInfo(_id, count, store_price, body_part, type2, owner_price));
		}

		_list = new L2TradeList(0);
		for(L2ItemInstance item : buyer.getInventory().getItems())
			if(item != null && item.canBeTraded(buyer) && item.getItemId() != L2Item.ITEM_ID_ADENA)
			{
				for(TradeItem ti : buyer.getBuyList())
					if(ti.getItemId() == item.getItemId())
						continue;
				_list.addItem(item);
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xBD);
		//section 1
		writeD(buyer_id);
		writeQ(buyer_adena);

		//section2
		writeD(_list.getItems().size());//for potential sells
		for(L2ItemInstance temp : _list.getItems())
		{
			writeD(temp.getItemId());
			writeH(0); //show enchant lvl as 0, as you can't buy enchanted weapons
			writeQ(temp.getCount());
			writeQ(temp.getPriceToSell());
			writeH(0);
			writeD(temp.getBodyPart());
			writeH(temp.getItem().getType2ForPackets());
			writeItemElements(temp);
			writeItemRev152(temp);
		}

		//section 3
		writeD(buylist.size());//count for any items already added for sell
		for(BuyItemInfo e : buylist)
		{
			writeD(e._id);
			writeH(0);
			writeQ(e.count);
			writeQ(e.store_price);
			writeH(0);
			writeD(e.body_part);
			writeH(e.type2);
			writeQ(e.owner_price);
			writeQ(e.store_price);
			writeItemElements();
			writeItemRev152();
		}
	}

	static class BuyItemInfo
	{
		public int _id, body_part, type2;
		public long count, store_price, owner_price;

		public BuyItemInfo(int __id, long count2, long store_price2, int _body_part, int _type2, long owner_price2)
		{
			_id = __id;
			count = count2;
			store_price = store_price2;
			body_part = _body_part;
			type2 = _type2;
			owner_price = owner_price2;
		}
	}
}