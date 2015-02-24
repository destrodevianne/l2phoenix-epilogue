package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.FortressManager;
import l2p.gameserver.model.entity.residence.Fortress;
import l2p.gameserver.serverpackets.ExShowFortressMapInfo;

public class RequestFortressMapInfo extends L2GameClientPacket
{
	private int fort_id;

	@Override
	public void readImpl()
	{
		fort_id = readD();
	}

	@Override
	public void runImpl()
	{
		Fortress fortress = FortressManager.getInstance().getFortressByIndex(fort_id);
		if(fortress != null)
			sendPacket(new ExShowFortressMapInfo(fortress));
	}
}