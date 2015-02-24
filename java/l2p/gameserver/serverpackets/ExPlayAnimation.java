package l2p.gameserver.serverpackets;

public class ExPlayAnimation extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x5A);
		// TODO dcdS
	}
}