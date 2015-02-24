package npc.model;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.templates.L2NpcTemplate;

/**
 * Данный инстанс используется при смерти Tumor of Death
 * @author SYS
 */
public class DestroyedTumorInstance extends L2NpcInstance
{
	public DestroyedTumorInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2Player player, boolean shift)
	{
		player.sendActionFailed();
	}
}