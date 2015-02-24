package quests._144_PailakaInjuredDragon;

import javolution.util.FastMap;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.InstancedZoneManager;
import l2p.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.Reflection;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.ReflectionTable;
import l2p.gameserver.tables.SkillTable;
import l2p.util.GArray;
import l2p.util.Rnd;

public class _144_PailakaInjuredDragon extends Quest implements ScriptFile
{
	// NPC
	private static final int KETRAOSHAMAN = 32499;
	private static final int KOSUPPORTER = 32502;
	private static final int KOIO = 32509;
	private static final int KOSUPPORTER2 = 32512;

	private static final int VSWARRIOR1 = 18636;
	private static final int VSWARRIOR2 = 18642;
	private static final int VSCOMMAO1 = 18646;
	private static final int VSCOMMAO2 = 18654;
	private static final int VSGMAG1 = 18649;
	private static final int VSGMAG2 = 18650;
	private static final int VSHGAPG1 = 18655;
	private static final int VSHGAPG2 = 18657;

	private static final int[] Pailaka3rd = new int[] { 18635, VSWARRIOR1, 18638, 18639, 18640, 18641, VSWARRIOR2, 18644,
			18645, VSCOMMAO1, 18648, VSGMAG1, VSGMAG2, 18652, 18653, VSCOMMAO2, VSHGAPG1, 18656, VSHGAPG2, 18658, 18659 };

	private static final int[] Antelopes = new int[] { 18637, 18643, 18647, 18651 };

	// BOSS
	private static final int LATANA = 18660;

	// ITEMS
	private static final int ScrollOfEscape = 736;
	private static final int SPEAR = 13052;
	private static final int ENCHSPEAR = 13053;
	private static final int LASTSPEAR = 13054;
	private static final int STAGE1 = 13056;
	private static final int STAGE2 = 13057;

	private static final int[] PAILAKA3DROP = { 8600, 8601, 8603, 8604 };
	private static final int[] ANTELOPDROP = { 13032, 13033 };

	// REWARDS
	private static final int PSHIRT = 13296;

	private static final int[][] BUFFS = { { 4357, 2 }, // Haste Lv2
			{ 4342, 2 }, // Wind Walk Lv2
			{ 4356, 3 }, // Empower Lv3
			{ 4355, 3 }, // Acumen Lv3
			{ 4351, 6 }, // Concentration Lv6
			{ 4345, 3 }, // Might Lv3
			{ 4358, 3 }, // Guidance Lv3
			{ 4359, 3 }, // Focus Lv3
			{ 4360, 3 }, // Death Wisper Lv3
			{ 4352, 2 }, // Berserker Spirit Lv2
			{ 4354, 4 }, // Vampiric Rage Lv4
			{ 4347, 6 } // Blessed Body Lv6
	};

	private static FastMap<Integer, Long> _instances = new FastMap<Integer, Long>();

	public _144_PailakaInjuredDragon()
	{
		super(false);

		addStartNpc(KETRAOSHAMAN);
		addTalkId(KOSUPPORTER, KOIO, KOSUPPORTER2);
		addAttackId(LATANA, VSWARRIOR1, VSWARRIOR2, VSCOMMAO1, VSCOMMAO2, VSGMAG1, VSGMAG2, VSHGAPG1, VSHGAPG2);
		addKillId(LATANA);
		addKillId(Pailaka3rd);
		addKillId(Antelopes);
		addQuestItem(STAGE1, STAGE2, SPEAR, ENCHSPEAR, LASTSPEAR, 13033, 13032);
	}

	private void makeBuff(L2NpcInstance npc, L2Player player, int skillId, int level)
	{
		GArray<L2Character> target = new GArray<L2Character>();
		target.add(player);
		npc.broadcastPacket(new MagicSkillUse(npc, player, skillId, level, 0, 0));
		npc.callSkill(SkillTable.getInstance().getInfo(skillId, level), target, true);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		L2Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("Enter"))
		{
			enterInstance(player);
			return null;
		}
		else if(event.startsWith("buff"))
		{
			int[] skill = BUFFS[Integer.parseInt(event.split("buff")[1])];
			if(st.getInt("spells") < 4)
			{
				makeBuff(npc, player, skill[0], skill[1]);
				st.set("spells", "" + (st.getInt("spells") + 1));
				htmltext = "32509-06.htm";
				return htmltext;
			}
			if(st.getInt("spells") == 4)
			{
				makeBuff(npc, player, skill[0], skill[1]);
				st.set("spells", "5");
				htmltext = "32509-05.htm";
				return htmltext;
			}
		}
		else if(event.equalsIgnoreCase("Support"))
		{
			if(st.getInt("spells") < 5)
				htmltext = "32509-06.htm";
			else
				htmltext = "32509-04.htm";
			return htmltext;
		}
		else if(event.equalsIgnoreCase("32499-02.htm"))
		{
			st.set("spells", "0");
			st.set("stage", "1");
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("32499-05.htm"))
		{
			st.set("cond", "2");
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("32502-05.htm"))
		{
			st.set("cond", "3");
			st.playSound(SOUND_MIDDLE);
			st.giveItems(SPEAR, 1);
		}
		else if(event.equalsIgnoreCase("32512-02.htm"))
		{
			st.takeItems(SPEAR, 1);
			st.takeItems(ENCHSPEAR, 1);
			st.takeItems(LASTSPEAR, 1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		int id = st.getState();
		L2Player player = st.getPlayer();
		if(npcId == KETRAOSHAMAN)
		{
			if(cond == 0)
			{
				if(player.getLevel() < 73 || player.getLevel() > 77)
				{
					htmltext = "32499-no.htm";
					st.exitCurrentQuest(true);
				}
				else
					return "32499-01.htm";
			}
			else if(id == COMPLETED)
				htmltext = "32499-no.htm";
			else if(cond == 1 || cond == 2 || cond == 3)
				htmltext = "32499-06.htm";
			else
				htmltext = "32499-07.htm";
		}
		else if(npcId == KOSUPPORTER)
		{
			if(cond == 1 || cond == 2)
				htmltext = "32502-01.htm";
			else
				htmltext = "32502-05.htm";
		}
		else if(npcId == KOIO)
		{
			if(st.getQuestItemsCount(SPEAR) > 0 && st.getQuestItemsCount(STAGE1) == 0)
				htmltext = "32509-01.htm";
			if(st.getQuestItemsCount(ENCHSPEAR) > 0 && st.getQuestItemsCount(STAGE2) == 0)
				htmltext = "32509-01.htm";
			if(st.getQuestItemsCount(SPEAR) == 0 && st.getQuestItemsCount(STAGE1) > 0)
				htmltext = "32509-07.htm";
			if(st.getQuestItemsCount(ENCHSPEAR) == 0 && st.getQuestItemsCount(STAGE2) > 0)
				htmltext = "32509-07.htm";
			if(st.getQuestItemsCount(SPEAR) == 0 && st.getQuestItemsCount(ENCHSPEAR) == 0)
				htmltext = "32509-07.htm";
			if(st.getQuestItemsCount(STAGE1) == 0 && st.getQuestItemsCount(STAGE2) == 0)
				htmltext = "32509-01.htm";
			if(st.getQuestItemsCount(SPEAR) > 0 && st.getQuestItemsCount(STAGE1) > 0)
			{
				st.takeItems(SPEAR, 1);
				st.takeItems(STAGE1, 1);
				st.giveItems(ENCHSPEAR, 1);
				htmltext = "32509-02.htm";
			}
			if(st.getQuestItemsCount(ENCHSPEAR) > 0 && st.getQuestItemsCount(STAGE2) > 0)
			{
				st.takeItems(ENCHSPEAR, 1);
				st.takeItems(STAGE2, 1);
				st.giveItems(LASTSPEAR, 1);
				htmltext = "32509-03.htm";
			}
			if(st.getQuestItemsCount(LASTSPEAR) > 0)
				htmltext = "32509-03.htm";
		}
		else if(npcId == KOSUPPORTER2)
		{
			if(cond == 4)
			{
				st.giveItems(ScrollOfEscape, 1);
				st.giveItems(PSHIRT, 1);
				st.addExpAndSp(28000000, 2850000);
				st.set("cond", "5");
				st.setState(COMPLETED);
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(false);
				player.setVitality(20000);
				player.getReflection().startCollapseTimer(60000);
				htmltext = "32512-01.htm";
			}
			else if(id == COMPLETED)
				htmltext = "32512-03.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		int npcId = npc.getNpcId();
		long refId = player.getReflection().getId();

		switch(npcId)
		{
			case VSWARRIOR1:
			case VSWARRIOR2:
				if(st.getInt("stage") == 1)
					st.set("stage", "2");
				break;
			case VSCOMMAO1:
			case VSCOMMAO2:
				if(st.getInt("stage") == 2)
					st.set("stage", "3");
				if(st.getQuestItemsCount(SPEAR) > 0 && st.getQuestItemsCount(STAGE1) == 0)
					st.giveItems(STAGE1, 1);
				break;
			case VSGMAG1:
			case VSGMAG2:
				if(st.getInt("stage") == 3)
					st.set("stage", "4");
				if(st.getQuestItemsCount(ENCHSPEAR) > 0 && st.getQuestItemsCount(STAGE2) == 0)
					st.giveItems(STAGE2, 1);
				break;
			case VSHGAPG1:
			case VSHGAPG2:
				if(st.getInt("stage") == 4)
					st.set("stage", "5");
				break;
			case LATANA:
				st.setCond(4);
				st.playSound(SOUND_MIDDLE);
				addSpawnToInstance(KOSUPPORTER2, npc.getLoc(), 0, refId);
				break;
		}

		if(contains(Pailaka3rd, npcId))
		{
			if(Rnd.get(100) < 30)
				dropItem(npc, PAILAKA3DROP[Rnd.get(PAILAKA3DROP.length)], 1);
		}

		if(contains(Antelopes, npcId))
			dropItem(npc, ANTELOPDROP[Rnd.get(ANTELOPDROP.length)], Rnd.get(1, 10));

		return null;
	}

	@Override
	public String onAttack(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case VSCOMMAO1:
			case VSCOMMAO2:
				if(st.getInt("stage") < 2)
				{
					player.teleToLocation(122789, -45692, -3036);
					return null;
				}
				break;
			case VSGMAG1:
			case VSGMAG2:
				if(st.getInt("stage") == 1)
				{
					player.teleToLocation(122789, -45692, -3036);
					return null;
				}
				else if(st.getInt("stage") == 2)
				{
					player.teleToLocation(116948, -46445, -2673);
					return null;
				}
				break;
			case VSHGAPG1:
			case VSHGAPG2:
				if(st.getInt("stage") == 1)
				{
					player.teleToLocation(122789, -45692, -3036);
					return null;
				}
				else if(st.getInt("stage") == 2)
				{
					player.teleToLocation(116948, -46445, -2673);
					return null;
				}
				else if(st.getInt("stage") == 3)
				{
					player.teleToLocation(112445, -44118, -2700);
					return null;
				}
				break;
			case LATANA:
				if(st.getInt("stage") == 1)
				{
					player.teleToLocation(122789, -45692, -3036);
					return null;
				}
				else if(st.getInt("stage") == 2)
				{
					player.teleToLocation(116948, -46445, -2673);
					return null;
				}
				else if(st.getInt("stage") == 3)
				{
					player.teleToLocation(112445, -44118, -2700);
					return null;
				}
				else if(st.getInt("stage") == 4)
				{
					player.teleToLocation(109947, -41433, -2311);
					return null;
				}
				break;
		}
		return null;
	}

	private void enterInstance(L2Player player)
	{
		int instancedZoneId = 14;
		InstancedZoneManager ilm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> ils = ilm.getById(instancedZoneId);
		if(ils == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		InstancedZone il = ils.get(0);

		assert il != null;

		if(player.isInParty())
		{
			player.sendPacket(Msg.A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA);
			return;
		}

		if(player.isCursedWeaponEquipped())
		{
			player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
			return;
		}

		Long old = _instances.get(player.getObjectId());
		if(old != null)
		{
			Reflection old_r = ReflectionTable.getInstance().get(old);
			if(old_r != null)
			{
				player.setReflection(old_r);
				player.teleToLocation(il.getTeleportCoords());
				player.setVar("backCoords", old_r.getReturnLoc().toXYZString());
				return;
			}
		}

		Reflection r = new Reflection(il.getName());
		r.setInstancedZoneId(instancedZoneId);
		for(InstancedZone i : ils.values())
		{
			if(r.getReturnLoc() == null)
				r.setReturnLoc(i.getReturnCoords());
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
		}

		int timelimit = il.getTimelimit();

		player.setReflection(r);
		player.teleToLocation(il.getTeleportCoords());
		player.setVar("backCoords", r.getReturnLoc().toXYZString());
		player.sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(timelimit));

		r.setNotCollapseWithoutPlayers(true);
		r.startCollapseTimer(timelimit * 60 * 1000L);

		_instances.put(player.getObjectId(), r.getId());
	}

	private void dropItem(L2NpcInstance npc, int itemId, int count)
	{
		L2ItemInstance item = ItemTable.getInstance().createItem(itemId);
		item.setCount(count);
		item.dropMe(npc, npc.getLoc());
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
}