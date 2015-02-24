package l2p.gameserver.serverpackets;

public class ExSendUIEvent extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x8E);
		// TODO ddddSSSSSS
	}
}