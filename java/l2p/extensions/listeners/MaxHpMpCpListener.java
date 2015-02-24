package l2p.extensions.listeners;

import l2p.gameserver.skills.Env;
import l2p.gameserver.skills.Stats;

public class MaxHpMpCpListener extends StatsChangeListener
{
	public MaxHpMpCpListener(Stats stat)
	{
		super(stat);
	}

	@Override
	public void statChanged(Double oldValue, double newValue, double baseValue, Env env)
	{
		_calculator._character.startRegeneration();
	}
}