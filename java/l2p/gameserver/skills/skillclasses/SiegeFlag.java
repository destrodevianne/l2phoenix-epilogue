package l2p.gameserver.skills.skillclasses;

import java.util.logging.Logger;

import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.instancemanager.SiegeManager;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2Zone.ZoneType;
import l2p.gameserver.model.entity.siege.Siege;
import l2p.gameserver.model.entity.siege.SiegeClan;
import l2p.gameserver.model.instances.L2SiegeHeadquarterInstance;
import l2p.gameserver.skills.Stats;
import l2p.gameserver.skills.funcs.FuncMul;
import l2p.gameserver.tables.NpcTable;
import l2p.gameserver.templates.StatsSet;
import l2p.util.GArray;

public class SiegeFlag extends L2Skill
{
	protected static Logger _log = Logger.getLogger(SiegeFlag.class.getName());
	private final boolean _advanced;
	private final double _advancedMult;

	public SiegeFlag(StatsSet set)
	{
		super(set);
		_advanced = set.getBool("advancedFlag", false);
		_advancedMult = set.getDouble("advancedMultiplier", 1.);
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;

		L2Player player = (L2Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
		{
			_log.warning(player.toFullString() + " has " + toString() + ", but he isn't in a clan leader.");
			return false;
		}

		if(player.isInZone(ZoneType.siege_residense))
		{
			activeChar.sendMessage("Flag can't be placed at castle.");
			return false;
		}

		Siege siege = SiegeManager.getSiege(activeChar, true);
		if(siege == null || siege.getAttackerClan(player.getClan()) == null)
		{
			activeChar.sendMessage("You must be an attacker to place a flag.");
			return false;
		}

		return true;
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		L2Player player = (L2Player) activeChar;

		L2Clan clan = player.getClan();
		if(clan == null || !player.isClanLeader())
		{
			activeChar.sendMessage("You must be a clan leader to place a flag.");
			return;
		}

		Siege siege = SiegeManager.getSiege(activeChar, true);
		if(siege == null)
		{
			activeChar.sendMessage("You must be an attacker to place a flag.");
			return;
		}

		SiegeClan siegeClan = siege.getAttackerClan(clan);
		if(siegeClan == null)
		{
			activeChar.sendMessage("You must be an attacker to place a flag.");
			return;
		}

		if(siegeClan.getHeadquarter() != null)
		{
			activeChar.sendMessage("You already has a flag.");
			return;
		}

		L2SiegeHeadquarterInstance flag = new L2SiegeHeadquarterInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getTemplate(35062));

		if(_advanced)
			flag.addStatFunc(new FuncMul(Stats.MAX_HP, 0x50, flag, _advancedMult));

		flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp(), true);
		flag.setHeading(player.getHeading());

		// Ставим флаг перед чаром
		int x = (int) (player.getX() + 100 * Math.cos(player.headingToRadians(player.getHeading() - 32768)));
		int y = (int) (player.getY() + 100 * Math.sin(player.headingToRadians(player.getHeading() - 32768)));
		flag.spawnMe(GeoEngine.moveCheck(player.getX(), player.getY(), player.getZ(), x, y, player.getReflection().getGeoIndex()));

		siegeClan.setHeadquarter(flag);
	}
}