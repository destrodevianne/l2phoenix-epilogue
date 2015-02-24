package l2p.gameserver.serverpackets;

public class ExEventMatchCreate extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x1D);
		// TODO d
	}
}