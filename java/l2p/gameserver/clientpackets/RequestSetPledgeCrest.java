package l2p.gameserver.clientpackets;

import java.util.logging.Logger;

import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;

public class RequestSetPledgeCrest extends L2GameClientPacket
{
	// Format: cdb
	static Logger _log = Logger.getLogger(RequestSetPledgeCrest.class.getName());

	private int _length;
	private byte[] _data;

	@Override
	public void readImpl()
	{
		_length = readD();
		if(_length == 0)
			return; // удаление значка
		if(_length > _buf.remaining() || _length != 256)
		{
			_log.warning("Possibly server crushing packet: " + getType() + " with length " + _length);
			_buf.clear();
			return;
		}
		_data = new byte[_length];
		readB(_data);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Clan clan = activeChar.getClan();
		if((activeChar.getClanPrivileges() & L2Clan.CP_CL_EDIT_CREST) == L2Clan.CP_CL_EDIT_CREST)
		{
			if(clan.getLevel() < 3)
			{
				activeChar.sendPacket(Msg.CLAN_CREST_REGISTRATION_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3);
				return;
			}

			if(clan.hasCrest())
				CrestCache.removePledgeCrest(clan);

			if(_data != null && _length != 0)
				CrestCache.savePledgeCrest(clan, _data);

			clan.broadcastClanStatus(false, true, false);
		}
	}
}