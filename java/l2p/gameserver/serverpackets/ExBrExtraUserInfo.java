package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class ExBrExtraUserInfo extends L2GameServerPacket
{
	private int _charId;

	public ExBrExtraUserInfo(L2Player cha)
	{
		_charId = cha.getObjectId();
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xBE);
		writeD(_charId);
		writeD(0);
		if(getClient().getRevision() >= 152)
			writeC(0); //?
	}
}