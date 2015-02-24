package commands.voiced;

import static l2p.gameserver.model.L2Zone.ZoneType.OlympiadStadia;
import static l2p.gameserver.model.L2Zone.ZoneType.Siege;
import static l2p.gameserver.model.L2Zone.ZoneType.no_restart;
import static l2p.gameserver.model.L2Zone.ZoneType.no_summon;

import java.sql.ResultSet;

import l2p.Config;
import l2p.common.ThreadPoolManager;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.extensions.multilang.CustomMessage;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.IVoicedCommandHandler;
import l2p.gameserver.handler.VoicedCommandHandler;
import l2p.gameserver.instancemanager.CoupleManager;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.entity.Couple;
import l2p.gameserver.model.items.MailParcelController;
import l2p.gameserver.model.items.MailParcelController.Letter;
import l2p.gameserver.serverpackets.ConfirmDlg;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.SetupGauge;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.tables.SkillTable;
import l2p.util.Location;
import l2p.util.Util;

public class Wedding implements IVoicedCommandHandler, ScriptFile
{
	private static String[] _voicedCommands = { "divorce", "engage", "gotolove" };

	public boolean useVoicedCommand(String command, L2Player activeChar, String target)
	{
		if(command.startsWith("engage"))
			return engage(activeChar);
		else if(command.startsWith("divorce"))
			return divorce(activeChar);
		else if(command.startsWith("gotolove"))
			return goToLove(activeChar);
		return false;
	}

	public boolean divorce(L2Player activeChar)
	{
		if(activeChar.getPartnerId() == 0)
			return false;

		int _partnerId = activeChar.getPartnerId();
		long AdenaAmount = 0;

		if(activeChar.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.Divorced", activeChar));
			AdenaAmount = Math.abs(activeChar.getAdena() * Config.WEDDING_DIVORCE_COSTS / 100);
		}
		else
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.Disengaged", activeChar));

		activeChar.setMaried(false);
		activeChar.setPartnerId(0);
		Couple couple = CoupleManager.getInstance().getCouple(activeChar.getCoupleId());
		couple.divorce();
		couple = null;

		L2Player partner = L2ObjectsStorage.getPlayer(_partnerId);

		if(partner != null)
		{
			partner.setPartnerId(0);
			if(partner.isMaried())
				partner.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.PartnerDivorce", partner));
			else
				partner.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.PartnerDisengage", partner));
			partner.setMaried(false);

			// give adena
			if(AdenaAmount > 0)
			{
				activeChar.reduceAdena(AdenaAmount, true);
				partner.addAdena(AdenaAmount);
			}
		}
		else
		{
			Letter letter = new Letter();
			letter.receiverId = _partnerId;
			letter.receiverName = Util.getPlayerNameByObjId(_partnerId);
			letter.senderId = activeChar.getObjectId();
			letter.senderName = activeChar.getName();
			letter.topic = "Divorce notification";
			letter.body = "Your Partner has decided to divorce from you.";
			letter.price = 0;
			letter.unread = 1;
			letter.system = 1;
			letter.validtime = 1296000 + (int) (System.currentTimeMillis() / 1000); // 14 days

			if(AdenaAmount > 0)
				MailParcelController.getInstance().sendLetter(letter, new int[] { activeChar.getInventory().getItemByItemId(57).getObjectId() }, new long[] { AdenaAmount }, activeChar);
			else
				MailParcelController.getInstance().sendLetter(letter);
		}
		return true;
	}

	public boolean engage(L2Player activeChar)
	{
		// check target
		if(activeChar.getTarget() == null)
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.NoneTargeted", activeChar));
			return false;
		}
		// check if target is a L2Player
		if(!activeChar.getTarget().isPlayer())
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.OnlyAnotherPlayer", activeChar));
			return false;
		}
		// check if player is already engaged
		if(activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.AlreadyEngaged", activeChar));
			if(Config.WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
				// Head
				// lets recycle the sevensigns debuffs
				int skillId;

				int skillLevel = 1;

				if(activeChar.getLevel() > 40)
					skillLevel = 2;

				if(activeChar.isMageClass())
					skillId = 4361;
				else
					skillId = 4362;

				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

				if(activeChar.getEffectList().getEffectsBySkill(skill) == null)
				{
					skill.getEffects(activeChar, activeChar, false, false);
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_S2S_EFFECT_CAN_BE_FELT).addSkillName(skillId, skillLevel));
				}
			}
			return false;
		}

		L2Player ptarget = (L2Player) activeChar.getTarget();

		// check if player target himself
		if(ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.EngagingYourself", activeChar));
			return false;
		}

		if(ptarget.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.PlayerAlreadyMarried", activeChar));
			return false;
		}

		if(ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.PlayerAlreadyEngaged", activeChar));
			return false;
		}

		if(ptarget.isEngageRequest())
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.PlayerAlreadyAsked", activeChar));
			return false;
		}

		if(ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.PlayerAlreadyEngaged", activeChar));
			return false;
		}

		if(ptarget.getSex() == activeChar.getSex() && !Config.WEDDING_SAMESEX)
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.SameSex", activeChar));
			return false;
		}

		// check if target has player on friendlist
		boolean FoundOnFriendList = false;
		int objectId;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
			statement.setInt(1, ptarget.getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				objectId = rset.getInt("friend_id");
				if(objectId == activeChar.getObjectId())
				{
					FoundOnFriendList = true;
					break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		if(!FoundOnFriendList)
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.NotInFriendlist", activeChar));
			return false;
		}

		ptarget.setEngageRequest(true, activeChar.getObjectId());
		// ptarget.sendMessage("Player "+activeChar.getName()+" wants to engage with you.");
		ptarget.sendPacket(new ConfirmDlg(SystemMessage.S1, 60000, 4).addString("Player " + activeChar.getName() + " asking you to engage. Do you want to start new relationship?"));
		return true;
	}

	public boolean goToLove(L2Player activeChar)
	{
		if(!activeChar.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.YoureNotMarried", activeChar));
			return false;
		}

		if(activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.PartnerNotInDB", activeChar));
			return false;
		}

		L2Player partner = L2ObjectsStorage.getPlayer(activeChar.getPartnerId());
		if(partner == null)
		{
			activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.PartnerOffline", activeChar));
			return false;
		}

		if(partner.isInOlympiadMode() || partner.isFestivalParticipant() || activeChar.isMovementDisabled() || activeChar.isPMuted() || activeChar.isInOlympiadMode() || activeChar.getDuel() != null || activeChar.isFestivalParticipant() || partner.isInZone(no_summon))
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar));
			return false;
		}

		if(activeChar.isInParty() && activeChar.getParty().isInDimensionalRift() || partner.isInParty() && partner.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar));
			return false;
		}

		if(activeChar.getTeleMode() != 0 || activeChar.getReflection().getId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar));
			return false;
		}

		// "Нельзя вызывать персонажей в/из зоны свободного PvP"
		// "в зоны осад"
		// "на Олимпийский стадион"
		// "в зоны определенных рейд-боссов и эпик-боссов"
		if(partner.isInZoneBattle() || partner.isInZone(Siege) || partner.isInZone(no_restart) || partner.isInZone(OlympiadStadia) || activeChar.isInZoneBattle() || activeChar.isInZone(Siege) || activeChar.isInZone(no_restart) || activeChar.isInZone(OlympiadStadia) || partner.getReflection().getId() != 0 || partner.isInZone(no_summon))
		{
			activeChar.sendPacket(Msg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		activeChar.abortAttack(true, true);
		activeChar.abortCast(true);
		activeChar.sendActionFailed();
		activeChar.stopMove();

		int teleportTimer = Config.WEDDING_TELEPORT_INTERVAL * 1000;

		if(activeChar.getInventory().getAdena() < Config.WEDDING_TELEPORT_PRICE)
		{
			activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return false;
		}

		activeChar.reduceAdena(Config.WEDDING_TELEPORT_PRICE, true);

		activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Wedding.Teleport", activeChar).addNumber(teleportTimer / 60000));

		// SoE Animation section
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1050, 1, teleportTimer, 0));
		activeChar.sendPacket(new SetupGauge(0, teleportTimer));
		// End SoE Animation section

		// continue execution later
		activeChar._skillTask = ThreadPoolManager.getInstance().scheduleAi(new EscapeFinalizer(activeChar, partner.getLoc()), teleportTimer, true);
		return true;
	}

	static class EscapeFinalizer implements Runnable
	{
		private L2Player _activeChar;
		private Location _loc;

		EscapeFinalizer(L2Player activeChar, Location loc)
		{
			_activeChar = activeChar;
			_loc = loc;
		}

		public void run()
		{
			_activeChar.sendActionFailed();
			_activeChar.clearCastVars();
			if(_activeChar.isDead())
				return;
			_activeChar.teleToLocation(_loc);
		}
	}

	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}

	public void onLoad()
	{
		if(Config.ALLOW_WEDDING)
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}