package l2p.gameserver.serverpackets;

import java.util.Map.Entry;

import l2p.gameserver.model.entity.siege.territory.TerritorySiege;
import l2p.util.GArray;
import l2p.util.Location;

public class ExShowOwnthingPos extends L2GameServerPacket
{
	private GArray<Location> _locs = new GArray<Location>();

	public ExShowOwnthingPos()
	{
		for(Entry<Integer, Location> e : TerritorySiege.getWardsLoc().entrySet())
			_locs.add(e.getValue().setH(e.getKey()));
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x93);
		writeD(_locs.size());
		for(Location loc : _locs)
		{
			writeD(0x50 + loc.h);
			writeD(loc.x);
			writeD(loc.y);
			writeD(loc.z);
		}
	}
}