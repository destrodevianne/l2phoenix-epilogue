package l2p.gameserver.tables;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2p.Config;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.extensions.scripts.Scripts;
import l2p.extensions.scripts.Scripts.ScriptClassAndMethod;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Playable;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.skills.DocumentItem;
import l2p.gameserver.templates.Item;
import l2p.gameserver.templates.L2Armor;
import l2p.gameserver.templates.L2EtcItem;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.templates.L2Weapon;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.templates.L2Armor.ArmorType;
import l2p.gameserver.templates.L2EtcItem.EtcItemType;
import l2p.gameserver.templates.L2Item.Grade;
import l2p.gameserver.templates.L2Weapon.WeaponType;
import l2p.util.GArray;
import l2p.util.Log;

@SuppressWarnings( { "nls", "unqualified-field-access", "boxing" })
public class ItemTable
{
	private final Logger _log = Logger.getLogger(ItemTable.class.getName());

	private static final HashMap<String, Integer> _crystalTypes = new HashMap<String, Integer>();
	private static final HashMap<String, WeaponType> _weaponTypes = new HashMap<String, WeaponType>();
	private static final HashMap<String, ArmorType> _armorTypes = new HashMap<String, ArmorType>();
	private static final HashMap<String, Integer> _slots = new HashMap<String, Integer>();

	static
	{
		_crystalTypes.put("s84", Grade.S84.ordinal());
		_crystalTypes.put("s80", Grade.S80.ordinal());
		_crystalTypes.put("s", Grade.S.ordinal());
		_crystalTypes.put("a", Grade.A.ordinal());
		_crystalTypes.put("b", Grade.B.ordinal());
		_crystalTypes.put("c", Grade.C.ordinal());
		_crystalTypes.put("d", Grade.D.ordinal());
		_crystalTypes.put("none", Grade.NONE.ordinal());

		_weaponTypes.put("blunt", WeaponType.BLUNT);
		_weaponTypes.put("bigblunt", WeaponType.BIGBLUNT);
		_weaponTypes.put("bow", WeaponType.BOW);
		_weaponTypes.put("crossbow", WeaponType.CROSSBOW);
		_weaponTypes.put("dagger", WeaponType.DAGGER);
		_weaponTypes.put("dual", WeaponType.DUAL);
		_weaponTypes.put("dualfist", WeaponType.DUALFIST);
		_weaponTypes.put("etc", WeaponType.ETC);
		_weaponTypes.put("fist", WeaponType.FIST);
		_weaponTypes.put("none", WeaponType.NONE); // these are shields !
		_weaponTypes.put("pole", WeaponType.POLE);
		_weaponTypes.put("sword", WeaponType.SWORD);
		_weaponTypes.put("bigsword", WeaponType.BIGSWORD); //Two-Handed Swords
		_weaponTypes.put("rapier", WeaponType.RAPIER); //Kamael Rapier
		_weaponTypes.put("ancientsword", WeaponType.ANCIENTSWORD); //Kamael Two-Handed Swords
		_weaponTypes.put("pet", WeaponType.PET); //Pet Weapon
		_weaponTypes.put("rod", WeaponType.ROD); //Fishing Rods
		_weaponTypes.put("dualdagger", WeaponType.DUALDAGGER); //Dual Daggers

		_armorTypes.put("none", ArmorType.NONE);
		_armorTypes.put("light", ArmorType.LIGHT);
		_armorTypes.put("heavy", ArmorType.HEAVY);
		_armorTypes.put("magic", ArmorType.MAGIC);
		_armorTypes.put("sigil", ArmorType.SIGIL);
		_armorTypes.put("pet", ArmorType.PET);

		_slots.put("chest", L2Item.SLOT_CHEST);
		_slots.put("belt", L2Item.SLOT_BELT);
		_slots.put("rbracelet", L2Item.SLOT_R_BRACELET);
		_slots.put("lbracelet", L2Item.SLOT_L_BRACELET);
		_slots.put("talisman", L2Item.SLOT_DECO);
		_slots.put("fullarmor", L2Item.SLOT_FULL_ARMOR);
		_slots.put("head", L2Item.SLOT_HEAD);
		_slots.put("hair", L2Item.SLOT_HAIR);
		_slots.put("face", L2Item.SLOT_DHAIR);
		_slots.put("dhair", L2Item.SLOT_HAIRALL);
		_slots.put("underwear", L2Item.SLOT_UNDERWEAR);
		_slots.put("cloak", L2Item.SLOT_CLOAK);
		_slots.put("back", L2Item.SLOT_BACK);
		_slots.put("neck", L2Item.SLOT_NECK);
		_slots.put("legs", L2Item.SLOT_LEGS);
		_slots.put("feet", L2Item.SLOT_FEET);
		_slots.put("gloves", L2Item.SLOT_GLOVES);
		_slots.put("chest,legs", L2Item.SLOT_CHEST | L2Item.SLOT_LEGS);
		_slots.put("rhand", L2Item.SLOT_R_HAND);
		_slots.put("lhand", L2Item.SLOT_L_HAND);
		_slots.put("lrhand", L2Item.SLOT_LR_HAND);
		_slots.put("rear,lear", L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR);
		_slots.put("rfinger,lfinger", L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER);
		_slots.put("none", L2Item.SLOT_NONE);
		_slots.put("wolf", L2Item.SLOT_WOLF); // for wolf
		_slots.put("gwolf", L2Item.SLOT_GWOLF); // for great wolf
		_slots.put("hatchling", L2Item.SLOT_HATCHLING); // for hatchling
		_slots.put("strider", L2Item.SLOT_STRIDER); // for strider
		_slots.put("baby", L2Item.SLOT_BABYPET); // for baby pet
		_slots.put("pendant", L2Item.SLOT_PENDANT); // magic armor for pet
		_slots.put("formalwear", L2Item.SLOT_FORMAL_WEAR);
		_slots.put("sigil", L2Item.SLOT_SIGIL);
	}

	private L2Item[] _allTemplates;

	private final HashMap<Integer, L2EtcItem> _etcItems = new HashMap<Integer, L2EtcItem>();
	private final HashMap<Integer, L2Armor> _armors = new HashMap<Integer, L2Armor>();
	private final HashMap<Integer, L2Weapon> _weapons = new HashMap<Integer, L2Weapon>();

	private final HashMap<Integer, Item> itemData = new HashMap<Integer, Item>();
	private final HashMap<Integer, Item> weaponData = new HashMap<Integer, Item>();
	private final HashMap<Integer, Item> armorData = new HashMap<Integer, Item>();

	private boolean _initialized = true;

	private static ItemTable _instance;

	/** Table of SQL request in order to obtain items from tables [etcitem], [armor], [weapon] */
	private static final String[] SQL_ITEM_SELECTS = {
			"SELECT item_id, name, class, icon, crystallizable, item_type, weight, consume_type, crystal_type, durability, price, crystal_count, sellable, skill_id, skill_level, tradeable, dropable, destroyable, flags, temporal FROM etcitem",
			"SELECT item_id, name, `additional_name`, icon, bodypart, crystallizable, armor_type, player_class, weight, crystal_type, avoid_modify, durability, p_def, m_def, mp_bonus, price, crystal_count, sellable, tradeable, dropable, destroyable, flags, skill_id, skill_level, enchant4_skill_id, enchant4_skill_lvl, temporal FROM armor",
			"SELECT item_id, name, `additional_name`, icon, bodypart, crystallizable, weight, soulshots, spiritshots, crystal_type, p_dam, rnd_dam, weaponType, critical, hit_modify, avoid_modify, shield_def, shield_def_rate, atk_speed, mp_consume, m_dam, durability, price, crystal_count, sellable, tradeable, dropable, destroyable, flags, skill_id, skill_level, enchant4_skill_id, enchant4_skill_lvl, temporal FROM weapon" };

	/**
	 * Returns instance of ItemTable
	 * @return ItemTable
	 */
	public static ItemTable getInstance()
	{
		if(_instance == null)
			_instance = new ItemTable();
		return _instance;
	}

	/**
	 * Не работает, не использовать...
	public static void reload()
	{
		_instance = null;
		getInstance();
	}
	*/

	/**
	 * Returns a new object Item
	 * @return
	 */
	public Item newItem()
	{
		return new Item();
	}

	/**
	 * Constructor.
	 */
	private ItemTable()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			for(String selectQuery : SQL_ITEM_SELECTS)
			{
				statement = con.prepareStatement(selectQuery);
				rset = statement.executeQuery();

				// Add item in correct HashMap
				while(rset.next())
					if(selectQuery.endsWith("etcitem"))
					{
						Item newItem = readItem(rset);
						itemData.put(newItem.id, newItem);
					}
					else if(selectQuery.endsWith("armor"))
					{
						Item newItem = readArmor(rset);
						armorData.put(newItem.id, newItem);
					}
					else if(selectQuery.endsWith("weapon"))
					{
						Item newItem = readWeapon(rset);
						weaponData.put(newItem.id, newItem);
					}
				DatabaseUtils.closeDatabaseSR(statement, rset);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "data error on item: ", e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		for(Entry<Integer, Item> e : armorData.entrySet())
			_armors.put(e.getKey(), new L2Armor((ArmorType) e.getValue().type, e.getValue().set));
		_log.config("ItemTable: Loaded " + _armors.size() + " Armors.");

		for(Entry<Integer, Item> e : itemData.entrySet())
			_etcItems.put(e.getKey(), new L2EtcItem((EtcItemType) e.getValue().type, e.getValue().set));
		_log.config("ItemTable: Loaded " + _etcItems.size() + " Items.");

		new File("log/game/unimplemented_sa.txt").delete();
		for(Entry<Integer, Item> e : weaponData.entrySet())
			_weapons.put(e.getKey(), new L2Weapon((WeaponType) e.getValue().type, e.getValue().set));
		_log.config("ItemTable: Loaded " + _weapons.size() + " Weapons.");

		buildFastLookupTable();

		_item_idx = new HashMap<Integer, Integer>();
		loadExtendedWeaponsTable();
		loadExtendedArmorTable();

		for(int[] item : _weapon_ex)
			if(item != null)
			{
				if(item[WEX_RARE] > 0)
					_allTemplates[item[WEX_RARE]].setRare(true);
				if(item[WEX_RARE_SA1] > 0)
				{
					_allTemplates[item[WEX_RARE_SA1]].setRare(true);
					_allTemplates[item[WEX_RARE_SA1]].setSa(true);
				}
				if(item[WEX_RARE_SA2] > 0)
				{
					_allTemplates[item[WEX_RARE_SA2]].setRare(true);
					_allTemplates[item[WEX_RARE_SA2]].setSa(true);
				}
				if(item[WEX_RARE_SA3] > 0)
				{
					_allTemplates[item[WEX_RARE_SA3]].setRare(true);
					_allTemplates[item[WEX_RARE_SA3]].setSa(true);
				}
				if(item[WEX_RARE_PVP1] > 0)
				{
					_allTemplates[item[WEX_RARE_PVP1]].setRare(true);
					_allTemplates[item[WEX_RARE_PVP1]].setPvP(true);
					_allTemplates[item[WEX_RARE_PVP1]].setSa(true);
				}
				if(item[WEX_RARE_PVP2] > 0)
				{
					_allTemplates[item[WEX_RARE_PVP2]].setRare(true);
					_allTemplates[item[WEX_RARE_PVP2]].setPvP(true);
					_allTemplates[item[WEX_RARE_PVP2]].setSa(true);
				}
				if(item[WEX_RARE_PVP3] > 0)
				{
					_allTemplates[item[WEX_RARE_PVP3]].setRare(true);
					_allTemplates[item[WEX_RARE_PVP3]].setPvP(true);
					_allTemplates[item[WEX_RARE_PVP3]].setSa(true);
				}
				if(item[WEX_PVP1] > 0)
				{
					_allTemplates[item[WEX_PVP1]].setPvP(true);
					_allTemplates[item[WEX_PVP1]].setSa(true);
				}
				if(item[WEX_PVP2] > 0)
				{
					_allTemplates[item[WEX_PVP2]].setPvP(true);
					_allTemplates[item[WEX_PVP2]].setSa(true);
				}
				if(item[WEX_PVP3] > 0)
				{
					_allTemplates[item[WEX_PVP3]].setPvP(true);
					_allTemplates[item[WEX_PVP3]].setSa(true);
				}
				if(item[WEX_SA1] > 0)
					_allTemplates[item[WEX_SA1]].setSa(true);
				if(item[WEX_SA2] > 0)
					_allTemplates[item[WEX_SA2]].setSa(true);
				if(item[WEX_SA3] > 0)
					_allTemplates[item[WEX_SA3]].setSa(true);
			}

		for(int[] item : _armor_ex)
			if(item != null)
			{
				if(item[AEX_SEALED_RARE_1] > 0)
					_allTemplates[item[AEX_SEALED_RARE_1]].setRare(true);
				if(item[AEX_SEALED_RARE_2] > 0)
					_allTemplates[item[AEX_SEALED_RARE_2]].setRare(true);
				if(item[AEX_SEALED_RARE_3] > 0)
					_allTemplates[item[AEX_SEALED_RARE_3]].setRare(true);
				if(item[AEX_UNSEALED_RARE_1] > 0)
					_allTemplates[item[AEX_UNSEALED_RARE_1]].setRare(true);
				if(item[AEX_UNSEALED_RARE_2] > 0)
					_allTemplates[item[AEX_UNSEALED_RARE_2]].setRare(true);
				if(item[AEX_UNSEALED_RARE_3] > 0)
					_allTemplates[item[AEX_UNSEALED_RARE_3]].setRare(true);
				if(item[AEX_PvP] > 0)
					_allTemplates[item[AEX_PvP]].setPvP(true);
			}

		// Загрузка дополнительных PvP бонусов.
		for(L2Item item : _allTemplates)
			if(item != null && item.isPvP())
				if(item instanceof L2Armor)
					switch((ArmorType) item.type)
					{
						case LIGHT:
							//item.attachSkill(SkillTable.getInstance().getInfo(, 1));
							break;
						case HEAVY:
							//item.attachSkill(SkillTable.getInstance().getInfo(, 1));
							break;
						case MAGIC:
							//item.attachSkill(SkillTable.getInstance().getInfo(, 1));
							break;
					}
				else
					switch((WeaponType) item.type)
					{
						case BOW:
						case CROSSBOW:
							item.attachSkill(SkillTable.getInstance().getInfo(3655, 1)); // PvP Weapon - Rapid Fire
							break;

						case BIGSWORD:
						case BIGBLUNT:
						case ANCIENTSWORD:
							if(item.isMageSA())
								item.attachSkill(SkillTable.getInstance().getInfo(3654, 1)); // PvP Weapon - Casting
							else
								item.attachSkill(SkillTable.getInstance().getInfo(3653, 1)); // PvP Weapon - Attack Chance
							break;

						case SWORD:
						case BLUNT:
						case RAPIER:
							if(item.isMageSA())
								item.attachSkill(SkillTable.getInstance().getInfo(3654, 1)); // PvP Weapon - Casting
							else
								item.attachSkill(SkillTable.getInstance().getInfo(3650, 1)); // PvP Weapon - CP Drain
							break;

						case FIST:
						case DUALFIST:
						case DAGGER:
						case DUALDAGGER:
							item.attachSkill(SkillTable.getInstance().getInfo(3651, 1)); // PvP Weapon - Cancel
							item.attachSkill(SkillTable.getInstance().getInfo(3652, 1)); // PvP Weapon - Ignore Shield Defense
							break;

						case POLE:
							item.attachSkill(SkillTable.getInstance().getInfo(3653, 1)); // PvP Weapon - Attack Chance
							break;

						case DUAL:
							item.attachSkill(SkillTable.getInstance().getInfo(3656, 1)); // PvP Weapon - Decrease Range
							break;
					}

		new Thread(new Runnable(){
			public void run()
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{}
				for(File f : new File("./data/stats/items/").listFiles())
					if(!f.isDirectory())
						new DocumentItem(f);
			}
		}).start();
	}

	/**
	 * Returns object Item from the record of the database
	 * @param rset : ResultSet designating a record of the [weapon] table of database
	 * @return Item : object created from the database record
	 * @throws SQLException
	 */
	private Item readWeapon(ResultSet rset) throws SQLException
	{
		Item item = new Item();
		item.set = new StatsSet();
		item.id = rset.getInt("item_id");
		item.type = _weaponTypes.get(rset.getString("weaponType"));
		if(item.type == null)
			System.out.println("Error in weapons table: unknown weapon type " + rset.getString("weaponType") + " for item " + item.id);
		item.name = rset.getString("name");
		item.set.set("class", "EQUIPMENT");

		item.set.set("item_id", item.id);
		item.set.set("name", item.name);
		item.set.set("additional_name", rset.getString("additional_name"));

		// lets see if this is a shield
		if(item.type == WeaponType.NONE)
		{
			item.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
			item.set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
		}
		else
		{
			item.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
			item.set.set("type2", L2Item.TYPE2_WEAPON);
		}

		item.set.set("bodypart", _slots.get(rset.getString("bodypart")));
		item.set.set("crystal_type", _crystalTypes.get(rset.getString("crystal_type")));
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")).booleanValue());
		item.set.set("weight", rset.getInt("weight"));
		item.set.set("soulshots", rset.getInt("soulshots"));
		item.set.set("spiritshots", rset.getInt("spiritshots"));
		item.set.set("p_dam", rset.getInt("p_dam"));
		item.set.set("rnd_dam", rset.getInt("rnd_dam"));
		item.set.set("critical", rset.getInt("critical"));
		item.set.set("hit_modify", rset.getDouble("hit_modify"));
		item.set.set("avoid_modify", rset.getInt("avoid_modify"));
		item.set.set("shield_def", rset.getInt("shield_def"));
		item.set.set("shield_def_rate", rset.getInt("shield_def_rate"));
		item.set.set("atk_speed", rset.getInt("atk_speed"));
		item.set.set("mp_consume", rset.getInt("mp_consume"));
		item.set.set("m_dam", rset.getInt("m_dam"));
		item.set.set("durability", rset.getInt("durability"));
		item.set.set("price", rset.getInt("price"));
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("tradeable", rset.getInt("tradeable") > 0);
		item.set.set("dropable", rset.getInt("dropable") > 0);
		item.set.set("destroyable", rset.getInt("destroyable") > 0);
		item.set.set("flags", rset.getInt("flags"));
		item.set.set("temporal", rset.getInt("temporal") > 0);
		item.set.set("skill_id", rset.getString("skill_id"));
		item.set.set("skill_level", rset.getString("skill_level"));
		item.set.set("enchant4_skill_id", rset.getInt("enchant4_skill_id"));
		item.set.set("enchant4_skill_lvl", rset.getInt("enchant4_skill_lvl"));
		item.set.set("icon", rset.getString("icon"));

		if(item.type == WeaponType.PET)
		{
			item.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);

			if(item.set.getInteger("bodypart") == L2Item.SLOT_WOLF)
				item.set.set("type2", L2Item.TYPE2_PET_WOLF);
			else if(item.set.getInteger("bodypart") == L2Item.SLOT_GWOLF)
				item.set.set("type2", L2Item.TYPE2_PET_GWOLF);
			else if(item.set.getInteger("bodypart") == L2Item.SLOT_HATCHLING)
				item.set.set("type2", L2Item.TYPE2_PET_HATCHLING);
			else
				item.set.set("type2", L2Item.TYPE2_PET_STRIDER);

			item.set.set("bodypart", L2Item.SLOT_R_HAND);
		}

		return item;
	}

	/**
	 * Returns object Item from the record of the database
	 * @param rset : ResultSet designating a record of the [armor] table of database
	 * @return Item : object created from the database record
	 * @throws SQLException
	 */
	private Item readArmor(ResultSet rset) throws SQLException
	{
		Item item = new Item();
		try
		{
			item.set = new StatsSet();
			item.type = _armorTypes.get(rset.getString("armor_type"));
			item.id = rset.getInt("item_id");
			item.name = rset.getString("name");
			item.set.set("class", "EQUIPMENT");

			item.set.set("item_id", item.id);
			item.set.set("name", item.name);
			int bodypart = _slots.get(rset.getString("bodypart"));
			item.set.set("bodypart", bodypart);
			item.set.set("player_class", rset.getString("player_class"));
			item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
			item.set.set("crystal_count", rset.getInt("crystal_count"));
			item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
			item.set.set("flags", rset.getInt("flags"));

			if(bodypart == L2Item.SLOT_NECK || (bodypart & L2Item.SLOT_L_EAR) != 0 || (bodypart & L2Item.SLOT_L_FINGER) != 0)
			{
				item.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				item.set.set("type2", L2Item.TYPE2_ACCESSORY);
			}
			else if(bodypart == L2Item.SLOT_HAIR || bodypart == L2Item.SLOT_DHAIR || bodypart == L2Item.SLOT_HAIRALL)
			{
				item.set.set("type1", L2Item.TYPE1_OTHER);
				item.set.set("type2", L2Item.TYPE2_OTHER);
			}
			else
			{
				item.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
				item.set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
			}

			item.set.set("weight", rset.getInt("weight"));
			item.set.set("crystal_type", _crystalTypes.get(rset.getString("crystal_type")));
			item.set.set("avoid_modify", rset.getInt("avoid_modify"));
			item.set.set("durability", rset.getInt("durability"));
			item.set.set("p_def", rset.getInt("p_def"));
			item.set.set("m_def", rset.getInt("m_def"));
			item.set.set("mp_bonus", rset.getInt("mp_bonus"));
			item.set.set("price", rset.getInt("price"));
			item.set.set("tradeable", rset.getInt("tradeable") > 0);
			item.set.set("dropable", rset.getInt("dropable") > 0);
			item.set.set("destroyable", rset.getInt("destroyable") > 0);
			item.set.set("temporal", rset.getInt("temporal") > 0);
			item.set.set("skill_id", rset.getString("skill_id"));
			item.set.set("skill_level", rset.getString("skill_level"));
			item.set.set("enchant4_skill_id", rset.getInt("enchant4_skill_id"));
			item.set.set("enchant4_skill_lvl", rset.getInt("enchant4_skill_lvl"));
			item.set.set("icon", rset.getString("icon"));

			if(item.type == ArmorType.PET)
			{
				item.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);

				if(item.set.getInteger("bodypart") == L2Item.SLOT_WOLF)
				{
					item.set.set("type2", L2Item.TYPE2_PET_WOLF);
					item.set.set("bodypart", L2Item.SLOT_CHEST);
				}
				else if(item.set.getInteger("bodypart") == L2Item.SLOT_GWOLF)
				{
					item.set.set("type2", L2Item.TYPE2_PET_GWOLF);
					item.set.set("bodypart", L2Item.SLOT_CHEST);
				}
				else if(item.set.getInteger("bodypart") == L2Item.SLOT_HATCHLING)
				{
					item.set.set("type2", L2Item.TYPE2_PET_HATCHLING);
					item.set.set("bodypart", L2Item.SLOT_CHEST);
				}
				else if(item.set.getInteger("bodypart") == L2Item.SLOT_PENDANT)
				{
					item.set.set("type2", L2Item.TYPE2_PENDANT);
					item.set.set("bodypart", L2Item.SLOT_NECK);
				}
				else if(item.set.getInteger("bodypart") == L2Item.SLOT_BABYPET)
				{
					item.set.set("type2", L2Item.TYPE2_PET_BABY);
					item.set.set("bodypart", L2Item.SLOT_CHEST);
				}
				else
				{
					item.set.set("type2", L2Item.TYPE2_PET_STRIDER);
					item.set.set("bodypart", L2Item.SLOT_CHEST);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return item;
	}

	/**
	 * Returns object Item from the record of the database
	 * @param rset : ResultSet designating a record of the [etcitem] table of database
	 * @return Item : object created from the database record
	 * @throws SQLException
	 */
	private Item readItem(ResultSet rset) throws SQLException
	{
		Item item = new Item();
		item.set = new StatsSet();
		item.id = rset.getInt("item_id");

		item.set.set("item_id", item.id);
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
		item.set.set("type1", L2Item.TYPE1_ITEM_QUESTITEM_ADENA);
		item.set.set("type2", L2Item.TYPE2_OTHER);
		item.set.set("bodypart", 0);
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("temporal", rset.getInt("temporal") > 0);
		item.set.set("icon", rset.getString("icon"));
		item.set.set("class", rset.getString("class"));
		String itemType = rset.getString("item_type");
		if(itemType.equals("none"))
			item.type = EtcItemType.OTHER; // only for default
		else if(itemType.equals("mticket"))
			item.type = EtcItemType.SCROLL; // dummy
		else if(itemType.equals("material"))
			item.type = EtcItemType.MATERIAL;
		else if(itemType.equals("pet_collar"))
			item.type = EtcItemType.PET_COLLAR;
		else if(itemType.equals("potion"))
			item.type = EtcItemType.POTION;
		else if(itemType.equals("recipe"))
			item.type = EtcItemType.RECIPE;
		else if(itemType.equals("scroll"))
			item.type = EtcItemType.SCROLL;
		else if(itemType.equals("seed"))
			item.type = EtcItemType.SEED;
		else if(itemType.equals("spellbook"))
			item.type = EtcItemType.SPELLBOOK; // Spellbook, Amulet, Blueprint
		else if(itemType.equals("shot"))
			item.type = EtcItemType.SHOT;
		else if(itemType.equals("arrow"))
		{
			item.type = EtcItemType.ARROW;
			item.set.set("bodypart", L2Item.SLOT_L_HAND);
		}
		else if(itemType.equals("bolt"))
		{
			item.type = EtcItemType.BOLT;
			item.set.set("bodypart", L2Item.SLOT_L_HAND);
		}
		else if(itemType.equals("bait"))
		{
			item.type = EtcItemType.BAIT;
			item.set.set("bodypart", L2Item.SLOT_L_HAND);
		}
		else if(itemType.equals("quest"))
		{
			item.type = EtcItemType.QUEST;
			item.set.set("type2", L2Item.TYPE2_QUEST);
		}
		else
		{
			_log.fine("unknown etcitem type:" + itemType);
			item.type = EtcItemType.OTHER;
		}

		String consume = rset.getString("consume_type");
		if(consume.equals("asset"))
		{
			item.type = EtcItemType.MONEY;
			item.set.set("stackable", true);
			item.set.set("type2", L2Item.TYPE2_MONEY);
		}
		else if(consume.equals("stackable"))
			item.set.set("stackable", true);
		else
			item.set.set("stackable", false);

		int crystal = _crystalTypes.get(rset.getString("crystal_type"));
		item.set.set("crystal_type", crystal);

		int weight = rset.getInt("weight");
		item.set.set("weight", weight);
		item.name = rset.getString("name");
		item.set.set("name", item.name);

		item.set.set("durability", rset.getInt("durability"));
		item.set.set("price", rset.getInt("price"));
		item.set.set("skill_id", rset.getString("skill_id"));
		item.set.set("skill_level", rset.getString("skill_level"));
		item.set.set("tradeable", rset.getInt("tradeable") > 0);
		item.set.set("dropable", rset.getInt("dropable") > 0);
		item.set.set("destroyable", rset.getInt("destroyable") > 0);
		item.set.set("flags", rset.getInt("flags"));

		return item;
	}

	/**
	 * Builds a variable in which all items are putting in in function of their ID.
	 */
	private void buildFastLookupTable()
	{
		int highestId = 0;

		for(Integer id : _armors.keySet())
			if(id > highestId)
				highestId = id;
		for(Integer id : _weapons.keySet())
			if(id > highestId)
				highestId = id;
		for(Integer id : _etcItems.keySet())
			if(id > highestId)
				highestId = id;

		// Create a FastLookUp Table called _allTemplates of size : value of the highest item ID
		_allTemplates = new L2Item[highestId + 1];

		for(Integer id : _armors.keySet())
		{
			L2Armor item = _armors.get(id);
			assert _allTemplates[id.intValue()] == null;
			_allTemplates[id.intValue()] = item;
		}

		for(Integer id : _weapons.keySet())
		{
			L2Weapon item = _weapons.get(id);
			assert _allTemplates[id.intValue()] == null;
			_allTemplates[id.intValue()] = item;
		}

		for(Integer id : _etcItems.keySet())
		{
			L2EtcItem item = _etcItems.get(id);
			assert _allTemplates[id.intValue()] == null;
			_allTemplates[id.intValue()] = item;
		}
	}

	/**
	 * Create the L2ItemInstance corresponding to the Item Identifier and add it to _allObjects of L2world.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create and Init the L2ItemInstance corresponding to the Item Identifier </li>
	 * <li>Add the L2ItemInstance object to _allObjects of L2world </li><BR><BR>
	 *
	 * @param itemId The Item Identifier of the L2ItemInstance that must be created
	 */
	public L2ItemInstance createItem(int itemId)
	{
		if(Config.DISABLE_CREATION_ID_LIST.contains(itemId))
		{
			Log.displayStackTrace(new Throwable(), "Try creating DISABLE_CREATION item " + itemId);
			return null;
		}
		return new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
	}

	public static boolean useHandler(L2Playable self, L2ItemInstance item, Boolean ctrl)
	{
		L2Player player;
		if(self.isPlayer())
			player = (L2Player) self;
		else if(self.isPet())
			player = self.getPlayer();
		else
			return false;

		// Вызов всех определенных скриптовых итемхэндлеров
		GArray<ScriptClassAndMethod> handlers = Scripts.itemHandlers.get(item.getItemId());
		if(handlers != null && handlers.size() > 0)
		{
			if(player.isInFlyingTransform())
			{
				player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
				return false;
			}

			Object[] script_args = new Object[] { player, ctrl };
			for(ScriptClassAndMethod handler : handlers)
				player.callScripts(handler.scriptClass, handler.method, script_args);
			return true;
		}

		L2Skill[] skills = item.getItem().getAttachedSkills();
		if(skills != null && skills.length > 0)
		{
			for(L2Skill skill : skills)
			{
				L2Character aimingTarget = skill.getAimingTarget(player, player.getTarget());
				if(skill.checkCondition(player, aimingTarget, false, false, true))
					player.getAI().Cast(skill, aimingTarget, false, false);
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns the item corresponding to the item ID
	 * @param id : int designating the item
	 */
	public L2Item getTemplate(int id)
	{
		if(!isExists(id))
		{
			_log.warning("ItemTable[604]: Not defined item_id=" + id + ", or out of range");
			Thread.dumpStack();
			return null;
		}
		return _allTemplates[id];
	}

	public L2Item[] getAllTemplates()
	{
		return _allTemplates;
	}

	public boolean isExists(int id)
	{
		return id < _allTemplates.length && id >= 0 && _allTemplates[id] != null;
	}

	public Collection<L2Weapon> getAllWeapons()
	{
		return _weapons.values();
	}

	/**
	 * Returns if ItemTable initialized
	 */
	public boolean isInitialized()
	{
		return _initialized;
	}

	public int[][] _weapon_ex;
	public int[][] _armor_ex;
	public HashMap<Integer, Integer> _item_idx;

	public static final int WEX_FOUNDATION = 0;
	public static final int WEX_RARE = 1;
	public static final int WEX_SA1 = 2;
	public static final int WEX_SA2 = 3;
	public static final int WEX_SA3 = 4;
	public static final int WEX_RARE_SA1 = 5;
	public static final int WEX_RARE_SA2 = 6;
	public static final int WEX_RARE_SA3 = 7;
	public static final int WEX_PVP1 = 8;
	public static final int WEX_PVP2 = 9;
	public static final int WEX_PVP3 = 10;
	public static final int WEX_RARE_PVP1 = 11;
	public static final int WEX_RARE_PVP2 = 12;
	public static final int WEX_RARE_PVP3 = 13;
	public static final int WEX_COMMON = 14;
	public static final int WEX_KAMAEL_EX = 15;
	public static final int WEX_SA_CRY1 = 16;
	public static final int WEX_SA_CRY2 = 17;
	public static final int WEX_SA_CRY3 = 18;
	public static final int WEX_VARNISH_COUNT = 19;

	private void loadExtendedWeaponsTable()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("select * from weapon_ex where item_id not in (select item_id from armor_ex)");
			rset = statement.executeQuery();
			HashMap<Integer, int[]> res = new HashMap<Integer, int[]>();
			while(rset.next())
			{
				int item_id = rset.getInt("item_id");
				int[] item = new int[] { rset.getInt("foundation"), rset.getInt("rare"), rset.getInt("sa1"),
						rset.getInt("sa2"), rset.getInt("sa3"), rset.getInt("raresa1"), rset.getInt("raresa2"),
						rset.getInt("raresa3"), rset.getInt("pvp1"), rset.getInt("pvp2"), rset.getInt("pvp3"),
						rset.getInt("rarepvp1"), rset.getInt("rarepvp2"), rset.getInt("rarepvp3"), rset.getInt("common"),
						rset.getInt("kamaex"), rset.getInt("sacry1"), rset.getInt("sacry2"), rset.getInt("sacry3"),
						rset.getInt("varnish"), };
				res.put(item_id, item);
				for(int i : item)
					if(i > 0)
						_item_idx.put(i, item_id);
			}
			_weapon_ex = new int[_allTemplates.length + 1][];
			for(Entry<Integer, int[]> e : res.entrySet())
				for(int i : e.getValue())
					if(i > 0)
					{
						_weapon_ex[e.getKey()] = e.getValue();
						break;
					}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static final int AEX_UNSEALED_1 = 0;
	public static final int AEX_UNSEALED_2 = 1;
	public static final int AEX_UNSEALED_3 = 2;
	public static final int AEX_FOUNDATION = 3;
	public static final int AEX_SEALED_RARE_1 = 4;
	public static final int AEX_SEALED_RARE_2 = 5;
	public static final int AEX_SEALED_RARE_3 = 6;
	public static final int AEX_UNSEALED_RARE_1 = 7;
	public static final int AEX_UNSEALED_RARE_2 = 8;
	public static final int AEX_UNSEALED_RARE_3 = 9;
	public static final int AEX_PvP = 10;
	public static final int AEX_COMMON = 11;
	public static final int AEX_UNSEALED_COMMON_1 = 12;
	public static final int AEX_UNSEALED_COMMON_2 = 13;
	public static final int AEX_UNSEALED_COMMON_3 = 14;
	public static final int AEX_VARNISH = 15;
	public static final int AEX_UNSEAL_PRICE = 16;
	public static final int AEX_RESEAL_PRICE = 17;

	private void loadExtendedArmorTable()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("select * from armor_ex");
			rset = statement.executeQuery();
			HashMap<Integer, int[]> res = new HashMap<Integer, int[]>();
			while(rset.next())
			{
				int item_id = rset.getInt("item_id");
				int[] item = new int[] { rset.getInt("uns1"), rset.getInt("uns2"), rset.getInt("uns3"),
						rset.getInt("foundation"), rset.getInt("srare1"), rset.getInt("srare2"), rset.getInt("srare3"),
						rset.getInt("rare1"), rset.getInt("rare2"), rset.getInt("rare3"), rset.getInt("pvp"),
						rset.getInt("common"), rset.getInt("unsc1"), rset.getInt("unsc2"), rset.getInt("unsc3"),
						rset.getInt("varnish"), rset.getInt("unseal"), rset.getInt("reseal"), };
				res.put(item_id, item);
				for(int i : item)
					if(i > 0)
						_item_idx.put(i, item_id);
			}
			_armor_ex = new int[_allTemplates.length + 1][];
			for(Entry<Integer, int[]> e : res.entrySet())
				for(int i : e.getValue())
					if(i > 0)
					{
						_armor_ex[e.getKey()] = e.getValue();
						break;
					}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public int findBaseId(int id)
	{
		Integer ret = _item_idx.get(id);
		return ret != null ? ret : 0;
	}

	public int[][] getWeaponEx()
	{
		return _weapon_ex;
	}

	public int[][] getArmorEx()
	{
		return _armor_ex;
	}

	public static void unload()
	{
		if(_instance != null)
		{
			_instance._etcItems.clear();
			_instance._armors.clear();
			_instance._weapons.clear();
			_instance.itemData.clear();
			_instance.weaponData.clear();
			_instance.armorData.clear();
			_instance = null;
		}
		_crystalTypes.clear();
		_weaponTypes.clear();
		_armorTypes.clear();
		_slots.clear();
	}
}