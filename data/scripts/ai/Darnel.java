package ai;

import java.util.HashMap;

import javolution.util.FastMap;
import l2p.common.ThreadPoolManager;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.instances.L2TrapInstance;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.tables.NpcTable;
import l2p.gameserver.tables.SkillTable;
import l2p.util.Location;
import l2p.util.Rnd;

/**
 * @author Diamond
 */
public class Darnel extends DefaultAI
{
	private class TrapTask implements Runnable
	{
		public void run()
		{
			L2NpcInstance actor = getActor();
			if(actor == null || actor.isDead())
				return;

			// Спавним 10 ловушек
			for(int i = 0; i < 10; i++)
				new L2TrapInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(13037), actor, trapSkills[Rnd.get(trapSkills.length)], new Location(Rnd.get(151896, 153608), Rnd.get(145032, 146808), -12584));
		}
	}

	final L2Skill[] trapSkills = new L2Skill[] { SkillTable.getInstance().getInfo(5267, 1),
			SkillTable.getInstance().getInfo(5268, 1), SkillTable.getInstance().getInfo(5269, 1),
			SkillTable.getInstance().getInfo(5270, 1) };

	final L2Skill Poison;
	final L2Skill Paralysis;

	public Darnel(L2Character actor)
	{
		super(actor);

		HashMap<Integer, L2Skill> skills = getActor().getTemplate().getSkills();

		Poison = skills.get(4182);
		Paralysis = skills.get(4189);
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

		int rnd_per = Rnd.get(100);

		if(rnd_per < 5)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 5440, 1, 3000, 0));
			ThreadPoolManager.getInstance().scheduleGeneral(new TrapTask(), 3000);
			return true;
		}

		double distance = actor.getDistance(target);

		if(!actor.isAMuted() && rnd_per < 75)
			return chooseTaskAndTargets(null, target, distance);

		FastMap<L2Skill, Integer> d_skill = new FastMap<L2Skill, Integer>();

		addDesiredSkill(d_skill, target, distance, Poison);
		addDesiredSkill(d_skill, target, distance, Paralysis);

		L2Skill r_skill = selectTopSkill(d_skill);

		return chooseTaskAndTargets(r_skill, target, distance);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}