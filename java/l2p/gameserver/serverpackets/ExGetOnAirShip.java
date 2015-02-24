package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.vehicle.L2AirShip;
import l2p.util.Location;

public class ExGetOnAirShip extends L2GameServerPacket
{
	private int _char_id, _boat_id;
	private Location _loc;

	public ExGetOnAirShip(L2Player cha, L2AirShip boat, Location loc)
	{
		_char_id = cha.getObjectId();
		_boat_id = boat.getObjectId();
		_loc = loc;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x63);
		writeD(_char_id);
		writeD(_boat_id);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}