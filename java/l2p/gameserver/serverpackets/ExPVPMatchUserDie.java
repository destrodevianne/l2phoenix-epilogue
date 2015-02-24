package l2p.gameserver.serverpackets;

/**
 * Format: dd
 * blueTeamTotalKillCnt:%d, redTeamTotalKillCnt:%d
 */

public class ExPVPMatchUserDie extends L2GameServerPacket
{
	private int blueTeamTotalKillCnt, redTeamTotalKillCnt;

	public ExPVPMatchUserDie(int blueTeamTotalKillCnt, int redTeamTotalKillCnt)
	{
		this.blueTeamTotalKillCnt = blueTeamTotalKillCnt;
		this.redTeamTotalKillCnt = redTeamTotalKillCnt;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x7F);
		writeD(blueTeamTotalKillCnt);
		writeD(redTeamTotalKillCnt);
	}
}