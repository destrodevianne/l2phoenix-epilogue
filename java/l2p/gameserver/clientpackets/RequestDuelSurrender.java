package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Duel;

public class RequestDuelSurrender extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player p = getClient().getActiveChar();

		if(p == null)
			return;

		Duel d = p.getDuel();

		if(d == null)
			return;

		d.doSurrender(p);
	}
}