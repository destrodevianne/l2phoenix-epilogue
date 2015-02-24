package l2p.gameserver.serverpackets;

public class ExPartyMemberRenamed extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xA6);
		// TODO ddd
	}
}