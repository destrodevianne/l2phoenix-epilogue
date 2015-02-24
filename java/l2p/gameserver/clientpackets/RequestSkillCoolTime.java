package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.SkillCoolTime;

public class RequestSkillCoolTime extends L2GameClientPacket
{
	L2GameClient _client;

	@Override
	public void readImpl()
	{
		_client = getClient();
	}

	@Override
	public void runImpl()
	{
		L2Player pl = _client.getActiveChar();
		if(pl != null)
			pl.sendPacket(new SkillCoolTime(pl));
	}
}