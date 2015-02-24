package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExReplyPostItemList;
import l2p.gameserver.serverpackets.ExShowReceivedPostList;

/**
 *  Нажатие на кнопку "send mail" в списке из {@link ExShowReceivedPostList}, запрос создания нового письма
 *  В ответ шлется {@link ExReplyPostItemList}
 */
public class RequestExPostItemList extends L2GameClientPacket
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
			cha.sendPacket(new ExReplyPostItemList(cha));
	}
}