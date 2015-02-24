package l2p.gameserver.serverpackets;

public class ExEventMatchObserver extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x0E);
		// TODO dccSS
	}
}