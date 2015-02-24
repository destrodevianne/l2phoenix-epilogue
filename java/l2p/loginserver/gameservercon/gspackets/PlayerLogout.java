package l2p.loginserver.gameservercon.gspackets;

import java.util.logging.Logger;

import l2p.Config;
import l2p.loginserver.GameServerTable;
import l2p.loginserver.LoginController;
import l2p.loginserver.gameservercon.AttGS;

public class PlayerLogout extends ClientBasePacket
{
	public static final Logger log = Logger.getLogger(PlayerLogout.class.getName());

	public PlayerLogout(byte[] decrypt, AttGS gameserver)
	{
		super(decrypt, gameserver);
	}

	@Override
	public void read()
	{
		String account = readS();

		getGameServer().removeAccountFromGameServer(account);
		LoginController.getInstance().removeAuthedLoginClient(account);

		if(Config.LOGIN_DEBUG)
			log.info("Player " + account + " logged out from gameserver [" + getGameServer().getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getGameServer().getServerId()));
	}
}