package l2p.gameserver.loginservercon.lspackets;

import java.util.logging.Logger;

import l2p.Config;
import l2p.gameserver.loginservercon.AttLS;

public class RSAKey extends LoginServerBasePacket
{
	private static final Logger log = Logger.getLogger(RSAKey.class.getName());

	public RSAKey(byte[] decrypt, AttLS loginServer)
	{
		super(decrypt, loginServer);
	}

	@Override
	public void read()
	{
		byte[] rsaKey = readB(128);
		getLoginServer().initRSA(rsaKey);
		getLoginServer().initCrypt();

		if(Config.DEBUG_GS_LS)
			log.info("GS Debug: RSAKey packet readed.");
	}
}