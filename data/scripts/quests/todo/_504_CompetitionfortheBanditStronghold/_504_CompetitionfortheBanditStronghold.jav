package quests._504_CompetitionfortheBanditStronghold;

import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.instancemanager.ClanHallManager;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.entity.siege.clanhall.ClanHallSiege;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;

/**
 * Квест на захват Клан Холла Bandit Stronghold Reserve (35)
 * @author PaInKiLlEr
 */
public class _504_CompetitionfortheBanditStronghold extends Quest implements ScriptFile
{
	//NPC
	private static final int MESSENGER = 35437;
	//MOBS
	private static int TARLK_BUGBEAR = 20570;
	private static int TARLK_BASILISK = 20573;
	private static int ELDER_TARLK_BASILISK = 20574;

	//ITEMS
	private static int AMULET = 4332;

	//SHANCE
	private static int AMULET_SHANCE = 10;

	public void onLoad()
	{
		System.out.println("Loaded Quest: 504 Competition for the Bandit Stronghold");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _504_CompetitionfortheBanditStronghold()
	{
		super(false);

		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
		addKillId(TARLK_BUGBEAR);
		addKillId(TARLK_BASILISK);
		addKillId(ELDER_TARLK_BASILISK);
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		ClanHall ch = ClanHallManager.getInstance().getClanHall(Integer.valueOf(35));
		ClanHallSiege chSiege = ch.getSiege();

		String htmltext = event;
		int cond = st.getInt("cond");
		if(event.equalsIgnoreCase("35437-02.htm"))
		{
			st.set("cond", "2");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
			if(st.getQuestItemsCount(AMULET) > 0)
				st.takeItems(AMULET, -1);
		}
		if(event.equalsIgnoreCase("35437-03.htm"))
		{
			st.takeItems(AMULET, -1);
			st.set("cond", "3");
		}
		if(event.equalsIgnoreCase("35437-11.htm"))
		{
			st.takeItems(AMULET, -1);
			st.exitCurrentQuest(true);
		}
		if(event.equalsIgnoreCase("35437-12.htm"))
		{
			st.setState(STARTED);
			if(st.getQuestItemsCount(AMULET) > 0)
				st.takeItems(AMULET, -1);
			if(cond == 0)
			{
				st.takeItems(57, 200000);
				st.set("cond", "1");
				st.set("SiegePeriod", String.valueOf(chSiege.getSiegeDate().getTimeInMillis() / 1000));
			}
			if(cond == 1)
			{
				if(st.get("SiegePeriod") != String.valueOf(chSiege.getSiegeDate().getTimeInMillis() / 1000))
				{
					st.takeItems(57, 200000);
					st.set("SiegePeriod", String.valueOf(chSiege.getSiegeDate().getTimeInMillis() / 1000));
				}
			}
			if(cond == 3)
				st.set("SiegePeriod", String.valueOf(chSiege.getSiegeDate().getTimeInMillis() / 1000));
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getInt("cond");
		int npcId = npc.getNpcId();
		if(npcId == MESSENGER)
		{
			if(st.getPlayer().getClanHall() != null)
			{
				htmltext = "nohaveCh.htm";
				st.exitCurrentQuest(true);
			}

			ClanHall ch = ClanHallManager.getInstance().getClanHall(Integer.valueOf(35));
			ClanHallSiege chSiege = ch.getSiege();
			if(chSiege != null)
			{
				if(cond == 0)
				{
					if(st.get("SiegePeriod") == null)
						st.set("SiegePeriod", String.valueOf(chSiege.getSiegeDate().getTimeInMillis() / 1000));

					if(!st.getPlayer().isClanLeader())
					{
						if(st.getPlayer().getClan() != null)
							if(st.getPlayer().getClan().getLeader().getPlayer() != null)
								if(st.getPlayer().getClan().getLeader().getPlayer().getListenerEngine().getProperty(getName()) != null)
									st.set("cond", String.valueOf(st.getPlayer().getClan().getLeader().getPlayer().getListenerEngine().getProperty(getName()).equals("cond")));
					}
					else
						htmltext = "35437-05.htm";
				}
				if(cond > 0)
				{
					if(st.get("SiegePeriod") != String.valueOf(chSiege.getSiegeDate().getTimeInMillis() / 1000))
						htmltext = "35437-11.htm";
					else if(cond == 1)
						htmltext = "35437-12.htm";
					else if(cond == 2)
					{
						if(st.getQuestItemsCount(AMULET) < 30)
							htmltext = "noitem.htm";
						else
							htmltext = "35437-03.htm";
					}
					if(cond == 3)
						htmltext = "35437-12.htm";
				}
				else if(!st.getPlayer().isClanLeader() && cond == 0)
				{
					htmltext = "35437-05.htm";
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int cond = st.getInt("cond");
		if(cond == 2)
		{
			st.rollAndGive(AMULET, 1, 1, 30, AMULET_SHANCE);
		}
		return null;
	}
}