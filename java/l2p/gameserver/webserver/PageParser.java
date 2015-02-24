package l2p.gameserver.webserver;

import java.util.TreeSet;

import l2p.Config;
import l2p.gameserver.GameServer;
import l2p.gameserver.Shutdown;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.tables.FakePlayersTable;
import l2p.gameserver.tables.GmListTable;
import l2p.gameserver.taskmanager.MemoryWatchDog;
import l2p.util.Stats;
import l2p.util.Util;

/**
 * Это затычка для минимальной функциональности вебсервера
 * @author Abaddon
 */
abstract class PageParser
{
	public static String parse(String s)
	{
		// Количество игроков в мире
		if(s.contains("%online%"))
			s = s.replaceAll("%online%", String.valueOf(Stats.getOnline(true)));

		// Количество ГМов
		if(s.contains("%gms%"))
			s = s.replaceAll("%gms%", String.valueOf(GmListTable.getAllGMs().size()));

		// Количество оффлайн торговцев
		if(s.contains("%off%"))
			s = s.replaceAll("%off%", String.valueOf(L2ObjectsStorage.getAllOfflineCount()));

		// Количество фейковых игроков в мире
		if(s.contains("%fake%"))
			s = s.replaceAll("%fake%", String.valueOf(FakePlayersTable.getFakePlayersCount()));

		// Список имен игроков онлайн
		if(s.contains("%players_list%"))
		{
			String playersList = "";
			final int maxCols = 4; // Количество столбцов для списка онлайн игроков
			int cols = 0;
			playersList += "<table id=\"tbl-pl-list\">";

			TreeSet<String> playerNames = new TreeSet<String>();

			for(L2Player player : L2ObjectsStorage.getAllPlayers())
			{
				if(player == null || player.isInvisible() && player.isGM())
					continue;
				if(player.getName() != null)
					playerNames.add(player.getName());
			}

			try
			{
				for(String name : FakePlayersTable.getActiveFakePlayers())
					playerNames.add(name);
			}
			catch(Exception e)
			{}

			for(String name : playerNames)
			{
				if(cols == 0)
					playersList += "<tr>";
				playersList += "<td>" + name + "</td>";
				if(++cols == maxCols)
				{
					cols = 0;
					playersList += "</tr>";
				}
			}

			if(cols > 0)
				playersList += "</tr>";

			playersList += "</table>";
			s = s.replaceAll("%players_list%", playersList);
		}

		// Информация о текущей ревизии сервера
		if(s.contains("%revision%"))
			s = s.replaceAll("%revision%", Config.SERVER_VERSION.equalsIgnoreCase("${l2p.revision}") ? Config.SERVER_VERSION_UNSUPPORTED : Config.SERVER_VERSION);

		// Информация о состоянии памяти
		if(s.contains("%memory%"))
			s = s.replaceAll("%memory%", MemoryWatchDog.getMemFreeMb() + " / " + MemoryWatchDog.getMemMaxMb());

		// Время до рестарта
		if(s.contains("%countdown%"))
		{
			int mtc = Shutdown.getInstance().getSeconds();
			if(mtc > 0)
				s = s.replaceAll("%countdown%", Util.formatTime(mtc));
			else
				s = s.replaceAll("%countdown%", "Restart task not launched");
		}

		// Аптайм сервера
		if(s.contains("%uptime%"))
			s = s.replaceAll("%uptime%", Util.formatTime(GameServer.uptime()));
		return s;
	}
}