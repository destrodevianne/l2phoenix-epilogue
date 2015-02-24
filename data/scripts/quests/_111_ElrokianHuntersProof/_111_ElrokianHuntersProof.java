package quests._111_ElrokianHuntersProof;

import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;

public class _111_ElrokianHuntersProof extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	private static final int Marquez = 32113;
	private static final int Asamah = 32115;
	private static final int Kirikachin = 32116;

	private static final int[] Velociraptor = { 22196, 22197, 22198, 22218, 22223 };
	private static final int[] Ornithomimus = { 22200, 22201, 22202, 22219, 22224 };
	private static final int[] Deinonychus = { 22203, 22204, 22205, 22220, 22225 };
	private static final int[] Pachycephalosaurus = { 22208, 22209, 22210, 22221, 22226 };

	private static final int DiaryFragment = 8768;
	private static final int OrnithomimusClaw = 8770;
	private static final int DeinonychusBone = 8771;
	private static final int PachycephalosaurusSkin = 8772;

	public _111_ElrokianHuntersProof()
	{
		super(true);
		addStartNpc(Marquez);
		addTalkId(Asamah);
		addTalkId(Kirikachin);

		addKillId(Velociraptor);
		addKillId(Ornithomimus);
		addKillId(Deinonychus);
		addKillId(Pachycephalosaurus);

		addQuestItem(DiaryFragment);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		/*
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getInt("cond");
		*/
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int id = npc.getNpcId();
		int cond = st.getInt("cond");
		if(cond == 4)
		{
			for(int i : Velociraptor)
				if(id == i)
				{
					st.rollAndGive(DiaryFragment, 1, 1, 50, 33);
					return null;
				}
		}
		else if(cond == 10)
		{
			for(int i : Ornithomimus)
				if(id == i)
				{
					st.giveItems(OrnithomimusClaw, 1, false);
					return null;
				}
			for(int i : Deinonychus)
				if(id == i)
				{
					st.giveItems(DeinonychusBone, 1, false);
					return null;
				}
			for(int i : Pachycephalosaurus)
				if(id == i)
				{
					st.giveItems(PachycephalosaurusSkin, 1, false);
					return null;
				}
		}
		return null;
	}
}