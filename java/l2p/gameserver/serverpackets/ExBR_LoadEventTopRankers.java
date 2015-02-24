package l2p.gameserver.serverpackets;

public class ExBR_LoadEventTopRankers extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xC1);
		// TODO ddddd
	}
}