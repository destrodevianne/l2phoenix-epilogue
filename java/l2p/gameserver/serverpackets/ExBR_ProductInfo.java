package l2p.gameserver.serverpackets;

public class ExBR_ProductInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xBA);
		// TODO dd dx[dddd]
	}
}