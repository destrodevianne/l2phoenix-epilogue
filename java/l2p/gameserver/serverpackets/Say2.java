package l2p.gameserver.serverpackets;

public class Say2 extends L2GameServerPacket
{
	private int _objectId, _textType;
	private String _charName, _text;

	public Say2(int objectId, int messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x4A);
		writeD(_objectId);
		writeD(_textType);
		if(_textType == 11)
		{
			writeD(0x00); //npcId?
			writeD(0x00); //SysMsgId?
		}
		else
		{
			writeS(_charName);
			writeS(_text);
		}
	}
}