package l2p.gameserver.communitybbs.Manager;

import java.sql.ResultSet;

import javolution.text.TextBuilder;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ShowBoard;
import l2p.util.Files;

public class StatBBSManager extends BaseBBSManager
{
	public class CBStatMan
	{
		public int PlayerId = 0; // 
		public String ChName = ""; // 
		public int ChGameTime = 0; // 
		public int ChPk = 0; //
		public int ChPvP = 0; //
		public int ChOnOff = 0; //
		public int ChSex = 0; //
	}

	public static StatBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void parsecmd(String command, L2Player player)
	{
		if(command.equals("_bbsstat;"))
		{
			showPvp(player);
		}
		else if(command.startsWith("_bbsstat;pk"))
		{
			showPK(player);
		}
		else
		{
			ShowBoard.separateAndSend("<html><body><br><br><center>В bbsstat функция: " + command + " пока не реализована</center><br><br></body></html>", player);
		}
	}

	private void showPvp(L2Player player)
	{

		CBStatMan tp;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pvpkills DESC LIMIT 20;");
			rs = statement.executeQuery();

			TextBuilder html = new TextBuilder();
			html.append("<center>ТОП 20 PVP</center>");
			html.append("<img src=L2UI.SquareWhite width=450 height=1>");
			html.append("<table width=450 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=250>Ник</td>");
			html.append("<td width=50>Пол</td>");
			html.append("<td width=100>Время в игре</td>");
			html.append("<td width=50>PK</td>");
			html.append("<td width=50><font color=00CC00>PVP</font></td>");
			html.append("<td width=100>Статус</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=450 height=1>");
			html.append("<table width=450>");
			while(rs.next())
			{
				tp = new CBStatMan();
				tp.PlayerId = rs.getInt("obj_Id");
				tp.ChName = rs.getString("char_name");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPk = rs.getInt("pkkills");
				tp.ChPvP = rs.getInt("pvpkills");
				tp.ChOnOff = rs.getInt("online");
				String OnOff;
				String color;
				String sex;
				sex = tp.ChSex == 1 ? "Ж" : "М";
				if(tp.ChOnOff == 1)
				{
					OnOff = "Онлайн";
					color = "00CC00";
				}
				else
				{
					OnOff = "Оффлайн";
					color = "D70000";
				}
				html.append("<tr>");
				html.append("<td width=250>" + tp.ChName + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100>" + OnlineTime(tp.ChGameTime) + "</td>");
				html.append("<td width=50>" + tp.ChPk + "</td>");
				html.append("<td width=50><font color=00CC00>" + tp.ChPvP + "</font></td>");
				html.append("<td width=100><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");

			String content = Files.read("data/html/CommunityBoardPVP/200.htm", player);
			content = content.replace("%stat%", html.toString());
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

	private void showPK(L2Player player)
	{

		CBStatMan tp;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pkkills DESC LIMIT 20;");
			rs = statement.executeQuery();

			TextBuilder html = new TextBuilder();
			html.append("<center>ТОП 20 PK</center>");
			html.append("<img src=L2UI.SquareWhite width=450 height=1>");
			html.append("<table width=450 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=250>Ник</td>");
			html.append("<td width=50>Пол</td>");
			html.append("<td width=100>Время в игре</td>");
			html.append("<td width=50><font color=00CC00>PK</font></td>");
			html.append("<td width=50>PVP</td>");
			html.append("<td width=100>Статус</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=450 height=1>");
			html.append("<table width=450>");
			while(rs.next())
			{
				tp = new CBStatMan();
				tp.PlayerId = rs.getInt("obj_Id");
				tp.ChName = rs.getString("char_name");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPk = rs.getInt("pkkills");
				tp.ChPvP = rs.getInt("pvpkills");
				tp.ChOnOff = rs.getInt("online");
				String OnOff;
				String color;
				String sex;
				sex = tp.ChSex == 1 ? "Ж" : "М";
				if(tp.ChOnOff == 1)
				{
					OnOff = "Онлайн";
					color = "00CC00";
				}
				else
				{
					OnOff = "Оффлайн";
					color = "D70000";
				}
				html.append("<tr>");
				html.append("<td width=250>" + tp.ChName + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100>" + OnlineTime(tp.ChGameTime) + "</td>");
				html.append("<td width=50><font color=00CC00>" + tp.ChPk + "</font></td>");
				html.append("<td width=50>" + tp.ChPvP + "</td>");
				html.append("<td width=100><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");

			String content = Files.read("data/html/CommunityBoardPVP/200.htm", player);
			content = content.replace("%stat%", html.toString());
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

	String OnlineTime(int time)
	{
		long onlinetimeH;
		int onlinetimeM;
		if(time / 60 / 60 - 0.5 <= 0)
		{
			onlinetimeH = 0;
		}
		else
		{
			onlinetimeH = Math.round((time / 60 / 60) - 0.5);
		}
		onlinetimeM = Math.round(((time / 60 / 60) - onlinetimeH) * 60);
		return "" + onlinetimeH + " ч. " + onlinetimeM + " м.";
	}

	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{

	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final StatBBSManager _instance = new StatBBSManager();
	}
}