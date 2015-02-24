package npc.model;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.templates.L2NpcTemplate;

/**
 * Данный инстанс используется NPC Sandstorm в локации Hellbound
 * @author SYS
 */
public class SandstormInstance extends L2NpcInstance
{
	public SandstormInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2Player player, boolean shift)
	{
		player.sendActionFailed();
	}
}