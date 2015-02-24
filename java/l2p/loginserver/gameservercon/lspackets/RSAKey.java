package l2p.loginserver.gameservercon.lspackets;

public class RSAKey extends ServerBasePacket
{
	public RSAKey(byte[] data)
	{
		writeC(0);
		writeB(data);
	}
}
