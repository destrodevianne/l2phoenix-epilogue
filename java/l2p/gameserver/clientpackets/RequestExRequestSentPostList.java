package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExShowSentPostList;

/**
 * Нажатие на кнопку "sent mail",запрос списка исходящих писем.
 * В ответ шлется {@link ExShowSentPostList}
 */
public class RequestExRequestSentPostList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{
	//just a trigger
	}

	@Override
	public void runImpl()
	{
		L2Player cha = getClient().getActiveChar();
		if(cha != null)
			cha.sendPacket(new ExShowSentPostList(cha));
	}
}