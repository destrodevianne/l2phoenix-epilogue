package l2p.loginserver.gameservercon.gspackets;

import java.util.Arrays;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2p.Config;
import l2p.gameserver.loginservercon.AdvIP;
import l2p.loginserver.GameServerTable;
import l2p.loginserver.gameservercon.AttGS;
import l2p.loginserver.gameservercon.GameServerInfo;
import l2p.loginserver.gameservercon.lspackets.AuthResponse;
import l2p.loginserver.gameservercon.lspackets.BanIPList;
import l2p.loginserver.gameservercon.lspackets.LoginServerFail;

public class AuthRequest extends ClientBasePacket
{
	protected static Logger log = Logger.getLogger(AuthRequest.class.getName());

	public AuthRequest(byte[] decrypt, AttGS gameserver)
	{
		super(decrypt, gameserver);
	}

	@Override
	public void read()
	{
		int requestId = readC();
		boolean acceptAlternateID = readC() == 1;
		boolean reserveHostOnLogin = readC() == 1; //FIXME: оно всегда false
		String externalIp = readS();
		String internalIp = readS();
		int portsCount = readH();
		int[] ports;
		if(portsCount == 0xFFFF) //новый формат, многопортовый
		{
			ports = new int[readC()];
			for(int i = 0; i < ports.length; i++)
				ports[i] = readH();
		}
		else
			ports = new int[] { portsCount }; // старый формат, однопортовый
		int maxOnline = readD();
		int hexIdLenth = readD();
		byte[] hexId = readB(hexIdLenth);
		int advIpsSize = readD();

		FastList<AdvIP> advIpList = FastList.newInstance();

		for(int i = 0; i < advIpsSize; i++)
		{
			AdvIP ip = new AdvIP();
			ip.ipadress = readS();
			ip.ipmask = readS();
			ip.bitmask = readS();
			advIpList.add(ip);
		}

		int protocolVersion;
		try
		{
			protocolVersion = readH();
		}
		catch(Exception e)
		{
			protocolVersion = 0;
		}

		log.info("Trying to register server: " + requestId + ", " + getGameServer().getConnectionIpAddress());
		AttGS client = getGameServer();

		GameServerTable gameServerTable = GameServerTable.getInstance();

		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(requestId);
		// is there a gameserver registered with this id?
		if(gsi != null)
		{
			// does the hex id match?
			if(Arrays.equals(gsi.getHexId(), hexId))
				// check to see if this GS is already connected
				synchronized (gsi)
				{
					if(gsi.isAuthed())
						sendPacket(new LoginServerFail(LoginServerFail.REASON_ALREADY_LOGGED8IN));
					else
					{
						client.setGameServerInfo(gsi);
						gsi.setGameServer(client);
						gsi.setPorts(ports);
						gsi.setGameHosts(externalIp, internalIp, advIpList);
						gsi.setMaxPlayers(maxOnline);
						gsi.setAuthed(true);
					}
				}
			else // there is already a server registered with the desired id and different hex id
			// try to register this one with an alternative id
			if(Config.ACCEPT_NEW_GAMESERVER && acceptAlternateID)
			{
				gsi = new GameServerInfo(requestId, hexId, client);
				if(gameServerTable.registerWithFirstAvailableId(gsi))
				{
					client.setGameServerInfo(gsi);
					gsi.setGameServer(client);
					gsi.setPorts(ports);
					gsi.setGameHosts(externalIp, internalIp, advIpList);
					gsi.setMaxPlayers(maxOnline);
					gsi.setAuthed(true);
					if(reserveHostOnLogin)
						gameServerTable.registerServerOnDB(gsi);
				}
				else
					sendPacket(new LoginServerFail(LoginServerFail.REASON_NO_FREE_ID));
			}
			else
				// server id is already taken, and we cant get a new one for you
				sendPacket(new LoginServerFail(LoginServerFail.REASON_WRONG_HEXID));
		}
		else if(Config.ACCEPT_NEW_GAMESERVER)
		{
			gsi = new GameServerInfo(requestId, hexId, client);
			if(gameServerTable.register(requestId, gsi))
			{
				client.setGameServerInfo(gsi);
				gsi.setGameServer(client);
				gsi.setPorts(ports);
				gsi.setGameHosts(externalIp, internalIp, advIpList);
				gsi.setMaxPlayers(maxOnline);
				gsi.setAuthed(true);
				if(reserveHostOnLogin)
					gameServerTable.registerServerOnDB(gsi);
			}
			else
				// some one took this ID meanwhile
				sendPacket(new LoginServerFail(LoginServerFail.REASON_ID_RESERVED));
		}
		else
			sendPacket(new LoginServerFail(LoginServerFail.REASON_WRONG_HEXID));

		if(gsi != null && gsi.isAuthed())
		{
			AuthResponse ar = new AuthResponse(gsi.getId());
			client.setProtocolVersion(protocolVersion);
			client.setAuthed(true);
			client.setServerId(gsi.getId());
			sendPacket(ar);
			sendPacket(new BanIPList());
			log.info("Server registration successful.");
		}
		else
			log.info("Server registration failed.");
	}
}
