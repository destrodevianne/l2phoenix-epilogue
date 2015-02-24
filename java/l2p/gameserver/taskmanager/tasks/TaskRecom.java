package l2p.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.taskmanager.Task;
import l2p.gameserver.taskmanager.TaskManager;
import l2p.gameserver.taskmanager.TaskTypes;
import l2p.gameserver.taskmanager.TaskManager.ExecutedTask;

public class TaskRecom extends Task
{
	private static final Logger _log = Logger.getLogger(TaskRecom.class.getName());
	private static final String NAME = "sp_recommendations";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		_log.config("Recommendation Global Task: launched.");
		for(L2Player player : L2ObjectsStorage.getAllPlayers())
			player.restartRecom();
		//player.sendUserInfo(false);
		_log.config("Recommendation Global Task: completed.");
	}

	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "13:00:00", "");
	}
}