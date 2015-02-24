package l2p.gameserver.serverpackets;

public class ExBR_ProductList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xB9);
		// TODO dx[dhddddcccccdd]
	}
}