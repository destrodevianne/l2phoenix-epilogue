package l2p.loginserver.gameservercon;

import java.io.IOException;
import java.util.logging.Logger;

import l2p.loginserver.gameservercon.gspackets.AuthRequest;
import l2p.loginserver.gameservercon.gspackets.BanIP;
import l2p.loginserver.gameservercon.gspackets.BlowFishKey;
import l2p.loginserver.gameservercon.gspackets.ChangeAccessLevel;
import l2p.loginserver.gameservercon.gspackets.ChangePassword;
import l2p.loginserver.gameservercon.gspackets.ClientBasePacket;
import l2p.loginserver.gameservercon.gspackets.LockAccountIP;
import l2p.loginserver.gameservercon.gspackets.MoveCharToAcc;
import l2p.loginserver.gameservercon.gspackets.PlayerAuthRequest;
import l2p.loginserver.gameservercon.gspackets.PlayerInGame;
import l2p.loginserver.gameservercon.gspackets.PlayersInGame;
import l2p.loginserver.gameservercon.gspackets.PlayerLogout;
import l2p.loginserver.gameservercon.gspackets.Restart;
import l2p.loginserver.gameservercon.gspackets.ServerStatus;
import l2p.loginserver.gameservercon.gspackets.TestConnectionResponse;
import l2p.loginserver.gameservercon.gspackets.UnbanIP;

public class PacketHandler
{
	private static Logger log = Logger.getLogger(PacketHandler.class.getName());

	public static ClientBasePacket handlePacket(byte[] data, AttGS gameserver)
	{
		ClientBasePacket packet = null;
		try
		{
			data = gameserver.decrypt(data);
			int packetType = data[0] & 0xff;

			if(!gameserver.isCryptInitialized() && packetType > 0)
			{
				log.severe("Packet id[" + packetType + "] from not crypt initialized server.");
				return null;
			}

			if(!gameserver.isAuthed() && packetType > 1)
			{
				log.severe("Packet id[" + packetType + "] from not authed server.");
				return null;
			}

			switch(packetType)
			{
				case 0x00:
					new BlowFishKey(data, gameserver).run();
					break;
				case 0x01:
					new AuthRequest(data, gameserver).run();
					break;
				case 0x02:
					packet = new PlayerInGame(data, gameserver);
					break;
				case 0x03:
					packet = new PlayerLogout(data, gameserver);
					break;
				case 0x04:
					packet = new ChangeAccessLevel(data, gameserver);
					break;
				case 0x05:
					packet = new PlayerAuthRequest(data, gameserver);
					break;
				case 0x06:
					packet = new ServerStatus(data, gameserver);
					break;
				case 0x07:
					packet = new BanIP(data, gameserver);
					break;
				case 0x08:
					packet = new ChangePassword(data, gameserver);
					break;
				case 0x09:
					packet = new Restart(data, gameserver);
					break;
				case 0x0a:
					packet = new UnbanIP(data, gameserver);
					break;
				case 0x0b:
					packet = new LockAccountIP(data, gameserver);
					break;
				case 0x0c:
					packet = new MoveCharToAcc(data, gameserver);
					break;
				case 0x0d:
					packet = new TestConnectionResponse(data, gameserver);
					break;
				case 0x0e:
					packet = new PlayersInGame(data, gameserver);
					break;
				default:
					log.severe("Unknown packet from GS: " + packetType);

			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return packet;
	}
}