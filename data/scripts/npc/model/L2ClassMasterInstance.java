package npc.model;

import java.util.StringTokenizer;

import l2p.Config;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.instances.L2MerchantInstance;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.NpcTable;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.templates.L2NpcTemplate;
import l2p.util.Files;
import l2p.util.Util;

public final class L2ClassMasterInstance extends L2MerchantInstance implements ScriptFile
{
	public L2ClassMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private String makeMessage(L2Player player)
	{
		ClassId classId = player.getClassId();

		int jobLevel = classId.getLevel();
		int level = player.getLevel();

		StringBuilder html = new StringBuilder();
		if(Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() || !Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
			jobLevel = 4;
		if((level >= 20 && jobLevel == 1 || level >= 40 && jobLevel == 2 || level >= 76 && jobLevel == 3) && Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
		{
			L2Item item = ItemTable.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEM);
			if(Config.CLASS_MASTERS_PRICE_LIST[jobLevel] > 0)
				html.append("Price: ").append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LIST[jobLevel])).append(" ").append(item.getName()).append("<br1>");
			for(ClassId cid : ClassId.values())
			{
				// Инспектор является наследником trooper и warder, но сменить его как профессию нельзя,
				// т.к. это сабкласс. Наследуется с целью получения скилов родителей.
				if(cid == ClassId.inspector)
					continue;
				if(cid.childOf(classId) && cid.getLevel() == classId.getLevel() + 1)
					html.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_change_class ").append(cid.getId()).append(" ").append(Config.CLASS_MASTERS_PRICE_LIST[jobLevel]).append("\">").append(cid.name()).append("</a><br>");
			}
			player.sendPacket(new NpcHtmlMessage(player, this).setHtml(html.toString()));
		}
		else
			switch(jobLevel)
			{
				case 1:
					html.append("Come back here when you reached level 20 to change your class.");
					break;
				case 2:
					html.append("Come back here when you reached level 40 to change your class.");
					break;
				case 3:
					html.append("Come back here when you reached level 76 to change your class.");
					break;
				case 4:
					html.append("There is no class changes for you any more.");
					break;
			}
		return html.toString();
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(player, this, null, 0);
		String html = Files.read("data/html/custom/31860.htm", player);
		if(Config.SERVICES_CLASSMASTERS_BASIC_SHOP)
			html += "<br><a action=\"bypass -h npc_%objectId%_Buy 318601\">Buy basic items</a>";
		if(Config.SERVICES_CLASSMASTERS_COL_SHOP)
			html += "<br><a action=\"bypass -h npc_%objectId%_Multisell 1\">Special shop</a>";
		if(Config.SERVICES_CHANGE_NICK_ENABLED)
			html += "<br><a action=\"bypass -h scripts_services.Rename:rename_page\">Nick change</a>";
		if(Config.SERVICES_CHANGE_SEX_ENABLED)
			html += "<br><a action=\"bypass -h scripts_services.Rename:changesex_page\">Sex change</a>";
		if(Config.SERVICES_CHANGE_BASE_ENABLED)
			html += "<br><a action=\"bypass -h scripts_services.Rename:changebase_page\">Base class change</a>";
		if(Config.SERVICES_SEPARATE_SUB_ENABLED)
			html += "<br><a action=\"bypass -h scripts_services.Rename:separate_page\">Separate subclass</a>";
		if(Config.SERVICES_CHANGE_NICK_COLOR_ENABLED)
			html += "<br><a action=\"bypass -h scripts_services.NickColor:list\">Nick color change</a>";
		if(Config.SERVICES_RATE_BONUS_ENABLED)
			html += "<br><a action=\"bypass -h scripts_services.RateBonus:list\">Luck boost</a>";
		if(Config.SERVICES_NOBLESS_SELL_ENABLED && !player.isNoble())
			html += "<br><a action=\"bypass -h scripts_services.NoblessSell:get\">Become a Nobless</a>";
		if(Config.SERVICES_CHANGE_CLAN_NAME_ENABLED)
			html += "<br><a action=\"bypass -h scripts_services.Rename:rename_clan_page\">Clan name change</a>";
		if(Config.SERVICES_HOW_TO_GET_COL)
			html += "<br><a action=\"bypass -h scripts_services.RateBonus:howtogetcol\">How to get Coin of Luck</a>";
		msg.setHtml(html);
		msg.replace("%classmaster%", makeMessage(player));
		player.sendPacket(msg);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command);
		if(st.nextToken().equals("change_class"))
		{
			short val = Short.parseShort(st.nextToken());
			long price = Long.parseLong(st.nextToken());
			L2Item item = ItemTable.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEM);
			L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
			if(pay != null && pay.getCount() >= price)
			{
				player.getInventory().destroyItem(pay, price, true);
				changeClass(player, val);
			}
			else if(Config.CLASS_MASTERS_PRICE_ITEM == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void changeClass(L2Player player, short val)
	{
		if(player.getClassId().getLevel() == 3)
			player.sendPacket(Msg.YOU_HAVE_COMPLETED_THE_QUEST_FOR_3RD_OCCUPATION_CHANGE_AND_MOVED_TO_ANOTHER_CLASS_CONGRATULATIONS); // для 3 профы
		else
			player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS); // для 1 и 2 профы

		player.setClassId(val, false);
		player.broadcastUserInfo(true);
	}

	@Override
	public void onLoad()
	{
		L2NpcTemplate t = NpcTable.getTemplate(31860);
		t.title = "Service Manager";
	}

	@Override
	public void onReload()
	{
		L2NpcTemplate t = NpcTable.getTemplate(31860);
		t.title = "Service Manager";
	}

	@Override
	public void onShutdown()
	{}

}