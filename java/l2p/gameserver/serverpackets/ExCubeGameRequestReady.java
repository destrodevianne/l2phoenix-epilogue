package l2p.gameserver.serverpackets;

/**
 * Format: (chd)
 */
public class ExCubeGameRequestReady extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x97);
		writeD(0x04);
	}
}