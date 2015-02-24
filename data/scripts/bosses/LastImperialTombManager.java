package bosses;

import java.util.concurrent.ScheduledFuture;

import l2p.common.ThreadPoolManager;
import l2p.extensions.scripts.Functions;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2CommandChannel;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Spawn;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.L2Zone.ZoneType;
import l2p.gameserver.model.instances.L2DoorInstance;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.serverpackets.ExShowScreenMessage;
import l2p.gameserver.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2p.gameserver.tables.DoorTable;
import l2p.util.GArray;
import l2p.util.Rnd;

public class LastImperialTombManager extends Functions implements ScriptFile
{
	private static boolean _isInvaded = false;

	// instance list of monsters.
	private static GArray<L2NpcInstance> _hallAlarmDevices = new GArray<L2NpcInstance>();
	private static GArray<L2NpcInstance> _darkChoirCaptains = new GArray<L2NpcInstance>();
	private static GArray<L2NpcInstance> _room1Monsters = new GArray<L2NpcInstance>();
	private static GArray<L2NpcInstance> _room2InsideMonsters = new GArray<L2NpcInstance>();
	private static GArray<L2NpcInstance> _room2OutsideMonsters = new GArray<L2NpcInstance>();

	// instance list of doors.
	private static GArray<L2DoorInstance> _room1Doors = new GArray<L2DoorInstance>();
	private static GArray<L2DoorInstance> _room2InsideDoors = new GArray<L2DoorInstance>();
	private static L2DoorInstance _room2OutsideDoor1;
	private static L2DoorInstance _room2OutsideDoor2;
	private static L2DoorInstance _room3Door = null;

	private static L2Player _commander = null;

	// Frintezza's Magic Force Field Removal Scroll.
	private static final int SCROLL = 8073;

	// player does reach to HallofFrintezza
	private static boolean _isReachToHall = false;

	private static final int[][] _invadeLoc = { { 173235, -76884, -5107 }, { 175003, -76933, -5107 },
			{ 174196, -76190, -5107 }, { 174013, -76120, -5107 }, { 173263, -75161, -5107 } };

	private static ScheduledFuture<?> _InvadeTask = null;
	private static ScheduledFuture<?> _RegistrationTimeInfoTask = null;
	private static ScheduledFuture<?> _Room1SpawnTask = null;
	private static ScheduledFuture<?> _Room2InsideDoorOpenTask = null;
	private static ScheduledFuture<?> _Room2OutsideSpawnTask = null;
	private static ScheduledFuture<?> _CheckTimeUpTask = null;

	private static L2Zone _zone;

	private static final int ALARM_DEVICE = 18328;
	private static final int CHOIR_PRAYER = 18339;
	private static final int CHOIR_CAPTAIN = 18334;

	private static final int LIT_MIN_PARTY_CNT = 4;
	private static final int LIT_MAX_PARTY_CNT = 5;
	private static final int LIT_TIME_LIMIT = 35;

	private static void init()
	{
		_zone = ZoneManager.getInstance().getZoneById(ZoneType.epic, 702121, false);
		LastImperialTombSpawnlist.clear();
		LastImperialTombSpawnlist.fill();
		initDoors();
		System.out.println("LastImperialTombManager: Init The Last Imperial Tomb.");
	}

	private static void initDoors()
	{
		_room1Doors.clear();
		_room1Doors.add(DoorTable.getInstance().getDoor(25150042));
		for(int i = 25150051; i <= 25150058; i++)
			_room1Doors.add(DoorTable.getInstance().getDoor(i));
		_room2InsideDoors.clear();
		for(int i = 25150061; i <= 25150070; i++)
			_room2InsideDoors.add(DoorTable.getInstance().getDoor(i));
		_room2OutsideDoor1 = DoorTable.getInstance().getDoor(25150043);
		_room2OutsideDoor2 = DoorTable.getInstance().getDoor(25150045);
		_room3Door = DoorTable.getInstance().getDoor(25150046);
		for(L2DoorInstance door : _room1Doors)
			door.closeMe();
		for(L2DoorInstance door : _room2InsideDoors)
			door.closeMe();
		_room2OutsideDoor1.closeMe();
		_room2OutsideDoor2.closeMe();
		_room3Door.closeMe();
	}

	// RegistrationMode = command channel.
	public void tryRegistration()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;
		if(!FrintezzaManager.isEnableEnterToLair())
		{
			player.sendMessage("Currently no entry possible.");
			return;
		}
		if(_isInvaded)
		{
			player.sendMessage("Another group is already fighting inside the imperial tomb.");
			return;
		}
		if(player.getParty() == null || !player.getParty().isInCommandChannel())
		{
			player.sendPacket(Msg.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_IN_A_CURRENT_COMMAND_CHANNEL);
			return;
		}
		L2CommandChannel cc = player.getParty().getCommandChannel();
		for(L2Player member : cc.getMembers())
			if(member != null && !player.isInRange(member, 500))
			{
				member.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
				player.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
				return;
			}
		if(cc.getChannelLeader() != player)
		{
			player.sendMessage("You must be leader of the command channel.");
			return;
		}
		if(cc.getParties().size() < LIT_MIN_PARTY_CNT)
		{
			player.sendMessage("The command channel must contains at least " + LIT_MIN_PARTY_CNT + " parties.");
			return;
		}
		if(cc.getParties().size() > LIT_MAX_PARTY_CNT)
		{
			player.sendMessage("The command channel must contains not more than " + LIT_MAX_PARTY_CNT + " parties.");
			return;
		}
		if(player.getInventory().getCountOf(SCROLL) < 1)
		{
			player.sendMessage("You must possess a \"Frintezza's Magic Force Field Removal Scroll\".");
			return;
		}
		registration(player);
	}

	// registration to enter to tomb.
	private static synchronized void registration(L2Player pc)
	{
		if(_commander != null)
			return;
		_commander = pc;
		pc.getInventory().destroyItemByItemId(SCROLL, 1, true);
		if(_InvadeTask != null)
			_InvadeTask.cancel(true);
		_InvadeTask = ThreadPoolManager.getInstance().scheduleGeneral(new Invade(), 10000);
	}

	private static void doInvade()
	{
		L2Party party = _commander.getParty();
		if(party == null)
		{
			_commander = null;
			return;
		}
		L2CommandChannel channel = party.getCommandChannel();
		if(channel == null)
		{
			_commander = null;
			return;
		}
		GArray<L2Party> parties = channel.getParties();
		if(parties == null || parties.size() < LIT_MIN_PARTY_CNT || parties.size() > LIT_MAX_PARTY_CNT)
		{
			_commander = null;
			return;
		}
		int locId = 0;
		for(L2Party pt : parties)
		{
			if(locId >= 5)
				locId = 0;
			for(L2Player pc : pt.getPartyMembers())
				pc.teleToLocation(_invadeLoc[locId][0] + Rnd.get(50), _invadeLoc[locId][1] + Rnd.get(50), _invadeLoc[locId][2]);
			locId++;
		}

		initDoors();
		_isInvaded = true;

		L2NpcInstance mob;
		for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom1SpawnList1st())
			if(spawn.getNpcId() == ALARM_DEVICE)
			{
				mob = spawn.doSpawn(true);
				mob.getSpawn().stopRespawn();
				_hallAlarmDevices.add(mob);
			}

		if(_Room1SpawnTask != null)
			_Room1SpawnTask.cancel(true);
		_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs1st(), 5000);
		if(_CheckTimeUpTask != null)
			_CheckTimeUpTask.cancel(true);
		_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(LIT_TIME_LIMIT * 60000), 15000);
	}

	public static void OnDie(L2Character self, L2Character killer)
	{
		if(self == null)
			return;
		switch(self.getNpcId())
		{
			case ALARM_DEVICE:
				onKillHallAlarmDevice();
				break;
			case CHOIR_PRAYER:
				onKillDarkChoirPlayer();
				break;
			case CHOIR_CAPTAIN:
				onKillDarkChoirCaptain();
				break;
		}
	}

	// Is the door of room1 in confirmation to open.
	private static void onKillHallAlarmDevice()
	{
		int killCnt = 0;
		for(L2NpcInstance HallAlarmDevice : _hallAlarmDevices)
			if(HallAlarmDevice.isDead())
				killCnt++;
		switch(killCnt)
		{
			case 1:
				if(_Room1SpawnTask != null)
					_Room1SpawnTask.cancel(true);
				_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs2nd(), 3000);
				break;
			case 2:
				if(_Room1SpawnTask != null)
					_Room1SpawnTask.cancel(true);
				_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs3rd(), 3000);
				break;
			case 3:
				if(_Room1SpawnTask != null)
					_Room1SpawnTask.cancel(true);
				_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs4th(), 3000);
				break;
			case 4:
				if(_Room1SpawnTask != null)
				{
					_Room1SpawnTask.cancel(true);
					_Room1SpawnTask = null;
				}
				openRoom1Doors();
				_room2OutsideDoor1.openMe();
				spawnRoom2InsideMob();
				break;
		}
	}

	// Is the door of inside of room2 in confirmation to open.
	private static void onKillDarkChoirPlayer()
	{
		int killCnt = 0;
		for(L2NpcInstance DarkChoirPlayer : _room2InsideMonsters)
			if(DarkChoirPlayer.isDead())
				killCnt++;
		if(_room2InsideMonsters.size() <= killCnt)
		{
			if(_Room2InsideDoorOpenTask != null)
				_Room2InsideDoorOpenTask.cancel(true);
			if(_Room2OutsideSpawnTask != null)
				_Room2OutsideSpawnTask.cancel(true);
			_Room2InsideDoorOpenTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenRoom2InsideDoors(), 3000);
			_Room2OutsideSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom2OutsideMobs(), 4000);
		}
	}

	// Is the door of outside of room2 in confirmation to open.
	private static void onKillDarkChoirCaptain()
	{
		int killCnt = 0;
		for(L2NpcInstance DarkChoirCaptain : _darkChoirCaptains)
			if(DarkChoirCaptain.isDead())
				killCnt++;
		if(_darkChoirCaptains.size() <= killCnt)
		{
			_room2OutsideDoor1.openMe();
			_room2OutsideDoor2.openMe();
			_room3Door.openMe();
			for(L2NpcInstance mob : _room2OutsideMonsters)
				mob.deleteMe();
			for(L2NpcInstance DarkChoirCaptain : _darkChoirCaptains)
				DarkChoirCaptain.deleteMe();
		}
	}

	private static void openRoom1Doors()
	{
		for(L2NpcInstance npc : _hallAlarmDevices)
			npc.deleteMe();
		for(L2NpcInstance npc : _room1Monsters)
			npc.deleteMe();
		for(L2DoorInstance door : _room1Doors)
			door.openMe();
	}

	private static void spawnRoom2InsideMob()
	{
		for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom2InsideSpawnList())
		{
			L2NpcInstance mob = spawn.doSpawn(true);
			mob.getSpawn().stopRespawn();
			_room2InsideMonsters.add(mob);
		}
	}

	public static void setReachToHall()
	{
		_isReachToHall = true;
	}

	private static void doCheckTimeUp(int remaining)
	{
		if(_isReachToHall)
			return;
		int timeLeft;
		int interval;
		String text = "";
		if(remaining > 300000)
		{
			timeLeft = remaining / 60000;
			interval = 300000;
			text = timeLeft + " minute(s) are remaining.";
			remaining = remaining - 300000;
		}
		else if(remaining > 60000)
		{
			timeLeft = remaining / 60000;
			interval = 60000;
			text = timeLeft + " minute(s) are remaining.";
			remaining = remaining - 60000;
		}
		else if(remaining > 30000)
		{
			timeLeft = remaining / 1000;
			interval = 30000;
			text = timeLeft + " second(s) are remaining.";
			remaining = remaining - 30000;
		}
		else
		{
			timeLeft = remaining / 1000;
			interval = 10000;
			text = timeLeft + " second(s) are remaining.";
			remaining = remaining - 10000;
		}
		ExShowScreenMessage msg = new ExShowScreenMessage(text, 3000, ScreenMessageAlign.TOP_CENTER, true, 1, -1, false);
		for(L2Player player : getPlayersInside())
			player.sendPacket(msg);
		if(_CheckTimeUpTask != null)
			_CheckTimeUpTask.cancel(true);
		if(remaining >= 10000)
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(remaining), interval);
		else
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), interval);
	}

	public static void cleanUpTomb(boolean banish)
	{
		initDoors();
		cleanUpMobs();
		if(banish)
			banishForeigners();
		_commander = null;
		_isInvaded = false;
		_isReachToHall = false;
		if(_InvadeTask != null)
			_InvadeTask.cancel(true);
		if(_RegistrationTimeInfoTask != null)
			_RegistrationTimeInfoTask.cancel(true);
		if(_Room1SpawnTask != null)
			_Room1SpawnTask.cancel(true);
		if(_Room2InsideDoorOpenTask != null)
			_Room2InsideDoorOpenTask.cancel(true);
		if(_Room2OutsideSpawnTask != null)
			_Room2OutsideSpawnTask.cancel(true);
		if(_CheckTimeUpTask != null)
			_CheckTimeUpTask.cancel(true);
		_InvadeTask = null;
		_RegistrationTimeInfoTask = null;
		_Room1SpawnTask = null;
		_Room2InsideDoorOpenTask = null;
		_Room2OutsideSpawnTask = null;
		_CheckTimeUpTask = null;
	}

	private static void cleanUpMobs()
	{
		for(L2NpcInstance mob : _hallAlarmDevices)
			mob.deleteMe();
		for(L2NpcInstance mob : _darkChoirCaptains)
			mob.deleteMe();
		for(L2NpcInstance mob : _room1Monsters)
			mob.deleteMe();
		for(L2NpcInstance mob : _room2InsideMonsters)
			mob.deleteMe();
		for(L2NpcInstance mob : _room2OutsideMonsters)
			mob.deleteMe();
		_hallAlarmDevices.clear();
		_darkChoirCaptains.clear();
		_room1Monsters.clear();
		_room2InsideMonsters.clear();
		_room2OutsideMonsters.clear();
	}

	private static class SpawnRoom1Mobs1st implements Runnable
	{
		public void run()
		{
			L2NpcInstance mob;
			for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom1SpawnList1st())
				if(spawn.getNpcId() != ALARM_DEVICE)
				{
					mob = spawn.doSpawn(true);
					mob.getSpawn().stopRespawn();
					_room1Monsters.add(mob);
				}
			if(Rnd.chance(90))
				_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs1st(), 15000);
		}
	}

	private static class SpawnRoom1Mobs2nd implements Runnable
	{
		public void run()
		{
			L2NpcInstance mob;
			for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom1SpawnList2nd())
			{
				mob = spawn.doSpawn(true);
				mob.getSpawn().stopRespawn();
				_room1Monsters.add(mob);
			}
			if(Rnd.chance(90))
				_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs2nd(), 15000);
		}
	}

	private static class SpawnRoom1Mobs3rd implements Runnable
	{
		public void run()
		{
			L2NpcInstance mob;
			for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom1SpawnList3rd())
			{
				mob = spawn.doSpawn(true);
				mob.getSpawn().stopRespawn();
				_room1Monsters.add(mob);
			}
			if(Rnd.chance(75))
				_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs3rd(), 15000);
		}
	}

	private static class SpawnRoom1Mobs4th implements Runnable
	{
		public void run()
		{
			L2NpcInstance mob;
			for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom1SpawnList4th())
			{
				mob = spawn.doSpawn(true);
				mob.getSpawn().stopRespawn();
				_room1Monsters.add(mob);
			}
			if(Rnd.chance(90))
				_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs4th(), 15000);
		}
	}

	private static class OpenRoom2InsideDoors implements Runnable
	{
		public void run()
		{
			_room2OutsideDoor1.closeMe();
			for(L2DoorInstance door : _room2InsideDoors)
				door.openMe();
		}
	}

	private static class SpawnRoom2OutsideMobs implements Runnable
	{
		public void run()
		{
			for(L2Spawn spawn : LastImperialTombSpawnlist.getRoom2OutsideSpawnList())
			{
				if(spawn.getNpcId() == CHOIR_CAPTAIN)
				{
					L2NpcInstance mob = spawn.doSpawn(true);
					mob.getSpawn().stopRespawn();
					_darkChoirCaptains.add(mob);
				}
				else
				{
					L2NpcInstance mob = spawn.doSpawn(true);
					mob.getSpawn().startRespawn();
					_room2OutsideMonsters.add(mob);
				}
			}
		}
	}

	private static class Invade implements Runnable
	{
		public void run()
		{
			doInvade();
		}
	}

	private static class CheckTimeUp implements Runnable
	{
		private int _remaining;

		public CheckTimeUp(int remaining)
		{
			_remaining = remaining;
		}

		public void run()
		{
			doCheckTimeUp(_remaining);
		}
	}

	private static class TimeUp implements Runnable
	{
		public void run()
		{
			cleanUpTomb(true);
		}
	}

	private static void banishForeigners()
	{
		for(L2Player player : getPlayersInside())
			if(!player.isGM())
				player.teleToClosestTown();
	}

	private static GArray<L2Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}

	public static L2Zone getZone()
	{
		return _zone;
	}

	public void onLoad()
	{
		init();
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}