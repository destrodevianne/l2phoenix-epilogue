package l2p.gameserver.model;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;
import l2p.Config;
import l2p.extensions.multilang.CustomMessage;
import l2p.gameserver.model.base.MultiSellEntry;
import l2p.gameserver.model.base.MultiSellIngredient;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.serverpackets.MultiSellList;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.templates.L2Weapon;
import l2p.gameserver.templates.L2Item.Grade;
import l2p.gameserver.templates.L2Weapon.WeaponType;
import l2p.util.GArray;
import l2p.util.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Multisell list manager
 */
public class L2Multisell
{
	private static Logger _log = Logger.getLogger(L2Multisell.class.getName());
	private FastMap<Integer, MultiSellListContainer> entries = new FastMap<Integer, MultiSellListContainer>();
	private static L2Multisell _instance = new L2Multisell();

	public static final String NODE_PRODUCTION = "production";
	public static final String NODE_INGRIDIENT = "ingredient";

	public MultiSellListContainer getList(int id)
	{
		return entries.get(id);
	}

	public L2Multisell()
	{
		parseData();
	}

	public void reload()
	{
		parseData();
	}

	public static L2Multisell getInstance()
	{
		return _instance;
	}

	private synchronized void parseData()
	{
		entries.clear();
		parse();
		loadHardcoded();
	}

	public static class MultiSellListContainer
	{
		private int _listId;
		private boolean _showall = true;
		private boolean keep_enchanted = false;
		private boolean is_dutyfree = false;
		private boolean nokey = false;
		GArray<MultiSellEntry> entries = new GArray<MultiSellEntry>();

		public void setListId(int listId)
		{
			_listId = listId;
		}

		public int getListId()
		{
			return _listId;
		}

		public void setShowAll(boolean bool)
		{
			_showall = bool;
		}

		public boolean isShowAll()
		{
			return _showall;
		}

		public void setNoTax(boolean bool)
		{
			is_dutyfree = bool;
		}

		public boolean isNoTax()
		{
			return is_dutyfree;
		}

		public void setNoKey(boolean bool)
		{
			nokey = bool;
		}

		public boolean isNoKey()
		{
			return nokey;
		}

		public void setKeepEnchant(boolean bool)
		{
			keep_enchanted = bool;
		}

		public boolean isKeepEnchant()
		{
			return keep_enchanted;
		}

		public void addEntry(MultiSellEntry e)
		{
			entries.add(e);
		}

		public GArray<MultiSellEntry> getEntries()
		{
			return entries;
		}

		public boolean isEmpty()
		{
			return entries.isEmpty();
		}
	}

	private void hashFiles(String dirname, GArray<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if(!dir.exists())
		{
			_log.config("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		File[] files = dir.listFiles();
		for(File f : files)
			if(f.getName().endsWith(".xml"))
				hash.add(f);
			else if(f.isDirectory() && !f.getName().equals(".svn"))
				hashFiles(dirname + "/" + f.getName(), hash);
	}

	public void addMultiSellListContainer(int id, MultiSellListContainer list)
	{
		if(entries.containsKey(id))
			_log.warning("MultiSell redefined: " + id);

		list.setListId(id);
		entries.put(id, list);
	}

	public MultiSellListContainer remove(String s)
	{
		return remove(new File(s));
	}

	public MultiSellListContainer remove(File f)
	{
		return remove(Integer.parseInt(f.getName().replaceAll(".xml", "")));
	}

	public MultiSellListContainer remove(int id)
	{
		return entries.remove(id);
	}

	public void parseFile(File f)
	{
		int id = 0;
		try
		{
			id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error loading file " + f, e);
			return;
		}
		Document doc = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(f);
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error loading file " + f, e);
			return;
		}
		try
		{
			addMultiSellListContainer(id, parseDocument(doc, id));
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error in file " + f, e);
		}
	}

	private void parse()
	{
		new File("log/game/multiselldebug.txt").delete();
		GArray<File> files = new GArray<File>();
		hashFiles("multisell", files);
		for(File f : files)
			parseFile(f);
	}

	protected MultiSellListContainer parseDocument(Document doc, int id)
	{
		MultiSellListContainer list = new MultiSellListContainer();
		int entId = 1;

		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if("list".equalsIgnoreCase(n.getNodeName()))
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if("item".equalsIgnoreCase(d.getNodeName()))
					{
						MultiSellEntry e = parseEntry(d, id);
						if(e != null)
						{
							e.setEntryId(entId++);
							list.addEntry(e);
						}
					}
					else if("config".equalsIgnoreCase(d.getNodeName()))
					{
						list.setShowAll(XMLUtil.getAttributeBooleanValue(d, "showall", true));
						list.setNoTax(XMLUtil.getAttributeBooleanValue(d, "notax", false));
						list.setKeepEnchant(XMLUtil.getAttributeBooleanValue(d, "keepenchanted", false));
						list.setNoKey(XMLUtil.getAttributeBooleanValue(d, "nokey", false));
					}

		return list;
	}

	protected MultiSellEntry parseEntry(Node n, int MultiSellId)
	{
		MultiSellEntry entry = new MultiSellEntry();

		l2p.util.Log.add(MultiSellId + " loading new entry", "multiselldebug");
		GArray<String> debuglist = new GArray<String>();

		for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			//l2p.util.Log.add(MultiSellId + " processing node " + d.getNodeName(), "multiselldebug");
			debuglist.add(d.getNodeName() + " " + d.getAttributes() + " " + d.getNodeName().hashCode() + " " + d.getNodeName().length());
			if(NODE_INGRIDIENT.equalsIgnoreCase(d.getNodeName()))
			{
				int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
				long count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
				int enchant = 0, element = L2Item.ATTRIBUTE_NONE, elementValue = 0;
				if(d.getAttributes().getNamedItem("enchant") != null)
					enchant = Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue());
				if(d.getAttributes().getNamedItem("element") != null)
					element = Integer.parseInt(d.getAttributes().getNamedItem("element").getNodeValue());
				if(d.getAttributes().getNamedItem("elementValue") != null)
					elementValue = Integer.parseInt(d.getAttributes().getNamedItem("elementValue").getNodeValue());

				l2p.util.Log.add(MultiSellId + " loaded ingredient " + id + " count " + count, "multiselldebug");
				entry.addIngredient(new MultiSellIngredient(id, count, enchant, element, elementValue));
			}
			else if(NODE_PRODUCTION.equalsIgnoreCase(d.getNodeName()))
			{
				int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
				long count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
				int enchant = 0, element = L2Item.ATTRIBUTE_NONE, elementValue = 0;
				if(d.getAttributes().getNamedItem("enchant") != null)
					enchant = Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue());
				if(d.getAttributes().getNamedItem("element") != null)
					element = Integer.parseInt(d.getAttributes().getNamedItem("element").getNodeValue());
				if(d.getAttributes().getNamedItem("elementValue") != null)
					elementValue = Integer.parseInt(d.getAttributes().getNamedItem("elementValue").getNodeValue());

				if(!Config.ALT_ALLOW_SHADOW_WEAPONS && id > 0)
				{
					L2Item item = ItemTable.getInstance().getTemplate(id);
					if(item != null && item.isShadowItem() && item.isWeapon() && !Config.ALT_ALLOW_SHADOW_WEAPONS)
						return null;
				}

				l2p.util.Log.add(MultiSellId + " loaded product " + id + " count " + count, "multiselldebug");
				entry.addProduct(new MultiSellIngredient(id, count, enchant, element, elementValue));
			}
			else
				l2p.util.Log.add(MultiSellId + " skipping node " + d.getNodeName(), "multiselldebug");
		}

		if(entry.getIngredients().isEmpty() || entry.getProduction().isEmpty())
		{
			l2p.util.Log.add(MultiSellId + " wrong node", "multiselldebug");
			l2p.util.Log.add(MultiSellId + " LIST: " + debuglist.toString(), "multiselldebug");
			for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				l2p.util.Log.add(d.getNodeName() + " " + d.getAttributes() + " " + d.getNodeName().hashCode() + " " + d.getNodeName().length() + " IsProduction: " + NODE_PRODUCTION.equalsIgnoreCase(d.getNodeName()), "multiselldebug");
			return null;
		}

		if(entry.getIngredients().size() == 1 && entry.getProduction().size() == 1 && entry.getIngredients().get(0).getItemId() == 57)
		{
			L2Item item = ItemTable.getInstance().getTemplate(entry.getProduction().get(0).getItemId());
			if(item == null)
			{
				_log.warning("WARNING!!! MultiSell [" + MultiSellId + "] Production [" + entry.getProduction().get(0).getItemId() + "] is null");
				return null;
			}
			if(MultiSellId < 70000 || MultiSellId > 70010) // Все кроме GM Shop
				if(item.getReferencePrice() > entry.getIngredients().get(0).getItemCount())
					_log.warning("WARNING!!! MultiSell [" + MultiSellId + "] Production '" + item.getName() + "' [" + entry.getProduction().get(0).getItemId() + "] price is lower than referenced | " + item.getReferencePrice() + " > " + entry.getIngredients().get(0).getItemCount());
			//return null;
		}

		return entry;
	}

	private static long[] parseItemIdAndCount(String s)
	{
		if(s == null || s.isEmpty())
			return null;
		String[] a = s.split(":");
		try
		{
			long id = Integer.parseInt(a[0]);
			long count = a.length > 1 ? Long.parseLong(a[1]) : 1;
			return new long[] { id, count };
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static MultiSellEntry parseEntryFromStr(String s)
	{
		if(s == null || s.isEmpty())
			return null;

		String[] a = s.split("->");
		if(a.length != 2)
			return null;

		long[] ingredient, production;
		if((ingredient = parseItemIdAndCount(a[0])) == null || (production = parseItemIdAndCount(a[1])) == null)
			return null;

		MultiSellEntry entry = new MultiSellEntry();
		entry.addIngredient(new MultiSellIngredient((int) ingredient[0], ingredient[1]));
		entry.addProduct(new MultiSellIngredient((int) production[0], production[1]));
		return entry;
	}

	public void SeparateAndSend(int listId, L2Player player, double taxRate)
	{
		for(int i : Config.ALT_DISABLED_MULTISELL)
			if(i == listId)
			{
				player.sendMessage(new CustomMessage("common.Disabled", player));
				return;
			}

		MultiSellListContainer list = generateMultiSell(listId, player, taxRate);
		if(list == null)
			return;
		MultiSellListContainer temp = new MultiSellListContainer();
		int page = 1;

		temp.setListId(list.getListId());

		// Запоминаем отсылаемый лист, чтобы не подменили
		player.setMultisell(list);

		for(MultiSellEntry e : list.getEntries())
		{
			if(temp.getEntries().size() == Config.MULTISELL_SIZE)
			{
				player.sendPacket(new MultiSellList(temp, page, 0));
				page++;
				temp = new MultiSellListContainer();
				temp.setListId(list.getListId());
			}
			temp.addEntry(e);
		}

		player.sendPacket(new MultiSellList(temp, page, 1));
	}

	private MultiSellListContainer generateMultiSell(int listId, L2Player player, double taxRate)
	{
		MultiSellListContainer list = new MultiSellListContainer();
		list._listId = listId;

		// Hardcoded  - обмен вещей на равноценные
		GArray<L2ItemInstance> _items;
		if(listId == 9999)
		{
			list.setShowAll(false);
			list.setKeepEnchant(true);
			list.setNoTax(true);
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance itm : inv.getItems())
				if(itm.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !itm.getItem().isSa() // SA менять нельзя
						&& !itm.getItem().isRare() // Rare менять нельзя
						&& !itm.getItem().isCommonItem() // Common менять нельзя
						&& !itm.getItem().isPvP() // PvP менять нельзя
						&& itm.canBeTraded(player) // универсальная проверка
						&& !itm.isStackable() //
						&& itm.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& itm.getItem().getCrystalType() != Grade.NONE //
						&& itm.getReferencePrice() <= Config.ALT_MAMMON_EXCHANGE //
						&& itm.getItem().getCrystalCount() > 0 //
						&& itm.getItem().isTradeable() //
						&& (itm.getCustomFlags() & L2ItemInstance.FLAG_NO_TRADE) != L2ItemInstance.FLAG_NO_TRADE //
				)
					_items.add(itm);

			for(final L2ItemInstance itm : _items)
				for(L2Weapon i : ItemTable.getInstance().getAllWeapons())
					if(i.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !i.isSa() // На SA менять нельзя
							&& !i.isRare() // На Rare менять нельзя
							&& !i.isCommonItem() // На Common менять нельзя
							&& !i.isPvP() // На PvP менять нельзя
							&& !i.isShadowItem() // На Shadow менять нельзя
							&& i.isTradeable() // можно использовать чтобы запретить менять специальные вещи
							&& i.getItemId() != itm.getItemId() //
							&& i.getType2() == L2Item.TYPE2_WEAPON //
							&& itm.getItem().getCrystalType() != Grade.NONE //
							&& i.getItemType() == WeaponType.DUAL == (itm.getItem().getItemType() == WeaponType.DUAL) //
							&& itm.getItem().getCrystalType() == i.getCrystalType() //
							&& itm.getItem().getCrystalCount() == i.getCrystalCount() //
					)
					{
						final int entry = new int[] { itm.getItemId(), i.getItemId(), itm.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, i.getItemId(), 1, itm.getEnchantLevel());
						possibleEntry.addIngredient(new MultiSellIngredient(itm.getItemId(), 1, itm.getEnchantLevel()));
						list.entries.add(possibleEntry);
					}
		}

		// Hardcoded  - обмен вещей с доплатой за AA
		else if(listId == 9998)
		{
			list.setShowAll(false);
			list.setKeepEnchant(false);
			list.setNoTax(true);
			final Inventory inv = player.getInventory();
			_items = new GArray<L2ItemInstance>();
			for(final L2ItemInstance itm : inv.getItems())
				if(itm.getItem().getAdditionalName().isEmpty() // Менять можно только обычные предметы
						&& !itm.getItem().isSa() // SA менять нельзя
						&& !itm.getItem().isRare() // Rare менять нельзя
						&& !itm.getItem().isCommonItem() // Common менять нельзя
						&& !itm.getItem().isPvP() // PvP менять нельзя
						&& !itm.getItem().isShadowItem() // Shadow менять нельзя
						&& !itm.isTemporalItem() // Temporal менять нельзя
						&& !itm.isStackable() //
						&& itm.getItem().getType2() == L2Item.TYPE2_WEAPON //
						&& itm.getItem().getCrystalType() != Grade.NONE //
						&& itm.getReferencePrice() <= Config.ALT_MAMMON_UPGRADE //
						&& itm.getItem().getCrystalCount() > 0 //
						&& !itm.isEquipped() //
						&& itm.getItem().isTradeable() //
						&& (itm.getCustomFlags() & L2ItemInstance.FLAG_NO_TRADE) != L2ItemInstance.FLAG_NO_TRADE //
				)
					_items.add(itm);

			for(final L2ItemInstance itemtosell : _items)
				for(final L2Weapon itemtobuy : ItemTable.getInstance().getAllWeapons())
					if(itemtobuy.getAdditionalName().isEmpty() // Менять можно только обычные предметы
							&& !itemtobuy.isSa() // На SA менять нельзя
							&& !itemtobuy.isRare() // На Rare менять нельзя
							&& !itemtobuy.isCommonItem() // На Common менять нельзя
							&& !itemtobuy.isPvP() // На PvP менять нельзя
							&& !itemtobuy.isShadowItem() // На Shadow менять нельзя
							&& itemtobuy.isTradeable() //
							&& itemtobuy.getType2() == L2Item.TYPE2_WEAPON //
							&& itemtobuy.getItemType() == WeaponType.DUAL == (itemtosell.getItem().getItemType() == WeaponType.DUAL) //
							&& itemtobuy.getCrystalType().ordinal() >= itemtosell.getItem().getCrystalType().ordinal() //
							&& itemtobuy.getReferencePrice() <= Config.ALT_MAMMON_UPGRADE //
							&& itemtosell.getItem().getReferencePrice() < itemtobuy.getReferencePrice() //
							&& itemtosell.getReferencePrice() * 1.7 > itemtobuy.getReferencePrice() //
					)
					{
						final int entry = new int[] { itemtosell.getItemId(), itemtobuy.getItemId(), itemtosell.getEnchantLevel() }.hashCode();
						MultiSellEntry possibleEntry = new MultiSellEntry(entry, itemtobuy.getItemId(), 1, 0);
						possibleEntry.addIngredient(new MultiSellIngredient(itemtosell.getItemId(), 1, itemtosell.getEnchantLevel()));
						possibleEntry.addIngredient(new MultiSellIngredient((short) 5575, (int) ((itemtobuy.getReferencePrice() - itemtosell.getReferencePrice()) * 1.2), 0));
						list.entries.add(possibleEntry);
					}
		}

		// Hardcoded  - обмен вещей на кристаллы
		else if(listId == 9997)
		{
			list.setShowAll(false);
			list.setKeepEnchant(true);
			list.setNoTax(false);
			final Inventory inv = player.getInventory();
			for(final L2ItemInstance itm : inv.getItems())
				if(!itm.isStackable() && itm.getItem().isCrystallizable() && itm.getItem().getCrystalType() != Grade.NONE && itm.getItem().getCrystalCount() > 0 && !itm.isShadowItem() && !itm.isTemporalItem() && !itm.isEquipped() && (itm.getCustomFlags() & L2ItemInstance.FLAG_NO_CRYSTALLIZE) != L2ItemInstance.FLAG_NO_CRYSTALLIZE)
				{
					final L2Item crystal = ItemTable.getInstance().getTemplate(itm.getItem().getCrystalType().cry);
					final int entry = new int[] { itm.getItemId(), itm.getEnchantLevel() }.hashCode();
					MultiSellEntry possibleEntry = new MultiSellEntry(entry, crystal.getItemId(), itm.getItem().getCrystalCount(), 0);
					possibleEntry.addIngredient(new MultiSellIngredient(itm.getItemId(), 1, itm.getEnchantLevel()));
					possibleEntry.addIngredient(new MultiSellIngredient((short) 57, (int) (itm.getItem().getCrystalCount() * crystal.getReferencePrice() * 0.05), 0));
					list.entries.add(possibleEntry);
				}
		}

		// Все мультиселлы из датапака
		else
		{
			MultiSellListContainer container = L2Multisell.getInstance().getList(listId);
			if(container == null)
			{
				_log.warning("Not found myltisell " + listId);
				return null;
			}
			else if(container.isEmpty())
			{
				player.sendMessage(new CustomMessage("common.Disabled", player));
				return null;
			}

			boolean enchant = container.isKeepEnchant();
			boolean notax = container.isNoTax();
			boolean showall = container.isShowAll();
			boolean nokey = container.isNoKey();

			list.setShowAll(showall);
			list.setKeepEnchant(enchant);
			list.setNoTax(notax);
			list.setNoKey(nokey);

			final Inventory inv = player.getInventory();
			for(MultiSellEntry origEntry : container.getEntries())
			{
				MultiSellEntry ent = origEntry.clone();

				// Обработка налога, если лист не безналоговый
				// Адены добавляются в лист если отсутствуют или прибавляются к существующим
				GArray<MultiSellIngredient> ingridients;
				if(!notax && taxRate > 0.)
				{
					double tax = 0, adena = 0;
					ingridients = new GArray<MultiSellIngredient>(ent.getIngredients().size() + 1);
					for(MultiSellIngredient i : ent.getIngredients())
					{
						if(i.getItemId() == 57)
						{
							adena += i.getItemCount();
							tax += i.getItemCount() * (taxRate);
							continue;
						}
						ingridients.add(i);
						if(i.getItemId() == L2Item.ITEM_ID_CLAN_REPUTATION_SCORE)
							// hardcoded. Налог на клановую репутацию. Формула проверена на с6 и соответсвует на 100%.
							//TODO: Проверить на корейском(?) оффе налог на банг поинты и fame
							tax += i.getItemCount() / 120 * 1000 * taxRate * 100;
						if(i.getItemId() < 1)
							continue;

						final L2Item item = ItemTable.getInstance().getTemplate(i.getItemId());
						if(item == null)
							System.out.println("Not found template for itemId: " + i.getItemId());
						else if(item.isStackable())
							tax += item.getReferencePrice() * i.getItemCount() * taxRate;
					}

					adena = Math.round(adena + tax);
					if(adena >= 1)
						ingridients.add(new MultiSellIngredient(57, (long) adena));

					tax = Math.round(tax);
					if(tax >= 1)
						ent.setTax((long) tax);

					ent.getIngredients().clear();
					ent.getIngredients().addAll(ingridients);
				}
				else
					ingridients = ent.getIngredients();

				// Если стоит флаг "показывать все" не проверять наличие ингридиентов
				if(showall)
					list.entries.add(ent);
				else
				{
					GArray<Integer> _itm = new GArray<Integer>();
					// Проверка наличия у игрока ингридиентов
					for(MultiSellIngredient i : ingridients)
					{
						L2Item template = i.getItemId() <= 0 ? null : ItemTable.getInstance().getTemplate(i.getItemId());
						if(i.getItemId() <= 0 || template.getType2() <= L2Item.TYPE2_ACCESSORY || template.getType2() >= (nokey ? L2Item.TYPE2_OTHER : L2Item.TYPE2_PET_WOLF)) // Экипировка
						{
							if(i.getItemId() == 12374) // Mammon's Varnish Enhancer
								continue;

							//TODO: а мы должны тут сверять count?
							if(i.getItemId() == L2Item.ITEM_ID_CLAN_REPUTATION_SCORE)
							{
								if(!_itm.contains(i.getItemId()) && player.getClan() != null && player.getClan().getReputationScore() >= i.getItemCount())
									_itm.add(i.getItemId());
								continue;
							}
							else if(i.getItemId() == L2Item.ITEM_ID_PC_BANG_POINTS)
							{
								if(!_itm.contains(i.getItemId()) && player.getPcBangPoints() >= i.getItemCount())
									_itm.add(i.getItemId());
								continue;
							}
							else if(i.getItemId() == L2Item.ITEM_ID_FAME)
							{
								if(!_itm.contains(i.getItemId()) && player.getFame() >= i.getItemCount())
									_itm.add(i.getItemId());
								continue;
							}

							for(final L2ItemInstance item : inv.getItems())
								if(item.getItemId() == i.getItemId() && !item.isEquipped() && (item.getCustomFlags() & L2ItemInstance.FLAG_NO_TRADE) != L2ItemInstance.FLAG_NO_TRADE)
								{
									if(_itm.contains(enchant ? i.getItemId() + i.getItemEnchant() * 100000L : i.getItemId())) // Не проверять одинаковые вещи
										continue;

									if(item.getEnchantLevel() < i.getItemEnchant()) // Некоторые мультиселлы требуют заточки
										continue;

									if(item.isStackable() && item.getCount() < i.getItemCount())
										break;

									_itm.add(enchant ? i.getItemId() + i.getItemEnchant() * 100000 : i.getItemId());
									MultiSellEntry possibleEntry = new MultiSellEntry(enchant ? ent.getEntryId() + item.getEnchantLevel() * 100000 : ent.getEntryId());

									for(MultiSellIngredient p : ent.getProduction())
									{
										p.setItemEnchant(item.getEnchantLevel());
										possibleEntry.addProduct(p);
									}

									for(MultiSellIngredient ig : ingridients)
									{
										if(template != null && template.getType2() <= L2Item.TYPE2_ACCESSORY)
											ig.setItemEnchant(item.getEnchantLevel());

										possibleEntry.addIngredient(ig);
									}
									list.entries.add(possibleEntry);
									break;
								}
						}
					}
				}
			}
		}

		return list;
	}

	private void loadHardcoded()
	{
		// 4000 - Enchance Weapon - C, B (Insert SA)
		MultiSellListContainer list = new MultiSellListContainer();
		list.setListId(4000);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(true);
		generateSAInsertion(Grade.C, Grade.B, list);
		entries.put(list.getListId(), list);

		// 4001 - Enchance Weapon - A (Insert SA)
		list = new MultiSellListContainer();
		list.setListId(4001);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(true);
		generateSAInsertion(Grade.A, Grade.A, list);
		entries.put(list.getListId(), list);

		// 40011 - Enchance Weapon - S (Insert SA)
		list = new MultiSellListContainer();
		list.setListId(40011);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(true);
		generateSAInsertion(Grade.S, Grade.S, list);
		entries.put(list.getListId(), list);

		// 326150002 - Enchance Weapon - S80/S84 (Insert SA)
		list = new MultiSellListContainer();
		list.setListId(326150002);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(true);
		generateSAInsertion(Grade.S80, Grade.S84, list);
		entries.put(list.getListId(), list);

		// 4002 - Remove SA
		list = new MultiSellListContainer();
		list.setListId(4002);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(true);
		generateSARemove(list);
		entries.put(list.getListId(), list);

		// 311262517 - Finish Masterwork
		list = new MultiSellListContainer();
		list.setListId(311262517);
		list.setShowAll(true);
		list.setKeepEnchant(true);
		list.setNoTax(true);
		list.setNoKey(true);
		generateFinishMastetrwork(list);
		entries.put(list.getListId(), list);

		// 364790006 - PvP weapon add
		list = new MultiSellListContainer();
		list.setListId(364790006);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(true);
		generatePvPAdd(list);
		entries.put(list.getListId(), list);

		// 364790007 - PvP weapon remove
		list = new MultiSellListContainer();
		list.setListId(364790007);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(true);
		generatePvPRemove(list);
		entries.put(list.getListId(), list);

		// 1002 - unseal B
		list = new MultiSellListContainer();
		list.setListId(1002);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(false);
		generateUnseal(Grade.B, Grade.B, list);
		entries.put(list.getListId(), list);

		// 1003 - reseal B
		list = new MultiSellListContainer();
		list.setListId(1003);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(false);
		generateReseal(Grade.B, Grade.B, list);
		entries.put(list.getListId(), list);

		// 1005 - unseal A
		list = new MultiSellListContainer();
		list.setListId(1005);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(false);
		generateUnseal(Grade.A, Grade.A, list);
		entries.put(list.getListId(), list);

		// 1007 - reseal A
		list = new MultiSellListContainer();
		list.setListId(1007);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(false);
		generateReseal(Grade.A, Grade.A, list);
		entries.put(list.getListId(), list);

		// 1008 - unseal S
		list = new MultiSellListContainer();
		list.setListId(1008);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(false);
		generateUnseal(Grade.S, Grade.S, list);
		entries.put(list.getListId(), list);

		// 1009 - unseal S80
		list = new MultiSellListContainer();
		list.setListId(1009);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(false);
		generateUnseal(Grade.S80, Grade.S80, list);
		entries.put(list.getListId(), list);

		// 1010 - unseal S84
		list = new MultiSellListContainer();
		list.setListId(1010);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(false);
		generateUnseal(Grade.S84, Grade.S84, list);
		entries.put(list.getListId(), list);

		// 1011 - unseal S80/S84 using adena
		list = new MultiSellListContainer();
		list.setListId(1011);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(false);
		generateUnseal(Grade.S80, Grade.S84, list, 57, 5);
		entries.put(list.getListId(), list);
	}

	private void generateSAInsertion(Grade mingrade, Grade maxgrade, MultiSellListContainer list)
	{
		int entId = 1;
		int[][] weapons = ItemTable.getInstance().getWeaponEx();
		for(int i = 0; i < weapons.length; i++)
		{
			int[] item = weapons[i];
			if(item == null)
				continue;
			L2Item itm = ItemTable.getInstance().getTemplate(i);
			if(itm.getItemGrade().ordinal() < mingrade.ordinal() || itm.getItemGrade().ordinal() > maxgrade.ordinal())
				continue;
			int[] price = getSaInsertPrice(itm);
			if(price == null)
				continue;

			if(item[ItemTable.WEX_SA1] > 0 && item[ItemTable.WEX_SA_CRY1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(i, 1));
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA_CRY1], 1));
				e1.addIngredient(new MultiSellIngredient(price[0], price[1]));
				if(price[2] > 0)
					e1.addIngredient(new MultiSellIngredient(57, price[2]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_SA1], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_SA2] > 0 && item[ItemTable.WEX_SA_CRY2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(i, 1));
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA_CRY2], 1));
				e1.addIngredient(new MultiSellIngredient(price[0], price[1]));
				if(price[2] > 0)
					e1.addIngredient(new MultiSellIngredient(57, price[2]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_SA2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_SA3] > 0 && item[ItemTable.WEX_SA_CRY3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(i, 1));
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA_CRY3], 1));
				e1.addIngredient(new MultiSellIngredient(price[0], price[1]));
				if(price[2] > 0)
					e1.addIngredient(new MultiSellIngredient(57, price[2]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_SA3], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_SA1] > 0 && item[ItemTable.WEX_SA_CRY1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE], 1));
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA_CRY1], 1));
				e1.addIngredient(new MultiSellIngredient(price[0], price[1]));
				if(price[2] > 0)
					e1.addIngredient(new MultiSellIngredient(57, price[2]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA1], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_SA2] > 0 && item[ItemTable.WEX_SA_CRY2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE], 1));
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA_CRY2], 1));
				e1.addIngredient(new MultiSellIngredient(price[0], price[1]));
				if(price[2] > 0)
					e1.addIngredient(new MultiSellIngredient(57, price[2]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_SA3] > 0 && item[ItemTable.WEX_SA_CRY3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE], 1));
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA_CRY3], 1));
				e1.addIngredient(new MultiSellIngredient(price[0], price[1]));
				if(price[2] > 0)
					e1.addIngredient(new MultiSellIngredient(57, price[2]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA3], 1));
				list.addEntry(e1);
			}
		}
	}

	/**
	 * 
	 * @param base item
	 * @return [0] = gemstone id             <br>
	 *         [1] = gemstone count          <br>
	 *         [2] = adena count (can be 0)
	 */
	private int[] getSaInsertPrice(L2Item i)
	{
		switch(i.getCrystalType())
		{
			case C:
				if(i.getCrystalCount() <= 706)
					return new int[] { 2131, 97, 97 * 3000 };
				else if(i.getCrystalCount() <= 884)
					return new int[] { 2131, 238, 238 * 3000 };
				else if(i.getCrystalCount() <= 1325)
					return new int[] { 2131, 306, 306 * 3000 };
				return new int[] { 2131, 555, 555 * 3000 };
			case B:
				if(i.getCrystalCount() <= 892)
					return new int[] { 2132, 222, 222 * 10000 };
				return new int[] { 2132, 339, 339 * 10000 };
			case A:
				if(i.getCrystalCount() <= 1128)
					return new int[] { 2133, 147, 0 };
				return new int[] { 2133, 157, 0 };
			case S:
				return new int[] { 2134, 82, 0 };
			case S80:
				if(i.getCrystalCount() <= 7050)
					return new int[] { 2134, 285, 0 };
				return new int[] { 2134, 399, 0 };
			case S84:
				return new int[] { 2134, 623, 0 };
		}
		return null;
	}

	private void generateSARemove(MultiSellListContainer list)
	{
		int entId = 1;
		int[][] weapons = ItemTable.getInstance().getWeaponEx();
		for(int i = 0; i < weapons.length; i++)
		{
			int[] item = weapons[i];
			if(item == null)
				continue;
			L2Item itm = ItemTable.getInstance().getTemplate(i);
			int price = getSARemovePrice(itm);

			if(item[ItemTable.WEX_SA1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA1], 1));
				if(price > 0)
					e1.addIngredient(new MultiSellIngredient(5575, price));
				e1.addProduct(new MultiSellIngredient(i, 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_SA2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA2], 1));
				if(price > 0)
					e1.addIngredient(new MultiSellIngredient(5575, price));
				e1.addProduct(new MultiSellIngredient(i, 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_SA3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA3], 1));
				if(price > 0)
					e1.addIngredient(new MultiSellIngredient(5575, price));
				e1.addProduct(new MultiSellIngredient(i, 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_SA1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA1], 1));
				if(price > 0)
					e1.addIngredient(new MultiSellIngredient(5575, price));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_SA2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA2], 1));
				if(price > 0)
					e1.addIngredient(new MultiSellIngredient(5575, price));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_SA3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA3], 1));
				if(price > 0)
					e1.addIngredient(new MultiSellIngredient(5575, price));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE], 1));
				list.addEntry(e1);
			}
		}
	}

	/**
	 * @param base item
	 * @return price in AA (can be 0)
	 */
	private int getSARemovePrice(L2Item i)
	{
		switch(i.getCrystalType())
		{
			case C:
				if(i.getCrystalCount() <= 706)
					return 14550;
				else if(i.getCrystalCount() <= 884)
					return 35700;
				else if(i.getCrystalCount() <= 1325)
					return 45900;
				else
					return 83250;
			case B:
				if(i.getCrystalCount() <= 892)
					return 111000;
				return 169500;
			case A:
				return 210000;
			case S:
			case S80:
			case S84:
				return 250000; // FIXME: цифра с потолка
		}
		return 0;
	}

	private void generateFinishMastetrwork(MultiSellListContainer list)
	{
		int entId = 1;
		int[][] weapons = ItemTable.getInstance().getWeaponEx();
		for(int i = 0; i < weapons.length; i++)
		{
			int[] item = weapons[i];
			if(item == null)
				continue;

			if(item[ItemTable.WEX_FOUNDATION] > 0 && item[ItemTable.WEX_RARE] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_FOUNDATION], 1));
				if(item[ItemTable.WEX_VARNISH_COUNT] > 0)
					e1.addIngredient(new MultiSellIngredient(12374, item[ItemTable.WEX_VARNISH_COUNT]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE], 1));
				list.addEntry(e1);
			}
		}

		int[][] armors = ItemTable.getInstance().getArmorEx();
		for(int i = 0; i < armors.length; i++)
		{
			int[] item = armors[i];
			if(item == null)
				continue;

			if(item[ItemTable.AEX_FOUNDATION] <= 0)
				continue;

			if(item[ItemTable.AEX_SEALED_RARE_1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_FOUNDATION], 1));
				if(item[ItemTable.AEX_VARNISH] > 0)
					e1.addIngredient(new MultiSellIngredient(12374, item[ItemTable.AEX_VARNISH]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_SEALED_RARE_1], 1));
				list.addEntry(e1);
				if(item[ItemTable.AEX_SEALED_RARE_2] > 0)
				{
					MultiSellEntry e2 = new MultiSellEntry(entId++);
					e2.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_FOUNDATION], 1));
					if(item[ItemTable.AEX_VARNISH] > 0)
						e2.addIngredient(new MultiSellIngredient(12374, item[ItemTable.AEX_VARNISH]));
					e2.addProduct(new MultiSellIngredient(item[ItemTable.AEX_SEALED_RARE_2], 1));
					list.addEntry(e2);
				}
				if(item[ItemTable.AEX_SEALED_RARE_3] > 0)
				{
					MultiSellEntry e3 = new MultiSellEntry(entId++);
					e3.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_FOUNDATION], 1));
					if(item[ItemTable.AEX_VARNISH] > 0)
						e3.addIngredient(new MultiSellIngredient(12374, item[ItemTable.AEX_VARNISH]));
					e3.addProduct(new MultiSellIngredient(item[ItemTable.AEX_SEALED_RARE_3], 1));
					list.addEntry(e3);
				}
			}
			else if(item[ItemTable.AEX_UNSEALED_RARE_1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_FOUNDATION], 1));
				if(item[ItemTable.AEX_VARNISH] > 0)
					e1.addIngredient(new MultiSellIngredient(12374, item[ItemTable.AEX_VARNISH]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_1], 1));
				list.addEntry(e1);
				if(item[ItemTable.AEX_UNSEALED_RARE_2] > 0)
				{
					MultiSellEntry e2 = new MultiSellEntry(entId++);
					e2.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_FOUNDATION], 1));
					if(item[ItemTable.AEX_VARNISH] > 0)
						e2.addIngredient(new MultiSellIngredient(12374, item[ItemTable.AEX_VARNISH]));
					e2.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_2], 1));
					list.addEntry(e2);
				}
				if(item[ItemTable.AEX_UNSEALED_RARE_3] > 0)
				{
					MultiSellEntry e3 = new MultiSellEntry(entId++);
					e3.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_FOUNDATION], 1));
					if(item[ItemTable.AEX_VARNISH] > 0)
						e3.addIngredient(new MultiSellIngredient(12374, item[ItemTable.AEX_VARNISH]));
					e3.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_3], 1));
					list.addEntry(e3);
				}
			}
		}
	}

	private void generatePvPAdd(MultiSellListContainer list)
	{
		int entId = 1;
		int[][] weapons = ItemTable.getInstance().getWeaponEx();
		for(int i = 0; i < weapons.length; i++)
		{
			int[] item = weapons[i];
			if(item == null)
				continue;

			L2Item itm = ItemTable.getInstance().getTemplate(i);
			int[] price = getPvPWeaponPrice(itm);
			if(price == null)
				continue;

			if(item[ItemTable.WEX_PVP1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA1] > 0 ? item[ItemTable.WEX_SA1] : i, 1));
				e1.addIngredient(new MultiSellIngredient(L2Item.ITEM_ID_FAME, price[0]));
				e1.addIngredient(new MultiSellIngredient(57, price[1]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_PVP1], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_PVP2] > 0 && item[ItemTable.WEX_SA2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA2], 1));
				e1.addIngredient(new MultiSellIngredient(L2Item.ITEM_ID_FAME, price[0]));
				e1.addIngredient(new MultiSellIngredient(57, price[1]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_PVP2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_PVP3] > 0 && item[ItemTable.WEX_SA3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_SA3], 1));
				e1.addIngredient(new MultiSellIngredient(L2Item.ITEM_ID_FAME, price[0]));
				e1.addIngredient(new MultiSellIngredient(57, price[1]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_PVP3], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_PVP1] > 0 && (item[ItemTable.WEX_RARE_SA1] > 0 || item[ItemTable.WEX_RARE] > 0))
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA1] > 0 ? item[ItemTable.WEX_RARE_SA1] : item[ItemTable.WEX_RARE], 1));
				e1.addIngredient(new MultiSellIngredient(L2Item.ITEM_ID_FAME, price[0]));
				e1.addIngredient(new MultiSellIngredient(57, price[1]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE_PVP1], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_PVP2] > 0 && item[ItemTable.WEX_RARE_SA2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA2], 1));
				e1.addIngredient(new MultiSellIngredient(L2Item.ITEM_ID_FAME, price[0]));
				e1.addIngredient(new MultiSellIngredient(57, price[1]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE_PVP2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_PVP3] > 0 && item[ItemTable.WEX_RARE_SA3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA3], 1));
				e1.addIngredient(new MultiSellIngredient(L2Item.ITEM_ID_FAME, price[0]));
				e1.addIngredient(new MultiSellIngredient(57, price[1]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE_PVP3], 1));
				list.addEntry(e1);
			}
		}

		int[][] armors = ItemTable.getInstance().getArmorEx();
		for(int i = 0; i < armors.length; i++)
		{
			int[] item = armors[i];
			if(item == null || item[ItemTable.AEX_PvP] <= 0)
				continue;

			L2Item itm = ItemTable.getInstance().getTemplate(item[ItemTable.AEX_UNSEALED_1] > 0 ? item[ItemTable.AEX_UNSEALED_1] : i);
			int[] price = getPvPArmorPrice(itm);
			if(price == null)
				continue;

			if(item[ItemTable.AEX_PvP] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(itm.getItemId(), 1));
				e1.addIngredient(new MultiSellIngredient(L2Item.ITEM_ID_FAME, price[0]));
				e1.addIngredient(new MultiSellIngredient(57, price[1]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_PvP], 1));
				list.addEntry(e1);
			}
		}
	}

	private int[] getPvPWeaponPrice(L2Item i)
	{
		switch(i.getCrystalType())
		{
			case A:
				if(i.getCrystalCount() <= 1128)
					return null;
				return new int[] { 8091, 809050 };
			case S:
				return new int[] { 11545, 1154520 };
			case S80:
				if(i.getCrystalCount() == 3597) // Dynasty
					return new int[] { 26437, 2643720 };
				else
					return new int[] { 25900, 5171950 }; // Icarus
			case S84:
				return new int[] { 36700, 7343650 }; // Vesper
		}
		return null;
	}

	private int[] getPvPArmorPrice(L2Item i)
	{
		switch(i.getCrystalType())
		{
			case A:
				if(i.getCrystalCount() < 477)
					return null;
				return new int[] { 1981, 198100 };
			case S:
				return new int[] { 4206, 420570 };
			case S80:
			case S84:
				return new int[] { 8608, 860640 };
		}
		return null;
	}

	private void generatePvPRemove(MultiSellListContainer list)
	{
		int entId = 1;
		int[][] weapons = ItemTable.getInstance().getWeaponEx();
		for(int i = 0; i < weapons.length; i++)
		{
			int[] item = weapons[i];
			if(item == null)
				continue;

			if(item[ItemTable.WEX_PVP1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_PVP1], 1));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_SA1] > 0 ? item[ItemTable.WEX_SA1] : i, 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_PVP2] > 0 && item[ItemTable.WEX_SA2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_PVP2], 1));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_SA2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_PVP3] > 0 && item[ItemTable.WEX_SA3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_PVP3], 1));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_SA3], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_PVP1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE_PVP1], 1));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA1] > 0 ? item[ItemTable.WEX_RARE_SA1] : item[ItemTable.WEX_RARE], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_PVP2] > 0 && item[ItemTable.WEX_RARE_SA2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE_PVP2], 1));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.WEX_RARE_PVP3] > 0 && item[ItemTable.WEX_RARE_SA3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.WEX_RARE_PVP3], 1));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.WEX_RARE_SA3], 1));
				list.addEntry(e1);
			}
		}

		int[][] armors = ItemTable.getInstance().getArmorEx();
		for(int i = 0; i < armors.length; i++)
		{
			int[] item = armors[i];
			if(item == null)
				continue;

			L2Item itm = ItemTable.getInstance().getTemplate(i);
			int[] price = getPvPArmorPrice(itm);
			if(price == null)
				continue;

			if(item[ItemTable.AEX_PvP] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_PvP], 1));
				if(item[ItemTable.AEX_UNSEALED_1] > 0)
					e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_1], 1));
				else
					e1.addProduct(new MultiSellIngredient(i, 1));
				list.addEntry(e1);
			}
		}
	}

	private void generateUnseal(Grade mingrade, Grade maxgrade, MultiSellListContainer list)
	{
		generateUnseal(mingrade, maxgrade, list, 5575, 1);
	}

	private void generateUnseal(Grade mingrade, Grade maxgrade, MultiSellListContainer list, int unsealItem, int priceMult)
	{
		int entId = 1;
		int[][] armors = ItemTable.getInstance().getArmorEx();
		for(int i = 0; i < armors.length; i++)
		{
			int[] item = armors[i];
			if(item == null)
				continue;

			L2Item itm = ItemTable.getInstance().getTemplate(i);

			if(itm.getItemGrade().ordinal() < mingrade.ordinal() || itm.getItemGrade().ordinal() > maxgrade.ordinal())
				continue;

			if(item[ItemTable.AEX_UNSEALED_1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(i, 1));
				if(item[ItemTable.AEX_UNSEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(unsealItem, item[ItemTable.AEX_UNSEAL_PRICE] * priceMult));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_1], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(i, 1));
				if(item[ItemTable.AEX_UNSEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(unsealItem, item[ItemTable.AEX_UNSEAL_PRICE] * priceMult));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(i, 1));
				if(item[ItemTable.AEX_UNSEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(unsealItem, item[ItemTable.AEX_UNSEAL_PRICE] * priceMult));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_3], 1));
				list.addEntry(e1);
			}

			if(item[ItemTable.AEX_UNSEALED_RARE_1] > 0 && item[ItemTable.AEX_SEALED_RARE_1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_SEALED_RARE_1], 1));
				if(item[ItemTable.AEX_UNSEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(unsealItem, item[ItemTable.AEX_UNSEAL_PRICE] * priceMult));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_1], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_RARE_2] > 0 && (item[ItemTable.AEX_SEALED_RARE_2] > 0 || item[ItemTable.AEX_SEALED_RARE_1] > 0))
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				if(item[ItemTable.AEX_SEALED_RARE_2] > 0)
					e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_SEALED_RARE_2], 1));
				else
					e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_SEALED_RARE_1], 1));
				if(item[ItemTable.AEX_UNSEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(unsealItem, item[ItemTable.AEX_UNSEAL_PRICE] * priceMult));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_RARE_3] > 0 && (item[ItemTable.AEX_SEALED_RARE_3] > 0 || item[ItemTable.AEX_SEALED_RARE_2] > 0 || item[ItemTable.AEX_SEALED_RARE_1] > 0))
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				if(item[ItemTable.AEX_SEALED_RARE_3] > 0)
					e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_SEALED_RARE_3], 1));
				else
					e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_SEALED_RARE_1], 1));
				if(item[ItemTable.AEX_UNSEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(unsealItem, item[ItemTable.AEX_UNSEAL_PRICE] * priceMult));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_3], 1));
				list.addEntry(e1);
			}

			if(item[ItemTable.AEX_UNSEALED_COMMON_1] > 0 && item[ItemTable.AEX_COMMON] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_COMMON], 1));
				if(item[ItemTable.AEX_UNSEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(unsealItem, item[ItemTable.AEX_UNSEAL_PRICE] * priceMult / 20));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_COMMON_1], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_COMMON_2] > 0 && item[ItemTable.AEX_COMMON] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_COMMON], 1));
				if(item[ItemTable.AEX_UNSEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(unsealItem, item[ItemTable.AEX_UNSEAL_PRICE] * priceMult / 20));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_COMMON_2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_COMMON_3] > 0 && item[ItemTable.AEX_COMMON] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_COMMON], 1));
				if(item[ItemTable.AEX_UNSEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(unsealItem, item[ItemTable.AEX_UNSEAL_PRICE] * priceMult / 20));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_COMMON_3], 1));
				list.addEntry(e1);
			}
		}
	}

	private void generateReseal(Grade mingrade, Grade maxgrade, MultiSellListContainer list)
	{
		int entId = 1;
		int[][] armors = ItemTable.getInstance().getArmorEx();
		for(int i = 0; i < armors.length; i++)
		{
			int[] item = armors[i];
			if(item == null || item[ItemTable.AEX_RESEAL_PRICE] < 0)
				continue;

			L2Item itm = ItemTable.getInstance().getTemplate(i);

			if(itm.getItemGrade().ordinal() < mingrade.ordinal() || itm.getItemGrade().ordinal() > maxgrade.ordinal())
				continue;

			if(item[ItemTable.AEX_UNSEALED_1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_1], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE]));
				e1.addProduct(new MultiSellIngredient(i, 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_2], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE]));
				e1.addProduct(new MultiSellIngredient(i, 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_3], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE]));
				e1.addProduct(new MultiSellIngredient(i, 1));
				list.addEntry(e1);
			}

			if(item[ItemTable.AEX_UNSEALED_RARE_1] > 0 && item[ItemTable.AEX_UNSEALED_RARE_2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_1], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_2], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_RARE_1] > 0 && item[ItemTable.AEX_UNSEALED_RARE_3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_1], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_3], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_RARE_2] > 0 && item[ItemTable.AEX_UNSEALED_RARE_1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_2], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_1], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_RARE_2] > 0 && item[ItemTable.AEX_UNSEALED_RARE_3] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_2], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_3], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_RARE_3] > 0 && item[ItemTable.AEX_UNSEALED_RARE_1] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_3], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_1], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_RARE_3] > 0 && item[ItemTable.AEX_UNSEALED_RARE_2] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_3], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE]));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_RARE_2], 1));
				list.addEntry(e1);
			}

			if(item[ItemTable.AEX_UNSEALED_COMMON_1] > 0 && item[ItemTable.AEX_COMMON] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_COMMON_1], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE] / 20));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_COMMON], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_COMMON_2] > 0 && item[ItemTable.AEX_COMMON] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_COMMON_2], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE] / 20));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_COMMON], 1));
				list.addEntry(e1);
			}
			if(item[ItemTable.AEX_UNSEALED_COMMON_3] > 0 && item[ItemTable.AEX_COMMON] > 0)
			{
				MultiSellEntry e1 = new MultiSellEntry(entId++);
				e1.addIngredient(new MultiSellIngredient(item[ItemTable.AEX_UNSEALED_COMMON_3], 1));
				if(item[ItemTable.AEX_RESEAL_PRICE] > 0)
					e1.addIngredient(new MultiSellIngredient(5575, item[ItemTable.AEX_RESEAL_PRICE] / 20));
				e1.addProduct(new MultiSellIngredient(item[ItemTable.AEX_COMMON], 1));
				list.addEntry(e1);
			}
		}
	}

	public static void unload()
	{
		if(_instance != null)
		{
			_instance.entries.clear();
			_instance = null;
		}
	}
}