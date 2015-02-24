package l2p.gameserver.serverpackets;

public class ExBR_RecentProductListPacket extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xC0);
		// TODO dx[dhddddcccccdd]
	}
}