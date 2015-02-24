package l2p.gameserver.serverpackets;

public class ExDominionChannelSet extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x96);
		writeD(0); // unk
	}
}