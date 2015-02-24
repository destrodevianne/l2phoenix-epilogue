package l2p.gameserver.communitybbs.Manager;

import java.sql.ResultSet;
import java.util.StringTokenizer;

import l2p.Config;
import l2p.common.ThreadPoolManager;
import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.FiltredStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.extensions.multilang.CustomMessage;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.TownManager;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2Summon;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.entity.siege.Siege;
import l2p.gameserver.serverpackets.MagicSkillLaunched;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.ShowBoard;
import l2p.gameserver.skills.Env;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.tables.SkillTable;

public class BuffBBSManager extends BaseBBSManager
{
	private static BuffBBSManager _Instance = null;

	public static BuffBBSManager getInstance()
	{
		if(_Instance == null)
			_Instance = new BuffBBSManager();
		return _Instance;
	}

	BuffBBSManager()
	{
		Load();
	}

	/** Количество бафов в группах */
	private static int grpCount1, grpCount2, grpCount3, grpCount4, grpCount5;

	// Количество бафов в 1 и второй группах должно быть одинаковое
	private static int buffs[][] = { // id, lvl, group
	// Chants
			{ 1251, 2, 5 }, // Chant of Fury
			{ 1252, 3, 5 }, // Chant of Evasion
			{ 1253, 3, 5 }, // Chant of Rage
			{ 1284, 3, 5 }, // Chant of Revenge
			{ 1308, 3, 5 }, // Chant of Predator
			{ 1309, 3, 5 }, // Chant of Eagle
			{ 1310, 4, 5 }, // Chant of Vampire
			{ 1362, 1, 5 }, // Chant of Spirit
			{ 1363, 1, 5 }, // Chant of Victory
			{ 1390, 3, 5 }, // War Chant
			{ 1391, 3, 5 }, // Earth Chant
			// Songs
			{ 264, 1, 4 }, // Song of Earth
			{ 265, 1, 4 }, // Song of Life
			{ 266, 1, 4 }, // Song of Water
			{ 267, 1, 4 }, // Song of Warding
			{ 268, 1, 4 }, // Song of Wind
			{ 269, 1, 4 }, // Song of Hunter
			{ 270, 1, 4 }, // Song of Invocation
			{ 304, 1, 4 }, // Song of Vitality
			{ 305, 1, 4 }, // Song of Vengeance
			{ 306, 1, 4 }, // Song of Flame Guard
			{ 308, 1, 4 }, // Song of Storm Guard
			{ 349, 1, 4 }, // Song of Renewal
			{ 363, 1, 4 }, // Song of Meditation
			{ 364, 1, 4 }, // Song of Champion
			// Dances
			{ 271, 1, 3 }, // Dance of Warrior
			{ 272, 1, 3 }, // Dance of Inspiration
			{ 273, 1, 3 }, // Dance of Mystic
			{ 274, 1, 3 }, // Dance of Fire
			{ 275, 1, 3 }, // Dance of Fury
			{ 276, 1, 3 }, // Dance of Concentration
			{ 277, 1, 3 }, // Dance of Light
			{ 307, 1, 3 }, // Dance of Aqua Guard
			{ 309, 1, 3 }, // Dance of Earth Guard
			{ 310, 1, 3 }, // Dance of Vampire
			{ 311, 1, 3 }, // Dance of Protection
			{ 365, 1, 3 }, // Dance of Siren
			// Группа для магов 2
			{ 7059, 1, 2 }, // Wild Magic
			{ 4356, 3, 2 }, // Empower
			{ 4355, 3, 2 }, // Acumen
			{ 4352, 1, 2 }, // Berserker Spirit
			{ 4346, 4, 2 }, // Mental Shield
			{ 4351, 6, 2 }, // Concentration
			{ 4342, 2, 2 }, // Wind Walk
			{ 4347, 6, 2 }, // Bless the Body
			{ 4348, 6, 2 }, // Bless the Soul
			{ 4344, 3, 2 }, // Shield
			{ 7060, 1, 2 }, // Clarity
			{ 4350, 4, 2 }, // Resist Shock
			// Группа для воинов 1
			{ 7057, 1, 1 }, // Greater Might
			{ 4345, 3, 1 }, // Might
			{ 4344, 3, 1 }, // Shield
			{ 4349, 2, 1 }, // Magic Barrier
			{ 4342, 2, 1 }, // Wind Walk
			{ 4347, 6, 1 }, // Bless the Body
			{ 4357, 2, 1 }, // Haste
			{ 4359, 3, 1 }, // Focus
			{ 4358, 3, 1 }, // Guidance
			{ 4360, 3, 1 }, // Death Whisper
			{ 4354, 4, 1 }, // Vampiric Rage
			{ 4346, 4, 1 } // Mental Shield
	};

	public void Load()
	{
		for(int buff[] : buffs)
			switch(buff[2])
			{
				case 1:
					grpCount1++;
					break;
				case 2:
					grpCount2++;
					break;
				case 3:
					grpCount3++;
					break;
				case 4:
					grpCount4++;
					break;
				case 5:
					grpCount5++;
					break;
			}
	}

	@Override
	public void parsecmd(String command, L2Player player)
	{
		if(command.equals("_bbsbuff;"))
		{

		}
		else if(command.startsWith("_bbsbuff;buff;"))
		{
			StringTokenizer stBuff = new StringTokenizer(command, ";");
			stBuff.nextToken();
			stBuff.nextToken();
			int skill_id = Integer.parseInt(stBuff.nextToken());
			int skill_lvl = Integer.parseInt(stBuff.nextToken());
			String BuffTarget = stBuff.nextToken();

			doBuff(skill_id, skill_lvl, BuffTarget, player);

		}
		else if(command.startsWith("_bbsbuff;grp;"))
		{
			StringTokenizer stBuffGrp = new StringTokenizer(command, ";");
			stBuffGrp.nextToken();
			stBuffGrp.nextToken();
			int id_groups = Integer.parseInt(stBuffGrp.nextToken());
			String BuffTarget = stBuffGrp.nextToken();

			doBuffGroup(id_groups, BuffTarget, player);

		}
		else if(command.equals("_bbsbuff;cancel"))
			player.getEffectList().stopAllEffects();
		else if(command.equals("_bbsbuff;regmp"))
			player.setCurrentMp(player.getMaxMp());
		else if(command.equals("_bbsbuff;save"))
		{
			if(!Config.PVPCB_BUFFER_ALLOW_SAVE_RESTOR)
			{
				player.sendMessage(new CustomMessage("l2p.gameserver.communitybbs.Manager.BuffBBSManager.SaveBuff", player));
				return;
			}
			else
				SAVE(player);
		}
		else if(command.equals("_bbsbuff;restore"))
		{
			if(!Config.PVPCB_BUFFER_ALLOW_SAVE_RESTOR)
			{
				player.sendMessage(new CustomMessage("l2p.gameserver.communitybbs.Manager.BuffBBSManager.RestorBuff", player));
				return;
			}
			else
				RESTOR(player);
		}
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>В bbsbuff функция: " + command + " пока не реализована</center><br><br></body></html>", player);
	}

	public void doBuff(int skill_id, int skill_lvl, String BuffTarget, L2Player player)
	{

		L2Summon pet = player.getPet();

		if(!checkCondition(player))
			return;

		if(player.getAdena() < Config.PVPCB_BUFFER_PRICE_ONE)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		try
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skill_id, skill_lvl);

			if(BuffTarget.startsWith(" Player"))
				for(EffectTemplate et : skill.getEffectTemplates())
				{
					Env env = new Env(player, player, skill);
					L2Effect effect = et.getEffect(env);
					effect.setPeriod(Config.PVPCB_BUFFER_ALT_TIME);
					player.getEffectList().addEffect(effect);
				}
			if(BuffTarget.startsWith(" Pet"))
			{
				if(pet == null)
					return;

				for(EffectTemplate et : skill.getEffectTemplates())
				{
					Env env = new Env(pet, pet, skill);
					L2Effect effect = et.getEffect(env);
					effect.setPeriod(Config.PVPCB_BUFFER_ALT_TIME);
					pet.getEffectList().addEffect(effect);
				}
			}
			player.reduceAdena(Config.PVPCB_BUFFER_PRICE_ONE, false);
		}
		catch(Exception e)
		{
			player.sendMessage("Invalid skill!");
		}
	}

	public void doBuffGroup(int id_groups, String BuffTarget, L2Player player)
	{

		L2Summon pet = player.getPet();

		if(!checkCondition(player))
			return;

		if(player.getAdena() < Config.PVPCB_BUFFER_PRICE_ONE * Config.PVPCB_BUFFER_PRICE_MOD_GRP)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.reduceAdena(Config.PVPCB_BUFFER_PRICE_ONE * Config.PVPCB_BUFFER_PRICE_MOD_GRP, false);

		L2Skill skill;
		for(int buff[] : buffs)
			if(buff[2] == id_groups)
			{
				if(BuffTarget.startsWith(" Player"))
				{
					skill = SkillTable.getInstance().getInfo(buff[0], buff[1]);
					for(EffectTemplate et : skill.getEffectTemplates())
					{
						Env env = new Env(player, player, skill);
						L2Effect effect = et.getEffect(env);
						effect.setPeriod(Config.PVPCB_BUFFER_ALT_TIME);
						player.getEffectList().addEffect(effect);
					}

				}
				if(BuffTarget.startsWith(" Pet"))
				{
					if(pet == null)
						return;

					skill = SkillTable.getInstance().getInfo(buff[0], buff[1]);

					for(EffectTemplate et : skill.getEffectTemplates())
					{
						Env env = new Env(pet, pet, skill);
						L2Effect effect = et.getEffect(env);
						effect.setPeriod(Config.PVPCB_BUFFER_ALT_TIME);
						pet.getEffectList().addEffect(effect);
					}
				}
			}
	}

	private void SAVE(L2Player player)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(*) FROM community_skillsave WHERE charId=?;");
			statement.setInt(1, player.getObjectId());
			rs = statement.executeQuery();
			rs.next();
			String allbuff = "";

			L2Effect skill[] = player.getEffectList().getAllFirstEffects();
			for(int j = 0; j < skill.length; j++)
				allbuff = new StringBuilder().append(allbuff).append(skill[j].getSkill().getId() + ";").toString();

			if(rs.getInt(1) == 0)
			{
				statement = con.prepareStatement("INSERT INTO community_skillsave (charId,skills) values (?,?)");
				statement.setInt(1, player.getObjectId());
				statement.setString(2, allbuff);
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement("UPDATE community_skillsave SET skills=? WHERE charId=?;");
				statement.setString(1, allbuff);
				statement.setInt(2, player.getObjectId());
				statement.execute();
				statement.close();
			}
			rs.close();
			statement.close();
		}
		catch(Exception ignored)
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	private void RESTOR(L2Player player)
	{
		if(player.isInOlympiadMode())
		{
			player.sendMessage("Бафф закрыт!"); // TODO: сделать нормальную многоязычность
			return;
		}

		if(!checkCondition(player))
			return;

		if(player.getAdena() < Config.PVPCB_BUFFER_PRICE_ONE * Config.PVPCB_BUFFER_PRICE_MOD_GRP)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.reduceAdena(Config.PVPCB_BUFFER_PRICE_ONE * Config.PVPCB_BUFFER_PRICE_MOD_GRP, false);

		ThreadConnection con = null;
		FiltredStatement community_skillsave_statement = null;
		FiltredPreparedStatement communitybuff_statement = null;
		ResultSet community_skillsave_rs = null, communitybuff_rs = null;
		L2Skill skill;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			community_skillsave_statement = con.createStatement();
			// тут нам вообще не нужен PreparedStatement - у нас всего один раз исполняется запрос
			community_skillsave_rs = community_skillsave_statement.executeQuery("SELECT `charId`, `skills`, `pet` FROM `community_skillsave` WHERE `charId`='" + player.getObjectId() + "'");

			if(!community_skillsave_rs.next())
				return;

			String allskills = community_skillsave_rs.getString(2); // XXX: лучше использщовать обращение по именам столбцов во избежание ппутаницы
			StringTokenizer stBuff = new StringTokenizer(allskills, ";");
			while(stBuff.hasMoreTokens())
			{
				int skilltoresatore = Integer.parseInt(stBuff.nextToken());
				int skilllevel = SkillTable.getInstance().getBaseLevel(skilltoresatore);
				skill = SkillTable.getInstance().getInfo(skilltoresatore, skilllevel);

				if(communitybuff_statement == null) // инициируем только первую итерацию, а потом подставляем новые данные - на порядок быстрее 
					communitybuff_statement = con.prepareStatement("SELECT COUNT(*) FROM `communitybuff` WHERE `skillID`=?");

				communitybuff_statement.setInt(1, skilltoresatore);
				communitybuff_rs = communitybuff_statement.executeQuery();

				if(communitybuff_rs.next())
					if(communitybuff_rs.getInt(1) != 0)
						for(EffectTemplate et : skill.getEffectTemplates())
						{ // TODO: посмотреть как это сделано в l2p.gameserver.model.L2Player.restoreEffects()
							Env env = new Env(player, player, skill);
							L2Effect effect = et.getEffect(env);
							effect.setPeriod(Config.PVPCB_BUFFER_ALT_TIME);
							player.getEffectList().addEffect(effect);
						}
					else
						player.sendMessage("Бафф: " + skill.getName() + " (" + skill.getId() + "), не может быть восстановлен!"); // TODO: сделать нормальную многоязычность
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, community_skillsave_statement, community_skillsave_rs);
			DatabaseUtils.closeDatabaseSR(communitybuff_statement, communitybuff_rs);
		}
	}

	public boolean checkCondition(L2Player player)
	{
		if(player == null)
			return false;
		if(!Config.ALLOW_PVPCB_BUFFER)
		{
			player.sendMessage("Баффер отключен!");
			return false;
		}
		// Проверяем по уровню
		if(player.getLevel() > Config.PVPCB_BUFFER_MAX_LVL || player.getLevel() < Config.PVPCB_BUFFER_MIN_LVL)
		{
			player.sendMessage("Ваш уровень не отвечает требованиям!");
			return false;
		}

		//Можно ли юзать бафера во время осады?
		if(!Config.PVPCB_BUFFER_ALLOW_SIEGE)
		{
			Residence castle = TownManager.getInstance().getClosestTown(player).getCastle();
			Siege siege = castle.getSiege();
			if(siege != null)
			{
				player.sendMessage("Нельзя использовать бафф во время осады!");
				return false;
			}
		}
		return true;
	}

	public class BeginBuff implements Runnable
	{
		L2Character _buffer;
		L2Skill _skill;
		L2Player _target;

		public BeginBuff(L2Character buffer, L2Skill skill, L2Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			if(_target.isInOlympiadMode())
				return;
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), _skill.getHitTime(), 0));
			ThreadPoolManager.getInstance().scheduleGeneral(new EndBuff(_buffer, _skill, _target), _skill.getHitTime());
		}
	}

	public class EndBuff implements Runnable
	{
		L2Character _buffer;
		L2Skill _skill;
		L2Player _target;

		public EndBuff(L2Character buffer, L2Skill skill, L2Player target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target, _skill.isOffensive()));
		}
	}

	public class BeginPetBuff implements Runnable
	{
		L2Character _buffer;
		L2Skill _skill;
		L2Summon _target;

		public BeginPetBuff(L2Character buffer, L2Skill skill, L2Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			_buffer.broadcastPacket(new MagicSkillUse(_buffer, _target, _skill.getDisplayId(), _skill.getLevel(), _skill.getHitTime(), 0));
			ThreadPoolManager.getInstance().scheduleGeneral(new EndPetBuff(_buffer, _skill, _target), _skill.getHitTime());
		}
	}

	public class EndPetBuff implements Runnable
	{
		L2Character _buffer;
		L2Skill _skill;
		L2Summon _target;

		public EndPetBuff(L2Character buffer, L2Skill skill, L2Summon target)
		{
			_buffer = buffer;
			_skill = skill;
			_target = target;
		}

		public void run()
		{
			_skill.getEffects(_buffer, _target, false, false);
			_buffer.broadcastPacket(new MagicSkillLaunched(_buffer.getObjectId(), _skill.getId(), _skill.getLevel(), _target, _skill.isOffensive()));
		}
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{

	}
}