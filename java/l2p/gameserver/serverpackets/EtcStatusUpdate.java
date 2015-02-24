package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class EtcStatusUpdate extends L2GameServerPacket
{
	/**
	 *
	 * Packet for lvl 3 client buff line
	 *
	 * Example:(C4)
	 * F9 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - empty statusbar
	 * F9 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - increased force lvl 1
	 * F9 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - weight penalty lvl 1
	 * F9 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 - chat banned
	 * F9 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 - Danger Area lvl 1
	 * F9 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 - lvl 1 grade penalty
	 *
	 * packet format: cdd //and last three are ddd???
	 *
	 * Some test results:
	 * F9 07 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - lvl 7 increased force lvl 4 weight penalty
	 *
	 * Example:(C5 709)
	 * F9 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0F 00 00 00 - lvl 1 charm of courage lvl 15 Death Penalty
	 *
	 *
	 * NOTE:
	 * End of buff:
	 * You must send empty packet
	 * F9 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
	 * to remove the statusbar or just empty value to remove some icon.
	 */

	private int IncreasedForce, WeightPenalty, MessageRefusal, DangerArea;
	private int _expertisePenalty, CharmOfCourage, DeathPenaltyLevel, ConsumedSouls;
	private boolean can_writeImpl = false;

	public EtcStatusUpdate(L2Player player)
	{
		if(player == null || player.getActiveClass() == null)
			return;
		IncreasedForce = player.getIncreasedForce();
		WeightPenalty = player.getWeightPenalty();
		MessageRefusal = player.getMessageRefusal() || player.getNoChannel() != 0 || player.isBlockAll() ? 1 : 0;
		DangerArea = player.isInDangerArea() ? 1 : 0;
		_expertisePenalty = player.getExpertisePenalty();
		CharmOfCourage = player.isCharmOfCourage() ? 1 : 0;
		DeathPenaltyLevel = player.getDeathPenalty() == null ? 0 : player.getDeathPenalty().getLevel();
		ConsumedSouls = player.getConsumedSouls();
		can_writeImpl = true;
	}

	@Override
	protected final void writeImpl()
	{
		if(!can_writeImpl)
			return;

		// dddddddd
		writeC(0xf9); //Packet type
		writeD(IncreasedForce); // skill id 4271, 7 lvl
		writeD(WeightPenalty); // skill id 4270, 4 lvl
		writeD(MessageRefusal); //skill id 4269, 1 lvl
		writeD(DangerArea); // skill id 4268, 1 lvl
		writeD(_expertisePenalty); // weapon grade penalty, skill 6209 in epilogue, skill id 4267, 1 lvl at off c4 server scripts
		writeD(0); // armor grade penalty, skill 6213 in epilogue
		writeD(CharmOfCourage); //Charm of Courage, "Prevents experience value decreasing if killed during a siege war".
		writeD(DeathPenaltyLevel); //Death Penalty max lvl 15, "Combat ability is decreased due to death."
		writeD(ConsumedSouls);
	}
}