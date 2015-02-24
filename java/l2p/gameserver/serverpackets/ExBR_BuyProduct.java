package l2p.gameserver.serverpackets;

public class ExBR_BuyProduct extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xBB);
		writeD(1); // если послать не 1 будет некрасивоее сообщение об ошибке
	}
}