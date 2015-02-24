package l2p.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2p.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2EtcItem;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.templates.L2Item.Grade;
import l2p.util.Files;
import l2p.util.Log;

public class EnchantBBSManager extends BaseBBSManager
{
	private static EnchantBBSManager _Instance = null;

	public static EnchantBBSManager getInstance()
	{
		if(_Instance == null)
			_Instance = new EnchantBBSManager();
		return _Instance;
	}

	@Override
	public void parsecmd(String command, L2Player activeChar)
	{
		if(command.equals("_bbsechant"))
		{
			String name = "None Name";
			name = ItemTable.getInstance().getTemplate(Config.ALT_CB_ENCH_ITEM).getName();
			TextBuilder sb = new TextBuilder();
			sb.append("<table width=400>");
			L2ItemInstance arr[] = activeChar.getInventory().getItems();
			int len = arr.length;
			for(int i = 0; i < len; i++)
			{
				L2ItemInstance _item = arr[i];
				if(_item == null || (_item.getItem() instanceof L2EtcItem) || !_item.isEquipped() || _item.isHeroWeapon() || _item.getItem().getCrystalType() == Grade.NONE || _item.getItemId() >= 7816 && _item.getItemId() <= 7831 || _item.isShadowItem() || _item.isCommonItem() || _item.isWear() || _item.getEnchantLevel() >= Config.ENCHANT_MAX + 1)
					continue;
				sb.append((new StringBuilder()).append("<tr><td><img src=icon.").append(_item.getItem().getIcon()).append(" width=32 height=32></td><td>").toString());
				sb.append((new StringBuilder()).append("<font color=\"LEVEL\">").append(_item.getItem().getName()).append(" ").append(_item.getEnchantLevel() <= 0 ? "" : (new StringBuilder()).append("</font><br1><font color=3293F3>Заточено на: +").append(_item.getEnchantLevel()).toString()).append("</font><br1>").toString());

				sb.append((new StringBuilder()).append("Заточка за: <font color=\"LEVEL\">").append(name).append("</font>").toString());
				sb.append("<img src=\"l2ui.squaregray\" width=\"170\" height=\"1\">");
				sb.append("</td><td>");
				sb.append((new StringBuilder()).append("<button value=\"Обычная\" action=\"bypass -h _bbsechant;enchlistpage;").append(_item.getObjectId()).append("\" width=75 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString());
				sb.append("</td><td>");
				sb.append((new StringBuilder()).append("<button value=\"Атрибут\" action=\"bypass -h _bbsechant;enchlistpageAtrChus;").append(_item.getObjectId()).append("\" width=75 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString());
				sb.append("</td></tr>");

			}
			sb.append("</table>");
			String content = Files.read("data/html/CommunityBoardPVP/804.htm", activeChar);
			content = content.replace("%enchanter%", sb.toString());
			separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbsechant;enchlistpage;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int ItemForEchantObjID = Integer.parseInt(st.nextToken());
			int price = 0;
			double mod = 0;
			String name = "None Name";
			name = ItemTable.getInstance().getTemplate(Config.ALT_CB_ENCH_ITEM).getName();
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			if(EhchantItem.getItem().getCrystalType() == Grade.D)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 2;
					mod = 1.1;
				}
				else
				{
					price = 1;
					mod = 1.1;
				}

			}
			else if(EhchantItem.getItem().getCrystalType() == Grade.C)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 4;
					mod = 1.2;
				}
				else
				{
					price = 2;
					mod = 1.2;
				}
			}
			else if(EhchantItem.getItem().getCrystalType() == Grade.B)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 6;
					mod = 1.3;
				}
				else
				{
					price = 4;
					mod = 1.3;
				}
			}
			else if(EhchantItem.getItem().getCrystalType() == Grade.A)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 8;
					mod = 1.4;
				}
				else
				{
					price = 6;
					mod = 1.4;
				}
			}
			else if(EhchantItem.getItem().getCrystalType() == Grade.S)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 10;
					mod = 1.5;
				}
				else
				{
					price = 8;
					mod = 1.5;
				}
			}
			else if(EhchantItem.getItem().getCrystalType() == Grade.S80)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 12;
					mod = 1.6;
				}
				else
				{
					price = 10;
					mod = 1.6;
				}
			}
			else if(EhchantItem.getItem().getCrystalType() == Grade.S84)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 14;
					mod = 1.7;
				}
				else
				{
					price = 12;
					mod = 1.7;
				}
			}
			TextBuilder sb = new TextBuilder();
			sb.append("Для обычной заточки выбрана вещь:<br1><table width=300>");
			sb.append((new StringBuilder()).append("<tr><td width=32><img src=icon.").append(EhchantItem.getItem().getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>").toString());
			sb.append((new StringBuilder()).append("<font color=\"LEVEL\">").append(EhchantItem.getItem().getName()).append(" ").append(EhchantItem.getEnchantLevel() <= 0 ? "" : (new StringBuilder()).append("</font><br1><font color=3293F3>Заточено на: +").append(EhchantItem.getEnchantLevel()).toString()).append("</font><br1>").toString());

			sb.append((new StringBuilder()).append("Заточка производится за: <font color=\"LEVEL\">").append(name).append("</font>").toString());
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append((new StringBuilder()).append("<td width=32><img src=icon.").append(EhchantItem.getItem().getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>").toString());
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br1>");
			sb.append("<br1>");
			sb.append("<table border=0 width=400><tr><td width=200>");
			sb.append("<button value=\"На +5 (Цена:" + (int) ((double) price * (mod + 2)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;5;" + (int) ((double) price * (mod + 2)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +6 (Цена:" + (int) ((double) price * (mod + 4)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;6;" + (int) ((double) price * (mod + 4)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +7 (Цена:" + (int) ((double) price * (mod + 6)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;7;" + (int) ((double) price * (mod + 6)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +8 (Цена:" + (int) ((double) price * (mod + 8)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;8;" + (int) ((double) price * (mod + 8)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +9 (Цена:" + (int) ((double) price * (mod + 10)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;9;" + (int) ((double) price * (mod + 10)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +10 (Цена:" + (int) ((double) price * (mod + 12)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;10;" + (int) ((double) price * (mod + 12)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +11 (Цена:" + (int) ((double) price * (mod + 14)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;11;" + (int) ((double) price * (mod + 14)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +12 (Цена:" + (int) ((double) price * (mod + 16)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;12;" + (int) ((double) price * (mod + 16)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td><td width=200>");
			sb.append("<button value=\"На +13 (Цена:" + (int) ((double) price * (mod + 18)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;13;" + (int) ((double) price * (mod + 18)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +14 (Цена:" + (int) ((double) price * (mod + 24)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;14;" + (int) ((double) price * (mod + 24)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +15 (Цена:" + (int) ((double) price * (mod + 38)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;15;" + (int) ((double) price * (mod + 38)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +16 (Цена:" + (int) ((double) price * (mod + 48)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;16;" + (int) ((double) price * (mod + 48)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +17 (Цена:" + (int) ((double) price * (mod + 58)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;17;" + (int) ((double) price * (mod + 58)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +18 (Цена:" + (int) ((double) price * (mod + 68)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;18;" + (int) ((double) price * (mod + 68)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +19 (Цена:" + (int) ((double) price * (mod + 88)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;19;" + (int) ((double) price * (mod + 88)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1>");
			sb.append("<button value=\"На +20 (Цена:" + (int) ((double) price * (mod + 108)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;20;" + (int) ((double) price * (mod + 108)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td></tr></table><br1><button value=\"Назад\" action=\"bypass -h _bbsechant\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			String content = Files.read("data/html/CommunityBoardPVP/804.htm", activeChar);
			content = content.replace("%enchanter%", sb.toString());
			separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbsechant;enchlistpageAtrChus;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int ItemForEchantObjID = Integer.parseInt(st.nextToken());
			String name = "None Name";
			name = ItemTable.getInstance().getTemplate(Config.ALT_CB_ENCH_ITEM).getName();
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);

			TextBuilder sb = new TextBuilder();
			sb.append("Для заточки на атрибут выбрана вещь:<br1><table width=300>");
			sb.append((new StringBuilder()).append("<tr><td width=32><img src=icon.").append(EhchantItem.getItem().getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>").toString());
			sb.append((new StringBuilder()).append("<font color=\"LEVEL\">").append(EhchantItem.getItem().getName()).append(" ").append(EhchantItem.getEnchantLevel() <= 0 ? "" : (new StringBuilder()).append("</font><br1><font color=3293F3>Заточено на: +").append(EhchantItem.getEnchantLevel()).toString()).append("</font><br1>").toString());

			sb.append((new StringBuilder()).append("Заточка производится за: <font color=\"LEVEL\">").append(name).append("</font>").toString());
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append((new StringBuilder()).append("<td width=32><img src=icon.").append(EhchantItem.getItem().getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>").toString());
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br1>");
			sb.append("<br1>");
			sb.append("<table border=0 width=400><tr><td width=200>");
			sb.append("<center><img src=icon.etc_wind_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Wind \" action=\"bypass -h _bbsechant;enchlistpageAtr;2;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_earth_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Earth \" action=\"bypass -h _bbsechant;enchlistpageAtr;3;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_fire_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Fire \" action=\"bypass -h _bbsechant;enchlistpageAtr;0;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td><td width=200>");
			sb.append("<center><img src=icon.etc_water_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Water \" action=\"bypass -h _bbsechant;enchlistpageAtr;1;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_holy_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Divine \" action=\"bypass -h _bbsechant;enchlistpageAtr;4;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_unholy_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=\"Dark \" action=\"bypass -h _bbsechant;enchlistpageAtr;5;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td></tr></table><br1><button value=\"Назад\" action=\"bypass -h _bbsechant\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			String content = Files.read("data/html/CommunityBoardPVP/804.htm", activeChar);
			content = content.replace("%enchanter%", sb.toString());
			separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbsechant;enchlistpageAtr;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int AtributType = Integer.parseInt(st.nextToken());
			int ItemForEchantObjID = Integer.parseInt(st.nextToken());
			int price = 0;
			double mod = 0;
			String ElementName = "";
			if(AtributType == 0)
			{
				ElementName = "Fire";
			}
			else if(AtributType == 1)
			{
				ElementName = "Water";
			}
			else if(AtributType == 2)
			{
				ElementName = "Wind";
			}
			else if(AtributType == 3)
			{
				ElementName = "Earth";
			}
			else if(AtributType == 4)
			{
				ElementName = "Divine";
			}
			else if(AtributType == 5)
			{
				ElementName = "Dark";
			}
			String name = "None Name";
			name = ItemTable.getInstance().getTemplate(Config.ALT_CB_ENCH_ITEM).getName();
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			if(EhchantItem.getItem().getCrystalType() == Grade.S)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 10;
					mod = 1.5;
				}
				else
				{
					price = 8;
					mod = 1.5;
				}
			}
			else if(EhchantItem.getItem().getCrystalType() == Grade.S80)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 12;
					mod = 1.6;
				}
				else
				{
					price = 10;
					mod = 1.6;
				}
			}
			else if(EhchantItem.getItem().getCrystalType() == Grade.S84)
			{
				if(EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 14;
					mod = 1.7;
				}
				else
				{
					price = 12;
					mod = 1.7;
				}
			}
			TextBuilder sb = new TextBuilder();
			sb.append("Выбран элемент: <font color=\"LEVEL\">" + ElementName + "</font><br1> Для заточки выбрана вещь:<br1><table width=300>");
			sb.append((new StringBuilder()).append("<tr><td width=32><img src=icon.").append(EhchantItem.getItem().getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>").toString());
			sb.append((new StringBuilder()).append("<font color=\"LEVEL\">").append(EhchantItem.getItem().getName()).append(" ").append(EhchantItem.getEnchantLevel() <= 0 ? "" : (new StringBuilder()).append("</font><br1><font color=3293F3>Заточено на: +").append(EhchantItem.getEnchantLevel()).toString()).append("</font><br1>").toString());

			sb.append((new StringBuilder()).append("Заточка производится за: <font color=\"LEVEL\">").append(name).append("</font>").toString());
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append((new StringBuilder()).append("<td width=32><img src=icon.").append(EhchantItem.getItem().getIcon()).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>").toString());
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br1>");
			sb.append("<br1>");
			if(EhchantItem.getItem().getCrystalType() == Grade.S || EhchantItem.getItem().getCrystalType() == Grade.S80 || EhchantItem.getItem().getCrystalType() == Grade.S84)
			{
				sb.append("<table border=0 width=400><tr><td width=200>");
				sb.append("<button value=\"На +25 (Цена:" + (int) ((double) price * (mod + 68)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgoAtr;25;" + AtributType + ";" + (int) ((double) price * (mod + 68)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("<br1>");
				sb.append("<button value=\"На +50 (Цена:" + (int) ((double) price * (mod + 88)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgoAtr;50;" + AtributType + ";" + (int) ((double) price * (mod + 88)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("<br1>");
				sb.append("<button value=\"На +75 (Цена:" + (int) ((double) price * (mod + 108)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgoAtr;75;" + AtributType + ";" + (int) ((double) price * (mod + 108)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("</td><td width=200>");
				sb.append("<button value=\"На +100 (Цена:" + (int) ((double) price * (mod + 128)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgoAtr;100;" + AtributType + ";" + (int) ((double) price * (mod + 128)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("<br1>");
				sb.append("<button value=\"На +125 (Цена:" + (int) ((double) price * (mod + 148)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgoAtr;125;" + AtributType + ";" + (int) ((double) price * (mod + 148)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("<br1>");
				sb.append("<button value=\"На +150 (Цена:" + (int) ((double) price * (mod + 168)) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgoAtr;150;" + AtributType + ";" + (int) ((double) price * (mod + 168)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
				sb.append("</td></tr></table><br1>");
			}
			else
			{
				sb.append("<table border=0 width=400><tr><td width=200>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<center><font color=\"LEVEL\">Заточка данной вещи не возможна!</font></center>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("</td></tr></table><br1>");
			}
			sb.append("<button value=\"Назад\" action=\"bypass -h _bbsechant\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			String content = Files.read("data/html/CommunityBoardPVP/804.htm", activeChar);
			content = content.replace("%enchanter%", sb.toString());
			separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbsechant;enchantgo;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int EchantVal = Integer.parseInt(st.nextToken());
			int EchantPrice = Integer.parseInt(st.nextToken());
			int EchantObjID = Integer.parseInt(st.nextToken());
			L2Item item = ItemTable.getInstance().getTemplate(Config.ALT_CB_ENCH_ITEM);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(EchantObjID);
			if(pay != null && pay.getCount() >= EchantPrice)
			{
				activeChar.getInventory().destroyItem(pay, EchantPrice, true);
				activeChar.getInventory().unEquipItemInSlot(EhchantItem.getEquipSlot());
				EhchantItem.setEnchantLevel(EchantVal);
				activeChar.getInventory().equipItem(EhchantItem, false);
				activeChar.sendPacket(new InventoryUpdate().addModifiedItem(EhchantItem));
				activeChar.broadcastUserInfo(true);
				activeChar.sendMessage("" + EhchantItem.getItem().getName() + " было заточена до " + EchantVal + ". Спасибо.");
				Log.add(activeChar.getName() + " enchant item:" + EhchantItem.getItem().getName() + " val: " + EchantVal + "", "wmzSeller");
				parsecmd("_bbsechant", activeChar);
			}
			else
			{
				activeChar.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			}

		}
		if(command.startsWith("_bbsechant;enchantgoAtr;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int EchantVal = Integer.parseInt(st.nextToken());
			int AtrType = Integer.parseInt(st.nextToken());
			int EchantPrice = Integer.parseInt(st.nextToken());
			int EchantObjID = Integer.parseInt(st.nextToken());
			L2Item item = ItemTable.getInstance().getTemplate(Config.ALT_CB_ENCH_ITEM);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(EchantObjID);
			if(pay != null && pay.getCount() >= EchantPrice)
			{
				activeChar.getInventory().destroyItem(pay, EchantPrice, true);
				activeChar.getInventory().unEquipItemInSlot(EhchantItem.getEquipSlot());
				EhchantItem.setAttributeElement((byte) AtrType, EchantVal, true);
				activeChar.getInventory().equipItem(EhchantItem, false);
				activeChar.sendPacket(new InventoryUpdate().addModifiedItem(EhchantItem));
				activeChar.broadcastUserInfo(true);
				activeChar.sendMessage("" + EhchantItem.getItem().getName() + " было заточена до " + EchantVal + ". Спасибо.");
				Log.add(activeChar.getName() + " enchant item:" + EhchantItem.getItem().getName() + " val: " + EchantVal + " AtributType:" + AtrType, "wmzSeller");
				parsecmd("_bbsechant", activeChar);
			}
			else
			{
				activeChar.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			}

		}

	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player activeChar)
	{

	}
}