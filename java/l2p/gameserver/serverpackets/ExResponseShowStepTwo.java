package l2p.gameserver.serverpackets;

public class ExResponseShowStepTwo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xAF);
		// TODO dS x[cS]
	}
}