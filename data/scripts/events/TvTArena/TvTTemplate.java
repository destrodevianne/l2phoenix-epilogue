package events.TvTArena;

import l2p.common.ThreadPoolManager;
import l2p.extensions.listeners.L2ZoneEnterLeaveListener;
import l2p.extensions.scripts.Functions;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.Announcements;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.clientpackets.Say2C;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Playable;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2Summon;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.serverpackets.Revive;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.tables.SkillTable;
import l2p.util.GArray;
import l2p.util.GCSArray;
import l2p.util.Location;
import l2p.util.Rnd;
import l2p.util.Util;

public abstract class TvTTemplate extends Functions implements ScriptFile
{
	private static int ITEM_ID = 4357;
	private static String ITEM_NAME = "Silver Shilen";
	private static int LENGTH_TEAM = 12;
	private static boolean ALLOW_BUFFS = false;
	private static boolean ALLOW_CLAN_SKILL = true;
	private static boolean ALLOW_HERO_SKILL = false;

	/*
	Config.EVENT_TVT_ARENA_ITEM_ID;
	Config.EVENT_TVT_ARENA_ITEM_NAME;
	Config.EVENT_TVT_ARENA_MAX_LENGTH_TEAM;
	Config.EVENT_TVT_ARENA_ALLOW_BUFFS;
	Config.EVENT_TVT_ARENA_ALLOW_CLAN_SKILL;
	Config.EVENT_TVT_ARENA_ALLOW_HERO_SKILL;
	*/

	protected int _managerId;
	protected String _className;

	protected Long _creatorId;
	protected L2NpcInstance _manager;
	protected int _status = 0;
	protected int _CharacterFound = 0;
	protected int _price = 10000;
	protected int _team1count = 1;
	protected int _team2count = 1;
	protected int _team1min = 1;
	protected int _team1max = 85;
	protected int _team2min = 1;
	protected int _team2max = 85;
	protected int _timeToStart = 10;
	protected boolean _timeOutTask;

	protected GArray<Location> _team1points;
	protected GArray<Location> _team2points;

	protected GCSArray<Long> _team1list = new GCSArray<Long>();
	protected GCSArray<Long> _team2list = new GCSArray<Long>();
	protected GCSArray<Long> _team1live = new GCSArray<Long>();
	protected GCSArray<Long> _team2live = new GCSArray<Long>();

	protected L2Zone _zone;
	protected ZoneListener _zoneListener;

	public void template_stop()
	{
		if(_status <= 0)
			return;

		sayToAll("Бой прерван по техническим причинам, ставки возвращены");

		unParalyzeTeams();
		ressurectPlayers();
		returnItemToTeams();
		healPlayers();
		removeBuff();
		teleportPlayersToSavedCoords();
		clearTeams();
		_status = 0;
		_timeOutTask = false;
	}

	public void template_create1(L2Player player)
	{
		if(_status > 0)
		{
			show("Дождитесь окончания боя", player);
			return;
		}

		if(player.getTeam() == 1 || player.getTeam() == 2)
		{
			show("Вы уже зарегистрированы", player);
			return;
		}
		show("data/scripts/events/TvTArena/" + _managerId + "-1.html", player);
	}

	public void template_register(L2Player player)
	{
		if(_status == 0)
		{
			show("Бой на данный момент не создан", player);
			return;
		}

		if(_status > 1)
		{
			show("Дождитесь окончания боя", player);
			return;
		}

		if(player.getTeam() == 1 || player.getTeam() == 2)
		{
			show("Вы уже зарегистрированы", player);
			return;
		}
		show("data/scripts/events/TvTArena/" + _managerId + "-3.html", player);
	}

	public void template_check1(L2Player player, L2NpcInstance manager, String[] var)
	{
		if(var.length != 8)
		{
			show("Некорректные данные", player);
			return;
		}

		if(_status > 0)
		{
			show("Дождитесь окончания боя", player);
			return;
		}
		if(manager == null || !manager.isNpc())
		{
			show("Hacker? :) " + manager, player);
			return;
		}
		_manager = manager;
		try
		{
			_price = Integer.valueOf(var[0]);
			_team1count = Integer.valueOf(var[1]);
			_team2count = Integer.valueOf(var[2]);
			_team1min = Integer.valueOf(var[3]);
			_team1max = Integer.valueOf(var[4]);
			_team2min = Integer.valueOf(var[5]);
			_team2max = Integer.valueOf(var[6]);
			_timeToStart = Integer.valueOf(var[7]);
		}
		catch(Exception e)
		{
			show("Некорректные данные", player);
			return;
		}
		if(_price < 1 || _price > 500)
		{
			show("Неправильная ставка", player);
			return;
		}
		if(_team1count < 1 || _team1count > LENGTH_TEAM || _team2count < 1 || _team2count > LENGTH_TEAM)
		{
			show("Неправильный размер команды", player);
			return;
		}
		if(_team1min < 1 || _team1min > 86 || _team2min < 1 || _team2min > 86 || _team1max < 1 || _team1max > 86 || _team2max < 1 || _team2max > 86 || _team1min > _team1max || _team2min > _team2max)
		{
			show("Неправильный уровень", player);
			return;
		}
		if(player.getLevel() < _team1min || player.getLevel() > _team1max)
		{
			show("Неправильный уровень", player);
			return;
		}
		if(_timeToStart < 1 || _timeToStart > 10)
		{
			show("Неправильное время", player);
			return;
		}
		if(getItemCount(player, ITEM_ID) < _price)
		{
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}
		_creatorId = player.getStoredId();
		removeItem(player, ITEM_ID, _price);
		player.setTeam(1, true);
		_status = 1;
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
		_team1list.add(player.getStoredId());
		sayToAll(player.getName() + " создал бой " + _team1count + "х" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, ставка " + _price + " " + ITEM_NAME + ", начало через " + _timeToStart + " мин");
		executeTask("events.TvTArena." + _className, "announce", new Object[0], 60000);
	}

	public void template_register_check(L2Player player)
	{
		if(_status == 0)
		{
			show("Бой на данный момент не создан", player);
			return;
		}

		if(_status > 1)
		{
			show("Дождитесь окончания боя", player);
			return;
		}

		if(_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId()))
		{
			show("Вы уже зарегистрированы", player);
			return;
		}

		if(player.getTeam() == 1 || player.getTeam() == 2)
		{
			show("Вы уже зарегистрированы", player);
			return;
		}

		if(getItemCount(player, ITEM_ID) < _price)
		{
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}

		int size1 = _team1list.size(), size2 = _team2list.size();

		if(size1 > size2)
		{
			String t = null;
			if(tryRegister(2, player) != null)
				if((t = tryRegister(1, player)) != null)
					show(t, player);
		}
		else if(size1 < size2)
		{
			String t = null;
			if(tryRegister(1, player) != null)
				if((t = tryRegister(2, player)) != null)
					show(t, player);
		}
		else
		{
			int team = Rnd.get(1, 2);
			String t = null;
			if(tryRegister(team, player) != null)
				if((t = tryRegister(team == 1 ? 2 : 1, player)) != null)
					show(t, player);
		}
	}

	private String tryRegister(int team, L2Player player)
	{
		if(team == 1)
		{
			if(player.getLevel() < _team1min || player.getLevel() > _team1max)
				return "Вы не подходите по уровню";
			if(_team1list.size() >= _team1count)
				return "Команда 1 переполнена";
			doRegister(1, player);
			return null;
		}
		if(player.getLevel() < _team2min || player.getLevel() > _team2max)
			return "Вы не подходите по уровню";
		if(_team2list.size() >= _team2count)
			return "Команда 2 переполнена";
		doRegister(2, player);
		return null;
	}

	private void doRegister(int team, L2Player player)
	{
		removeItem(player, ITEM_ID, _price);

		if(team == 1)
		{
			_team1list.add(player.getStoredId());
			player.setTeam(1, true);
			sayToAll(player.getName() + " зарегистрировался за 1 команду");
		}
		else
		{
			_team2list.add(player.getStoredId());
			player.setTeam(2, true);
			sayToAll(player.getName() + " зарегистрировался за 2 команду");
		}

		if(_team1list.size() >= _team1count && _team2list.size() >= _team2count)
		{
			sayToAll("Команды готовы, старт через 1 минуту.");
			_timeToStart = 1;
		}
	}

	public void template_announce()
	{
		L2Player creator = L2ObjectsStorage.getAsPlayer(_creatorId);

		if(_status != 1 || creator == null)
			return;

		if(_timeToStart > 1)
		{
			_timeToStart--;
			sayToAll(creator.getName() + " создал бой " + _team1count + "х" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, ставка " + _price + " " + ITEM_NAME + ", начало через " + _timeToStart + " мин");
			executeTask("events.TvTArena." + _className, "announce", new Object[0], 60000);
		}
		else if(_team2list.size() > 0)
		{
			sayToAll("Подготовка к бою");
			executeTask("events.TvTArena." + _className, "prepare", new Object[0], 5000);
		}
		else
		{
			sayToAll("Бой не состоялся, нет противников");
			_status = 0;
			returnItemToTeams();
			clearTeams();
		}
	}

	public void template_prepare()
	{
		if(_status != 1)
			return;

		_status = 2;
		for(L2Player player : getPlayers(_team1list))
			if(!player.isDead())
				_team1live.add(player.getStoredId());
		for(L2Player player : getPlayers(_team2list))
			if(!player.isDead())
				_team2live.add(player.getStoredId());
		if(!checkTeams())
			return;
		saveBackCoords();
		clearArena();
		ressurectPlayers();
		removeBuff();
		healPlayers();
		paralyzeTeams();
		teleportTeamsToArena();
		sayToAll("Бой начнется через 30 секунд");
		executeTask("events.TvTArena." + _className, "start", new Object[0], 30000);
	}

	public void template_start()
	{
		if(_status != 2)
			return;

		if(!checkTeams())
			return;
		sayToAll("Go!!!");
		unParalyzeTeams();
		_status = 3;
		executeTask("events.TvTArena." + _className, "timeOut", new Object[0], 180000);
		_timeOutTask = true;
	}

	public void clearArena()
	{
		for(L2Object obj : _zone.getObjects())
			if(obj != null && obj.isPlayable())
				((L2Playable) obj).teleToLocation(_zone.getSpawn());
	}

	public boolean checkTeams()
	{
		if(_team1live.isEmpty())
		{
			teamHasLost(1);
			return false;
		}
		else if(_team2live.isEmpty())
		{
			teamHasLost(2);
			return false;
		}
		return true;
	}

	public void saveBackCoords()
	{
		for(L2Player player : getPlayers(_team1list))
			player.setVar("TvTArena_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
		for(L2Player player : getPlayers(_team2list))
			player.setVar("TvTArena_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
	}

	public void teleportPlayersToSavedCoords()
	{
		for(L2Player player : getPlayers(_team1list))
			try
			{
				String var = player.getVar("TvTArena_backCoords");
				if(var == null || var.equals(""))
					continue;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					continue;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("TvTArena_backCoords");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		for(L2Player player : getPlayers(_team2list))
			try
			{
				String var = player.getVar("TvTArena_backCoords");
				if(var == null || var.equals(""))
					continue;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					continue;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("TvTArena_backCoords");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	public void healPlayers()
	{
		for(L2Player player : getPlayers(_team1list))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
		for(L2Player player : getPlayers(_team2list))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public void ressurectPlayers()
	{
		for(L2Player player : getPlayers(_team1list))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
		for(L2Player player : getPlayers(_team2list))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
	}

	public void removeBuff()
	{
		for(L2Player player : getPlayers(_team1list))
			if(player != null)
				try
				{
					if(player.isCastingNow())
						player.abortCast(true);

					if(!ALLOW_CLAN_SKILL)
						if(player.getClan() != null)
							for(L2Skill skill : player.getClan().getAllSkills())
								player.removeSkill(skill, false);

					if(!ALLOW_HERO_SKILL)
						if(player.isHero())
							Hero.removeSkills(player);

					if(!ALLOW_BUFFS)
					{
						player.getEffectList().stopAllEffects();

						if(player.getPet() != null)
						{
							L2Summon summon = player.getPet();
							summon.getEffectList().stopAllEffects();
							if(summon.isPet())
								summon.unSummon();
						}

						if(player.getAgathion() != null)
							player.setAgathion(0);
					}

					player.sendPacket(new SkillList(player));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

		for(L2Player player : getPlayers(_team2list))
			if(player != null)
				try
				{
					if(player.isCastingNow())
						player.abortCast(true);

					if(!ALLOW_CLAN_SKILL)
						if(player.getClan() != null)
							for(L2Skill skill : player.getClan().getAllSkills())
								player.removeSkill(skill, false);

					if(!ALLOW_HERO_SKILL)
						if(player.isHero())
							Hero.removeSkills(player);

					if(!ALLOW_BUFFS)
					{
						player.getEffectList().stopAllEffects();

						if(player.getPet() != null)
						{
							L2Summon summon = player.getPet();
							summon.getEffectList().stopAllEffects();
							if(summon.isPet())
								summon.unSummon();
						}

						if(player.getAgathion() != null)
							player.setAgathion(0);
					}

					player.sendPacket(new SkillList(player));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
	}

	public void backBuff()
	{
		for(L2Player player : getPlayers(_team1list))
		{
			if(player == null)
				continue;
			try
			{
				player.getEffectList().stopAllEffects();

				if(!ALLOW_CLAN_SKILL)
					if(player.getClan() != null)
						for(L2Skill skill : player.getClan().getAllSkills())
							if(skill.getMinPledgeClass() <= player.getPledgeClass())
								player.addSkill(skill, false);

				if(!ALLOW_HERO_SKILL)
					if(player.isHero())
						Hero.addSkills(player);

				player.sendPacket(new SkillList(player));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		for(L2Player player : getPlayers(_team2list))
		{
			if(player == null)
				continue;
			try
			{
				player.getEffectList().stopAllEffects();

				if(!ALLOW_CLAN_SKILL)
					if(player.getClan() != null)
						for(L2Skill skill : player.getClan().getAllSkills())
							if(skill.getMinPledgeClass() <= player.getPledgeClass())
								player.addSkill(skill, false);

				if(!ALLOW_HERO_SKILL)
					if(player.isHero())
						Hero.addSkills(player);

				player.sendPacket(new SkillList(player));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void paralyzeTeams()
	{
		L2Skill revengeSkill = SkillTable.getInstance().getInfo(L2Skill.SKILL_RAID_CURSE, 1);
		for(L2Player player : getPlayers(_team1list))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
		for(L2Player player : getPlayers(_team2list))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
	}

	public void unParalyzeTeams()
	{
		for(L2Player player : getPlayers(_team1list))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);

			if(player.getParty() != null)
				player.getParty().oustPartyMember(player);
		}
		for(L2Player player : getPlayers(_team2list))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);

			if(player.getParty() != null)
				player.getParty().oustPartyMember(player);
		}
	}

	public void teleportTeamsToArena()
	{
		Integer n = 0;
		for(L2Player player : getPlayers(_team1live))
		{
			unRide(player);
			unSummonPet(player, true);
			player.teleToLocation(_team1points.get(n));
			n++;
		}
		n = 0;
		for(L2Player player : getPlayers(_team2live))
		{
			unRide(player);
			unSummonPet(player, true);
			player.teleToLocation(_team2points.get(n));
			n++;
		}
	}

	public boolean playerHasLost(L2Player player)
	{
		if(player.getTeam() == 1)
			_team1live.remove(player.getStoredId());
		else
			_team2live.remove(player.getStoredId());
		L2Skill revengeSkill = SkillTable.getInstance().getInfo(L2Skill.SKILL_RAID_CURSE, 1);
		revengeSkill.getEffects(player, player, false, false);
		return !checkTeams();
	}

	public void teamHasLost(Integer team_id)
	{
		if(team_id == 1)
		{
			sayToAll("Команда 2 победила");
			payItemToTeam(2);
		}
		else
		{
			sayToAll("Команда 1 победила");
			payItemToTeam(1);
		}
		unParalyzeTeams();
		backBuff();
		teleportPlayersToSavedCoords();
		ressurectPlayers();
		healPlayers();
		clearTeams();
		_status = 0;
		_timeOutTask = false;
	}

	public void template_timeOut()
	{
		if(_timeOutTask && _status == 3)
		{
			sayToAll("Время истекло, ничья!");
			returnItemToTeams();
			unParalyzeTeams();
			backBuff();
			teleportPlayersToSavedCoords();
			ressurectPlayers();
			healPlayers();
			clearTeams();
			_status = 0;
			_timeOutTask = false;
		}
	}

	public void payItemToTeam(Integer team_id)
	{
		if(team_id == 1)
			for(L2Player player : getPlayers(_team1list))
				addItem(player, ITEM_ID, _price + _team2list.size() * _price / _team1list.size());
		else
			for(L2Player player : getPlayers(_team2list))
				addItem(player, ITEM_ID, _price + _team2list.size() * _price / _team1list.size());
	}

	public void returnItemToTeams()
	{
		for(L2Player player : getPlayers(_team1list))
			addItem(player, ITEM_ID, _price);
		for(L2Player player : getPlayers(_team2list))
			addItem(player, ITEM_ID, _price);
	}

	public void clearTeams()
	{
		for(L2Player player : getPlayers(_team1list))
			player.setTeam(0, true);
		for(L2Player player : getPlayers(_team2list))
			player.setTeam(0, true);
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
	}

	public void onDie(L2Character self, L2Character killer)
	{
		if(_status >= 2 && self != null && self.isPlayer() && self.getTeam() > 0 && (_team1list.contains(self.getStoredId()) || _team2list.contains(self.getStoredId())))
		{
			L2Player player = self.getPlayer();
			L2Player kplayer = killer.getPlayer();
			if(kplayer != null)
			{
				sayToAll(kplayer.getName() + " убил " + player.getName());
				if(player.getTeam() == kplayer.getTeam() || !_team1list.contains(kplayer.getStoredId()) && !_team2list.contains(kplayer.getStoredId()))
				{
					sayToAll("Нарушение правил, игрок " + kplayer.getName() + " оштрафован на " + _price + " " + ITEM_NAME);
					removeItem(kplayer, ITEM_ID, _price);
				}
				playerHasLost(player);
			}
			else
			{
				sayToAll(player.getName() + " убит");
				playerHasLost(player);
			}
		}
	}

	/**
	 * Это не хендлер, хендлеры находятся в наследниках 
	 */
	public void onPlayerExit(L2Player player)
	{
		if(player != null && _status > 0 && player.getTeam() > 0 && (_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
			switch(_status)
			{
				case 1:
					removePlayer(player);
					sayToAll(player.getName() + " дисквалифицирован");
					if(player.getStoredId() == _creatorId)
					{
						sayToAll("Бой прерван, ставки возвращены");

						returnItemToTeams();
						backBuff();
						teleportPlayersToSavedCoords();
						unParalyzeTeams();
						ressurectPlayers();
						healPlayers();
						clearTeams();

						unParalyzeTeams();
						clearTeams();
						_status = 0;
						_timeOutTask = false;
					}
					break;
				case 2:
					removePlayer(player);
					sayToAll(player.getName() + " дисквалифицирован");
					checkTeams();
					break;
				case 3:
					removePlayer(player);
					sayToAll(player.getName() + " дисквалифицирован");
					checkTeams();
					break;
			}
	}

	/**
	 * Это не хендлер, хендлеры находятся в наследниках 
	 */
	public Location onEscape(L2Player player)
	{
		if(player != null && _status > 1 && player.getTeam() > 0 && player.isInZone(_zone))
			onPlayerExit(player);
		return null;
	}

	public class ZoneListener extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			L2Player player = object.getPlayer();
			if(_status >= 2 && player != null && !(_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, _zone.getSpawn()), 3000);
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			L2Player player = object.getPlayer();
			if(_status >= 2 && player != null && player.getTeam() > 0 && (_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
			{
				double angle = Util.convertHeadingToDegree(object.getHeading()); // угол в градусах
				double radian = Math.toRadians(angle - 90); // угол в радианах
				int x = (int) (object.getX() + 50 * Math.sin(radian));
				int y = (int) (object.getY() - 50 * Math.cos(radian));
				int z = object.getZ();
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, new Location(x, y, z)), 3000);
			}
		}
	}

	public class TeleportTask implements Runnable
	{
		Location loc;
		L2Character target;

		public TeleportTask(L2Character target, Location loc)
		{
			this.target = target;
			this.loc = loc;
			target.startStunning();
		}

		public void run()
		{
			target.stopStunning();
			target.teleToLocation(loc);
		}
	}

	private void removePlayer(L2Player player)
	{
		if(player != null)
		{
			_team1list.remove(player.getStoredId());
			_team2list.remove(player.getStoredId());
			_team1live.remove(player.getStoredId());
			_team2live.remove(player.getStoredId());
			player.setTeam(0, true);
		}
	}

	private GArray<L2Player> getPlayers(GCSArray<Long> list)
	{
		GArray<L2Player> result = new GArray<L2Player>();
		for(Long storeId : list)
		{
			L2Player player = L2ObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				result.add(player);
		}
		return result;
	}

	public void sayToAll(String text)
	{
		Announcements.getInstance().announceToAll(_manager.getName() + ": " + text, Say2C.CRITICAL_ANNOUNCEMENT);
	}
}