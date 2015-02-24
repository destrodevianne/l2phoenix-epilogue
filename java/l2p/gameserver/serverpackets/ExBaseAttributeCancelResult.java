package l2p.gameserver.serverpackets;

public class ExBaseAttributeCancelResult extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x75);
		writeH(0x00); //FIXME resultId?
	}
}