package commands.admin;

import l2p.extensions.Stat;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.TradeController;
import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.handler.AdminCommandHandler;
import l2p.gameserver.handler.IAdminCommandHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExBuySellList;
import l2p.gameserver.serverpackets.NpcHtmlMessage;

public class AdminShop implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_buy,
		admin_gmshop,
		admin_tax,
		admin_taxclear
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().UseGMShop)
			return false;

		switch(command)
		{
			case admin_buy:
				try
				{
					handleBuyRequest(activeChar, fullString.substring(10));
				}
				catch(IndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify buylist.");
				}
				break;
			case admin_gmshop:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/gmshops.htm"));
				break;
			case admin_tax:
				activeChar.sendMessage("TaxSum: " + Stat.getTaxSum());
				break;
			case admin_taxclear:
				Stat.addTax(-Stat.getTaxSum());
				activeChar.sendMessage("TaxSum: " + Stat.getTaxSum());
				break;
		}

		return true;
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleBuyRequest(L2Player activeChar, String command)
	{
		int val = -1;

		try
		{
			val = Integer.parseInt(command);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		NpcTradeList list = TradeController.getInstance().getBuyList(val);

		if(list != null)
			activeChar.sendPacket(new ExBuySellList(list, activeChar, 0));

		activeChar.sendActionFailed();
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