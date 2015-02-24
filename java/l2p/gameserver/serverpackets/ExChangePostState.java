package l2p.gameserver.serverpackets;

public class ExChangePostState extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xB3);
		// TODO d dx[dd]
	}
}