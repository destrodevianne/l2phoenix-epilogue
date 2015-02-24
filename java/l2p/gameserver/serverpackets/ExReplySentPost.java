package l2p.gameserver.serverpackets;

import l2p.gameserver.clientpackets.RequestExCancelSentPost;
import l2p.gameserver.clientpackets.RequestExRequestSentPost;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.items.MailParcelController;
import l2p.gameserver.model.items.MailParcelController.Letter;
import l2p.gameserver.templates.L2Item;

/**
 * Просмотр собственного отправленного письма. Шлется в ответ на {@link RequestExRequestSentPost}.
 * При нажатии на кнопку Cancel клиент шлет {@link RequestExCancelSentPost}.
 * @see ExReplyReceivedPost
 */
public class ExReplySentPost extends L2GameServerPacket
{
	private Letter _letter;

	public ExReplySentPost(L2Player cha, int post)
	{
		_letter = MailParcelController.getInstance().getLetter(post);

		if(_letter == null)
		{
			_letter = new Letter();
			cha.sendPacket(new ExShowSentPostList(cha));
		}
	}

	// ddSSS dx[hddQdddhhhhhhhhhh] Qd
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xAD);

		writeD(_letter.id); // id письма
		writeD(_letter.price > 0 ? 1 : 0); // 1 - письмо с запросом оплаты, 0 - просто письмо

		writeS(_letter.receiverName); // кому
		writeS(_letter.topic); // топик
		writeS(_letter.body); // тело

		writeD(_letter.attached.size()); // количество приложенных вещей
		for(TradeItem temp : _letter.attached)
		{
			L2Item item = temp.getItem();
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeQ(temp.getCount());
			writeD(temp.getEnchantLevel());
			writeD(0); // ?
			writeD(0); // ?
			writeH(0); // ?
			writeH(0); // ?
			if(getClient().getRevision() >= 152)
				writeD(0); // что-то связанное тольсо с посылками
			writeItemElements(temp);
			writeItemRev152(temp);
		}

		writeQ(_letter.price); // для писем с оплатой - цена
		writeD(0); // ?
	}
}