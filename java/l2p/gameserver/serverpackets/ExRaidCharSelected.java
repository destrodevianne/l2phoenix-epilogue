package l2p.gameserver.serverpackets;

public class ExRaidCharSelected extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xB5);
		// just a trigger
	}
}