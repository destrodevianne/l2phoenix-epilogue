package l2p.gameserver.communitybbs;

import java.util.HashMap;
import java.util.StringTokenizer;

import l2p.Config;
import l2p.gameserver.TradeController;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.communitybbs.Manager.BuffBBSManager;
import l2p.gameserver.communitybbs.Manager.ClanBBSManager;
import l2p.gameserver.communitybbs.Manager.ClassBBSManager;
import l2p.gameserver.communitybbs.Manager.EnchantBBSManager;
import l2p.gameserver.communitybbs.Manager.FailBBSManager;
import l2p.gameserver.communitybbs.Manager.FriendsBBSManager;
import l2p.gameserver.communitybbs.Manager.PostBBSManager;
import l2p.gameserver.communitybbs.Manager.RegionBBSManager;
import l2p.gameserver.communitybbs.Manager.SmsBBSManager;
import l2p.gameserver.communitybbs.Manager.StatBBSManager;
import l2p.gameserver.communitybbs.Manager.TeleportBBSManager;
import l2p.gameserver.communitybbs.Manager.TopBBSManager;
import l2p.gameserver.communitybbs.Manager.TopicBBSManager;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Multisell;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.ExBuySellList;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.ShowBoard;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

public class CommunityBoard
{
	private static CommunityBoard _instance;
	private static int MONEY_ID = 4357;
	public static final String PVPCB_FILE = "./config/pvpcommunityboard.properties";

	public static CommunityBoard getInstance()
	{

		if(_instance == null)
			_instance = new CommunityBoard();

		return _instance;
	}

	public void handleCommands(L2GameClient client, String command)
	{
		L2Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(!Config.ALLOW_COMMUNITYBOARD)
		{
			activeChar.sendPacket(Msg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}
		if(!Config.ALLOW_PVPCB_ABNORMAL)
		{
			if(activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isInVehicle() || activeChar.isFlying() || activeChar.isInFlyingTransform())
			{
				FailBBSManager.getInstance().parsecmd(command, activeChar);
				return;
			}
		}

		if(command.startsWith("_bbsclan"))
			ClanBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsmemo"))
			TopicBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbstopics"))
			TopicBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsposts"))
			PostBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbstop"))
			TopBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbshome"))
			TopBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsloc"))
			RegionBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_friend") || command.startsWith("_block"))
			FriendsBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsgetfav"))
			ShowBoard.separateAndSend("<html><body><br><br><center>Закладки пока не реализованы.</center><br><br></body></html>", activeChar);
		else if(command.startsWith("_mail"))
			ShowBoard.separateAndSend("<html><body><br><br><center>Почта пока не реализована.</center><br><br></body></html>", activeChar);
		else if(command.startsWith("_bbsteleport;"))
			if(!Config.ALLOW_PVPCB_TELEPORT)
			{
				FailBBSManager.getInstance().parsecmd(command, activeChar);
				return;
			}
			else
			{
				TeleportBBSManager.getInstance().parsecmd(command, activeChar);
			}
		else if(command.startsWith("_bbsechant"))
			if(!Config.ALLOW_PVPCB_ECHANT)
			{
				FailBBSManager.getInstance().parsecmd(command, activeChar);
				return;
			}
			else
			{
				EnchantBBSManager.getInstance().parsecmd(command, activeChar);
			}
		else if(command.startsWith("_bbsclass"))
			if(!Config.ALLOW_PVPCB_CLASSMASTER)
			{
				FailBBSManager.getInstance().parsecmd(command, activeChar);
				return;
			}
			else
			{
				ClassBBSManager.getInstance().parsecmd(command, activeChar);
			}
		else if(command.startsWith("_bbssms;"))
			SmsBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsbuff;"))
			BuffBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsstat;"))
			StatBBSManager.getInstance().parsecmd(command, activeChar);
		else if(command.startsWith("_bbsmultisell;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
			L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(st.nextToken()), activeChar, 0);
		}
		else if(command.startsWith("_bbssell;"))
		{
			TradeController.NpcTradeList list = TradeController.getInstance().getBuyList(-1);
			activeChar.sendPacket(new L2GameServerPacket[] { new ExBuySellList(list, activeChar, 0) });
		}
		else if(command.startsWith("_bbsscripts;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);

			String com = st.nextToken();
			String[] word = com.split("\\s+");
			String[] args = com.substring(word[0].length()).trim().split("\\s+");
			String[] path = word[0].split(":");
			if(path.length != 2)
			{
				System.out.println("Bad Script bypass!");
				return;
			}

			HashMap<String, Object> variables = new HashMap<String, Object>();
			variables.put("npc", null);
			activeChar.callScripts(path[0], path[1], word.length == 1 ? new Object[] {} : new Object[] { args }, variables);
		}
		else if(command.startsWith("_bbsscripts_ret;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String page = st.nextToken();

			String com = st.nextToken();
			String[] word = com.split("\\s+");
			String[] args = com.substring(word[0].length()).trim().split("\\s+");
			String[] path = word[0].split(":");
			if(path.length != 2)
			{
				System.out.println("Bad Script bypass!");
				return;
			}
			HashMap<String, Object> variables = new HashMap<String, Object>();
			variables.put("npc", null);
			Object subcontent = activeChar.callScripts(path[0], path[1], word.length == 1 ? new Object[] {} : new Object[] { args }, variables);

			TopBBSManager.getInstance().showTopPage(activeChar, page, String.valueOf(subcontent), path[0]);
		}
		else if(command.startsWith("_bbssps;"))
		{
			int price = 1;
			L2Item item = ItemTable.getInstance().getTemplate(4357);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			if(pay != null && pay.getCount() >= price)
			{
				activeChar.getInventory().destroyItem(pay, (long) price, true);
				activeChar.setSp(activeChar.getSp() + 10000000);
				activeChar.sendMessage("Вы получили 10kk SP");
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				activeChar.broadcastUserInfo(true);
			}
			else
			{
				activeChar.sendMessage("У Вас нет денег, мне очень жаль.");
			}
		}
		else if(command.startsWith("_bbsspa;"))
		{
			int price = 100000000;
			L2Item item = ItemTable.getInstance().getTemplate(57);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			if(pay != null && pay.getCount() >= price)
			{
				activeChar.getInventory().destroyItem(pay, (long) price, true);
				activeChar.setSp(activeChar.getSp() + 10000000);
				activeChar.sendMessage("Вы получили 10kk SP");
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				activeChar.broadcastUserInfo(true);
			}
			else
			{
				activeChar.sendMessage("У Вас нет денег, мне очень жаль.");
			}
		}
		else if(command.startsWith("_bbsnobles;"))
		{

			if(!activeChar.isNoble())
			{
				if(!checkCondition(activeChar, 30))
					return;

				if(activeChar.getSubLevel() < 75)
				{
					activeChar.sendMessage("Чтобы стать дворянином вы должны прокачать сабкласс до 75-го уровня");
					return;
				}

				L2Item item = ItemTable.getInstance().getTemplate(MONEY_ID);
				L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
				if(pay != null && pay.getCount() >= 30)
				{
					activeChar.getInventory().destroyItem(pay, 30, true);

					Olympiad.addNoble(activeChar);
					activeChar.setNoble(true);
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), SocialAction.VICTORY));
					activeChar.updatePledgeClass();
					activeChar.updateNobleSkills();
					activeChar.sendPacket(new SkillList(activeChar));
					activeChar.broadcastUserInfo(true);
				}
				else
				{
					activeChar.sendMessage("У Вас нет денег, мне очень жаль.");
				}
			}
			else
			{
				activeChar.sendMessage("Вам это вовсе ненужно!");
			}
		}
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>Функция: " + command + " пока не реализована</center><br><br></body></html>", activeChar);
	}

	public static boolean checkCondition(L2Player activeChar, int CoinCount)
	{
		synchronized (activeChar)
		{
			L2ItemInstance Coin = activeChar.getInventory().getItemByItemId(MONEY_ID);

			if(activeChar.isSitting())
				return false;
			if(Coin.getCount() < CoinCount)
			{
				activeChar.sendMessage("У Вас нет денег, мне очень жаль.");
				return false;
			}

			return true;
		}
	}

	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;
		if(!Config.ALLOW_COMMUNITYBOARD)
		{
			activeChar.sendPacket(Msg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}

		if(url.equals("Topic"))
			TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else if(url.equals("Post"))
			PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else if(url.equals("Region"))
			RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		else if(url.equals("Notice"))
		{
			if(arg4.length() > L2Clan.NOTICE_MAX_LENGHT)
			{
				ShowBoard.separateAndSend("<html><body><br><br><center>Вы ввели слишком длинное сообщение, оно будет сохранено не полностью.</center><br><br></body></html>", activeChar);
				return;
			}
			ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>Функция: " + url + " пока не реализована</center><br><br></body></html>", activeChar);
	}
}