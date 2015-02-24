package npc.model;

import java.util.StringTokenizer;

import l2p.extensions.scripts.Functions;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.taskmanager.DecayTaskManager;
import l2p.gameserver.templates.L2NpcTemplate;

/**
 * Данный инстанс используется в городе-инстансе на Hellbound
 * @author SYS
 */
public final class NativePrisonerInstance extends L2NpcInstance
{
	public NativePrisonerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		startAbnormalEffect(AbnormalEffect.HOLD_2);
		super.onSpawn();
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this) || isBusy())
			return;

		StringTokenizer st = new StringTokenizer(command);
		if(st.nextToken().equals("rescue"))
		{
			stopAbnormalEffect(AbnormalEffect.HOLD_2);
			Functions.npcSay(this, "Thank you for saving me! Guards are coming, run!");
			Functions.callScripts("services.Caravan", "changeConfidence", new Object[] { player, Long.valueOf(20) }); // Цифра с потолка
			DecayTaskManager.getInstance().addDecayTask(this);
			setBusy(true);
		}
		else
			super.onBypassFeedback(player, command);
	}
}