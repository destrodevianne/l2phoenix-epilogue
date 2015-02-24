package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Summon;

public class PetStatusShow extends L2GameServerPacket
{
	private int _summonType;

	public PetStatusShow(L2Summon summon)
	{
		_summonType = summon.getSummonType();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xb1);
		writeD(_summonType);
	}
}