package services;

import l2p.extensions.multilang.CustomMessage;
import l2p.extensions.scripts.Functions;
import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.tables.DoorTable;
import l2p.util.GArray;
import l2p.util.Location;
import l2p.util.Util;

/**
 * Используется для Hellbound
 */
public class Caravan extends Functions implements ScriptFile
{
	public void onLoad()
	{
		System.out.println("Loaded Service: Caravan");

		// Двери к Kief и Buron - всегда открыты
		DoorTable.getInstance().getDoor(19250001).openMe();
		DoorTable.getInstance().getDoor(19250002).openMe();

		long confidence = getConfidence();
		// Дверь к химерам
		if(confidence > 300000)
			DoorTable.getInstance().getDoor(20250002).openMe();
		// Дверь к цитадели
		if(confidence > 600000)
			DoorTable.getInstance().getDoor(20250001).openMe();
		// Двери в лабиринте
		if(confidence > 800000)
		{
			DoorTable.getInstance().getDoor(20260003).openMe();
			DoorTable.getInstance().getDoor(20260004).openMe();
		}
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public static final int NativeTransformationSkill = 3359;
	private static final int FieryDemonBloodSkill = 2357;
	private static Location TowerofInfinitumLocationPoint = new Location(-22204, 277056, -15045);
	private static Location TullyEntranceLocationPoint = new Location(17947, 283205, -9696);
	private static Location TullyFloor1LocationPoint = new Location(-13400, 272827, -15304);

	public static final int FirstMark = 9850;
	public static final int SecondMark = 9851;
	public static final int ThirdMark = 9852;
	public static final int ForthMark = 9853;

	public static final int NativeHelmet = 9669;
	public static final int NativeTunic = 9670;
	public static final int NativePants = 9671;
	public static final int MagicBottle = 9672;
	public static final int HolyWater = 9673;
	public static final int DarionsBadge = 9674;
	public static final int MarkOfBetrayal = 9676;
	public static final int LifeForce = 9681;
	public static final int ContainedLifeForce = 9682;
	public static final int ScorpionPoisonStinger = 10012;
	public static final int MapofHellbound = 9994;

	public void getFirstMark()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();

		if(getConfidence() < 300000) // слишком низкий уровень доверия
		{
			n.onBypassFeedback(p, "Chat 2");
			return;
		}

		if(getItemCount(p, FirstMark) > 0 || getItemCount(p, SecondMark) > 0 || getItemCount(p, ThirdMark) > 0 || getItemCount(p, ForthMark) > 0) // уже есть какая-то
		{
			n.onBypassFeedback(p, "Chat 3");
			return;
		}

		if(getItemCount(p, DarionsBadge) >= 20)
		{
			removeItem(p, DarionsBadge, 20); // Darion's Badge
			addItem(p, FirstMark, 1); // Basic Caravan Certificate
		}
		else
			n.onBypassFeedback(p, "Chat 2");
	}

	public void getSecondMark()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();

		if(getConfidence() < 600000) // слишком низкий уровень доверия
		{
			n.onBypassFeedback(p, "Chat 3");
			return;
		}

		if(getItemCount(p, FirstMark) == 0) // нет первой марки
		{
			n.onBypassFeedback(p, "Chat 1");
			return;
		}

		if(getItemCount(p, SecondMark) > 0 || getItemCount(p, ThirdMark) > 0 || getItemCount(p, ForthMark) > 0) // есть вторая или выше
		{
			n.onBypassFeedback(p, "Chat 2");
			return;
		}

		if(getItemCount(p, MarkOfBetrayal) >= 30 && getItemCount(p, ScorpionPoisonStinger) >= 60)
		{
			removeItem(p, MarkOfBetrayal, 30); // Mark of Betrayal
			removeItem(p, ScorpionPoisonStinger, 60); // Scorpion Poison Stingers
			removeItem(p, FirstMark, 1); // Basic Caravan Certificate
			addItem(p, SecondMark, 1); // Standard Caravan Certificate
		}
		else
			n.onBypassFeedback(p, "Chat 3");
	}

	public void getThirdMark()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();

		if(getConfidence() < 1000000) // слишком низкий уровень доверия
		{
			n.onBypassFeedback(p, "Chat 4");
			return;
		}

		if(getItemCount(p, SecondMark) == 0) // нет второй марки
		{
			n.onBypassFeedback(p, "Chat 1");
			return;
		}

		if(getItemCount(p, ThirdMark) > 0 || getItemCount(p, ForthMark) > 0) // есть третья или выше
		{
			n.onBypassFeedback(p, "Chat 2");
			return;
		}

		if(getItemCount(p, LifeForce) >= 56 && getItemCount(p, ContainedLifeForce) >= 14)
		{
			removeItem(p, LifeForce, 56); // Life Force
			removeItem(p, ContainedLifeForce, 14); // Contained Life Force
			removeItem(p, SecondMark, 1); // Standard Caravan Certificate
			addItem(p, ThirdMark, 1); // Premium Caravan Certificate
			addItem(p, MapofHellbound, 1); // Map of Hellbound
		}
		else
			n.onBypassFeedback(p, "Chat 4");
	}

	public void tradeAntiHeatBottle()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();
		if(getItemCount(p, SecondMark) == 0 && getItemCount(p, ThirdMark) == 0 && getItemCount(p, ForthMark) == 0) // нет второй или выше марки
		{
			n.onBypassFeedback(p, "Chat 1");
			return;
		}

		n.onBypassFeedback(p, "Multisell 250980014");
	}

	public void tradeS80()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();
		if(getItemCount(p, ThirdMark) == 0 && getItemCount(p, ForthMark) == 0) // нет третьей или выше марки
		{
			n.onBypassFeedback(p, "Chat 1");
			return;
		}

		n.onBypassFeedback(p, "Multisell 250980013");
	}

	public void craftNativeHelmet()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();
		if(getItemCount(p, FirstMark) == 0 && getItemCount(p, SecondMark) == 0 && getItemCount(p, ThirdMark) == 0 && getItemCount(p, ForthMark) == 0) // нет марки
		{
			n.onBypassFeedback(p, "Chat 2");
			return;
		}

		if(getItemCount(p, DarionsBadge) >= 10)
		{
			removeItem(p, DarionsBadge, 10); // Darion's Badge
			addItem(p, NativeHelmet, 1); // Native Helmet
		}
		else
			n.onBypassFeedback(p, "Chat 3");
	}

	public void craftNativeTunic()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();
		if(getItemCount(p, FirstMark) == 0 && getItemCount(p, SecondMark) == 0 && getItemCount(p, ThirdMark) == 0 && getItemCount(p, ForthMark) == 0) // нет марки
		{
			n.onBypassFeedback(p, "Chat 2");
			return;
		}

		if(getItemCount(p, DarionsBadge) >= 10)
		{
			removeItem(p, DarionsBadge, 10); // Darion's Badge
			addItem(p, NativeTunic, 1); // Native Tunic
		}
		else
			n.onBypassFeedback(p, "Chat 3");
	}

	public void craftNativePants()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();
		if(getItemCount(p, FirstMark) == 0 && getItemCount(p, SecondMark) == 0 && getItemCount(p, ThirdMark) == 0 && getItemCount(p, ForthMark) == 0) // нет марки
		{
			n.onBypassFeedback(p, "Chat 2");
			return;
		}

		if(getItemCount(p, DarionsBadge) >= 10)
		{
			removeItem(p, DarionsBadge, 10); // Darion's Badge
			addItem(p, NativePants, 1); // Native Pants
		}
		else
			n.onBypassFeedback(p, "Chat 3");
	}

	public void buyMagicBottle()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();
		if(getItemCount(p, SecondMark) == 0 && getItemCount(p, ThirdMark) == 0 && getItemCount(p, ForthMark) == 0) // нет второй или выше марки
		{
			n.onBypassFeedback(p, "Chat 1");
			return;
		}

		if(getItemCount(p, ScorpionPoisonStinger) >= 20)
		{
			removeItem(p, ScorpionPoisonStinger, 20); // Scorpion Poison Stingers
			addItem(p, MagicBottle, 1); // Magic Bottle
		}
		else
			n.onBypassFeedback(p, "Chat 1");
	}

	public void buyHolyWater()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();
		if(p.getEffectList().getEffectsBySkillId(NativeTransformationSkill) == null) // не в трансформе native
		{
			n.onBypassFeedback(p, "Chat 2");
			return;
		}

		if(getItemCount(p, DarionsBadge) >= 5)
		{
			removeItem(p, DarionsBadge, 5); // Darion's Badge
			addItem(p, HolyWater, 1); // Holy Water
		}
		else
			n.onBypassFeedback(p, "Chat 3");
	}

	public static void OnDie(L2Character cha, L2Character killer)
	{
		if(cha == null || killer == null || !cha.isMonster() || !killer.isPlayable())
			return;

		switch(cha.getNpcId())
		{
			case 22320: // Junior Watchman
			case 22321: // Junior Summoner
			case 22324: // Blind Huntsman
			case 22325: // Blind Watchman
				changeConfidence(killer, 1L); // confirmed
				break;
			case 22327: // Arcane Scout
			case 22328: // Arcane Guardian
			case 22329: // Arcane Watchman
			case 22342: // Darion's Enforcer
			case 22343: // Darion's Executioner
				changeConfidence(killer, 3L); // confirmed
				break;
			case 18463: // Remnant Diabolist
			case 18464: // Remnant Diviner
				changeConfidence(killer, 5L); // high probability
				break;
			case 18465: // Derek
				changeConfidence(killer, 10000L); // high probability
				break;

			// FIXME: unknown values
			case 22334: // Sand Scorpion
			case 22337: // Desiccator
			case 22339: // Wandering Caravan
			case 22340: // Sandstorm
			case 22344: // Quarry Supervisor
			case 22345: // Quarry Bowman
			case 22346: // Quarry Foreman
			case 22347: // Quarry Patrolman
			case 22355: // Enceinte Defender
			case 22356: // Enceinte Defender
			case 22357: // Enceinte Defender
			case 22358: // Enceinte Defender
			case 22341: // Keltas
				changeConfidence(killer, 3L);
				break;

			case 32299: // Quarry Slave
			case 22322: // Subjugated Native
			case 22323: // Charmed Native
				changeConfidence(killer, -10L); // high probability
				break;
		}
	}

	public static void changeConfidence(L2Character cha, Long mod)
	{
		L2Player p = cha.getPlayer();
		long curr = getConfidence();
		long n = Math.max(0, mod + curr);
		if(curr != n)
		{
			if(p != null && n < 1000000)
				p.sendMessage(new CustomMessage("HellboundConfidence", p).addNumber(n));
			ServerVariables.set("HellboundConfidence", n);

			// Дверь к каравану
			if(curr < 300000 && n > 300000)
				DoorTable.getInstance().getDoor(20250002).openMe();
			else if(curr > 300000 && n < 300000)
				DoorTable.getInstance().getDoor(20250002).closeMe();

			// Дверь к химерам
			if(curr < 600000 && n > 600000)
				DoorTable.getInstance().getDoor(20250001).openMe();
			else if(curr > 600000 && n < 600000)
				DoorTable.getInstance().getDoor(20250001).closeMe();
		}
	}

	private static long getConfidence()
	{
		return ServerVariables.getInt("HellboundConfidence", 0);
	}

	/** Обмен у Kief */
	public void badgesToConfidence(String[] param)
	{
		if(param == null || param.length < 1 || !Util.isNumber(param[0]))
			return;
		long count = Long.parseLong(param[0]);
		if(count <= 0)
			return;
		L2Player p = (L2Player) getSelf();
		count = Math.min(count, getItemCount(p, DarionsBadge));
		if(count <= 0)
			return;
		removeItem(p, DarionsBadge, count);
		changeConfidence(p, count * 10L);
	}

	public void enterToInfinitumTower()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();

		// Нету партии или не лидер партии
		if(p.getParty() == null || !p.getParty().isLeader(p))
		{
			n.onBypassFeedback(p, "Chat 1");
			return;
		}

		GArray<L2Player> members = p.getParty().getPartyMembers();

		// Далеко или нету эффекта херба Fiery Demon Blood
		for(L2Player member : members)
			if(member == null || !L2NpcInstance.canBypassCheck(member, n) || member.getEffectList().getEffectsBySkillId(FieryDemonBloodSkill) == null)
			{
				n.onBypassFeedback(p, "Chat 2");
				return;
			}

		// Телепортируем партию на 1 этаж Tower of Infinitum
		for(L2Player member : members)
			member.teleToLocation(TowerofInfinitumLocationPoint);
	}

	public void enterToTullyEntrance()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();

		if(!L2NpcInstance.canBypassCheck(p, n))
			return;

		// Телепортируем чара в предбанник Tully's Workshop
		if(p.isQuestCompleted("_132_MatrasCuriosity"))
			p.teleToLocation(TullyEntranceLocationPoint);
		else
			n.onBypassFeedback(p, "Chat 1");
	}

	public void enterToTullyFloor1()
	{
		L2Player p = (L2Player) getSelf();
		L2NpcInstance n = getNpc();

		if(!L2NpcInstance.canBypassCheck(p, n))
			return;

		// Нету партии или не лидер партии
		if(p.getParty() == null || !p.getParty().isLeader(p))
		{
			n.onBypassFeedback(p, "Chat 2");
			return;
		}

		GArray<L2Player> members = p.getParty().getPartyMembers();

		// Далеко или не выполнен 132 квест
		for(L2Player member : members)
			if(member == null || !L2NpcInstance.canBypassCheck(member, n) || !member.isQuestCompleted("_132_MatrasCuriosity"))
			{
				n.onBypassFeedback(p, "Chat 1");
				return;
			}

		// Телепортируем партию на 1 этаж Tully's Workshop
		for(L2Player member : members)
			member.teleToLocation(TullyFloor1LocationPoint);
	}
}