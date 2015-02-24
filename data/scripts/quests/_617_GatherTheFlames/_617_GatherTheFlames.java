package quests._617_GatherTheFlames;

import java.io.File;

import l2p.Config;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.model.L2Multisell;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.util.Rnd;

public class _617_GatherTheFlames extends Quest implements ScriptFile
{
	//npc
	private final static int VULCAN = 31539;
	private final static int HILDA = 31271;
	//items
	private final static int TORCH = 7264;
	//DROPLIST (MOB_ID, CHANCE)
	private final static int[][] DROPLIST = { { 21376, 48 }, { 21377, 48 }, { 21378, 48 }, { 21652, 48 }, { 21380, 48 },
			{ 21381, 51 }, { 21653, 51 }, { 21383, 51 }, { 21394, 51 }, { 21385, 51 }, { 21386, 51 }, { 21388, 53 },
			{ 21655, 53 }, { 21387, 53 }, { 21390, 56 }, { 21656, 56 }, { 21395, 56 }, { 21389, 56 }, { 21391, 56 },
			{ 21392, 56 }, { 21393, 58 }, { 21657, 58 }, { 21382, 60 }, { 21379, 60 }, { 21654, 64 }, { 21384, 64 } };

	public static final int[] Recipes = { 6881, 6883, 6885, 6887, 7580, 6891, 6893, 6895, 6897, 6899 };

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _617_GatherTheFlames()
	{
		super(true);

		addStartNpc(VULCAN);
		addStartNpc(HILDA);

		for(int[] element : DROPLIST)
			addKillId(element[0]);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("warsmith_vulcan_q0617_03.htm")) //VULCAN
		{
			if(st.getPlayer().getLevel() < 74)
				return "warsmith_vulcan_q0617_02.htm";
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
			st.set("cond", "1");
		}
		else if(event.equalsIgnoreCase("blacksmith_hilda_q0617_03.htm")) //HILDA
		{
			if(st.getPlayer().getLevel() < 74)
				return "blacksmith_hilda_q0617_02.htm";
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
			st.set("cond", "1");
		}
		else if(event.equalsIgnoreCase("warsmith_vulcan_q0617_08.htm"))
		{
			st.playSound(SOUND_FINISH);
			st.takeItems(TORCH, -1);
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("warsmith_vulcan_q0617_07.htm"))
		{
			if(st.getQuestItemsCount(TORCH) < 1000)
				return "warsmith_vulcan_q0617_05.htm";
			st.takeItems(TORCH, 1000);
			st.giveItems(Recipes[Rnd.get(Recipes.length)] + (Config.ALT_100_RECIPES_S ? 1 : 0), 1);
			st.playSound(SOUND_MIDDLE);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(npcId == VULCAN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 74)
				{
					htmltext = "warsmith_vulcan_q0617_02.htm";
					st.exitCurrentQuest(true);
				}
				else
					htmltext = "warsmith_vulcan_q0617_01.htm";
			}
			else
				htmltext = st.getQuestItemsCount(TORCH) < 1000 ? "warsmith_vulcan_q0617_05.htm" : "warsmith_vulcan_q0617_04.htm";
		}
		else if(npcId == HILDA)
			if(cond < 1)
				htmltext = st.getPlayer().getLevel() < 74 ? "blacksmith_hilda_q0617_02.htm" : "blacksmith_hilda_q0617_01.htm";
			else
				htmltext = "blacksmith_hilda_q0617_04.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		for(int[] element : DROPLIST)
			if(npc.getNpcId() == element[0])
			{
				st.rollAndGive(TORCH, 1, element[1]);
				return null;
			}
		return null;
	}

	private static void loadMultiSell()
	{
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_617_GatherTheFlames/32049.xml"));
	}

	public static void OnReloadMultiSell()
	{
		loadMultiSell();
	}

	static
	{
		loadMultiSell();
	}
}