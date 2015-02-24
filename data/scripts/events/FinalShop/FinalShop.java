package events.FinalShop;

import l2p.extensions.scripts.Functions;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.Announcements;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.util.Files;

public class FinalShop extends Functions implements ScriptFile
{
	private static boolean _active = false;

	/**
	 * Читает статус эвента из базы.
	 * @return
	 */
	private static boolean isActive()
	{
		return ServerVariables.getString("FinalShop", "off").equalsIgnoreCase("on");
	}

	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			System.out.println("Loaded Event: Final Shop [state: activated]");
		}
		else
			System.out.println("Loaded Event: Final Shop [state: deactivated]");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	/**
	* Запускает эвент
	*/
	public void startEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(!isActive())
		{
			ServerVariables.set("FinalShop", "on");
			System.out.println("Event 'Final Shop' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.FinalShop.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'Final Shop' already started.");

		_active = true;

		show(Files.read("data/html/admin/events.htm", player), player);
	}

	/**
	* Останавливает эвент
	*/
	public void stopEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(isActive())
		{
			ServerVariables.unset("FinalShop");
			System.out.println("Event 'Final Shop' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.FinalShop.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'Final Shop' not started.");

		_active = false;

		show(Files.read("data/html/admin/events.htm", player), player);
	}

	public String DialogAppend_30059(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30080(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30177(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30233(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30256(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30320(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30848(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30878(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30899(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31210(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31275(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31320(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31964(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30006(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30134(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30146(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_32163(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30576(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_30540(Integer val)
	{
		return getHtmlAppends(val);
	}

	private static final String en = "<br1>[scripts_events.FinalShop.FinalShop:show|\"Gracia Final Shop.\"]<br1>";
	private static final String ru = "<br1>[scripts_events.FinalShop.FinalShop:show|\"Магазин Gracia Final.\"]<br1>";

	public String getHtmlAppends(Integer val)
	{
		if(!_active || val != 0)
			return "";

		L2Player player = (L2Player) getSelf();
		if(player == null)
			return "";

		return player.isLangRus() ? ru : en;
	}

	public void show()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		show(Files.read("data/scripts/events/FinalShop/Shop.htm", player), player, npc);
	}
}