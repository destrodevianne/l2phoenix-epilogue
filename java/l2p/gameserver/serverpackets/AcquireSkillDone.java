package l2p.gameserver.serverpackets;

public class AcquireSkillDone extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		// just trigger - без аргументов
		writeC(0x94);
	}
}