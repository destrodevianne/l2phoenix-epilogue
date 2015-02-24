package l2p.gameserver.serverpackets;

public class ExSubPledgetSkillAdd extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x76);
		//TODO ddd
	}
}