package l2p.gameserver.model.entity.residence;

import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

public class TeleportLocation
{
	public final long _price;
	public final L2Item _item;
	public final String _name;
	public final String _target;

	public TeleportLocation(String target, int item, long price, String name)
	{
		_target = target;
		_price = price;
		_name = name;
		_item = ItemTable.getInstance().getTemplate(item);
	}
}