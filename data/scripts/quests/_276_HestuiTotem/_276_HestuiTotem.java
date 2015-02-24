package quests._276_HestuiTotem;

import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.serverpackets.ExShowScreenMessage;
import l2p.gameserver.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2p.gameserver.tables.ItemTable;

public class _276_HestuiTotem extends Quest implements ScriptFile
{
	//NPCs
	private static int Tanapi = 30571;
	//Mobs
	private static int Kasha_Bear = 20479;
	private static int Kasha_Bear_Totem_Spirit = 27044;
	//Items
	private static int Leather_Pants = 29;
	private static int Totem_of_Hestui = 1500;
	//Quest Items
	private static int Kasha_Parasite = 1480;
	private static int Kasha_Crystal = 1481;

	public _276_HestuiTotem()
	{
		super(false);
		addStartNpc(Tanapi);
		addKillId(Kasha_Bear);
		addKillId(Kasha_Bear_Totem_Spirit);
		addQuestItem(Kasha_Parasite);
		addQuestItem(Kasha_Crystal);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		if(event.equalsIgnoreCase("seer_tanapi_q0276_03.htm") && st.getState() == CREATED && st.getPlayer().getRace() == Race.orc && st.getPlayer().getLevel() >= 15)
		{
			st.setState(STARTED);
			st.set("cond", "1");
			st.playSound(SOUND_ACCEPT);
		}
		return event;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != Tanapi)
			return htmltext;
		int _state = st.getState();

		if(_state == CREATED)
		{
			if(st.getPlayer().getRace() != Race.orc)
			{
				htmltext = "seer_tanapi_q0276_00.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getPlayer().getLevel() < 15)
			{
				htmltext = "seer_tanapi_q0276_01.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "seer_tanapi_q0276_02.htm";
				st.set("cond", "0");
			}
		}
		else if(_state == STARTED)
			if(st.getQuestItemsCount(Kasha_Crystal) > 0)
			{
				htmltext = "seer_tanapi_q0276_05.htm";
				st.takeItems(Kasha_Parasite, -1);
				st.takeItems(Kasha_Crystal, -1);

				st.giveItems(Leather_Pants, 1);
				st.giveItems(Totem_of_Hestui, 1);
				if(st.getRateQuestsReward() > 1)
					st.giveItems(57, Math.round(ItemTable.getInstance().getTemplate(Totem_of_Hestui).getReferencePrice() * (st.getRateQuestsReward() - 1) / 2), false);

				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4"))
				{
					st.getPlayer().setVar("p1q4", "1");
					st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
				}

				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(true);
			}
			else
				htmltext = "seer_tanapi_q0276_04.htm";

		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != STARTED)
			return null;
		int npcId = npc.getNpcId();

		if(npcId == Kasha_Bear && qs.getQuestItemsCount(Kasha_Crystal) == 0)
		{
			if(qs.getQuestItemsCount(Kasha_Parasite) < 50)
			{
				qs.giveItems(Kasha_Parasite, 1);
				qs.playSound(SOUND_ITEMGET);
			}
			else
			{
				qs.takeItems(Kasha_Parasite, -1);
				qs.addSpawn(Kasha_Bear_Totem_Spirit);
			}
		}
		else if(npcId == Kasha_Bear_Totem_Spirit && qs.getQuestItemsCount(Kasha_Crystal) == 0)
		{
			qs.giveItems(Kasha_Crystal, 1);
			qs.playSound(SOUND_MIDDLE);
		}

		return null;
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
}