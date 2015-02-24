package l2p.gameserver.communitybbs.Manager;

import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.gameserver.communitybbs.BB.Forum;
import l2p.gameserver.model.L2Player;
import l2p.util.GArray;

public class ForumsBBSManager extends BaseBBSManager
{
	private static Logger _log = Logger.getLogger(ForumsBBSManager.class.getName());
	private static GArray<Forum> forums_table = new GArray<Forum>();
	private static int last_forum_id = 1;
	private static ReentrantLock last_forum_id_lock = new ReentrantLock();
	private static Map<Integer, Forum> _root = new FastMap<Integer, Forum>().setShared(true);

	static
	{
		load();
	}

	private static void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
			rset = statement.executeQuery();
			while(rset.next())
			{
				Forum f = new Forum(Integer.parseInt(rset.getString("forum_id")), null);
				_root.put(Integer.parseInt(rset.getString("forum_id")), f);
			}
		}
		catch(Exception e)
		{
			_log.warning("data error on Forum (root): " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static int GetANewID()
	{
		last_forum_id_lock.lock();
		last_forum_id++;
		last_forum_id_lock.unlock();
		return last_forum_id;
	}

	public static void addForum(Forum ff)
	{
		last_forum_id_lock.lock();
		try
		{
			forums_table.add(ff);
			if(ff.getID() > last_forum_id)
				last_forum_id = ff.getID();
		}
		finally
		{
			last_forum_id_lock.unlock();
		}
	}

	public static Forum getForumByID(int idf)
	{
		for(Forum f : forums_table)
			if(f.getID() == idf)
				return f;
		return null;
	}

	public static Forum getForumByName(String Name)
	{
		for(Forum f : forums_table)
			if(f.getName().equals(Name))
				return f;

		return null;
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player activeChar)
	{}

	@Override
	public void parsecmd(String command, L2Player activeChar)
	{}
}