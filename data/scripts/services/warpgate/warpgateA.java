package services.warpgate;

import l2p.extensions.scripts.Functions;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.util.Files;

/**
 * User: darkevil
 * Date: 23.02.2008
 * Edit darkevil 02.04.2008
 * Time: 22:43:18
 */
public class warpgateA extends Functions implements ScriptFile
{
	public void enter()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(player.isGM() || player.isQuestCompleted("_130_PathToHellbound") || player.isQuestCompleted("_133_ThatsBloodyHot"))
		{
			player.teleToLocation(-11272, 236464, -3248);
			return;
		}

		show(Files.read("data/scripts/services/warpgate/tele-no.htm", player), player, npc);
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Enter Hellbound Island");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}