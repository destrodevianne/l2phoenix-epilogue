package l2p.gameserver.skills.skillclasses;

import java.util.logging.Logger;

import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2Zone.ZoneType;
import l2p.gameserver.model.entity.siege.SiegeClan;
import l2p.gameserver.model.entity.siege.territory.TerritorySiege;
import l2p.gameserver.model.instances.L2SiegeHeadquarterInstance;
import l2p.gameserver.tables.NpcTable;
import l2p.gameserver.templates.StatsSet;
import l2p.util.GArray;

public class Outpost extends L2Skill
{
	protected static Logger _log = Logger.getLogger(Outpost.class.getName());
	private final boolean _build;

	public Outpost(StatsSet set)
	{
		super(set);
		_build = set.getBool("build", true);
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;

		L2Player player = (L2Player) activeChar;
		L2Clan clan = player.getClan();
		if(clan == null || !player.isClanLeader())
		{
			_log.warning(player.toFullString() + " has " + toString() + ", but he isn't in a clan leader.");
			return false;
		}

		if(player.isInZone(ZoneType.siege_residense) || !TerritorySiege.checkIfInZone(player))
		{
			activeChar.sendMessage("Outpost can't be placed here.");
			return false;
		}

		SiegeClan siegeClan = TerritorySiege.getSiegeClan(clan);
		if(player.getTerritorySiege() == -1 || siegeClan == null)
		{
			activeChar.sendMessage("You must be registered to place a Outpost.");
			return false;
		}

		if(_build && siegeClan.getHeadquarter() != null)
		{
			activeChar.sendMessage("You already has a Outpost.");
			return false;
		}

		if(!_build && siegeClan.getHeadquarter() == null)
			return false;

		return true;
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		L2Player player = (L2Player) activeChar;

		L2Clan clan = player.getClan();
		if(clan == null || !player.isClanLeader())
			return;

		if(!TerritorySiege.checkIfInZone(player) || clan.getTerritorySiege() == -1 || player.isInZone(ZoneType.siege_residense))
			return;

		SiegeClan siegeClan = TerritorySiege.getSiegeClan(clan);
		if(siegeClan == null)
			return;

		if(_build)
		{
			L2SiegeHeadquarterInstance outpost = new L2SiegeHeadquarterInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getTemplate(36590));

			outpost.setCurrentHpMp(outpost.getMaxHp(), outpost.getMaxMp(), true);
			outpost.setHeading(player.getHeading());
			outpost.setInvul(true);

			// Ставим аутпост перед чаром
			int x = (int) (player.getX() + 100 * Math.cos(player.headingToRadians(player.getHeading() - 32768)));
			int y = (int) (player.getY() + 100 * Math.sin(player.headingToRadians(player.getHeading() - 32768)));
			outpost.spawnMe(GeoEngine.moveCheck(player.getX(), player.getY(), player.getZ(), x, y, player.getReflection().getGeoIndex()));

			siegeClan.setHeadquarter(outpost);
		}
		else
		{
			L2SiegeHeadquarterInstance outpost = siegeClan.getHeadquarter();
			if(outpost == null)
				return;

			outpost.deleteMe();
			siegeClan.setHeadquarter(null);
		}
	}
}