package commands.admin;

import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.handler.AdminCommandHandler;
import l2p.gameserver.handler.IAdminCommandHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.serverpackets.ItemList;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.util.Log;
import l2p.util.Rnd;

public class AdminCreateItem implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_itemcreate,
		admin_create_item,
		admin_spreaditem
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().UseGMShop)
			return false;

		switch(command)
		{
			case admin_itemcreate:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/itemcreation.htm"));
				break;
			case admin_create_item:
				try
				{
					if(wordList.length < 2)
					{
						activeChar.sendMessage("USAGE: create_item id [count]");
						return false;
					}

					int item_id = Integer.parseInt(wordList[1]);
					long item_count = wordList.length < 3 ? 1 : Long.parseLong(wordList[2]);
					createItem(activeChar, item_id, item_count);
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("USAGE: create_item id [count]");
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/itemcreation.htm"));
				break;
			case admin_spreaditem:
				try
				{
					int id = Integer.parseInt(wordList[1]);
					int num = wordList.length > 2 ? Integer.parseInt(wordList[2]) : 1;
					long count = wordList.length > 3 ? Long.parseLong(wordList[3]) : 1;
					for(int i = 0; i < num; i++)
					{
						L2ItemInstance createditem = ItemTable.getInstance().createItem(id);
						createditem.setCount(count);
						createditem.dropToTheGround(activeChar, Rnd.coordsRandomize(activeChar, 100));
					}
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("Specify a valid number.");
				}
				catch(StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Can't create this item.");
				}
				break;
		}

		return true;
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void createItem(L2Player activeChar, int id, long num)
	{
		L2ItemInstance createditem = ItemTable.getInstance().createItem(id);
		createditem.setCount(num);
		activeChar.getInventory().addItem(createditem);
		Log.LogItem(activeChar, Log.Adm_AddItem, createditem);
		if(!createditem.isStackable())
			for(long i = 0; i < num - 1; i++)
			{
				createditem = ItemTable.getInstance().createItem(id);
				activeChar.getInventory().addItem(createditem);
				Log.LogItem(activeChar, Log.Adm_AddItem, createditem);
			}
		activeChar.sendPacket(new ItemList(activeChar, true), SystemMessage.obtainItems(id, num, 0));
	}

	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}