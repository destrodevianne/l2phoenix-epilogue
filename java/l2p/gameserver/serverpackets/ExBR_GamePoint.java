package l2p.gameserver.serverpackets;

public class ExBR_GamePoint extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xB8);
		// TODO dQd
	}
}