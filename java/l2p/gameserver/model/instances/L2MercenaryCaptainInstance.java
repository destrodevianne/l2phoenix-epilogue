package l2p.gameserver.model.instances;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExShowDominionRegistry;
import l2p.gameserver.templates.L2NpcTemplate;

public class L2MercenaryCaptainInstance extends L2NpcInstance
{
	public L2MercenaryCaptainInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("Territory"))
			player.sendPacket(new ExShowDominionRegistry(player, getNpcId() - 36480));
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		if(val == 0)
			return "data/html/MercenaryCaptain/" + npcId + ".htm";
		return "data/html/MercenaryCaptain/" + npcId + "-" + val + ".htm";
	}
}