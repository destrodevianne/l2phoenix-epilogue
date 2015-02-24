package l2p.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2p.Config;
import l2p.Server;
import l2p.common.ThreadPoolManager;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.database.mysql;
import l2p.extensions.Stat;
import l2p.extensions.network.MMOConnection;
import l2p.extensions.network.MMOSocket;
import l2p.extensions.network.SelectorConfig;
import l2p.extensions.network.SelectorThread;
import l2p.extensions.scripts.Events;
import l2p.extensions.scripts.ScriptObject;
import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.handler.AdminCommandHandler;
import l2p.gameserver.handler.ItemHandler;
import l2p.gameserver.handler.UserCommandHandler;
import l2p.gameserver.handler.VoicedCommandHandler;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.instancemanager.AuctionManager;
import l2p.gameserver.instancemanager.CastleManager;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.instancemanager.CastleSiegeManager;
import l2p.gameserver.instancemanager.ClanHallManager;
import l2p.gameserver.instancemanager.ClanHallSiegeManager;
import l2p.gameserver.instancemanager.CoupleManager;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.instancemanager.DimensionalRiftManager;
import l2p.gameserver.instancemanager.FortressManager;
import l2p.gameserver.instancemanager.FortressSiegeManager;
import l2p.gameserver.instancemanager.InstancedZoneManager;
import l2p.gameserver.instancemanager.LotteryManager;
import l2p.gameserver.instancemanager.MercTicketManager;
import l2p.gameserver.instancemanager.PartyRoomManager;
import l2p.gameserver.instancemanager.PlayerMessageStack;
import l2p.gameserver.instancemanager.RaidBossSpawnManager;
import l2p.gameserver.instancemanager.TownManager;
import l2p.gameserver.instancemanager.UnderGroundColliseumManager;
import l2p.gameserver.loginservercon.LSConnection;
import l2p.gameserver.model.AutoChatHandler;
import l2p.gameserver.model.AutoSpawnHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.MonsterRace;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.entity.siege.territory.TerritorySiege;
import l2p.gameserver.model.entity.vehicle.L2VehicleManager;
import l2p.gameserver.model.items.MailParcelController;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.network.L2GamePacketHandler;
import l2p.gameserver.tables.AirShipDocksTable;
import l2p.gameserver.tables.ArmorSetsTable;
import l2p.gameserver.tables.AugmentationData;
import l2p.gameserver.tables.CharNameTable;
import l2p.gameserver.tables.CharTemplateTable;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.tables.DoorTable;
import l2p.gameserver.tables.FakePlayersTable;
import l2p.gameserver.tables.HennaTable;
import l2p.gameserver.tables.HennaTreeTable;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.LevelUpTable;
import l2p.gameserver.tables.MapRegion;
import l2p.gameserver.tables.NpcTable;
import l2p.gameserver.tables.PetSkillsTable;
import l2p.gameserver.tables.SkillSpellbookTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTreeTable;
import l2p.gameserver.tables.SpawnTable;
import l2p.gameserver.tables.StaticObjectsTable;
import l2p.gameserver.tables.TeleportTable;
import l2p.gameserver.taskmanager.ItemsAutoDestroy;
import l2p.gameserver.taskmanager.MemoryWatchDog;
import l2p.gameserver.taskmanager.TaskManager;
import l2p.gameserver.webserver.WebServer;
import l2p.status.Status;
import l2p.util.GArray;
import l2p.util.HWID;
import l2p.util.Log;
import l2p.util.RRDTools;
import l2p.util.Strings;
import l2p.util.Util;

@SuppressWarnings({ "nls", "unqualified-field-access", "boxing" })
public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());

	private final SelectorThread<L2GameClient> _selectorThreads[];
	private final ItemTable _itemTable;
	public static GameServer gameServer;

	public static Status statusServer;
	public static Events events;

	public static FastMap<String, ScriptObject> scriptsObjects = new FastMap<String, ScriptObject>().setShared(true);

	private static int _serverStarted;
	private static boolean serverLoaded = false;
	private static String _serverName = null;
	private static int _serverId;

	public SelectorThread<L2GameClient>[] getSelectorThreads()
	{
		return _selectorThreads;
	}

	public static int time()
	{
		return (int) (System.currentTimeMillis() / 1000);
	}

	public static int uptime()
	{
		return time() - _serverStarted;
	}

	@SuppressWarnings("unchecked")
	public GameServer() throws Exception
	{
		Server.gameServer = this;

		_serverStarted = time();

		_log.finest("used mem:" + MemoryWatchDog.getMemUsedMb());

		Strings.reload();

		IdFactory _idFactory = IdFactory.getInstance();
		if(!_idFactory.isInitialized())
		{
			_log.severe("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}

		ThreadPoolManager.getInstance();

		if(Config.DEADLOCKCHECK_INTERVAL > 0)
			new DeadlockDetector().start();

		CrestCache.load();

		// start game time control early
		GameTimeController.getInstance();

		// keep the references of Singletons to prevent garbage collection
		CharNameTable.getInstance();

		AuctionManager.getInstance();

		ClanTable.getInstance();

		FakePlayersTable.getInstance();

		SkillTable.getInstance();

		PetSkillsTable.getInstance();

		_itemTable = ItemTable.getInstance();
		if(!_itemTable.isInitialized())
		{
			_log.severe("Could not find the >Items files. Please Check Your Data.");
			throw new Exception("Could not initialize the item table");
		}

		ArmorSetsTable _armorSetsTable = ArmorSetsTable.getInstance();
		if(!_armorSetsTable.isInitialized())
		{
			_log.severe("Could not find the ArmorSets files. Please Check Your Data.");
			throw new Exception("Could not initialize the armorSets table");
		}

		events = new Events();

		TradeController.getInstance();

		RecipeController.getInstance();

		SkillTreeTable.getInstance();
		SkillSpellbookTable.getInstance();
		CharTemplateTable.getInstance();

		NpcTable.getInstance();
		if(!NpcTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the npc table");
		}

		HennaTable _hennaTable = HennaTable.getInstance();
		if(!_hennaTable.isInitialized())
			throw new Exception("Could not initialize the Henna Table");
		HennaTreeTable.getInstance();
		if(!_hennaTable.isInitialized())
			throw new Exception("Could not initialize the Henna Tree Table");

		LevelUpTable.getInstance();

		GeoEngine.loadGeo();

		DoorTable.getInstance();

		UnderGroundColliseumManager.getInstance();

		TownManager.getInstance();

		CastleManager.getInstance();
		CastleSiegeManager.load();

		FortressManager.getInstance();
		FortressSiegeManager.load();

		ClanHallManager.getInstance();
		ClanHallSiegeManager.load();

		TerritorySiege.load();

		CastleManorManager.getInstance();

		SpawnTable.getInstance();

		RaidBossSpawnManager.getInstance();

		DimensionalRiftManager.getInstance();

		InstancedZoneManager.getInstance();

		Announcements.getInstance();

		LotteryManager.getInstance();

		MapRegion.getInstance();

		AugmentationData.getInstance();

		PlayerMessageStack.getInstance();

		if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance();

		MonsterRace.getInstance();

		StaticObjectsTable.getInstance();

		SevenSigns _sevenSignsEngine = SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		_sevenSignsEngine.updateFestivalScore();

		AutoSpawnHandler _autoSpawnHandler = AutoSpawnHandler.getInstance();
		_log.config("AutoSpawnHandler: Loaded " + _autoSpawnHandler.size() + " handlers in total.");

		AutoChatHandler _autoChatHandler = AutoChatHandler.getInstance();
		_log.config("AutoChatHandler: Loaded " + _autoChatHandler.size() + " handlers in total.");

		_sevenSignsEngine.spawnSevenSignsNPC();

		if(Config.ENABLE_OLYMPIAD)
		{
			Olympiad.load();
			Hero.getInstance();
		}

		CursedWeaponsManager.getInstance();

		if(!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			_log.config("CoupleManager initialized");
		}

		ItemHandler _itemHandler = ItemHandler.getInstance();
		_log.config("ItemHandler: Loaded " + _itemHandler.size() + " handlers.");

		AdminCommandHandler _adminCommandHandler = AdminCommandHandler.getInstance();
		_log.config("AdminCommandHandler: Loaded " + _adminCommandHandler.size() + " handlers.");

		UserCommandHandler _userCommandHandler = UserCommandHandler.getInstance();
		_log.config("UserCommandHandler: Loaded " + _userCommandHandler.size() + " handlers.");

		VoicedCommandHandler _voicedCommandHandler = VoicedCommandHandler.getInstance();
		_log.config("VoicedCommandHandler: Loaded " + _voicedCommandHandler.size() + " handlers.");

		TaskManager.getInstance();

		MercTicketManager.getInstance();

		L2VehicleManager.getInstance();
		AirShipDocksTable.getInstance();

		Shutdown _shutdownHandler = Shutdown.getInstance();
		Runtime.getRuntime().addShutdownHook(_shutdownHandler);

		try
		{
			// Colosseum doors
			DoorTable.getInstance().getDoor(24190001).openMe();
			DoorTable.getInstance().getDoor(24190002).openMe();
			DoorTable.getInstance().getDoor(24190003).openMe();
			DoorTable.getInstance().getDoor(24190004).openMe();

			// TOI doors
			DoorTable.getInstance().getDoor(23180001).openMe();
			DoorTable.getInstance().getDoor(23180002).openMe();
			DoorTable.getInstance().getDoor(23180003).openMe();
			DoorTable.getInstance().getDoor(23180004).openMe();
			DoorTable.getInstance().getDoor(23180005).openMe();
			DoorTable.getInstance().getDoor(23180006).openMe();

			// Эти двери, похоже выполняют декоративную функцию,
			// находятся во Frozen Labyrinth над мостом по пути к снежной королеве.
			DoorTable.getInstance().getDoor(23140001).openMe();
			DoorTable.getInstance().getDoor(23140002).openMe();

			DoorTable.getInstance().checkAutoOpen();
		}
		catch(NullPointerException e)
		{
			_log.warning("Doors table does not contain the right door info. Update doors.");
			e.printStackTrace();
		}

		_log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

		TeleportTable.getInstance();

		PartyRoomManager.getInstance();

		new File("./log/game").mkdirs();

		int restartTime = 0;
		int restartAt = 0;

		// Время запланированного на определенное время суток рестарта
		if(Config.RESTART_AT_TIME > -1)
		{
			Calendar calendarRestartAt = Calendar.getInstance();
			calendarRestartAt.set(Calendar.HOUR_OF_DAY, Config.RESTART_AT_TIME);
			calendarRestartAt.set(Calendar.MINUTE, 0);

			// Если запланированное время уже прошло, то берем +24 часа
			if(calendarRestartAt.getTimeInMillis() < System.currentTimeMillis())
				calendarRestartAt.add(Calendar.HOUR_OF_DAY, 24);

			restartAt = (int) (calendarRestartAt.getTimeInMillis() - System.currentTimeMillis()) / 1000;
		}

		// Время регулярного рестарта (через определенное время)
		restartTime = Config.RESTART_TIME * 60 * 60;

		// Проверяем какой рестарт раньше, регулярный или запланированный
		if(restartTime < restartAt && restartTime > 0 || restartTime > restartAt && restartAt == 0)
			Shutdown.getInstance().setAutoRestart(restartTime);
		else if(restartAt > 0)
			Shutdown.getInstance().setAutoRestart(restartAt);

		MailParcelController.getInstance();

		_log.info("GameServer Started");
		_log.config("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

		Stat.init();

		if(Config.RRD_ENABLED)
		{
			_log.config("Initializing RRD");
			RRDTools.init();
		}
		else
			_log.config("RRD Disabled");
			

		if(Config.PROTECT_ENABLE && Config.PROTECT_GS_ENABLE_HWID_BANS)
			HWID.reloadBannedHWIDs();

		if(Config.PROTECT_ENABLE && Config.PROTECT_GS_ENABLE_HWID_BONUS)
			HWID.reloadBonusHWIDs();

		MMOSocket.getInstance();
		LSConnection.getInstance().start();

		SelectorThread.setAntiFlood(Config.ANTIFLOOD_ENABLE);
		SelectorThread.setAntiFloodSocketsConf(Config.MAX_UNHANDLED_SOCKETS_PER_IP, Config.UNHANDLED_SOCKET_MIN_TTL);
		L2GamePacketHandler gph = new L2GamePacketHandler();
		SelectorConfig<L2GameClient> sc = new SelectorConfig<L2GameClient>(gph);
		sc.setMaxSendPerPass(30);
		sc.setSelectorSleepTime(1);
		SelectorThread.setGlobalReadLock(Config.PORTS_GAME.length > 1);
		_selectorThreads = new SelectorThread[Config.PORTS_GAME.length];
		for(int i = 0; i < Config.PORTS_GAME.length; i++)
		{
			_selectorThreads[i] = new SelectorThread<L2GameClient>(sc, gph, gph, gph, null);
			_selectorThreads[i].openServerSocket(null, Config.PORTS_GAME[i]);
			_selectorThreads[i].start();
		}

		if(Config.WEB_SERVER_DELAY > 0)
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WebServer(), 5000, Config.WEB_SERVER_DELAY);

		if(Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
			// Это довольно тяжелая задача поэтому пусть идет отдельным тридом
			new Thread(new Runnable(){
				@Override
				public void run()
				{
					if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
					{
						int min_offline_restore = (int) (System.currentTimeMillis() / 1000 - Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);
						mysql.set("DELETE FROM character_variables WHERE `name` = 'offline' AND `value` < " + min_offline_restore);
					}
					mysql.set("DELETE FROM character_variables WHERE `name` = 'offline' AND `obj_id` IN (SELECT `obj_id` FROM `characters` WHERE `accessLevel` < 0)");

					ThreadConnection con = null;
					FiltredPreparedStatement st = null;
					ResultSet rs = null;

					try
					{
						GArray<Object> logins = mysql.get_array(L2DatabaseFactory.getInstanceLogin(), "SELECT `login` FROM `accounts` WHERE `access_level` < 0");
						if(logins.size() > 0)
						{
							con = L2DatabaseFactory.getInstance().getConnection();
							st = con.prepareStatement("DELETE FROM character_variables WHERE `name` = 'offline' AND `obj_id` IN (SELECT `obj_id` FROM `characters` WHERE `account_name`=?)");
							for(Object login : logins)
							{
								st.setString(1, (String) login);
								st.executeUpdate();
							}
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						DatabaseUtils.closeDatabaseCSR(con, st, rs);
					}

					GArray<HashMap<String, Object>> list = mysql.getAll("SELECT `obj_id`, `value`, (SELECT `account_name` FROM `characters` WHERE `characters`.`obj_Id` = `character_variables`.`obj_id` LIMIT 1) AS `account_name` FROM `character_variables` WHERE name LIKE 'offline'");
					for(HashMap<String, Object> e : list)
					{
						L2GameClient client = new L2GameClient(new MMOConnection<L2GameClient>(null), true);
						client.setCharSelection((Integer) e.get("obj_id"));
						L2Player p = client.loadCharFromDisk(0);
						if(p == null || p.isDead())
							continue;
						client.setLoginName((String) e.get("account_name") == null ? "OfflineTrader_" + p.getName() : (String) e.get("account_name"));
						client.OnOfflineTrade();
						p.restoreBonus();
						p.spawnMe();
						p.updateTerritories();
						p.setOnlineStatus(true);
						p.setOfflineMode(true);
						p.setConnected(false);
						p.setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
						p.restoreEffects();
						p.restoreDisableSkills();
						p.broadcastUserInfo(true);
						if(p.getClan() != null && p.getClan().getClanMember(p.getObjectId()) != null)
							p.getClan().getClanMember(p.getObjectId()).setPlayerInstance(p);
						if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
							p.startKickTask((Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK + Integer.parseInt(e.get("value").toString())) * 1000L - System.currentTimeMillis());
					}
					_log.info("Restored " + list.size() + " offline traders");
				}
			}).start();
	}

	public final ItemTable getItemTable()
	{
		return _itemTable;
	}

	public static void main(String[] args) throws Exception
	{
		Server.SERVER_MODE = Server.MODE_GAMESERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./config/log.properties"; // Name of log file

		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();

		// Create input stream for log file -- or store file data into memory
		InputStream is = new FileInputStream(new File(LOG_NAME));
		LogManager.getLogManager().readConfiguration(is);
		is.close();

		// Initialize config
		Config.load();
		Util.waitForFreePorts(Config.GAMESERVER_HOSTNAME, Config.PORTS_GAME);
		L2DatabaseFactory.getInstance();
		Log.InitGSLoggers();

		gameServer = new GameServer();

		if(Config.IS_TELNET_ENABLED)
		{
			statusServer = new Status(Server.MODE_GAMESERVER);
			statusServer.start();
		}
		else
			_log.info("Telnet server is currently disabled.");

		Util.gc(5, 1000);
		_log.info("Free memory " + MemoryWatchDog.getMemFreeMb() + " of " + MemoryWatchDog.getMemMaxMb());
		Log.LogServ(Log.GS_started, (int) MemoryWatchDog.getMemFree(), (int) MemoryWatchDog.getMemMax(), IdFactory.getInstance().size(), 0);
		serverLoaded = true;

		//Shutdown.getInstance().setAutoRestart(Config.RESTART_TIME * 60 * 60);
	}

	public static boolean isLoaded()
	{
		return serverLoaded;
	}

	public static void setName(String val)
	{
		_serverName = val;
	}

	public static void setId(int val)
	{
		_serverId = val;
	}

	public static String getRegisteredName()
	{
		if(_serverName == null)
			return "[not registered on login]";
		return String.format("%s[id=%d]", _serverName, _serverId);
	}
}