package quests._694_BreakThroughTheHallOfSuffering;

import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;
import l2p.Config;
import l2p.common.ThreadPoolManager;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.InstancedZoneManager;
import l2p.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.Reflection;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.serverpackets.ExShowScreenMessage;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2p.gameserver.skills.Stats;
import l2p.gameserver.skills.funcs.Func;
import l2p.gameserver.skills.funcs.FuncMul;
import l2p.gameserver.tables.ReflectionTable;
import l2p.util.GArray;
import l2p.util.Location;
import l2p.util.Rnd;

/**
 * Награды:
 * 00 minutes to 20 minutes 59 seconds - Jewel Ornamented Duel Supplies 
 * 21 minutes to 22 minutes 59 seconds - Mother-of-Pearl Ornamented Duel Supplies 
 * 23 minutes to 24 minutes 59 seconds - Gold-Ornamented Duel Supplies
 * 25 minutes to 26 minutes 59 seconds - Silver-Ornamented Duel Supplies
 * 27 minutes to 28 minutes 59 seconds - Bronze-Ornamented Duel Supplies 
 * 29 minutes to 60 minutes - Non-Ornamented, Weak-Looking, Sad-Looking, Poor-Looking, Worthless Duel Supplies
 * 
 * За первых три даются ценные награды (рандом):
 * a) набор S80 ресурсов (3 Orichalcum + 3 Adamantine + 4 Leonard + 6 Elixirs of CP S-Grade, 6 Elixirs of Life S-Grade, 6 Elixirs of Mental Strength S-Grade) ~ 65%
 * б) Collector's Agathion Bracelet ~5%
 * в) SB 81 лвл ~30%
 * Остальные мешки - гумно.
 * 
 * @author SYS
 */
public class _694_BreakThroughTheHallOfSuffering extends Quest implements ScriptFile
{
	// NPC
	private static final int TEPIOS = 32603;
	private static final int TEPIOS2 = 32530;
	private static final int MOUTH_OF_EKIMUS = 32537;
	private static final int YEHAN_KLODEKUS = 25665;
	private static final int YEHAN_KlANIKUS = 25666;
	private static final int TUMOR_OF_DEATH = 18704;
	private static final int DESTROYED_TUMOR = 18705;
	private static final int[] MOBS = { 22509, 22510, 22511, 22512, 22513, 22514, 22515 };

	// Item rewards
	private static final int MARK_OF_KEUCEREUS_STAGE_1 = 13691;
	private static final int SOE = 736; // Scroll of Escape
	private static final int SUPPLIES1 = 13777; // Jewel Ornamented Duel Supplies
	private static final int SUPPLIES2 = 13778; // Mother-of-Pearl Ornamented Duel Supplies
	private static final int SUPPLIES3 = 13779; // Gold-Ornamented Duel Supplies
	private static final int SUPPLIES4 = 13780; // Silver-Ornamented Duel Supplies
	private static final int SUPPLIES5 = 13781; // Bronze-Ornamented Duel Supplies
	private static final int[] SUPPLIES6_10 = { 13782, // Non-Ornamented Duel Supplies
			13783, // Weak-Looking Duel Supplies
			13784, // Sad-Looking Duel Supplies
			13785, // Poor-Looking Duel Supplies
			13786 // Worthless Duel Supplies
	};
	private static final int roomSpawnOffset = 500; // разброс спауна от центра комнаты
	private final static int COLLAPSE_AFTER_DEATH_TIME = 5; // 5 мин
	private final static long TIMER_SHOW_IINTERVAL = 1 * 1000L; // 1 сек
	private static HashMap<Long, World> worlds = new HashMap<Long, World>();

	private static Func STAT_PDEF;
	private static Func STAT_MDEF;

	public class World
	{
		public long instanceId;
		public int status;
		public boolean protecting;
		public long timer;
		public GArray<Room> rooms;
		private ScheduledFuture<?> timerTask;

		// TODO: переделать на пакетный таймер
		private class WorldTimerTask implements Runnable
		{
			public WorldTimerTask()
			{}

			public void run()
			{
				showTimer();
			}
		}

		public void initTimer()
		{
			stopTimer();
			timer = 0;
			timerTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WorldTimerTask(), TIMER_SHOW_IINTERVAL, TIMER_SHOW_IINTERVAL);
		}

		public void showTimer()
		{
			timer = timer + TIMER_SHOW_IINTERVAL;
			long time = timer / 1000;
			long numMins = time / 60;
			time -= numMins * 60;
			long numSeconds = time;
			String clock = (String.valueOf(numMins).length() < 2 ? "0" : "") + numMins + ":" + (String.valueOf(numSeconds).length() < 2 ? "0" : "") + numSeconds;
			Reflection r = ReflectionTable.getInstance().get(instanceId);
			if(r.getParty() != null)
				r.getParty().broadcastToPartyMembers(new ExShowScreenMessage(clock, 1000, ScreenMessageAlign.TOP_CENTER, false));
		}

		public long stopTimer()
		{
			if(timerTask != null)
			{
				timerTask.cancel(true);
				timerTask = null;
			}

			return timer;
		}
	}

	public class Room
	{
		public Room()
		{
			npclist = new HashMap<L2NpcInstance, Boolean>();
		}

		public HashMap<L2NpcInstance, Boolean> npclist;
		public Location center;
	}

	public void onLoad()
	{
		System.out.println("Loaded Quest: 694: Break Through The Hall Of Suffering");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _694_BreakThroughTheHallOfSuffering()
	{
		super(PARTY_ALL);
		addStartNpc(TEPIOS);
		addTalkId(MOUTH_OF_EKIMUS);
		addTalkId(TEPIOS2);

		addKillId(MOBS);
		addKillId(TUMOR_OF_DEATH);
		addKillId(YEHAN_KLODEKUS);
		addKillId(YEHAN_KlANIKUS);

		addAttackId(YEHAN_KLODEKUS);
		addAttackId(YEHAN_KlANIKUS);

		STAT_PDEF = new FuncMul(Stats.POWER_DEFENCE, 0x30, this, 1.3);
		STAT_MDEF = new FuncMul(Stats.MAGIC_DEFENCE, 0x30, this, 1.3);

	}

	@Override
	public String onEvent(String event, QuestState qs, L2NpcInstance npc)
	{
		L2Player player = qs.getPlayer();

		if(event.equalsIgnoreCase("32603-04.htm"))
		{
			qs.setCond(1);
			qs.setState(STARTED);
			qs.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("Enter"))
		{
			enterInstance(player);
			return null;
		}
		else if(event.equalsIgnoreCase("ReceiveSupply"))
		{
			if(player.getVar("q694") != null && player.getVar("q694").equalsIgnoreCase("done"))
			{
				player.unsetVar("q694");
				if(qs.getQuestItemsCount(MARK_OF_KEUCEREUS_STAGE_1) == 0)
					qs.giveItems(MARK_OF_KEUCEREUS_STAGE_1, 1);
				qs.giveItems(SOE, 1);

				// тип сундука должен зависет от таймера
				World world = worlds.get(npc.getReflection().getId());
				if(world != null)
				{
					// 0 мин - 20 мин 59 сек
					if(world.timer <= (20 * 60 + 59) * 1000L)
						qs.giveItems(SUPPLIES1, 1);
					// 21 мин - 22 мин 59 сек
					else if(world.timer > (20 * 60 + 59) * 1000L && world.timer <= (22 * 60 + 59) * 1000L)
						qs.giveItems(SUPPLIES2, 1);
					// 23 мин - 24 мин 59 сек
					else if(world.timer > (22 * 60 + 59) * 1000L && world.timer <= (24 * 60 + 59) * 1000L)
						qs.giveItems(SUPPLIES3, 1);
					// 25 мин - 26 мин 59 сек
					else if(world.timer > (24 * 60 + 59) * 1000L && world.timer <= (26 * 60 + 59) * 1000L)
						qs.giveItems(SUPPLIES4, 1);
					// 27 мин - 28 мин 59 сек
					else if(world.timer > (26 * 60 + 59) * 1000L && world.timer <= (28 * 60 + 59) * 1000L)
						qs.giveItems(SUPPLIES5, 1);
					// 29 мин - 60 мин
					else if(world.timer > (26 * 60 + 59) * 1000L)
						qs.giveItems(SUPPLIES6_10[Rnd.get(SUPPLIES6_10.length)], 1);
				}

				qs.exitCurrentQuest(true);
				qs.playSound("ItemSound.quest_finish");
				return null;
			}
			else
				return "32530-01.htm"; // Уже получил награду, иди лесом
		}
		else if(event.equalsIgnoreCase("respawn_mobs_timer"))
		{
			if(player != null)
			{
				World world = worlds.get(player.getReflection().getId());
				if(world != null)
					world.protecting = false;
			}
			return null;
		}
		return event;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		L2Player player = st.getPlayer();

		if(npcId == TEPIOS)
		{
			if(st.getState() == CREATED)
			{
				if(player.getLevel() < 75 || player.getLevel() > 82)
				{
					st.exitCurrentQuest(true);
					return "32603-00.htm"; // Не подходит левел
				}
				return "32603-01.htm"; // Рассказываем что все умрут
			}
			return "32603-05.htm"; // Квест уже взят
		}
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		World world = worlds.get(npc.getReflection().getId());
		if(world == null)
			return null;

		if(npc.getNpcId() == TUMOR_OF_DEATH)
		{
			addSpawnToInstance(DESTROYED_TUMOR, npc.getLoc(), 0, world.instanceId);
			makeRoomWeak(world.rooms.get(world.status));
		}

		L2Player player = st.getPlayer();
		if(checkKillProgress(npc, world.rooms.get(world.status)))
		{
			if(world.status == 5 && player.isInParty())
				for(L2Player member : player.getParty().getPartyMembers())
					if(!member.isDead() && member.isInRange(npc, Config.ALT_PARTY_DISTRIBUTION_RANGE) && Math.abs(member.getZ() - npc.getZ()) < 400)
						member.setVar("q694", "done");
			world.status++;
			runHallOfSuffering(world);
		}

		return null;
	}

	private void enterInstance(L2Player player)
	{
		if(!player.isInParty())
		{
			player.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
			return;
		}

		int instancedZoneId = 115;
		InstancedZoneManager ilm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> ils = ilm.getById(instancedZoneId);

		if(ils == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		InstancedZone il = ils.get(0);

		assert il != null;

		String name = il.getName();
		int timelimit = il.getTimelimit();
		boolean dispellBuffs = il.isDispellBuffs();
		int min_level = il.getMinLevel();
		int max_level = il.getMaxLevel();
		int minParty = il.getMinParty();
		int maxParty = il.getMaxParty();

		L2Party party = player.getParty();

		if(party.isInReflection())
		{
			party.broadcastToPartyMembers(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
			return;
		}

		if(!party.isLeader(player))
		{
			party.broadcastToPartyMembers(Msg.ONLY_A_PARTY_LEADER_CAN_TRY_TO_ENTER);
			return;
		}

		if(party.getMemberCount() > maxParty || party.getMemberCount() < minParty)
		{
			party.broadcastToPartyMembers(Msg.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
			return;
		}

		for(L2Player member : party.getPartyMembers())
		{
			if(ilm.getTimeToNextEnterInstance(name, member) > 0)
			{
				party.broadcastToPartyMembers(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(member));
				return;
			}
			QuestState qs = member.getQuestState(_694_BreakThroughTheHallOfSuffering.class);
			if(qs == null || qs.getCond() != 1)
			{
				party.broadcastToPartyMembers(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
				return;
			}
			if(member.getLevel() < min_level || member.getLevel() > max_level)
			{
				party.broadcastToPartyMembers(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
				return;
			}
			if(!player.isInRange(member, 500))
			{
				party.broadcastToPartyMembers(new SystemMessage(SystemMessage.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED).addName(member));
				return;
			}
		}

		Reflection r = new Reflection(name);
		r.setInstancedZoneId(instancedZoneId);
		for(InstancedZone i : ils.values())
		{
			if(r.getReturnLoc() == null)
				r.setReturnLoc(i.getReturnCoords());
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
		}

		// init
		World world = new World();
		world.rooms = new GArray<Room>();
		world.instanceId = r.getId();
		world.status = 0;
		worlds.put(r.getId(), world);
		runHallOfSuffering(world);

		for(L2Player member : party.getPartyMembers())
		{
			member.setReflection(r);
			member.teleToLocation(il.getTeleportCoords());
			member.setVar("backCoords", r.getReturnLoc().toXYZString());
			member.setVar(name, String.valueOf(System.currentTimeMillis()));
			if(dispellBuffs)
			{
				for(L2Effect e : player.getEffectList().getAllEffects())
					if(!e.getSkill().isOffensive() && !e.getSkill().getName().startsWith("Adventurer's "))
						e.exit();
				if(player.getPet() != null)
					for(L2Effect e : player.getPet().getEffectList().getAllEffects())
						if(!e.getSkill().isOffensive() && !e.getSkill().getName().startsWith("Adventurer's "))
							e.exit();
			}
		}

		party.setReflection(r);
		r.setParty(party);
		r.startCollapseTimer(timelimit * 60 * 1000L);
		world.initTimer();
	}

	/**
	 * Увеличивает mDef и pDef мобов в комнате на 30%
	 */
	private void makeRoomStrong(Room room)
	{
		for(L2NpcInstance mob : room.npclist.keySet())
		{
			if(mob.getNpcId() == TUMOR_OF_DEATH)
				continue;
			mob.addStatFunc(STAT_PDEF);
			mob.addStatFunc(STAT_MDEF);
		}
	}

	/**
	 * Уменьшает mDef и pDef мобов в комнате на 30%
	 */
	private void makeRoomWeak(Room room)
	{
		for(L2NpcInstance mob : room.npclist.keySet())
		{
			if(mob.getNpcId() == TUMOR_OF_DEATH)
				continue;
			mob.removeStatFunc(STAT_PDEF);
			mob.removeStatFunc(STAT_MDEF);
		}
	}

	private void runHallOfSuffering(World world)
	{
		Room room = new Room();
		switch(world.status)
		{
			case 0: // Комната 1
				room.center = new Location(-186200, 208376, -9544, 0);
				// Tumor of Death
				room.npclist.put(addSpawnToInstance(TUMOR_OF_DEATH, room.center, 0, world.instanceId), false);
				// Fanatic of Infinity
				room.npclist.put(addSpawnToInstance(22509, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22509, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22509, room.center, roomSpawnOffset, world.instanceId), false);
				// Rotten Messenger
				room.npclist.put(addSpawnToInstance(22510, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22510, room.center, roomSpawnOffset, world.instanceId), false);
				makeRoomStrong(room);
				world.rooms.add(room);
				break;
			case 1: // Комната 2
				room.center = new Location(-184472, 211096, -9544, 0);
				// Tumor of Death
				room.npclist.put(addSpawnToInstance(TUMOR_OF_DEATH, room.center, 0, world.instanceId), false);
				// Fanatic of Infinity
				room.npclist.put(addSpawnToInstance(22509, room.center, roomSpawnOffset, world.instanceId), false);
				// Rotten Messenger
				room.npclist.put(addSpawnToInstance(22510, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22510, room.center, roomSpawnOffset, world.instanceId), false);
				// Zealot of Infinity
				room.npclist.put(addSpawnToInstance(22511, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22511, room.center, roomSpawnOffset, world.instanceId), false);
				makeRoomStrong(room);
				world.rooms.add(room);
				break;
			case 2: // Комната 3
				room.center = new Location(-182808, 213880, -9496, 0);
				// Tumor of Death
				room.npclist.put(addSpawnToInstance(TUMOR_OF_DEATH, room.center, 0, world.instanceId), false);
				// Fanatic of Infinity
				room.npclist.put(addSpawnToInstance(22509, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22509, room.center, roomSpawnOffset, world.instanceId), false);
				// Rotten Messenger
				room.npclist.put(addSpawnToInstance(22510, room.center, roomSpawnOffset, world.instanceId), false);
				// Zealot of Infinity
				room.npclist.put(addSpawnToInstance(22511, room.center, roomSpawnOffset, world.instanceId), false);
				// Body Severer
				room.npclist.put(addSpawnToInstance(22512, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22512, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22512, room.center, roomSpawnOffset, world.instanceId), false);
				makeRoomStrong(room);
				world.rooms.add(room);
				break;
			case 3: // Комната 4
				room.center = new Location(-180888, 216696, -9544, 0);
				// Tumor of Death
				room.npclist.put(addSpawnToInstance(TUMOR_OF_DEATH, room.center, 0, world.instanceId), false);
				// Zealot of Infinity
				room.npclist.put(addSpawnToInstance(22511, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22511, room.center, roomSpawnOffset, world.instanceId), false);
				// Body Severer
				room.npclist.put(addSpawnToInstance(22512, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22512, room.center, roomSpawnOffset, world.instanceId), false);
				// Body Harvester
				room.npclist.put(addSpawnToInstance(22513, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22513, room.center, roomSpawnOffset, world.instanceId), false);
				// Soul Exploiter
				room.npclist.put(addSpawnToInstance(22514, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22514, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22514, room.center, roomSpawnOffset, world.instanceId), false);
				makeRoomStrong(room);
				world.rooms.add(room);
				break;
			case 4: // Комната 5
				room.center = new Location(-177304, 217832, -9544, 0);
				// Tumor of Death
				room.npclist.put(addSpawnToInstance(TUMOR_OF_DEATH, room.center, 0, world.instanceId), false);
				// Body Severer
				room.npclist.put(addSpawnToInstance(22512, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22512, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22512, room.center, roomSpawnOffset, world.instanceId), false);
				// Body Harvester
				room.npclist.put(addSpawnToInstance(22513, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22513, room.center, roomSpawnOffset, world.instanceId), false);
				// Soul Exploiter
				room.npclist.put(addSpawnToInstance(22514, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22514, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22514, room.center, roomSpawnOffset, world.instanceId), false);
				// Soul Devourer
				room.npclist.put(addSpawnToInstance(22515, room.center, roomSpawnOffset, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(22515, room.center, roomSpawnOffset, world.instanceId), false);
				makeRoomStrong(room);
				world.rooms.add(room);
				break;
			case 5: // Комната 6 (босы)
				room.center = new Location(-173704, 218088, -9528, 0);
				room.npclist.put(addSpawnToInstance(YEHAN_KLODEKUS, new Location(-173720, 218216, -9544, 49800), 0, world.instanceId), false);
				room.npclist.put(addSpawnToInstance(YEHAN_KlANIKUS, new Location(-173704, 217992, -9544, 17600), 0, world.instanceId), false);
				world.rooms.add(room);
				break;
			case 6: // Комната 6 (Tepios)
				room.center = new Location(-173704, 218088, -9528, 0);
				room.npclist.put(addSpawnToInstance(TEPIOS2, room.center, 0, world.instanceId), false);
				world.rooms.add(room);
				world.stopTimer();
				Reflection r = ReflectionTable.getInstance().get(world.instanceId);
				r.startCollapseTimer(COLLAPSE_AFTER_DEATH_TIME * 60 * 1000L); // запускаем 5 мин коллапс инстанса
				if(r.getParty() != null)
					r.getParty().broadcastToPartyMembers(new SystemMessage(SystemMessage.THIS_INSTANCE_ZONE_WILL_BE_TERMINATED_IN_S1_MINUTES_YOU_WILL_BE_FORCED_OUT_OF_THE_DANGEON_THEN_TIME_EXPIRES).addNumber(COLLAPSE_AFTER_DEATH_TIME));
				break;
		}
	}

	@Override
	public String onAttack(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		World world = worlds.get(npc.getReflection().getId());
		if(world != null && world.status == 5 && !world.protecting)
		{
			// Респаунит 2х рандомных мобов для охраны босов
			world.protecting = true;
			st.startQuestTimer("respawn_mobs_timer", 60000);
			Room room = world.rooms.get(world.status);
			for(int i = 0; i < 2; i++)
			{
				L2NpcInstance mob = addSpawnToInstance(MOBS[Rnd.get(MOBS.length)], room.center, roomSpawnOffset, world.instanceId);
				room.npclist.put(mob, false);
				mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, Rnd.get(1, 100));
			}
		}
		return null;
	}

	private boolean checkKillProgress(L2NpcInstance npc, Room room)
	{
		if(room.npclist.containsKey(npc))
			room.npclist.put(npc, true);
		for(boolean value : room.npclist.values())
			if(!value)
				return false;
		return true;
	}
}