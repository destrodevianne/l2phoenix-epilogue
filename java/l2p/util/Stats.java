package l2p.util;

import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.tables.FakePlayersTable;

public class Stats
{
	public static int getOnline()
	{
		return L2ObjectsStorage.getAllPlayersCount();
	}

	public static int getOnline(boolean includeFake)
	{
		return getOnline() + (includeFake ? FakePlayersTable.getFakePlayersCount() : 0);
	}
}