package l2p.gameserver.serverpackets;

public class DeleteRadar extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0xB8);
		//TODO ddd
	}
}