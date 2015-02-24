package l2p.gameserver.serverpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

public class PrivateStoreListBuy extends L2GameServerPacket
{
	private int buyer_id;
	private long seller_adena;
	private ConcurrentLinkedQueue<TradeItem> _buyerslist;

	/**
	 * Список вещей в личном магазине покупки, показываемый продающему
	 * @param seller
	 * @param storePlayer
	 */
	public PrivateStoreListBuy(L2Player seller, L2Player storePlayer)
	{
		seller_adena = seller.getAdena();
		buyer_id = storePlayer.getObjectId();

		ConcurrentLinkedQueue<L2ItemInstance> sellerItems = seller.getInventory().getItemsList();
		_buyerslist = new ConcurrentLinkedQueue<TradeItem>();
		_buyerslist.addAll(storePlayer.getBuyList());

		for(TradeItem buyListItem : _buyerslist)
			buyListItem.setCurrentValue(0);

		for(L2ItemInstance sellerItem : sellerItems)
			for(TradeItem buyListItem : _buyerslist)
				if(sellerItem.getItemId() == buyListItem.getItemId() && sellerItem.canBeTraded(seller))
				{
					buyListItem.setCurrentValue(Math.min(buyListItem.getCount(), sellerItem.getCount()));
					continue;
				}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xBE);

		writeD(buyer_id);
		writeQ(seller_adena);
		writeD(_buyerslist.size());
		for(TradeItem buyersitem : _buyerslist)
		{
			L2Item tmp = ItemTable.getInstance().getTemplate(buyersitem.getItemId());
			writeD(buyersitem.getObjectId());
			writeD(buyersitem.getItemId());
			writeH(buyersitem.getEnchantLevel());

			writeQ(buyersitem.getCurrentValue()); //give max possible sell amount
			writeQ(tmp.getReferencePrice());
			writeH(0);
			writeD(tmp.getBodyPart());
			writeH(tmp.getType2ForPackets());

			writeQ(buyersitem.getOwnersPrice());
			writeQ(buyersitem.getCount()); // maximum possible tradecount
			writeItemElements(buyersitem);
			writeItemRev152(buyersitem);
		}
	}
}