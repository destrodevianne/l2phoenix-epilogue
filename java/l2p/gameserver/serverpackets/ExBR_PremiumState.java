package l2p.gameserver.serverpackets;

public class ExBR_PremiumState extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xBC);
		// TODO dc
	}
}