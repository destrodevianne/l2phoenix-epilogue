package l2p.gameserver.serverpackets;

import l2p.gameserver.clientpackets.RequestExReceivePost;
import l2p.gameserver.clientpackets.RequestExRejectPost;
import l2p.gameserver.clientpackets.RequestExRequestReceivedPost;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.items.MailParcelController;
import l2p.gameserver.model.items.MailParcelController.Letter;
import l2p.gameserver.templates.L2Item;

/**
 * Просмотр полученного письма. Шлется в ответ на {@link RequestExRequestReceivedPost}.
 * При попытке забрать приложенные вещи клиент шлет {@link RequestExReceivePost}.
 * При возврате письма клиент шлет {@link RequestExRejectPost}.
 * @see ExReplySentPost
 */
public class ExReplyReceivedPost extends L2GameServerPacket
{
	private Letter _letter;

	public ExReplyReceivedPost(L2Player cha, int post)
	{
		_letter = MailParcelController.getInstance().getLetter(post);

		if(_letter == null)
			_letter = new Letter();
		else if(_letter.unread > 0)
		{
			MailParcelController.getInstance().markMailRead(post);
			_letter.unread = 0;
		}

		cha.sendPacket(new ExShowReceivedPostList(cha));
	}

	// dddSSS dx[hddQdddhhhhhhhhhh] Qdd
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xAB);

		writeD(_letter.id); // id письма
		writeD(_letter.price > 0 ? 1 : 0); // 1 - письмо с запросом оплаты, 0 - просто письмо
		writeD(0); // для писем с флагом "от news informer" в отправителе значится "****", всегда оверрайдит тип на просто письмо

		writeS(_letter.senderName); // от кого
		writeS(_letter.topic); // топик
		writeS(_letter.body); // тело

		writeD(_letter.attached.size()); // количество приложенных вещей
		L2Item item;
		for(TradeItem temp : _letter.attached)
		{
			item = temp.getItem();
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
		writeD(_letter.attachments > 0 ? 1 : 0); // 1 - письмо можно вернуть
		writeD(_letter.system); // 1 - на письмо нельзя отвечать, его нельзя вернуть, в отправителе значится news informer (или "****" если установлен флаг в начале пакета)
	}
}