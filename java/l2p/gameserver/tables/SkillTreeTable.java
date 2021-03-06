package l2p.gameserver.tables;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2SkillLearn;
import l2p.gameserver.model.L2Skill.SkillType;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.L2EnchantSkillLearn;
import l2p.gameserver.model.base.Race;
import l2p.util.GArray;
import l2p.util.Log;

@SuppressWarnings( { "nls", "unqualified-field-access", "boxing" })
public class SkillTreeTable
{
	public static final int NORMAL_ENCHANT_COST_MULTIPLIER = 1;
	public static final int SAFE_ENCHANT_COST_MULTIPLIER = 5;
	public static final int NORMAL_ENCHANT_BOOK = 6622;
	public static final int SAFE_ENCHANT_BOOK = 9627;
	public static final int CHANGE_ENCHANT_BOOK = 9626;
	public static final int UNTRAIN_ENCHANT_BOOK = 9625;

	private static final Logger _log = Logger.getLogger(SkillTreeTable.class.getName());

	private static SkillTreeTable _instance;

	private static FastMap<ClassId, GArray<L2SkillLearn>> _skillTrees;
	private static ArrayList<FastMap<Integer, FastMap<Integer, L2SkillLearn>>> _skillCostTable;
	private static FastMap<Integer, GArray<L2EnchantSkillLearn>> _enchant;
	private static GArray<L2SkillLearn> _fishingSkills;
	private static GArray<L2SkillLearn> _clanSkills;
	private static GArray<L2SkillLearn> _transformationSkills;

	private static GArray<L2SkillLearn> _transferSkills_b;
	private static GArray<L2SkillLearn> _transferSkills_ee;
	private static GArray<L2SkillLearn> _transferSkills_se;

	private static FastMap<Short, String> _unimplemented_skills;

	public static SkillTreeTable getInstance()
	{
		if(_instance == null)
			_instance = new SkillTreeTable();
		return _instance;
	}

	public static int getMinSkillLevel(int skillID, ClassId classID, int skillLVL)
	{
		if(skillLVL > 100) // enchanted skill - get max not enchanted level
		{
			GArray<L2EnchantSkillLearn> enchants = EnchantTable._enchant.get(skillID);
			if(enchants != null)
				skillLVL = enchants.get(0).getBaseLevel();
		}

		if(skillID > 0 && skillLVL > 0)
			for(L2SkillLearn sl : SkillTreeTable._skillTrees.get(classID))
				if(sl.skillLevel == skillLVL && sl.id == skillID)
					return sl.minLevel;

		return 0;
	}

	private SkillTreeTable()
	{
		new File("log/game/unimplemented_skills.txt").delete();

		_skillTrees = new FastMap<ClassId, GArray<L2SkillLearn>>().setShared(true);
		_fishingSkills = new GArray<L2SkillLearn>();
		_transformationSkills = new GArray<L2SkillLearn>();
		_clanSkills = new GArray<L2SkillLearn>();
		_unimplemented_skills = new FastMap<Short, String>().setShared(true);

		int classintid = 0;
		int count = 0;

		ThreadConnection con = null;
		FiltredPreparedStatement classliststatement = null;
		FiltredPreparedStatement skilltreestatement = null;
		ResultSet classlist = null, skilltree = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			classliststatement = con.prepareStatement("SELECT * FROM class_list ORDER BY id");
			skilltreestatement = con.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level, rep FROM skill_trees where class_id=? AND class_id >= 0 ORDER BY skill_id, level");
			classlist = classliststatement.executeQuery();
			while(classlist.next())
			{
				classintid = classlist.getInt("id");
				ClassId classId = ClassId.values()[classintid];
				GArray<L2SkillLearn> list = new GArray<L2SkillLearn>();

				skilltreestatement.setInt(1, classintid);
				skilltree = skilltreestatement.executeQuery();
				addSkills(con, skilltree, list);

				_skillTrees.put(ClassId.values()[classintid], list);
				count += list.size();

				ClassId secondparent = classId.getParent((byte) 1);
				if(secondparent == classId.getParent((byte) 0))
					secondparent = null;

				classId = classId.getParent((byte) 0);
				while(classId != null)
				{
					GArray<L2SkillLearn> parentList = _skillTrees.get(classId);
					list.addAll(parentList);
					classId = classId.getParent((byte) 0);
					if(classId == null && secondparent != null)
					{
						classId = secondparent;
						secondparent = secondparent.getParent((byte) 1);
					}
				}

				//_log.config("SkillTreeTable: skill tree for class " + classintid + " has " + list.size() + " skills");
			}
			DatabaseUtils.closeDatabaseSR(classliststatement, classlist);
			classliststatement = null;
			classlist = null;
			DatabaseUtils.closeDatabaseSR(skilltreestatement, skilltree);

			loadFishingSkills(con);
			loadTransformationSkills(con);
			loadClanSkills(con);

			_enchant = EnchantTable._enchant;
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "error while creating skill tree for classId " + classintid, e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseSR(classliststatement, classlist);
			DatabaseUtils.closeDatabaseSR(skilltreestatement, skilltree);
			DatabaseUtils.closeConnection(con);
		}

		loadSkillCostTable();

		_log.config("SkillTreeTable: Loaded " + count + " skills.");
		_log.config("SkillTreeTable: Loaded " + _fishingSkills.size() + " fishing skills.");
		_log.config("SkillTreeTable: Loaded " + _transformationSkills.size() + " transformation skills.");
		_log.config("SkillTreeTable: Loaded " + _clanSkills.size() + " clan skills.");
		_log.config("SkillTreeTable: Loaded " + _enchant.size() + " enchanted skills.");

		if(!_unimplemented_skills.isEmpty())
			_log.config("SkillTreeTable: Loaded " + _unimplemented_skills.size() + " not implemented skills!!!");

		for(Short id : _unimplemented_skills.keySet())
			Log.add(_unimplemented_skills.get(id) + " - " + id, "unimplemented_skills", "");

		_transferSkills_b = new GArray<L2SkillLearn>();
		_transferSkills_ee = new GArray<L2SkillLearn>();
		_transferSkills_se = new GArray<L2SkillLearn>();

		loadTransferSkills(_transferSkills_b, ClassId.bishop);
		loadTransferSkills(_transferSkills_ee, ClassId.elder);
		loadTransferSkills(_transferSkills_se, ClassId.shillienElder);
	}

	private void loadTransferSkills(GArray<L2SkillLearn> dest, ClassId classId)
	{
		for(L2SkillLearn temp : _skillTrees.get(classId))
			if(temp.getLevel() == SkillTable.getInstance().getBaseLevel(temp.id) && temp.id != 1520 && temp.id != 1521 && temp.id != 1522)
				dest.add(temp);
	}

	private void loadFishingSkills(ThreadConnection con) throws SQLException
	{
		FiltredPreparedStatement statement = null;
		ResultSet skilltree = null;
		try
		{
			statement = con.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level, rep FROM skill_trees WHERE class_id=-1 ORDER BY skill_id, level");
			skilltree = statement.executeQuery();
			addSkills(con, skilltree, _fishingSkills);
		}
		finally
		{
			DatabaseUtils.closeDatabaseSR(statement, skilltree);
		}
	}

	private void loadTransformationSkills(ThreadConnection con) throws SQLException
	{
		FiltredPreparedStatement statement = null;
		ResultSet skilltree = null;
		try
		{
			statement = con.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level, rep FROM skill_trees WHERE class_id=-4 ORDER BY skill_id, level");
			skilltree = statement.executeQuery();
			addSkills(con, skilltree, _transformationSkills);
		}
		finally
		{
			DatabaseUtils.closeDatabaseSR(statement, skilltree);
		}
	}

	private void loadClanSkills(ThreadConnection con) throws SQLException
	{
		FiltredPreparedStatement statement = null;
		ResultSet skilltree = null;
		try
		{
			statement = con.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level, rep FROM skill_trees WHERE class_id=-2 ORDER BY skill_id, level");
			skilltree = statement.executeQuery();
			addSkills(con, skilltree, _clanSkills);
		}
		finally
		{
			DatabaseUtils.closeDatabaseSR(statement, skilltree);
		}
	}

	private void addSkills(ThreadConnection con, ResultSet skilltree, GArray<L2SkillLearn> dest) throws SQLException
	{
		while(skilltree.next())
		{
			short id = skilltree.getShort("skill_id");
			byte lvl = skilltree.getByte("level");
			String name = skilltree.getString("name");
			if(lvl == 1)
			{
				L2Skill s = SkillTable.getInstance().getInfo(id, 1);
				if(s == null || s.getSkillType() == SkillType.NOTDONE)
					_unimplemented_skills.put(id, name == null ? "" : name);
			}
			byte minLvl = skilltree.getByte("min_level");
			int cost;
			short itemId = 0;
			int itemCount = 0;
			if(skilltree.getInt("class_id") == -2)
				cost = skilltree.getInt("rep");
			else
				cost = skilltree.getInt("sp");
			FiltredPreparedStatement statement2 = con.prepareStatement("SELECT item_id, item_count FROM skill_spellbooks WHERE skill_id=? AND level=?");
			statement2.setInt(1, id);
			statement2.setInt(2, lvl);
			ResultSet itemIdCount = statement2.executeQuery();
			if(itemIdCount.next())
			{
				itemId = itemIdCount.getShort("item_id");
				itemCount = itemIdCount.getInt("item_count");
			}
			statement2.close();
			L2SkillLearn skl = new L2SkillLearn(id, lvl, minLvl, name, cost, itemId, itemCount, skilltree.getInt("class_id") == -1, skilltree.getInt("class_id") == -2, skilltree.getInt("class_id") == -4);
			dest.add(skl);
		}
	}

	private void loadSkillCostTable()
	{
		_skillCostTable = new ArrayList<FastMap<Integer, FastMap<Integer, L2SkillLearn>>>(ClassId.values().length + 1);
		for(ClassId cid : ClassId.values())
			_skillCostTable.add(cid.getId(), new FastMap<Integer, FastMap<Integer, L2SkillLearn>>().setShared(true));

		for(ClassId classId : _skillTrees.keySet())
		{
			FastMap<Integer, FastMap<Integer, L2SkillLearn>> skt = _skillCostTable.get(classId.getId());

			GArray<L2SkillLearn> lst = _skillTrees.get(classId);
			for(L2SkillLearn skl : lst)
			{
				FastMap<Integer, L2SkillLearn> skillmap = skt.get((int) skl.getId());
				if(skillmap == null)
				{
					skillmap = new FastMap<Integer, L2SkillLearn>().setShared(true);
					skt.put((int) skl.getId(), skillmap);
				}
				skillmap.put((int) skl.getLevel(), skl);
			}
		}
	}

	public GArray<L2SkillLearn> getAvailableSkills(L2Player cha, ClassId classId)
	{
		GArray<L2SkillLearn> result = new GArray<L2SkillLearn>();
		GArray<L2SkillLearn> skills = _skillTrees.get(classId);
		if(skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning("Skilltree for class " + classId + " is not defined !");
			return new GArray<L2SkillLearn>(0);
		}

		L2Skill[] oldSkills = cha.getAllSkillsArray();
		for(L2SkillLearn temp : skills)
			if(temp.minLevel <= cha.getLevel())
			{
				boolean knownSkill = false;
				for(L2Skill s : oldSkills)
					if(s.getId() == temp.id)
					{
						if(s.getLevel() == temp.skillLevel - 1)
							result.add(temp); // this is the next level of a skill that we know
						knownSkill = true;
						break;
					}
				if(!knownSkill && temp.skillLevel == 1)
					result.add(temp); // this is a new skill
			}
		return result;
	}

	public GArray<L2SkillLearn> getAvailableTransferSkills(L2Player cha, ClassId classId)
	{
		GArray<L2SkillLearn> skills;
		switch(classId)
		{
			case cardinal:
				skills = _transferSkills_b;
				break;
			case evaSaint:
				skills = _transferSkills_ee;
				break;
			case shillienSaint:
				skills = _transferSkills_se;
				break;
			default:
				return new GArray<L2SkillLearn>(0);
		}

		L2Skill[] oldSkills = cha.getAllSkillsArray();
		GArray<L2SkillLearn> result = new GArray<L2SkillLearn>();

		for(L2SkillLearn temp : skills)
			if(temp.minLevel <= cha.getLevel())
			{
				boolean knownSkill = false;
				for(L2Skill s : oldSkills)
					if(s.getId() == temp.id)
					{
						knownSkill = true;
						break;
					}
				if(!knownSkill)
					result.add(temp);
			}

		return result;
	}

	public L2SkillLearn[] getAvailableClanSkills(L2Clan clan)
	{
		GArray<L2SkillLearn> result = new GArray<L2SkillLearn>();
		GArray<L2SkillLearn> skills = _clanSkills;

		if(skills == null)
			return new L2SkillLearn[0];

		L2Skill[] oldSkills = clan.getAllSkills();

		for(L2SkillLearn temp : skills)
			if(temp.minLevel <= clan.getLevel())
			{
				boolean knownSkill = false;

				for(int j = 0; j < oldSkills.length && !knownSkill; j++)
					if(oldSkills[j].getId() == temp.id)
					{
						knownSkill = true;

						if(oldSkills[j].getLevel() == temp.skillLevel - 1)
							// this is the next level of a skill that we know
							result.add(temp);
					}

				if(!knownSkill && temp.skillLevel == 1)
					// this is a new skill
					result.add(temp);
			}

		return result.toArray(new L2SkillLearn[result.size()]);
	}

	public GArray<L2Skill> getSkillsToEnchant(L2Player cha)
	{
		GArray<L2Skill> result = new GArray<L2Skill>();

		L2Skill[] skills = cha.getAllSkillsArray();
		if(skills.length == 0)
			return result;

		for(L2Skill s : skills)
		{
			GArray<L2EnchantSkillLearn> al = _enchant.get(s.getId());
			if(al != null && al.get(0).getBaseLevel() <= s.getLevel() && s.getLevel() < SkillTable.getInstance().getMaxLevel(s.getId()))
				result.add(s);
		}

		return result;
	}

	public static GArray<L2EnchantSkillLearn> getFirstEnchantsForSkill(int skillid)
	{
		GArray<L2EnchantSkillLearn> result = new GArray<L2EnchantSkillLearn>();

		GArray<L2EnchantSkillLearn> enchants = _enchant.get(skillid);
		if(enchants == null)
			return result;

		for(L2EnchantSkillLearn e : enchants)
			if(e.getLevel() % 100 == 1)
				result.add(e);

		return result;
	}

	public static int isEnchantable(L2Skill skill)
	{
		GArray<L2EnchantSkillLearn> enchants = _enchant.get(skill.getId());
		if(enchants == null)
			return 0;

		for(L2EnchantSkillLearn e : enchants)
			if(e.getBaseLevel() <= skill.getLevel())
				return 1;

		return 0;
	}

	public static GArray<L2EnchantSkillLearn> getEnchantsForChange(int skillid, int level)
	{
		GArray<L2EnchantSkillLearn> result = new GArray<L2EnchantSkillLearn>();

		GArray<L2EnchantSkillLearn> enchants = _enchant.get(skillid);
		if(enchants == null)
			return result;

		for(L2EnchantSkillLearn e : enchants)
			if(e.getLevel() % 100 == level % 100)
				result.add(e);

		return result;
	}

	public static L2EnchantSkillLearn getSkillEnchant(int skillid, int level)
	{
		GArray<L2EnchantSkillLearn> enchants = _enchant.get(skillid);
		if(enchants == null)
			return null;

		for(L2EnchantSkillLearn e : enchants)
			if(e.getLevel() == level)
				return e;
		return null;
	}

	/**
	 * Преобразует уровень скила из клиентского представления в серверное
	 * @param baseLevel базовый уровень скила - максимально возможный без заточки
	 * @param level - текущий уровень скила
	 * @param enchantlevels TODO
	 * @return уровень скила
	 */
	public static int convertEnchantLevel(int baseLevel, int level, int enchantlevels)
	{
		if(level < 100)
			return level;
		return baseLevel + ((level - level % 100) / 100 - 1) * enchantlevels + level % 100;
	}

	public static L2SkillLearn getSkillLearn(int skillid, int level, ClassId classid, L2Clan clan, boolean isTransfer)
	{
		if(isTransfer)
		{
			for(L2SkillLearn tmp : _transferSkills_b)
				if(tmp.id == skillid)
					return tmp;
			for(L2SkillLearn tmp : _transferSkills_ee)
				if(tmp.id == skillid)
					return tmp;
			for(L2SkillLearn tmp : _transferSkills_se)
				if(tmp.id == skillid)
					return tmp;
			return null;
		}

		if(clan != null)
		{
			L2SkillLearn[] clskills = getInstance().getAvailableClanSkills(clan);
			for(L2SkillLearn tmp : clskills)
				if(tmp.id == skillid && tmp.skillLevel == level)
					return tmp;
			return null;
		}

		if(_fishingSkills != null)
			for(L2SkillLearn tmp : _fishingSkills)
				if(tmp.id == skillid && tmp.skillLevel == level)
					return tmp;

		if(_transformationSkills != null)
			for(L2SkillLearn tmp : _transformationSkills)
				if(tmp.id == skillid && tmp.skillLevel == level)
					return tmp;

		for(L2SkillLearn tmp : _skillTrees.get(classid))
			if(tmp.id == skillid && tmp.skillLevel == level)
				return tmp;

		return null;
	}

	public L2SkillLearn[] getAvailableTransformationSkills(L2Player cha)
	{
		GArray<L2SkillLearn> result = new GArray<L2SkillLearn>();
		if(_transformationSkills == null)
		{
			_log.warning("Transformation skills not defined!");
			return new L2SkillLearn[0];
		}

		L2Skill[] oldSkills = cha.getAllSkillsArray();

		for(L2SkillLearn temp : _transformationSkills)
			if(temp.minLevel <= cha.getLevel())
			{
				boolean knownSkill = false;
				for(L2Skill s : oldSkills)
				{
					if(knownSkill)
						break;
					if(s.getId() == temp.id)
					{
						knownSkill = true;
						if(s.getLevel() == temp.skillLevel - 1)
							result.add(temp);
					}
				}

				if(!knownSkill && temp.skillLevel == 1)
					result.add(temp);
			}
		return result.toArray(new L2SkillLearn[result.size()]);
	}

	public L2SkillLearn[] getAvailableFishingSkills(L2Player cha)
	{
		GArray<L2SkillLearn> result = new GArray<L2SkillLearn>();
		if(_fishingSkills == null)
		{
			_log.warning("Fishing skills not defined!");
			return new L2SkillLearn[0];
		}

		L2Skill[] oldSkills = cha.getAllSkillsArray();

		for(L2SkillLearn temp : _fishingSkills)
			if(temp.minLevel <= cha.getLevel())
			{
				if(temp.getId() == 1368 && cha.getRace() != Race.dwarf)
					continue; //Expand Dwarven Craft

				boolean knownSkill = false;
				for(L2Skill s : oldSkills)
				{
					if(knownSkill)
						break;
					if(s.getId() == temp.id)
					{
						knownSkill = true;
						if(s.getLevel() == temp.skillLevel - 1)
							result.add(temp);
					}
				}

				if(!knownSkill && temp.skillLevel == 1)
					result.add(temp);
			}
		return result.toArray(new L2SkillLearn[result.size()]);
	}

	public byte getMinLevelForNewSkill(L2Player cha, ClassId classId)
	{
		byte minlevel = 0;
		//GArray<L2SkillLearn> result = new GArray<L2SkillLearn>();
		GArray<L2SkillLearn> skills = _skillTrees.get(classId);
		if(skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning("Skilltree for class " + classId + " is not defined !");
			return minlevel;
		}

		//L2Skill[] oldSkills = cha.getAllSkills();

		for(L2SkillLearn temp : skills)
			if(temp.minLevel > cha.getLevel())
				if(minlevel == 0 || temp.minLevel < minlevel)
					minlevel = temp.minLevel;
		return minlevel;
	}

	public int getSkillCost(L2Player player, L2Skill skill)
	{
		// Скилы трансформации
		if(skill.getSkillType() == SkillType.TRANSFORMATION)
			return 0;

		// TODO снести этот костыль
		switch(skill.getId())
		{
			// Рыбацкие скилы
			case 1312:
			case 1313:
			case 1314:
			case 1315:
			case 1368:
			case 1369:
			case 1370:
			case 1371:
			case 1372:
				return 0;
		}

		FastMap<Integer, FastMap<Integer, L2SkillLearn>> skt = _skillCostTable.get(player.getActiveClassId());
		if(skt == null)
			return Integer.MAX_VALUE;
		FastMap<Integer, L2SkillLearn> skillmap = skt.get(skill.getId());
		if(skillmap == null)
			return Integer.MAX_VALUE;
		L2SkillLearn skl = skillmap.get(1 + Math.max(player.getSkillLevel(skill.getId()), 0));
		if(skl == null)
			return Integer.MAX_VALUE;
		return skl.getSpCost();
	}

	public int getSkillRepCost(L2Clan clan, L2Skill skill)
	{
		int min = 100000000;
		int lvl = clan.getLeader().getPlayer().getSkillLevel(skill.getId());

		if(lvl > 0)
			lvl += 1;
		else
			lvl = 1;
		if(_clanSkills != null)
			for(L2SkillLearn tmp : _clanSkills)
			{
				if(tmp.id != skill.getId())
					continue;
				if(tmp.skillLevel != lvl)
					continue;
				if(tmp.minLevel > clan.getLevel())
					continue;
				min = Math.min(min, Math.round(tmp._repCost));
			}
		return min;
	}

	/**
	 * Возвращает true если скилл может быть изучен данным классом
	 * @param player
	 * @param skill_id
	 * @param skill_level
	 * @return true/false
	 */
	public boolean isSkillPossible(L2Player player, int skillid, int level)
	{
		for(L2SkillLearn tmp : _clanSkills)
			if(tmp.id == skillid && tmp.skillLevel <= level)
				return true;

		GArray<L2SkillLearn> skills = _skillTrees.get(ClassId.values()[player.getActiveClassId()]);
		for(L2SkillLearn skilllearn : skills)
			if(skilllearn.id == skillid && skilllearn.skillLevel <= level)
				return true;

		// Проверяем, трансферился ли скилл
		ClassId classId = player.getClassId();
		if(classId != null)
		{
			int item_id = 0;
			switch(classId)
			{
				case cardinal:
					item_id = 15307;
					break;
				case evaSaint:
					item_id = 15308;
					break;
				case shillienSaint:
					item_id = 15309;
					break;
			}
			if(item_id > 0)
			{
				String var = player.getVar("TransferSkills" + item_id);
				if(var != null && !var.isEmpty())
					for(String tmp : var.split(";"))
						if(Integer.parseInt(tmp) == skillid)
							return true;
			}
		}

		return false;
	}

	public static void unload()
	{
		if(_instance != null)
			_instance = null;
		_skillTrees.clear();
		_skillCostTable.clear();
		_enchant.clear();
		_fishingSkills.clear();
		_clanSkills.clear();
		_transformationSkills.clear();
		_unimplemented_skills.clear();
	}
}