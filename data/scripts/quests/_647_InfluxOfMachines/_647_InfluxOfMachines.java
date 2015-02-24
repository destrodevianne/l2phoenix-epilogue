package quests._647_InfluxOfMachines;

import l2p.Config;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.util.Rnd;

/**
 * Квест проверен и работает.
 * Рейты прописаны путем повышения шанса получения квестовых вещей.
 */
public class _647_InfluxOfMachines extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	// Settings: drop chance in %
	private static final int DROP_CHANCE = 60;

	// QUEST ITEMS
	private static final int DESTROYED_GOLEM_SHARD = 8100;

	// REWARDS
	private static final int[] RECIPES_60 = { 4963, 4964, 4965, 4966, 4967, 4968, 4969, 4970, 4971, 4972, 5000, 5001,
			5002, 5003, 5004, 5005, 5006, 5007, 8298, 8306, 8310, 8312, 8322, 8324 };

	private static final int[] RECIPES_100 = { 4182, 4183, 4184, 4185, 4186, 4187, 4188, 4189, 4190, 4191, 4192, 4193,
			4194, 4195, 4196, 4197, 4198, 4199, 8297, 8305, 8309, 8311, 8321, 8323 };

	public _647_InfluxOfMachines()
	{
		super(true);

		addStartNpc(32069);

		addTalkId(32069);
		addTalkId(32069);
		addTalkId(32069);

		for(int i = 22052; i < 22079; i++)
			addKillId(i);

		addQuestItem(DESTROYED_GOLEM_SHARD);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "collecter_gutenhagen_q0647_0103.htm";
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("647_3"))
			if(st.getQuestItemsCount(DESTROYED_GOLEM_SHARD) >= 500)
			{
				st.takeItems(DESTROYED_GOLEM_SHARD, -1);
				if(Config.ALT_100_RECIPES_B)
					st.giveItems(RECIPES_100[Rnd.get(RECIPES_100.length)], 1);
				else
					st.giveItems(RECIPES_60[Rnd.get(RECIPES_60.length)], 1);
				htmltext = "collecter_gutenhagen_q0647_0201.htm";
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(true);
			}
			else
				htmltext = "collecter_gutenhagen_q0647_0106.htm";
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getInt("cond");
		long count = st.getQuestItemsCount(DESTROYED_GOLEM_SHARD);
		if(cond == 0)
			if(st.getPlayer().getLevel() >= 46)
				htmltext = "collecter_gutenhagen_q0647_0101.htm";
			else
			{
				htmltext = "collecter_gutenhagen_q0647_0102.htm";
				st.exitCurrentQuest(true);
			}
		else if(cond == 1 && count < 500)
			htmltext = "collecter_gutenhagen_q0647_0106.htm";
		else if(cond == 2 && count >= 500)
			htmltext = "collecter_gutenhagen_q0647_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(st.getInt("cond") == 1 && st.rollAndGive(DESTROYED_GOLEM_SHARD, 1, 1, 500, DROP_CHANCE * npc.getTemplate().rateHp))
			st.set("cond", "2");
		return null;
	}
}