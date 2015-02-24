package l2p.gameserver.tables;

import java.sql.ResultSet;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.gameserver.model.L2ArmorSet;
import l2p.gameserver.model.L2Skill;

public class ArmorSetsTable
{
	private static Logger _log = Logger.getLogger(ArmorSetsTable.class.getName());
	private static ArmorSetsTable _instance;
	private boolean _initialized = true;

	private FastMap<Integer, L2ArmorSet> _armorSets;

	public static ArmorSetsTable getInstance()
	{
		if(_instance == null)
			_instance = new ArmorSetsTable();
		return _instance;
	}

	private ArmorSetsTable()
	{
		_armorSets = new FastMap<Integer, L2ArmorSet>().setShared(true);
		loadData();
	}

	private void loadData()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT chest, legs, head, gloves, feet, skill, shield, shield_skill, enchant6skill FROM armorsets");
			rset = statement.executeQuery();

			while(rset.next())
			{
				int chest = rset.getInt("chest");
				int legs = rset.getInt("legs");
				int head = rset.getInt("head");
				int gloves = rset.getInt("gloves");
				int feet = rset.getInt("feet");

				L2Skill skill = null;
				StringTokenizer st = new StringTokenizer(rset.getString("skill"), ";");
				if(st.hasMoreTokens())
					skill = SkillTable.getInstance().getInfo(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()));

				int shield = rset.getInt("shield");

				L2Skill shield_skill = null;
				st = new StringTokenizer(rset.getString("shield_skill"), ";");
				if(st.hasMoreTokens())
					shield_skill = SkillTable.getInstance().getInfo(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()));

				L2Skill enchant6skill = null;
				st = new StringTokenizer(rset.getString("enchant6skill"), ";");
				if(st.hasMoreTokens())
					enchant6skill = SkillTable.getInstance().getInfo(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()));

				if(_armorSets.containsKey(chest))
					_log.warning("Duplicate set for chest: " + chest);

				_armorSets.put(chest, new L2ArmorSet(chest, legs, head, gloves, feet, skill, shield, shield_skill, enchant6skill));

				int[] analog = ItemTable.getInstance().getArmorEx()[chest];
				if(analog != null)
				{
					if(analog[ItemTable.AEX_SEALED_RARE_1] > 0)
						_armorSets.put(analog[ItemTable.AEX_SEALED_RARE_1], new L2ArmorSet(analog[ItemTable.AEX_SEALED_RARE_1], legs, head, gloves, feet, skill, shield, shield_skill, enchant6skill));
				}
				else
					for(int[] arr : ItemTable.getInstance().getArmorEx())
						if(arr != null && arr[ItemTable.AEX_UNSEALED_1] == chest)
						{
							if(arr[ItemTable.AEX_UNSEALED_RARE_1] > 0)
								_armorSets.put(arr[ItemTable.AEX_UNSEALED_RARE_1], new L2ArmorSet(arr[ItemTable.AEX_UNSEALED_RARE_1], legs, head, gloves, feet, skill, shield, shield_skill, enchant6skill));
							break;
						}
			}

			_log.config("ArmorSetsTable: Loaded " + _armorSets.size() + " armor sets.");
		}
		catch(Exception e)
		{
			_log.severe("ArmorSetsTable: Error reading ArmorSets table: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public boolean setExists(int chestId)
	{
		return _armorSets.containsKey(chestId);
	}

	public L2ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	public static void unload()
	{
		if(_instance != null)
			_instance = null;
	}
}