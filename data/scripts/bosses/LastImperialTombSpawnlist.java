package bosses;

import java.sql.ResultSet;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.extensions.scripts.Functions;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.model.L2Spawn;
import l2p.gameserver.tables.NpcTable;
import l2p.gameserver.templates.L2NpcTemplate;
import l2p.util.GArray;

public class LastImperialTombSpawnlist extends Functions implements ScriptFile
{
	private static GArray<L2Spawn> _Room1SpawnList1st = new GArray<L2Spawn>();
	private static GArray<L2Spawn> _Room1SpawnList2nd = new GArray<L2Spawn>();
	private static GArray<L2Spawn> _Room1SpawnList3rd = new GArray<L2Spawn>();
	private static GArray<L2Spawn> _Room1SpawnList4th = new GArray<L2Spawn>();
	private static GArray<L2Spawn> _Room2InsideSpawnList = new GArray<L2Spawn>();
	private static GArray<L2Spawn> _Room2OutsideSpawnList = new GArray<L2Spawn>();

	public static void fill()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM lastimperialtomb_spawnlist");
			rset = statement.executeQuery();

			int npcTemplateId;
			L2Spawn spawnDat;
			L2NpcTemplate npcTemplate;

			while(rset.next())
			{
				npcTemplateId = rset.getInt("npc_templateid");
				npcTemplate = NpcTable.getTemplate(npcTemplateId);
				if(npcTemplate != null)
				{
					spawnDat = new L2Spawn(npcTemplate);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));

					switch(npcTemplateId)
					{
						case 18328:
						case 18330:
						case 18332:
							_Room1SpawnList1st.add(spawnDat);
							break;
						case 18329:
							_Room1SpawnList2nd.add(spawnDat);
							break;
						case 18333:
							_Room1SpawnList3rd.add(spawnDat);
							break;
						case 18331:
							_Room1SpawnList4th.add(spawnDat);
							break;
						case 18339:
							_Room2InsideSpawnList.add(spawnDat);
							break;
						case 18334:
						case 18335:
						case 18336:
						case 18337:
						case 18338:
							_Room2OutsideSpawnList.add(spawnDat);
							break;
					}
				}
				else
					System.out.println("LastImperialTombSpawnlist: Data missing in NPC table for ID: " + npcTemplateId + ".");
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

		System.out.println("LastImperialTombSpawnlist: Loaded " + _Room1SpawnList1st.size() + " Room1 1st Npc Spawn Locations.");
		System.out.println("LastImperialTombSpawnlist: Loaded " + _Room1SpawnList2nd.size() + " Room1 2nd Npc Spawn Locations.");
		System.out.println("LastImperialTombSpawnlist: Loaded " + _Room1SpawnList3rd.size() + " Room1 3rd Npc Spawn Locations.");
		System.out.println("LastImperialTombSpawnlist: Loaded " + _Room1SpawnList4th.size() + " Room1 4th Npc Spawn Locations.");
		System.out.println("LastImperialTombSpawnlist: Loaded " + _Room2InsideSpawnList.size() + " Room2 Inside Npc Spawn Locations.");
		System.out.println("LastImperialTombSpawnlist: Loaded " + _Room2OutsideSpawnList.size() + " Room2 Outside Npc Spawn Locations.");
	}

	public static void clear()
	{
		_Room1SpawnList1st.clear();
		_Room1SpawnList2nd.clear();
		_Room1SpawnList3rd.clear();
		_Room1SpawnList4th.clear();
		_Room2InsideSpawnList.clear();
		_Room2OutsideSpawnList.clear();
	}

	public static GArray<L2Spawn> getRoom1SpawnList1st()
	{
		return _Room1SpawnList1st;
	}

	public static GArray<L2Spawn> getRoom1SpawnList2nd()
	{
		return _Room1SpawnList2nd;
	}

	public static GArray<L2Spawn> getRoom1SpawnList3rd()
	{
		return _Room1SpawnList3rd;
	}

	public static GArray<L2Spawn> getRoom1SpawnList4th()
	{
		return _Room1SpawnList4th;
	}

	public static GArray<L2Spawn> getRoom2InsideSpawnList()
	{
		return _Room2InsideSpawnList;
	}

	public static GArray<L2Spawn> getRoom2OutsideSpawnList()
	{
		return _Room2OutsideSpawnList;
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
}