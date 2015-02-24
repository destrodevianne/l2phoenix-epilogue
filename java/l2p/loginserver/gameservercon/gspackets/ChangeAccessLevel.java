package l2p.loginserver.gameservercon.gspackets;

import java.util.logging.Logger;

import l2p.loginserver.LoginController;
import l2p.loginserver.gameservercon.AttGS;

public class ChangeAccessLevel extends ClientBasePacket
{
	public static final Logger log = Logger.getLogger(ChangeAccessLevel.class.getName());

	public ChangeAccessLevel(byte[] decrypt, AttGS gameserver)
	{
		super(decrypt, gameserver);
	}

	@Override
	public void read()
	{
		int level = readD();
		String account = readS();
		String comments = readS();
		int banTime = readD();

		LoginController.getInstance().setAccountAccessLevel(account, level, comments, banTime);
		log.info("Changed " + account + " access level to " + level);
	}
}