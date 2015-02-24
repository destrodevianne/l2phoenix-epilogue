package l2p.gameserver.serverpackets;

public class ExTutorialList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x6B);
		// todo writeB(new byte[128]);
	}
}