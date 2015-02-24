package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.vehicle.L2AirShip;
import l2p.gameserver.tables.AirShipDocksTable;
import l2p.gameserver.tables.AirShipDocksTable.AirShipDock;
import l2p.util.GArray;

public class ExAirShipTeleportList extends L2GameServerPacket
{
	private int _fuel;
	private GArray<AirShipDock> _airports;
	private boolean _canWriteImpl = false;

	public ExAirShipTeleportList(L2AirShip ship)
	{
		int currentDockNpcId = ship.getCurrentDockNpcId();
		if(currentDockNpcId == 0)
			return;
		_fuel = ship.getFuel();
		_airports = AirShipDocksTable.getInstance().getAirShipDocksForTeleports(currentDockNpcId);
		_canWriteImpl = true;
	}

	@Override
	protected void writeImpl()
	{
		if(!_canWriteImpl)
			return;

		writeC(EXTENDED_PACKET);
		writeH(0x9A);
		writeD(_fuel); // current fuel
		writeD(_airports.size());

		for(AirShipDock airport : _airports)
		{
			writeD(airport.getId()); // AirportID
			writeD(airport.getFuel()); // need fuel
			writeD(airport.getLoc().x); // Airport x
			writeD(airport.getLoc().y); // Airport y
			writeD(airport.getLoc().z); // Airport z
		}
	}
}