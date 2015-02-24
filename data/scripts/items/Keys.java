package items;

import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.IItemHandler;
import l2p.gameserver.handler.ItemHandler;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2Playable;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2DoorInstance;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.DoorTable;
import l2p.util.GArray;

public class Keys implements IItemHandler, ScriptFile
{
	private static int[] _itemIds = null;

	public Keys()
	{
		GArray<Integer> keys = new GArray<Integer>();
		for(L2DoorInstance door : DoorTable.getInstance().getDoors())
			if(door != null && door.key > 0)
				keys.add(door.key);
		_itemIds = new int[keys.size()];
		int i = 0;
		for(int id : keys)
		{
			_itemIds[i] = id;
			i++;
		}
	}

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2Player player = (L2Player) playable;

		if(item == null || item.getCount() < 1)
		{
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}

		L2Object target = player.getTarget();
		if(target == null || !target.isDoor())
		{
			player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return;
		}

		L2DoorInstance door = (L2DoorInstance) target;

		if(door.isOpen())
		{
			player.sendPacket(Msg.IT_IS_NOT_LOCKED);
			return;
		}

		if(door.key <= 0 || item.getItemId() != door.key) // ключ не подходит к двери
		{
			player.sendPacket(Msg.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR);
			return;
		}

		L2ItemInstance ri = player.getInventory().destroyItem(item, 1, true);
		player.sendPacket(SystemMessage.removeItems(ri.getItemId(), 1), new SystemMessage("l2p.gameserver.skills.skillclasses.Unlock.Success", player));
		door.openMe();
		door.onOpen();
	}

	public int[] getItemIds()
	{
		return _itemIds;
	}

	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}