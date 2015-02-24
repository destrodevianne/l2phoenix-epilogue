package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.siege.territory.TerritorySiege;
import l2p.gameserver.serverpackets.ExReplyDominionInfo;
import l2p.gameserver.serverpackets.ExShowOwnthingPos;

public class RequestExDominionInfo extends L2GameClientPacket
{
	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new ExReplyDominionInfo());
		if(TerritorySiege.isInProgress())
			activeChar.sendPacket(new ExShowOwnthingPos());
	}

	@Override
	public void readImpl()
	{}
}