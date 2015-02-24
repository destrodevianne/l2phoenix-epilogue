package l2p.gameserver.serverpackets;

public class PremiumState extends L2GameServerPacket
{
	private static final String _S__FE_AA_EXGETBOOKMARKINFO = "[S] FE:AA PremiumState";
	private int _objectId;
	private int _state;

	/**
	 * Неизвестно как работает, при получении открывается почта.
	 */
	public PremiumState(int objectId, int state)
	{
		_objectId = objectId;
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xaa);
		writeD(_objectId);
		writeC(_state);
	}

	@Override
	public String getType()
	{
		return _S__FE_AA_EXGETBOOKMARKINFO;
	}
}
