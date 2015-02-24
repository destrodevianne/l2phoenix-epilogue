package l2p.gameserver.clientpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.extensions.multilang.CustomMessage;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.serverpackets.ChangeWaitType;
import l2p.gameserver.serverpackets.PrivateStoreMsgBuy;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

public class SetPrivateStoreBuyList extends L2GameClientPacket
{
	// format: cdb, b - array of (dhhdd)
	private int _count;
	private long[] _items; // count * 3

	@Override
	public void readImpl()
	{
		_count = readD();
		if(_count * 40 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new long[_count * 3];
		for(int i = 0; i < _count; i++)
		{
			_items[i * 3 + 0] = readD(); // item id

			readH();
			readH();

			_items[i * 3 + 1] = readQ(); // count
			_items[i * 3 + 2] = readQ(); // price

			if(_items[i * 3 + 0] < 1 || _items[i * 3 + 1] < 1 || _items[i * 3 + 0] < 0)
			{
				_items = null;
				break;
			}

			// TODO Gracia Final
			readC(); // FE
			readD(); // FF 00 00 00
			readD(); // 00 00 00 00
			readB(new byte[7]); // Completely Unknown
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_items == null || !activeChar.checksForShop(false))
		{
			L2TradeList.cancelStore(activeChar);
			return;
		}

		int maxSlots = activeChar.getTradeLimit();

		if(_count > maxSlots)
		{
			activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			L2TradeList.cancelStore(activeChar);
			return;
		}

		TradeItem temp;
		ConcurrentLinkedQueue<TradeItem> listbuy = new ConcurrentLinkedQueue<TradeItem>();
		long totalCost = 0;
		int count = 0;

		outer: for(int x = 0; x < _count; x++)
		{
			int itemId = (int) _items[x * 3 + 0];
			long itemCount = _items[x * 3 + 1];
			long itemPrice = _items[x * 3 + 2];
			if(!ItemTable.getInstance().isExists(itemId) || itemCount < 1 || itemPrice < 0 || itemId == L2Item.ITEM_ID_ADENA)
				continue;

			L2Item item = ItemTable.getInstance().getTemplate(itemId);
			if(item.getReferencePrice() / 2 > itemPrice)
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.SetPrivateStoreBuyList.TooLowPrice", activeChar).addItemName(item).addNumber(item.getReferencePrice() / 2));
				continue;
			}

			if(item.isStackable())
				for(TradeItem ti : listbuy)
					if(ti.getItemId() == itemId)
					{
						if(ti.getOwnersPrice() == itemPrice)
							ti.setCount(ti.getCount() + itemCount);
						continue outer;
					}

			temp = new TradeItem();
			temp.setItemId(itemId);
			temp.setCount(itemCount);
			temp.setOwnersPrice(itemPrice);
			totalCost += temp.getOwnersPrice() * temp.getCount();
			listbuy.add(temp);
			count++;
		}

		if(totalCost > activeChar.getAdena())
		{
			activeChar.sendPacket(Msg.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(count > 0)
		{
			activeChar.setBuyList(listbuy);
			activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_BUY);
			activeChar.broadcastPacket(new ChangeWaitType(activeChar, ChangeWaitType.WT_SITTING));
			activeChar.broadcastUserInfo(true);
			activeChar.broadcastPacket(new PrivateStoreMsgBuy(activeChar));
			activeChar.sitDown();
			return;
		}

		L2TradeList.cancelStore(activeChar);
	}
}