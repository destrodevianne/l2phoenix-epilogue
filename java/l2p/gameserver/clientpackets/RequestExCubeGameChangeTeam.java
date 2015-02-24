package l2p.gameserver.clientpackets;

import java.util.logging.Logger;

import l2p.gameserver.model.L2Player;

/**
 * Format: chdd
 * d: team
 */
public final class RequestExCubeGameChangeTeam extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestExCubeGameChangeTeam.class.getName());

	int _team;

	@Override
	protected void readImpl()
	{
		_team = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.isDead())
			return;

		switch(_team)
		{
			case 0:
			case 1:
				// Change Player Team
				break;
			case -1:
				// Remove Player (me)
				break;
			default:
				_log.warning("Wrong Team ID: " + _team);
				break;
		}
	}
}
