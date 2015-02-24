package l2p.gameserver.communitybbs.BB;

import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2p.gameserver.communitybbs.Manager.TopicBBSManager;
import l2p.util.GCSArray;

public class Forum
{
	//type
	public static final int ROOT = 0;
	public static final int NORMAL = 1;
	public static final int CLAN = 2;
	public static final int MEMO = 3;
	public static final int MAIL = 4;
	//perm
	public static final int INVISIBLE = 0;
	public static final int ALL = 1;
	public static final int CLANMEMBERONLY = 2;
	public static final int OWNERONLY = 3;

	private static Logger _log = Logger.getLogger(Forum.class.getName());
	private GCSArray<Forum> _children;
	private Map<Integer, Topic> _topic;
	private int _ForumId;
	private String _ForumName;
	@SuppressWarnings("unused")
	private int _ForumParent;
	private int _ForumType;
	private int _ForumPost;
	private int _ForumPerm;
	private Forum _FParent;
	private int _OwnerID;
	private boolean loaded = false;
	private ReentrantLock loadedLock = new ReentrantLock();

	public Forum(int Forumid, Forum FParent)
	{
		_ForumId = Forumid;
		_FParent = FParent;
		_children = new GCSArray<Forum>();
		_topic = new FastMap<Integer, Topic>().setShared(true);

		// init()
		ForumsBBSManager.addForum(this);

	}

	public Forum(String name, Forum parent, int type, int perm, int OwnerID)
	{
		_ForumName = name;
		_ForumId = ForumsBBSManager.GetANewID();
		_ForumParent = parent.getID();
		_ForumType = type;
		_ForumPost = 0;
		_ForumPerm = perm;
		_FParent = parent;
		_OwnerID = OwnerID;
		_children = new GCSArray<Forum>();
		_topic = new FastMap<Integer, Topic>().setShared(true);
		parent._children.add(this);
		ForumsBBSManager.addForum(this);
		loaded = true;
		insertindb();
	}

	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			try
			{
				statement = con.prepareStatement("SELECT * FROM forums WHERE forum_id=?");
				statement.setInt(1, _ForumId);
				rset = statement.executeQuery();
				if(rset.next())
				{
					_ForumName = rset.getString("forum_name");
					_ForumParent = Integer.parseInt(rset.getString("forum_parent"));
					_ForumPost = Integer.parseInt(rset.getString("forum_post"));
					_ForumType = Integer.parseInt(rset.getString("forum_type"));
					_ForumPerm = Integer.parseInt(rset.getString("forum_perm"));
					_OwnerID = Integer.parseInt(rset.getString("forum_owner_id"));
				}
			}
			catch(Exception e)
			{
				_log.warning("data error on Forum " + _ForumId + " : " + e);
				throw e;
			}
			DatabaseUtils.closeDatabaseSR(statement, rset);

			try
			{
				statement = con.prepareStatement("SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC");
				statement.setInt(1, _ForumId);
				rset = statement.executeQuery();

				while(rset.next())
				{
					Topic t = new Topic(Topic.ConstructorType.RESTORE, Integer.parseInt(rset.getString("topic_id")), Integer.parseInt(rset.getString("topic_forum_id")), rset.getString("topic_name"), Long.parseLong(rset.getString("topic_date")), rset.getString("topic_ownername"), Integer.parseInt(rset.getString("topic_ownerid")), Integer.parseInt(rset.getString("topic_type")), Integer.parseInt(rset.getString("topic_reply")));
					_topic.put(t.getID(), t);
					if(t.getID() > TopicBBSManager.getInstance().getMaxID(this))
						TopicBBSManager.getInstance().setMaxID(t.getID(), this);
				}
			}
			catch(Exception e)
			{
				_log.warning("data error on Forum " + _ForumId + " : " + e);
				throw e;
			}
			DatabaseUtils.closeDatabaseSR(statement, rset);
		
			try
			{
				statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_parent=?");
				statement.setInt(1, _ForumId);
				rset = statement.executeQuery();

				while(rset.next())
					_children.add(new Forum(Integer.parseInt(rset.getString("forum_id")), this));
			}
			catch(Exception e)
			{
				_log.warning("data error on Forum (children): " + e);
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public int getTopicSize()
	{
		init();
		return _topic.size();
	}

	public Topic gettopic(int j)
	{
		init();
		return _topic.get(j);
	}

	public void addtopic(Topic t)
	{
		init();
		_topic.put(t.getID(), t);
	}

	public int getID()
	{
		return _ForumId;
	}

	public String getName()
	{
		init();
		return _ForumName;
	}

	public int getType()
	{
		init();
		return _ForumType;
	}

	public Forum GetChildByName(String name)
	{
		init();
		for(Forum f : _children)
			if(f.getName().equals(name))
				return f;
		return null;
	}

	public void RmTopicByID(int id)
	{
		_topic.remove(id);

	}

	public void insertindb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) values (?,?,?,?,?,?,?)");
			statement.setInt(1, _ForumId);
			statement.setString(2, _ForumName);
			statement.setInt(3, _FParent.getID());
			statement.setInt(4, _ForumPost);
			statement.setInt(5, _ForumType);
			statement.setInt(6, _ForumPerm);
			statement.setInt(7, _OwnerID);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("error while saving new Forum to db " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void init()
	{
		if(loaded)
			return;
		loadedLock.lock();
		if(loaded)
		{
			loadedLock.unlock();
			return;
		}
		try
		{
			load();
			loaded = true;
		}
		finally
		{
			loadedLock.unlock();
		}
	}
}