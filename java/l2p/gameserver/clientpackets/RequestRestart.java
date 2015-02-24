package l2p.gameserver.clientpackets;

import l2p.extensions.multilang.CustomMessage;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.network.L2GameClient.GameClientState;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import l2p.gameserver.serverpackets.RestartResponse;

public class RequestRestart extends L2GameClientPacket
{
	/**
	 * packet type id 0x57
	 * format:      c
	 */

	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.inObserverMode())
		{
			activeChar.sendPacket(Msg.OBSERVERS_CANNOT_PARTICIPATE, RestartResponse.FAIL, Msg.ActionFail);
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RESTART_WHILE_IN_COMBAT, RestartResponse.FAIL, Msg.ActionFail);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING, RestartResponse.FAIL, Msg.ActionFail);
			return;
		}

		if(activeChar.isBlocked() && !activeChar.isFlying()) // Разрешаем выходить из игры если используется сервис HireWyvern. Вернет в начальную точку.
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.OutOfControl", activeChar));
			activeChar.sendPacket(RestartResponse.FAIL, Msg.ActionFail);
			return;
		}

		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if(activeChar.isFestivalParticipant())
		{
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.Festival", activeChar));
				activeChar.sendPacket(RestartResponse.FAIL, Msg.ActionFail);
				return;
			}
			L2Party playerParty = activeChar.getParty();

			if(playerParty != null)
				playerParty.broadcastMessageToPartyMembers(activeChar.getName() + " has been removed from the upcoming festival.");
		}

		if(getClient() != null)
			getClient().setState(GameClientState.AUTHED);
		activeChar.logout(false, true, false, false);
		// send char list
		CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLoginName(), getClient().getSessionId().playOkID1);
		sendPacket(RestartResponse.OK, cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
}