package l2p.gameserver.model.entity.olympiad;

import l2p.util.GArray;

public class Stadia
{
	private boolean _freeToUse = true;
	private GArray<Integer> _doors = new GArray<Integer>();

	public boolean isFreeToUse()
	{
		return _freeToUse;
	}

	public void setStadiaBusy()
	{
		_freeToUse = false;
	}

	public void setStadiaFree()
	{
		_freeToUse = true;
	}

	public void setDoor(int id)
	{
		_doors.add(id);
	}

	public GArray<Integer> getDoors()
	{
		return _doors;
	}
}