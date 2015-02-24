package quests._125_InTheNameOfEvilPart1;

import l2p.Config;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.util.Files;
import l2p.util.Rnd;

public class _125_InTheNameOfEvilPart1 extends Quest implements ScriptFile
{
	private int Mushika = 32114;
	private int Karakawei = 32117;
	private int UluKaimu = 32119;
	private int BaluKaimu = 32120;
	private int ChutaKaimu = 32121;
	private int OrClaw = 8779;
	private int DienBone = 8780;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _125_InTheNameOfEvilPart1()
	{
		super(false);

		addStartNpc(Mushika);
		addTalkId(Karakawei);
		addTalkId(UluKaimu);
		addTalkId(BaluKaimu);
		addTalkId(ChutaKaimu);
	}

	private String getWordText32119(QuestState st)
	{
		String htmltext = Files.read("data/scripts/quests/_125_InTheNameOfEvilPart1/32119.htm", st.getPlayer());
		htmltext = htmltext.replace("%1%", st.getInt("T32119") == 0 ? "_" : "T");
		htmltext = htmltext.replace("%2%", st.getInt("E32119") == 0 ? "_" : "E");
		htmltext = htmltext.replace("%3%", st.getInt("P32119") == 0 ? "_" : "P");
		htmltext = htmltext.replace("%4%", st.getInt("U32119") == 0 ? "_" : "U");
		if(st.getInt("T32119") > 0 && st.getInt("E32119") > 0 && st.getInt("P32119") > 0 && st.getInt("U32119") > 0)
			htmltext = htmltext.replace("%5%", "<a action=\"bypass -h Quest _125_InTheNameOfEvilPart1 OK32119\">OK</a>");
		else
			htmltext = htmltext.replace("%5%", "");
		return htmltext;
	}

	private String getWordText32120(QuestState st)
	{
		String htmltext = Files.read("data/scripts/quests/_125_InTheNameOfEvilPart1/32120.htm", st.getPlayer());
		htmltext = htmltext.replace("%1%", st.getInt("T32120") == 0 ? "_" : "T");
		htmltext = htmltext.replace("%2%", st.getInt("O32120") == 0 ? "_" : "O");
		htmltext = htmltext.replace("%3%", st.getInt("O32120") <= 1 ? "_" : "O");
		htmltext = htmltext.replace("%4%", st.getInt("N32120") == 0 ? "_" : "N");
		if(st.getInt("T32120") > 0 && st.getInt("O32120") > 1 && st.getInt("N32120") > 0)
			htmltext = htmltext.replace("%5%", "<a action=\"bypass -h Quest _125_InTheNameOfEvilPart1 OK32120\">OK</a>");
		else
			htmltext = htmltext.replace("%5%", "");
		return htmltext;
	}

	private String getWordText32121(QuestState st)
	{
		String htmltext = Files.read("data/scripts/quests/_125_InTheNameOfEvilPart1/32121.htm", st.getPlayer());
		htmltext = htmltext.replace("%1%", st.getInt("W32121") == 0 ? "_" : "T");
		htmltext = htmltext.replace("%2%", st.getInt("A32121") == 0 ? "_" : "O");
		htmltext = htmltext.replace("%3%", st.getInt("G32121") == 0 ? "_" : "O");
		htmltext = htmltext.replace("%4%", st.getInt("U32121") == 0 ? "_" : "N");
		if(st.getInt("W32121") > 0 && st.getInt("A32121") > 0 && st.getInt("G32121") > 0 && st.getInt("U32121") > 0)
			htmltext = htmltext.replace("%5%", "<a action=\"bypass -h Quest _125_InTheNameOfEvilPart1 OK32121\">OK</a>");
		else
			htmltext = htmltext.replace("%5%", "");
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = "";

		if(event.equalsIgnoreCase("OK32119"))
		{
			htmltext = "32119-1.htm";
			st.set("cond", "4");
		}
		if(event.equalsIgnoreCase("T32119"))
		{
			if(st.getInt("T32119") < 1)
				st.set("T32119", "1");
			htmltext = getWordText32119(st);
		}
		else if(event.equalsIgnoreCase("E32119"))
		{
			if(st.getInt("E32119") < 1)
				st.set("E32119", "1");
			htmltext = getWordText32119(st);
		}
		else if(event.equalsIgnoreCase("P32119"))
		{
			if(st.getInt("P32119") < 1)
				st.set("P32119", "1");
			htmltext = getWordText32119(st);
		}
		else if(event.equalsIgnoreCase("U32119"))
		{
			if(st.getInt("U32119") < 1)
				st.set("U32119", "1");
			htmltext = getWordText32119(st);
		}
		else if(event.equalsIgnoreCase("OK32120"))
		{
			htmltext = "32120-1.htm";
			st.set("cond", "5");
		}
		if(event.equalsIgnoreCase("T32120"))
		{
			if(st.getInt("T32120") < 1)
				st.set("T32120", "1");
			htmltext = getWordText32120(st);
		}
		else if(event.equalsIgnoreCase("O32120"))
		{
			if(st.getInt("O32120") < 1)
				st.set("O32120", "1");
			else if(st.getInt("O32120") == 1)
				st.set("O32120", "2");
			htmltext = getWordText32120(st);
		}
		else if(event.equalsIgnoreCase("N32120"))
		{
			if(st.getInt("N32120") < 1)
				st.set("N32120", "1");
			htmltext = getWordText32120(st);
		}
		else if(event.equalsIgnoreCase("OK32121"))
		{
			htmltext = "32121-1.htm";
			st.set("cond", "6");
		}
		if(event.equalsIgnoreCase("W32121"))
		{
			if(st.getInt("W32121") < 1)
				st.set("W32121", "1");
			htmltext = getWordText32121(st);
		}
		else if(event.equalsIgnoreCase("A32121"))
		{
			if(st.getInt("A32121") < 1)
				st.set("A32121", "1");
			htmltext = getWordText32121(st);
		}
		else if(event.equalsIgnoreCase("G32121"))
		{
			if(st.getInt("G32121") < 1)
				st.set("G32121", "1");
			htmltext = getWordText32121(st);
		}
		else if(event.equalsIgnoreCase("U32121"))
		{
			if(st.getInt("U32121") < 1)
				st.set("U32121", "1");
			htmltext = getWordText32121(st);
		}

		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(npcId == Mushika)
			if(st.getPlayer().getLevel() < 76)
			{
				htmltext = "<html>This quest for 76 level characters.</html>";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				htmltext = "32114.htm";
				st.set("cond", "1");
				st.setState(STARTED);
				st.playSound(SOUND_ACCEPT);
			}
			else if(cond == 6)
			{
				st.unset("OK32119");
				st.unset("OK32120");
				st.unset("OK32121");
				st.unset("T32119");
				st.unset("E32119");
				st.unset("P32119");
				st.unset("U32119");
				st.unset("T32120");
				st.unset("O32120");
				st.unset("N32120");
				st.unset("W32121");
				st.unset("A32121");
				st.unset("G32121");
				st.unset("U32121");
				st.unset("cond");

				htmltext = "<html>Quest In the Name of Evil Part 1 complete!</html>";
				st.addExpAndSp(859195, 86603);
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(false);
			}
			else if(npcId == Karakawei)
				if(cond == 1)
				{
					htmltext = "32117.htm";
					st.set("cond", "2");
				}
				else if(cond == 2 && (st.getQuestItemsCount(8779) < 2 || st.getQuestItemsCount(8780) < 2))
					htmltext = "32117.htm";
				else
				{
					htmltext = "32117-1.htm";
					st.set("cond", "3");
				}
			else if(npcId == UluKaimu)
			{
				if(cond == 3)
					htmltext = "32119.htm";
				else if(cond == 4)
					htmltext = "32119-1.htm";
			}
			else if(npcId == BaluKaimu)
			{
				if(cond == 4)
					htmltext = "32120.htm";
				else if(cond == 5)
					htmltext = "32120-1.htm";
			}
			else if(npcId == ChutaKaimu)
				if(cond == 5)
					htmltext = "32121.htm";
				else if(cond == 6)
					htmltext = "32121-1.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();

		if((npcId == 22200 || npcId == 22201 || npcId == 22202 || npcId == 22219 || npcId == 22224) && st.getQuestItemsCount(OrClaw) < 2 && Rnd.chance(10 * Config.RATE_QUESTS_DROP))
		{
			st.giveItems(OrClaw, 1);
			st.playSound(SOUND_MIDDLE);
		}

		if((npcId == 22203 || npcId == 22204 || npcId == 22205 || npcId == 22220 || npcId == 22225) && st.getQuestItemsCount(DienBone) < 2 && Rnd.chance(10 * Config.RATE_QUESTS_DROP))
		{
			st.giveItems(DienBone, 1);
			st.playSound(SOUND_MIDDLE);
		}

		return null;
	}
}