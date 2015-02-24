package l2p.gameserver.model.entity;

import java.util.Timer;
import java.util.TimerTask;

import l2p.gameserver.instancemanager.DimensionalRiftManager;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.util.Location;

public class DelusionChamber extends DimensionalRift
{
	public DelusionChamber(L2Party party, int type, int room)
	{
		super(party, type, room);
	}

	@Override
	public void createNewKillRiftTimer()
	{
		if(killRiftTimerTask != null)
		{
			killRiftTimerTask.cancel();
			killRiftTimerTask = null;
		}

		if(killRiftTimer != null)
		{
			killRiftTimer.cancel();
			killRiftTimer = null;
		}

		killRiftTimer = new Timer();
		killRiftTimerTask = new TimerTask(){
			@Override
			public void run()
			{
				for(L2Player p : getParty().getPartyMembers())
					if(p.getReflection() == DelusionChamber.this)
					{
						String var = p.getVar("backCoords");
						if(var == null || var.equals(""))
							continue;
						p.teleToLocation(new Location(var), 0);
						p.unsetVar("backCoords");
					}
				DelusionChamber.this.collapse();
			}
		};

		killRiftTimer.schedule(killRiftTimerTask, 100);
	}

	@Override
	public void partyMemberExited(L2Player player)
	{
		if(getPlayersInside(false) < 2 || getPlayersInside(true) == 0)
		{
			createNewKillRiftTimer();
			return;
		}
	}

	@Override
	public void manualExitRift(L2Player player, L2NpcInstance npc)
	{
		if(!player.isInParty() || player.getParty().getReflection() != this)
			return;

		if(!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/rift/NotPartyLeader.htm", npc);
			return;
		}

		createNewKillRiftTimer();
	}

	public static final String getNameById(int type)
	{
		switch(type)
		{
			case 7:
				return "Delusion Chamber, Eastern Seal";
			case 8:
				return "Delusion Chamber, Western Seal";
			case 9:
				return "Delusion Chamber, Southern Seal";
			case 10:
				return "Delusion Chamber, Northern Seal";
			case 11:
				return "Delusion Chamber, Great Seal";
			case 12:
				return "Delusion Chamber, Tower Seal";
			default:
				return "";
		}
	}

	@Override
	public String getName()
	{
		return "Delusion Chamber";
	}

	@Override
	protected int getManagerId()
	{
		return 32664;
	}
}