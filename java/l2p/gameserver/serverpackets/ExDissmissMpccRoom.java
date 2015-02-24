package l2p.gameserver.serverpackets;

public class ExDissmissMpccRoom extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x9D);
		// just trigger
	}
}