package l2p.gameserver.communitybbs.Manager;

import l2p.gameserver.model.L2Player;
import l2p.util.Files;

public class SmsBBSManager extends BaseBBSManager
{
	public static SmsBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private SmsBBSManager()
	{}

	public void parsecmd(String command, L2Player player)
	{
		String content = Files.read("data/html/CommunityBoardPVP/303.htm", player);
		content = content.replace("%name%", player.getName());
		separateAndSend(content, player);
	}

	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{

	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SmsBBSManager _instance = new SmsBBSManager();
	}
}