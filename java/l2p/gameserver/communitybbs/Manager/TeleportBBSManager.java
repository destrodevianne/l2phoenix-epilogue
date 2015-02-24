package l2p.gameserver.communitybbs.Manager;

import java.sql.ResultSet;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.TownManager;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.L2Zone.ZoneType;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.serverpackets.ShowBoard;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.util.Files;

public class TeleportBBSManager extends BaseBBSManager
{
	private static Logger _log = Logger.getLogger(TeleportBBSManager.class.getName());

	public class CBteleport
	{
		public int TpId = 0; // Teport loc ID
		public String TpName = ""; // Loc name
		public int PlayerId = 0; // charID
		public int xC = 0; // Location coords
		public int yC = 0; //
		public int zC = 0; //
	}

	private static TeleportBBSManager _Instance = null;

	public static TeleportBBSManager getInstance()
	{
		if(_Instance == null)
			_Instance = new TeleportBBSManager();
		return _Instance;
	}

	public String points[][];

	public void parsecmd(String command, L2Player player)
	{
		if(command.equals("_bbsteleport;"))
		{
			showTp(player);
		}
		else if(command.startsWith("_bbsteleport;delete;"))
		{
			StringTokenizer stDell = new StringTokenizer(command, ";");
			stDell.nextToken();
			stDell.nextToken();
			int TpNameDell = Integer.parseInt(stDell.nextToken());
			delTp(player, TpNameDell);
			showTp(player);
		}
		else if(command.startsWith("_bbsteleport;save; "))
		{
			StringTokenizer stAdd = new StringTokenizer(command, ";");
			stAdd.nextToken();
			stAdd.nextToken();
			String TpNameAdd = stAdd.nextToken();
			AddTp(player, TpNameAdd);
			showTp(player);
		}
		else if(command.startsWith("_bbsteleport;teleport;"))
		{
			StringTokenizer stGoTp = new StringTokenizer(command, " ");
			stGoTp.nextToken();
			int xTp = Integer.parseInt(stGoTp.nextToken());
			int yTp = Integer.parseInt(stGoTp.nextToken());
			int zTp = Integer.parseInt(stGoTp.nextToken());
			int priceTp = Integer.parseInt(stGoTp.nextToken());
			goTp(player, xTp, yTp, zTp, priceTp);
			showTp(player);
		}
		else
		{
			ShowBoard.separateAndSend("<html><body><br><br><center>Функция: " + command + " пока не реализована</center><br><br></body></html>", player);
		}
	}

	private void goTp(L2Player player, int xTp, int yTp, int zTp, int priceTp)
	{
		if(player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped() || player.isInZone(ZoneType.no_escape) || player.isInZone(ZoneType.Siege))
		{
			player.sendMessage("Телепортация невозможна");
			return;
		}
		if(priceTp > 0 && player.getAdena() < priceTp)
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
			return;
		}

		// Нельзя телепортироваться в города, где идет осада
		// Узнаем, идет ли осада в ближайшем замке к точке телепортации
		Castle castle = TownManager.getInstance().getClosestTown(xTp, yTp).getCastle();
		if(castle != null && castle.getSiege().isInProgress())
		{
			// Определяем, в город ли телепортируется чар
			boolean teleToTown = false;
			int townId = 0;
			for(L2Zone town : ZoneManager.getInstance().getZoneByType(ZoneType.Town))
				if(town.checkIfInZone(xTp, yTp))
				{
					teleToTown = true;
					townId = town.getIndex();
					break;
				}

			if(teleToTown && townId == castle.getTown())
			{
				player.sendPacket(Msg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				return;
			}
		}
		else
		{
			if(priceTp > 0)
			{
				player.reduceAdena((long) priceTp, false);
			}
			player.teleToLocation(xTp, yTp, zTp);
		}
	}

	private void showTp(L2Player player)
	{
		CBteleport tp;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM comteleport WHERE charId=?;");
			statement.setLong(1, player.getObjectId());
			rs = statement.executeQuery();
			TextBuilder html = new TextBuilder();
			html.append("<table width=220>");
			while(rs.next())
			{
				tp = new CBteleport();
				tp.TpId = rs.getInt("TpId");
				tp.TpName = rs.getString("name");
				tp.PlayerId = rs.getInt("charId");
				tp.xC = rs.getInt("xPos");
				tp.yC = rs.getInt("yPos");
				tp.zC = rs.getInt("zPos");
				html.append("<tr>");
				html.append("<td>");
				html.append("<button value=\"" + tp.TpName + "\" action=\"bypass -h _bbsteleport;teleport; " + tp.xC + " " + tp.yC + " " + tp.zC + " " + 100000 + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("<td>");
				html.append("<button value=\"Удалить\" action=\"bypass -h _bbsteleport;delete;" + tp.TpId + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");

			String content = Files.read("data/html/CommunityBoardPVP/50.htm", player);
			content = content.replace("%tp%", html.toString());
			separateAndSend(content, player);
			return;

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}

	}

	private void delTp(L2Player player, int TpNameDell)
	{
		ThreadConnection conDel = null;
		FiltredPreparedStatement statementDel = null;
		try
		{
			conDel = L2DatabaseFactory.getInstance().getConnection();
			statementDel = conDel.prepareStatement("DELETE FROM comteleport WHERE charId=? AND TpId=?;");
			statementDel.setInt(1, player.getObjectId());
			statementDel.setInt(2, TpNameDell);
			statementDel.execute();
		}
		catch(Exception e)
		{
			_log.warning("data error on Delete Teleport: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeConnection(conDel);
		}

	}

	private void AddTp(L2Player player, String TpNameAdd)
	{
		if(player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isAttackingNow())
		{
			player.sendMessage("Сохранить закладку в вашем состоянии нельзя");
			return;
		}

		if(player.isInCombat())
		{
			player.sendMessage("Сохранить закладку в режиме боя нельзя");
			return;
		}

		if(TpNameAdd.equals("") || TpNameAdd.equals(null))
		{
			player.sendMessage("Вы не ввели Имя закладки");
			return;
		}
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT COUNT(*) FROM comteleport WHERE charId=?;");
			statement.setLong(1, player.getObjectId());
			rs = statement.executeQuery();
			rs.next();
			if(rs.getInt(1) <= 9)
			{
				statement = con.prepareStatement("SELECT COUNT(*) FROM comteleport WHERE charId=? AND name=?;");
				statement.setLong(1, player.getObjectId());
				statement.setString(2, TpNameAdd);
				rs = statement.executeQuery();
				rs.next();
				if(rs.getInt(1) == 0)
				{
					statement = con.prepareStatement("INSERT INTO comteleport (charId,xPos,yPos,zPos,name) VALUES(?,?,?,?,?)");
					statement.setInt(1, player.getObjectId());
					statement.setInt(2, player.getX());
					statement.setInt(3, player.getY());
					statement.setInt(4, player.getZ());
					statement.setString(5, TpNameAdd);
					statement.execute();
				}
				else
				{
					statement = con.prepareStatement("UPDATE comteleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;");
					statement.setInt(1, player.getObjectId());
					statement.setInt(2, player.getX());
					statement.setInt(3, player.getY());
					statement.setInt(4, player.getZ());
					statement.setString(5, TpNameAdd);
					statement.execute();
				}
			}
			else
			{
				player.sendMessage("Вы не можете сохранить более 10 закладок");
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{

	}
}