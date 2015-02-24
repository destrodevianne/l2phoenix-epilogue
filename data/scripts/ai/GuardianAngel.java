package ai;

import l2p.extensions.scripts.Functions;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.util.Rnd;

public class GuardianAngel extends DefaultAI
{
	static final String[] flood = { "Waaaah! Step back from the confounded box! I will take it myself!",
			"Grr! Who are you and why have you stopped my?", "Grr. I've been hit..." };

	public GuardianAngel(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor != null)
			Functions.npcSay(actor, flood[Rnd.get(2)]);

		return super.thinkActive();
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor != null)
			Functions.npcSay(actor, flood[2]);
		super.onEvtDead(killer);
	}
}