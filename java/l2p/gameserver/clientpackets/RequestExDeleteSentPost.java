package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.MailParcelController;
import l2p.gameserver.serverpackets.ExShowSentPostList;

/**
 * Запрос на удаление отправленных сообщений. Удалить можно только письмо без вложения. Отсылается при нажатии на "delete" в списке отправленных писем.
 * @see ExShowSentPostList
 * @see RequestExDeleteReceivedPost
 */
public class RequestExDeleteSentPost extends L2GameClientPacket
{
	private int[] _list;

	/**
	 * format: dx[d]
	 */
	@Override
	public void readImpl()
	{
		_list = new int[readD()]; // количество элементов для удаления

		for(int i = 0; i < _list.length; i++)
			_list[i] = readD(); // уникальный номер письма
	}

	@Override
	public void runImpl()
	{
		if(_list.length == 0)
			return;

		MailParcelController.getInstance().deleteLetter(_list);

		L2Player cha = getClient().getActiveChar();
		if(cha != null)
			cha.sendPacket(new ExShowSentPostList(cha));
	}
}