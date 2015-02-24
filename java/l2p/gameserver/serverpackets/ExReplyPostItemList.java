package l2p.gameserver.serverpackets;

import java.util.TreeSet;

import l2p.gameserver.clientpackets.RequestExPostItemList;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.templates.L2Item;

/**
 * Ответ на запрос создания нового письма.
 * Отсылается при получении {@link RequestExPostItemList}
 * Содержит список вещей, которые можно приложить к письму.
 */
public class ExReplyPostItemList extends L2GameServerPacket
{
	private TreeSet<L2ItemInstance> _itemslist = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);

	public ExReplyPostItemList(L2Player cha)
	{
		if(!cha.getPlayerAccess().UseTrade) // если не разрешен трейд передавать предметы нельзя
			return;

		String tradeBan = cha.getVar("tradeBan"); // если трейд забанен тоже
		if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			return;

		for(L2ItemInstance item : cha.getInventory().getItems())
			if(item != null && item.canBeTraded(cha))
				_itemslist.add(item);
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xB2);
		// TODO dx[ddQhhdhhhhhhhhhh]

		writeD(_itemslist.size());
		for(L2ItemInstance temp : _itemslist)
		{
			L2Item item = temp.getItem();
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeQ(temp.getCount());
			writeH(item.getType2ForPackets());
			writeH(temp.getCustomType1());
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeItemElements(temp);
			writeItemRev152(temp);
		}
	}
}