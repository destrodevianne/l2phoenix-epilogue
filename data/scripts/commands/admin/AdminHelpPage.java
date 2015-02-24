package commands.admin;

import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.handler.AdminCommandHandler;
import l2p.gameserver.handler.IAdminCommandHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.NpcHtmlMessage;

public class AdminHelpPage implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_showhtml
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Menu)
			return false;

		switch(command)
		{
			case admin_showhtml:
				if(wordList.length != 2)
				{
					activeChar.sendMessage("Usage: //showhtml <file>");
					return false;
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/" + wordList[1]));
				break;
		}

		return true;
	}

	public static void showHelpHtml(L2Player targetChar, String content)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		targetChar.sendPacket(adminReply);
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
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