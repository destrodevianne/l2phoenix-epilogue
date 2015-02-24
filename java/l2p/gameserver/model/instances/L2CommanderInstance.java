package l2p.gameserver.model.instances;

import l2p.Config;
import l2p.gameserver.instancemanager.FortressSiegeManager;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2DropData;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.entity.residence.ResidenceType;
import l2p.gameserver.model.entity.siege.fortress.FortressSiege;
import l2p.gameserver.templates.L2NpcTemplate;
import l2p.util.Util;

public class L2CommanderInstance extends L2SiegeGuardInstance
{
	public L2CommanderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private static final L2DropData EPAULETTE = new L2DropData(9912, 50, 150, 100000, 1);

	@Override
	public void doDie(L2Character killer)
	{
		FortressSiege siege = FortressSiegeManager.getSiege(this);
		if(siege != null)
		{
			siege.killedCommander(this);

			if(siege.getSiegeUnit().getType() == ResidenceType.Fortress && killer.isPlayable())
			{
				L2Character topdam = getTopDamager(getAggroList());
				if(topdam == null)
					topdam = killer;

				double chancemod = Experience.penaltyModifier(calculateLevelDiffForDrop(topdam.getLevel()), 9);

				dropItem(killer.getPlayer(), EPAULETTE.getItemId(), Util.rollDrop(EPAULETTE.getMinDrop(), EPAULETTE.getMaxDrop(), EPAULETTE.getChance() * chancemod * Config.RATE_DROP_ITEMS * killer.getPlayer().getRateItems(), true));
			}
		}

		super.doDie(killer);
	}
}