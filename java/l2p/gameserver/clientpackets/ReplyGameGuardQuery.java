package l2p.gameserver.clientpackets;

public class ReplyGameGuardQuery extends L2GameClientPacket
{
	// Format: cdddd

	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		if(getClient() != null)
			getClient().setGameGuardOk(true);
	}
}