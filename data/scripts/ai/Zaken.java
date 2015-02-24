package ai;

import java.util.HashMap;

import javolution.util.FastMap;
import l2p.common.ThreadPoolManager;
import l2p.extensions.listeners.DayNightChangeListener;
import l2p.extensions.listeners.L2ZoneEnterLeaveListener;
import l2p.extensions.listeners.PropertyCollection;
import l2p.gameserver.GameTimeController;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2Playable;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2Spawn;
import l2p.gameserver.model.L2World;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.L2Zone.ZoneType;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.tables.NpcTable;
import l2p.util.GArray;
import l2p.util.Location;
import l2p.util.Rnd;

/**
 * Индивидуальное АИ эпик боса Zaken.<BR>
 * - имеет усиленный реген ночью<BR>
 * - получает 25% пенальти на реген в солнечной комнате (зона zaken_sunlight_room, id: 1335)<BR>
 * - спавнит охрану, в зависимости от количества HP
 * - каждые 25% HP телепортируется в случайную комнату
 * - иногда телепортирует выбранную цель в случайную комнату
 * - иногда телепортирует целую группу по разным комнатам
 * - после смерти проигрывает музыку<BR>
 * <BR>
 * @Author SYS & Diamond
 */
public class Zaken extends DefaultAI
{
	private static final int doll_blader_b = 29023;
	private static final int vale_master_b = 29024;
	private static final int pirates_zombie_captain_b = 29026;
	private static final int pirates_zombie_b = 29027;

	private static final Location[] locations = new Location[] { new Location(55272, 219112, -3496),
			new Location(56296, 218072, -3496), new Location(54232, 218072, -3496), new Location(54248, 220136, -3496),
			new Location(56296, 220136, -3496), new Location(55272, 219112, -3224), new Location(56296, 218072, -3224),
			new Location(54232, 218072, -3224), new Location(54248, 220136, -3224), new Location(56296, 220136, -3224),
			new Location(55272, 219112, -2952), new Location(56296, 218072, -2952), new Location(54232, 218072, -2952),
			new Location(54248, 220136, -2952), new Location(56296, 220136, -2952) };

	private GArray<L2NpcInstance> spawns = new GArray<L2NpcInstance>();

	private static final L2Zone _zone = ZoneManager.getInstance().getZoneById(ZoneType.no_restart, 1335, true);
	private ZoneListener _zoneListener = new ZoneListener();
	private NightInvulDayNightListener _timeListener = new NightInvulDayNightListener();

	private final float _baseHpReg;
	private final float _baseMpReg;
	private boolean _isInLightRoom = false;
	private int _stage = 0;
	private int _stage2 = 0;

	private final int ScatterEnemy = 4216;
	private final int MassTeleport = 4217;
	private final int InstantMove = 4222;
	private final int FaceChanceNightToDay = 4223;
	private final int FaceChanceDayToNight = 4224;

	private final L2Skill AbsorbHPMP;
	private final L2Skill Hold;
	private final L2Skill DeadlyDualSwordWeapon;
	private final L2Skill DeadlyDualSwordWeaponRangeAttack;

	public Zaken(L2Character actor)
	{
		super(actor);

		HashMap<Integer, L2Skill> skills = getActor().getTemplate().getSkills();

		AbsorbHPMP = skills.get(4218);
		Hold = skills.get(4219);
		DeadlyDualSwordWeapon = skills.get(4220);
		DeadlyDualSwordWeaponRangeAttack = skills.get(4221);

		_baseHpReg = actor.getTemplate().baseHpReg;
		_baseMpReg = actor.getTemplate().baseMpReg;
	}

	private class TeleportTask implements Runnable
	{
		L2Character _target;

		public TeleportTask(L2Character target)
		{
			_target = target;
		}

		public void run()
		{
			if(_target != null)
				_target.teleToLocation(getRndLoc());
			_target = null;
		}
	}

	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		L2Character target;
		if((target = prepareTarget()) == null)
			return false;

		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return false;

		teleportSelf();
		spawnMinions();

		int rnd_per = Rnd.get(100);

		if(rnd_per < 5)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, target, ScatterEnemy, 1, 2900, 0));
			ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(target), 2900);
			return true;
		}

		if(rnd_per < 8)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, target, MassTeleport, 1, 2900, 0));
			ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(target), 2900);
			for(L2Playable playable : L2World.getAroundPlayables(target, 200, 200))
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(playable), 2900);
			return true;
		}

		double distance = actor.getDistance(target);

		if(!actor.isAMuted() && rnd_per < 75)
			return chooseTaskAndTargets(null, target, distance);

		FastMap<L2Skill, Integer> d_skill = new FastMap<L2Skill, Integer>();

		addDesiredSkill(d_skill, target, distance, DeadlyDualSwordWeapon);
		addDesiredSkill(d_skill, target, distance, DeadlyDualSwordWeaponRangeAttack);
		addDesiredSkill(d_skill, target, distance, Hold);
		addDesiredSkill(d_skill, target, distance, AbsorbHPMP);

		L2Skill r_skill = selectTopSkill(d_skill);

		return chooseTaskAndTargets(r_skill, target, distance);
	}

	private void teleportSelf()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		double actor_hp_precent = actor.getCurrentHpPercents();
		switch(_stage2)
		{
			case 0:
				if(actor_hp_precent < 85)
				{
					actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, InstantMove, 1, 500, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(actor), 500);
					_stage2++;
				}
				break;
			case 1:
				if(actor_hp_precent < 50)
				{
					actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, InstantMove, 1, 500, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(actor), 500);
					_stage2++;
				}
				break;
			case 2:
				if(actor_hp_precent < 25)
				{
					actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, InstantMove, 1, 500, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(actor), 500);
					_stage2++;
				}
				break;
		}
	}

	private void spawnMinions()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		double actor_hp_precent = actor.getCurrentHpPercents();
		switch(_stage)
		{
			case 0:
				if(actor_hp_precent < 90)
				{
					addSpawn(pirates_zombie_captain_b, getRndLoc());
					_stage++;
				}
				break;
			case 1:
				if(actor_hp_precent < 80)
				{
					addSpawn(doll_blader_b, getRndLoc());
					_stage++;
				}
				break;
			case 2:
				if(actor_hp_precent < 70)
				{
					addSpawn(vale_master_b, getRndLoc());
					addSpawn(vale_master_b, getRndLoc());
					_stage++;
				}
				break;
			case 3:
				if(actor_hp_precent < 60)
				{
					addSpawn(pirates_zombie_b, getRndLoc());
					addSpawn(pirates_zombie_b, getRndLoc());
					addSpawn(pirates_zombie_b, getRndLoc());
					addSpawn(pirates_zombie_b, getRndLoc());
					addSpawn(pirates_zombie_b, getRndLoc());
					_stage++;
				}
				break;
			case 4:
				if(actor_hp_precent < 50)
				{
					addSpawn(doll_blader_b, new Location(52675, 219371, -3290, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(52687, 219596, -3368, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(52672, 219740, -3418, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(52857, 219992, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(52959, 219997, -3488, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(53381, 220151, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(54236, 220948, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(54885, 220144, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55264, 219860, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(55399, 220263, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55679, 220129, -3488, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(56276, 220783, -3488, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(57173, 220234, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(56267, 218826, -3488, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(56294, 219482, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(56094, 219113, -3488, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(56364, 218967, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(57113, 218079, -3488, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(56186, 217153, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55440, 218081, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(55202, 217940, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55225, 218236, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(54973, 218075, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(53412, 218077, -3488, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(54226, 218797, -3488, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(54394, 219067, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(54139, 219253, -3488, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(54262, 219480, -3488, Rnd.get(65536)));
					_stage++;
				}
				break;
			case 5:
				if(actor_hp_precent < 40)
				{
					addSpawn(pirates_zombie_b, new Location(53412, 218077, -3488, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(54413, 217132, -3488, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(54841, 217132, -3488, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(55372, 217128, -3343, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(55893, 217122, -3488, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(56282, 217237, -3216, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(56963, 218080, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(56267, 218826, -3216, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(56294, 219482, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(56094, 219113, -3216, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(56364, 218967, -3216, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(56276, 220783, -3216, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(57173, 220234, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(54885, 220144, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55264, 219860, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(55399, 220263, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55679, 220129, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(54236, 220948, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(54464, 219095, -3216, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(54226, 218797, -3216, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(54394, 219067, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(54139, 219253, -3216, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(54262, 219480, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(53412, 218077, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55440, 218081, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(55202, 217940, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55225, 218236, -3216, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(54973, 218075, -3216, Rnd.get(65536)));
					_stage++;
				}
				break;
			case 6:
				if(actor_hp_precent < 30)
				{
					addSpawn(pirates_zombie_b, new Location(54228, 217504, -3216, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(54181, 217168, -3216, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(54714, 217123, -3168, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(55298, 217127, -3073, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(55787, 217130, -2993, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(56284, 217216, -2944, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(56963, 218080, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(56267, 218826, -2944, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(56294, 219482, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(56094, 219113, -2944, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(56364, 218967, -2944, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(56276, 220783, -2944, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(57173, 220234, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(54885, 220144, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55264, 219860, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(55399, 220263, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55679, 220129, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(54236, 220948, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(54464, 219095, -2944, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(54226, 218797, -2944, Rnd.get(65536)));
					addSpawn(vale_master_b, new Location(54394, 219067, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(54139, 219253, -2944, Rnd.get(65536)));
					addSpawn(doll_blader_b, new Location(54262, 219480, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(53412, 218077, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(54280, 217200, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55440, 218081, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_captain_b, new Location(55202, 217940, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(55225, 218236, -2944, Rnd.get(65536)));
					addSpawn(pirates_zombie_b, new Location(54973, 218075, -2944, Rnd.get(65536)));
					_stage++;
				}
				break;
		}
	}

	private Location getRndLoc()
	{
		return locations[Rnd.get(locations.length)].rnd(0, 500, false).setH(Rnd.get(65536));
	}

	private void addSpawn(int id, Location loc)
	{
		L2NpcInstance actor = getActor();
		if(actor != null)
			try
			{
				L2Spawn sp = new L2Spawn(NpcTable.getTemplate(id));
				sp.setLoc(loc);
				sp.setReflection(actor.getReflection().getId());
				spawns.add(sp.doSpawn(true));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor != null)
			actor.broadcastPacket(new PlaySound(1, "BS02_D", 1, actor.getObjectId(), actor.getLoc()));
		super.onEvtDead(killer);
	}

	@Override
	public void startAITask()
	{
		if(_aiTask == null)
		{
			spawns.clear();
			GameTimeController.getInstance().getListenerEngine().addPropertyChangeListener(PropertyCollection.GameTimeControllerDayNightChange, _timeListener);
			_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
			_stage = 0;
			_stage2 = 0;
		}
		super.startAITask();
	}

	@Override
	public void stopAITask()
	{
		if(_aiTask != null)
		{
			spawns.clear();
			GameTimeController.getInstance().getListenerEngine().removePropertyChangeListener(PropertyCollection.GameTimeControllerDayNightChange, _timeListener);
			_zone.getListenerEngine().removeMethodInvokedListener(_zoneListener);
		}
		super.stopAITask();
	}

	private class NightInvulDayNightListener extends DayNightChangeListener
	{
		private NightInvulDayNightListener()
		{
			if(GameTimeController.getInstance().isNowNight())
				switchToNight();
			else
				switchToDay();
		}

		/**
		 * Вызывается, когда на сервере наступает ночь
		 */
		@Override
		public void switchToNight()
		{
			L2NpcInstance actor = getActor();
			if(actor != null)
			{
				if(_isInLightRoom)
				{
					actor.getTemplate().baseHpReg = (float) (_baseHpReg * 7.5);
					actor.getTemplate().baseMpReg = (float) (_baseMpReg * 7.5);
				}
				else
				{
					actor.getTemplate().baseHpReg = (float) (_baseHpReg * 10.);
					actor.getTemplate().baseMpReg = (float) (_baseMpReg * 10.);
				}
				actor.broadcastPacket(new MagicSkillUse(actor, actor, FaceChanceDayToNight, 1, 1100, 0));
			}
		}

		/**
		 * Вызывается, когда на сервере наступает день
		 */
		@Override
		public void switchToDay()
		{
			L2NpcInstance actor = getActor();
			if(actor != null)
			{
				actor.getTemplate().baseHpReg = _baseHpReg;
				actor.getTemplate().baseMpReg = _baseMpReg;
				actor.broadcastPacket(new MagicSkillUse(actor, actor, FaceChanceNightToDay, 1, 1100, 0));
			}
		}
	}

	private class ZoneListener extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			L2NpcInstance actor = getActor();
			if(actor == null)
				return;
			actor.getTemplate().baseHpReg = (float) (_baseHpReg * 0.75);
			actor.getTemplate().baseMpReg = (float) (_baseMpReg * 0.75);
			_isInLightRoom = true;
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			L2NpcInstance actor = getActor();
			if(actor == null)
				return;
			actor.getTemplate().baseHpReg = _baseHpReg;
			actor.getTemplate().baseMpReg = _baseMpReg;
			_isInLightRoom = false;
		}
	}
}