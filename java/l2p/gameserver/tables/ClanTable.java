package l2p.gameserver.tables;

import java.sql.ResultSet;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import l2p.Config;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.instancemanager.SiegeManager;
import l2p.gameserver.model.L2Alliance;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2ClanMember;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.util.GArray;
import l2p.util.Util;

public class ClanTable
{
	private static final Logger _log = Logger.getLogger(ClanTable.class.getName());

	private static ClanTable _instance;

	private final FastMap<Integer, L2Clan> _clans = new FastMap<Integer, L2Clan>().setShared(true);
	private final FastMap<Integer, L2Alliance> _alliances = new FastMap<Integer, L2Alliance>().setShared(true);

	public static ClanTable getInstance()
	{
		if(_instance == null)
			new ClanTable();
		return _instance;
	}

	public L2Clan[] getClans()
	{
		return _clans.values().toArray(new L2Clan[_clans.size()]);
	}

	public L2Alliance[] getAlliances()
	{
		return _alliances.values().toArray(new L2Alliance[_alliances.size()]);
	}

	private ClanTable()
	{
		_instance = this;

		restoreClans();
		restoreAllies();
		restoreWars();
	}

	public L2Clan getClan(int clanId)
	{
		if(clanId <= 0)
			return null;
		return _clans.get(clanId);
	}

	public String getClanName(int clanId)
	{
		L2Clan c = getClan(clanId);
		return c != null ? c.getName() : "";
	}

	public L2Clan getClanByCharId(int charId)
	{
		if(charId <= 0)
			return null;
		for(L2Clan clan : getClans())
			if(clan != null && clan.isMember(charId))
				return clan;
		return null;
	}

	public L2Alliance getAlliance(int allyId)
	{
		if(allyId <= 0)
			return null;
		return _alliances.get(allyId);
	}

	public L2Alliance getAllianceByCharId(int charId)
	{
		if(charId <= 0)
			return null;
		L2Clan charClan = getClanByCharId(charId);
		return charClan == null ? null : charClan.getAlliance();
	}

	public Map.Entry<L2Clan, L2Alliance> getClanAndAllianceByCharId(int charId)
	{
		L2Player player = L2ObjectsStorage.getPlayer(charId);
		L2Clan charClan = player != null ? player.getClan() : getClanByCharId(charId);
		return new SimpleEntry<L2Clan, L2Alliance>(charClan, charClan == null ? null : charClan.getAlliance());
	}

	public void restoreClans()
	{
		GArray<Integer> clanIds = new GArray<Integer>();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet result = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id FROM clan_data");
			result = statement.executeQuery();
			while(result.next())
				clanIds.add(result.getInt("clan_id"));
		}
		catch(Exception e)
		{
			_log.warning("Error while restoring clans!!! " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, result);
		}

		for(int clanId : clanIds)
		{
			L2Clan clan = L2Clan.restore(clanId);
			if(clan == null)
			{
				_log.warning("Error while restoring clanId: " + clanId);
				continue;
			}

			if(clan.getMembersCount() <= 0)
			{
				_log.warning("membersCount = 0 for clanId: " + clanId);
				continue;
			}

			if(clan.getLeader() == null)
			{
				_log.warning("Not found leader for clanId: " + clanId);
				continue;
			}

			_clans.put(clan.getClanId(), clan);
		}
	}

	public void restoreAllies()
	{
		GArray<Integer> allyIds = new GArray<Integer>();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet result = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT ally_id FROM ally_data");
			result = statement.executeQuery();
			while(result.next())
				allyIds.add(result.getInt("ally_id"));
		}
		catch(Exception e)
		{
			_log.warning("Error while restoring allies!!! " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, result);
		}

		for(int allyId : allyIds)
		{
			L2Alliance ally = new L2Alliance(allyId);

			if(ally.getMembersCount() <= 0)
			{
				_log.warning("membersCount = 0 for allyId: " + allyId);
				continue;
			}

			if(ally.getLeader() == null)
			{
				_log.warning("Not found leader for allyId: " + allyId);
				continue;
			}

			_alliances.put(ally.getAllyId(), ally);
		}
	}

	public L2Clan getClanByName(String clanName)
	{
		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
			return null;

		for(Entry<Integer, L2Clan> n = _clans.head(), end = _clans.tail(); (n = n.getNext()) != end;)
			if(n.getValue().getName().equalsIgnoreCase(clanName))
				return n.getValue();

		return null;
	}

	public L2Alliance getAllyByName(String allyName)
	{
		if(!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE))
			return null;

		for(Entry<Integer, L2Alliance> n = _alliances.head(), end = _alliances.tail(); (n = n.getNext()) != end;)
			if(n.getValue().getAllyName().equalsIgnoreCase(allyName))
				return n.getValue();

		return null;
	}

	public L2Clan createClan(L2Player player, String clanName)
	{
		L2Clan clan = null;

		if(getClanByName(clanName) == null)
		{
			L2ClanMember leader = new L2ClanMember(player);
			clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName, leader);
			clan.store();
			player.setClan(clan);
			player.setPowerGrade(6);
			leader.setPlayerInstance(player);
			_clans.put(clan.getClanId(), clan);
		}

		return clan;
	}

	public void dissolveClan(L2Player player)
	{
		L2Clan clan = player.getClan();
		long curtime = System.currentTimeMillis();
		SiegeManager.removeSiegeSkills(player);
		for(L2Player clanMember : clan.getOnlineMembers(0))
		{
			clanMember.setClan(null);
			clanMember.setTitle(null);
			clanMember.sendPacket(Msg.PledgeShowMemberListDeleteAll, Msg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS);
			clanMember.broadcastUserInfo(true);
			clanMember.setLeaveClanTime(curtime);
		}
		clan.flush();
		deleteClanFromDb(clan.getClanId());
		_clans.remove(clan.getClanId());
		player.sendPacket(Msg.CLAN_HAS_DISPERSED);
		player.setDeleteClanTime(curtime);
	}

	public void deleteClanFromDb(int clanId)
	{
		long curtime = System.currentTimeMillis();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET clanid=0,title='',pledge_type=0,pledge_rank=0,lvl_joined_academy=0,apprentice=0,leaveclan=? WHERE clanid=?");
			statement.setLong(1, curtime / 1000);
			statement.setInt(2, clanId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("could not dissolve clan:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public L2Alliance createAlliance(L2Player player, String allyName)
	{
		L2Alliance alliance = null;

		if(getAllyByName(allyName) == null)
		{
			L2Clan leader = player.getClan();
			alliance = new L2Alliance(IdFactory.getInstance().getNextId(), allyName, leader);
			alliance.store();
			_alliances.put(alliance.getAllyId(), alliance);

			player.getClan().setAllyId(alliance.getAllyId());
			for(L2Player temp : player.getClan().getOnlineMembers(0))
				temp.broadcastUserInfo(true);
		}

		return alliance;
	}

	public void dissolveAlly(L2Player player)
	{
		int allyId = player.getAllyId();
		for(L2Clan member : player.getAlliance().getMembers())
		{
			member.setAllyId(0);
			member.broadcastClanStatus(false, true, true);
			member.broadcastToOnlineMembers(Msg.YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE);
			member.setLeavedAlly();
		}
		deleteAllyFromDb(allyId);
		_alliances.remove(allyId);
		player.sendPacket(Msg.THE_ALLIANCE_HAS_BEEN_DISSOLVED);
		player.getClan().setDissolvedAlly();
	}

	public void deleteAllyFromDb(int allyId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET ally_id=0 WHERE ally_id=?");
			statement.setInt(1, allyId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM ally_data WHERE ally_id=?");
			statement.setInt(1, allyId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("could not dissolve clan:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void startClanWar(L2Clan clan1, L2Clan clan2)
	{
		// clan1 is declaring war against clan2
		clan1.setEnemyClan(clan2);
		clan2.setAttackerClan(clan1);
		clan1.broadcastClanStatus(false, false, true);
		clan2.broadcastClanStatus(false, false, true);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2) VALUES(?,?)");
			statement.setInt(1, clan1.getClanId());
			statement.setInt(2, clan2.getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("could not store clan war data:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		clan1.broadcastToOnlineMembers(new SystemMessage(SystemMessage.CLAN_WAR_HAS_BEEN_DECLARED_AGAINST_S1_CLAN_IF_YOU_ARE_KILLED_DURING_THE_CLAN_WAR_BY_MEMBERS_OF_THE_OPPOSING_CLAN_THE_EXPERIENCE_PENALTY_WILL_BE_REDUCED_TO_1_4_OF_NORMAL).addString(clan2.getName()));
		clan2.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S1_CLAN_HAS_DECLARED_CLAN_WAR).addString(clan1.getName()));
	}

	public void stopClanWar(L2Clan clan1, L2Clan clan2)
	{
		// clan1 is ceases war against clan2
		clan1.deleteEnemyClan(clan2);
		clan2.deleteAttackerClan(clan1);

		clan1.broadcastClanStatus(false, false, true);
		clan2.broadcastClanStatus(false, false, true);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
			statement.setInt(1, clan1.getClanId());
			statement.setInt(2, clan2.getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("could not delete war data:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		clan1.broadcastToOnlineMembers(new SystemMessage(SystemMessage.THE_WAR_AGAINST_S1_CLAN_HAS_BEEN_STOPPED).addString(clan2.getName()));
		clan2.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S1_CLAN_HAS_STOPPED_THE_WAR).addString(clan1.getName()));
	}

	private void restoreWars()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT clan1, clan2 FROM clan_wars");
			rset = statement.executeQuery();
			L2Clan clan1;
			L2Clan clan2;
			while(rset.next())
			{
				clan1 = getClan(rset.getInt("clan1"));
				clan2 = getClan(rset.getInt("clan2"));
				if(clan1 != null && clan2 != null)
				{
					clan1.setEnemyClan(clan2);
					clan2.setAttackerClan(clan1);
				}
			}
		}
		catch(Exception e)
		{
			_log.warning("could not restore clan wars data:");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static void unload()
	{
		if(_instance != null)
			try
			{
				_instance.finalize();
			}
			catch(Throwable e)
			{}
	}
}