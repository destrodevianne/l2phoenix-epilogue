package l2p.gameserver.serverpackets;

public class ExBrBroadcastEventState extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xBD);
		// TODO dddddddSS
	}
}