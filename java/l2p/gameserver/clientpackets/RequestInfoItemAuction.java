package l2p.gameserver.clientpackets;

public class RequestInfoItemAuction extends L2GameClientPacket
{
	@Override
	protected void runImpl()
	{}

	@Override
	protected void readImpl()
	{
		// Открыть окно с детальной информацией о предмете
		int obj_id = readD();
		System.out.println(getType() + " | id: " + obj_id);
	}
}