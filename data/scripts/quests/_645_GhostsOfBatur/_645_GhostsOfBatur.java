package quests._645_GhostsOfBatur;

import java.io.File;

import l2p.Config;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.model.L2Multisell;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.util.Rnd;

public class _645_GhostsOfBatur extends Quest implements ScriptFile
{
	//Npc
	private static final int Karuda = 32017;
	//Items
	private static final int CursedGraveGoods = 8089;
	private static final int CursedBurialItems = 14861;
	//Mobs
	private static final int[] MOBS = { 22007, 22009, 22010, 22011, 22012, 22013, 22014, 22015, 22016, 22703, 22704,
			22705, 22706, 22707 };

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _645_GhostsOfBatur()
	{
		super(true);

		addStartNpc(Karuda);
		for(int i : MOBS)
			addKillId(i);
		addQuestItem(CursedGraveGoods, CursedBurialItems);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("karuda_q0645_0103.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getInt("cond");
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() < 23)
			{
				htmltext = "karuda_q0645_0102.htm";
				st.exitCurrentQuest(true);
			}
			else
				htmltext = "karuda_q0645_0101.htm";
		}
		else
		{
			if(cond == 2)
				st.setCond(1);

			if(st.getQuestItemsCount(CursedGraveGoods, CursedBurialItems) == 0)
				htmltext = "karuda_q0645_0106.htm";
			else
				htmltext = Config.ALT_100_RECIPES_S80 ? "karuda_q0645_0105a.htm" : "karuda_q0645_0105.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(st.getCond() > 0)
			if(npc.getLevel() < 80)
			{
				if(Rnd.chance(70))
					st.giveItems(CursedGraveGoods, Rnd.get(1, 2), true);
			}
			else if(Rnd.chance(5))
				st.giveItems(CursedBurialItems, 1, true);
		return null;
	}

	private static void loadMultiSell()
	{
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_645_GhostsOfBatur/320170.xml"));
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_645_GhostsOfBatur/320171.xml"));
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