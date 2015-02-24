package l2p.gameserver.model.entity.olympiad;

import l2p.common.ThreadPoolManager;
import l2p.gameserver.Announcements;
import l2p.gameserver.cache.Msg;

class CompStartTask implements Runnable
{
	@Override
	public void run()
	{
		if(Olympiad.isOlympiadEnd())
			return;

		Olympiad._manager = new OlympiadManager();
		Olympiad._inCompPeriod = true;

		new Thread(Olympiad._manager).start();

		ThreadPoolManager.getInstance().scheduleGeneral(new CompEndTask(), Olympiad.getMillisToCompEnd());

		Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_STARTED);
		Olympiad._log.info("Olympiad System: Olympiad Game Started");
	}
}