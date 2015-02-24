package l2p.gameserver.serverpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

public class PrivateStoreListSell extends L2GameServerPacket
{
	private int seller_id;
	private long buyer_adena;
	private final boolean _package;
	private ConcurrentLinkedQueue<TradeItem> _sellList;

	/**
	 * Список вещей в личном магазине продажи, показываемый покупателю
	 * @param buyer
	 * @param seller
	 */
	public PrivateStoreListSell(L2Player buyer, L2Player seller)
	{
		seller_id = seller.getObjectId();
		buyer_adena = buyer.getAdena();
		_package = seller.getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL_PACKAGE;
		_sellList = seller.getSellList();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xA1);
		writeD(seller_id);
		writeD(_package ? 1 : 0);
		writeQ(buyer_adena);

		writeD(_sellList.size());
		for(TradeItem ti : _sellList)
		{
			L2Item tempItem = ItemTable.getInstance().getTemplate(ti.getItemId());
			writeD(tempItem.getType2ForPackets());
			writeD(ti.getObjectId());
			writeD(ti.getItemId());
			writeQ(ti.getCount());
			writeH(0);
			writeH(ti.getEnchantLevel());
			writeH(0x00);
			writeD(tempItem.getBodyPart());

			writeQ(ti.getOwnersPrice());
			writeQ(ti.getStorePrice());

			writeItemElements(ti);
			writeItemRev152(ti);
		}
	}
}