package l2p.gameserver.loginservercon.lspackets;

import l2p.extensions.multilang.CustomMessage;
import l2p.extensions.scripts.Functions;
import l2p.gameserver.loginservercon.AttLS;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.network.L2GameClient;

/**
 * @Author: Death
 * @Date: 8/2/2007
 * @Time: 14:39:46
 */
public class ChangePasswordResponse extends LoginServerBasePacket
{
	public ChangePasswordResponse(byte[] decrypt, AttLS loginServer)
	{
		super(decrypt, loginServer);
	}

	@Override
	public void read()
	{
		String account = readS();
		boolean changed = readD() == 1;

		L2GameClient client = getLoginServer().getCon().getAccountInGame(account);

		if(client == null)
			return;

		L2Player activeChar = client.getActiveChar();

		if(activeChar == null)
			return;

		if(changed)
			Functions.show(new CustomMessage("scripts.commands.user.password.ResultTrue", activeChar), activeChar);
		else
			Functions.show(new CustomMessage("scripts.commands.user.password.ResultFalse", activeChar), activeChar);
	}
}