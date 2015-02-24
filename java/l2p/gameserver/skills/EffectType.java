/**
 * 
 */
package l2p.gameserver.skills;

import java.lang.reflect.Constructor;

import l2p.gameserver.model.L2Effect;
import l2p.gameserver.skills.effects.EffectAddSkills;
import l2p.gameserver.skills.effects.EffectAggression;
import l2p.gameserver.skills.effects.EffectBetray;
import l2p.gameserver.skills.effects.EffectBlessNoblesse;
import l2p.gameserver.skills.effects.EffectBlockStat;
import l2p.gameserver.skills.effects.EffectBuff;
import l2p.gameserver.skills.effects.EffectBuffImmunity;
import l2p.gameserver.skills.effects.EffectCPDamPercent;
import l2p.gameserver.skills.effects.EffectCallSkills;
import l2p.gameserver.skills.effects.EffectCharmOfCourage;
import l2p.gameserver.skills.effects.EffectCombatPointHealOverTime;
import l2p.gameserver.skills.effects.EffectConsumeSoulsOverTime;
import l2p.gameserver.skills.effects.EffectCurseOfLifeFlow;
import l2p.gameserver.skills.effects.EffectDamOverTime;
import l2p.gameserver.skills.effects.EffectDamOverTimeLethal;
import l2p.gameserver.skills.effects.EffectDestroySummon;
import l2p.gameserver.skills.effects.EffectDisarm;
import l2p.gameserver.skills.effects.EffectDiscord;
import l2p.gameserver.skills.effects.EffectEnervation;
import l2p.gameserver.skills.effects.EffectFakeDeath;
import l2p.gameserver.skills.effects.EffectFear;
import l2p.gameserver.skills.effects.EffectGrow;
import l2p.gameserver.skills.effects.EffectHeal;
import l2p.gameserver.skills.effects.EffectHealBlock;
import l2p.gameserver.skills.effects.EffectHealCPPercent;
import l2p.gameserver.skills.effects.EffectHealOverTime;
import l2p.gameserver.skills.effects.EffectHealPercent;
import l2p.gameserver.skills.effects.EffectImobileBuff;
import l2p.gameserver.skills.effects.EffectInterrupt;
import l2p.gameserver.skills.effects.EffectInvisible;
import l2p.gameserver.skills.effects.EffectInvulnerable;
import l2p.gameserver.skills.effects.EffectLDManaDamOverTime;
import l2p.gameserver.skills.effects.EffectManaDamOverTime;
import l2p.gameserver.skills.effects.EffectManaHeal;
import l2p.gameserver.skills.effects.EffectManaHealOverTime;
import l2p.gameserver.skills.effects.EffectManaHealPercent;
import l2p.gameserver.skills.effects.EffectMeditation;
import l2p.gameserver.skills.effects.EffectMute;
import l2p.gameserver.skills.effects.EffectMuteAll;
import l2p.gameserver.skills.effects.EffectMuteAttack;
import l2p.gameserver.skills.effects.EffectMutePhisycal;
import l2p.gameserver.skills.effects.EffectNegateEffects;
import l2p.gameserver.skills.effects.EffectNegateMusic;
import l2p.gameserver.skills.effects.EffectParalyze;
import l2p.gameserver.skills.effects.EffectPetrification;
import l2p.gameserver.skills.effects.EffectRelax;
import l2p.gameserver.skills.effects.EffectRoot;
import l2p.gameserver.skills.effects.EffectSalvation;
import l2p.gameserver.skills.effects.EffectSilentMove;
import l2p.gameserver.skills.effects.EffectSleep;
import l2p.gameserver.skills.effects.EffectStun;
import l2p.gameserver.skills.effects.EffectSymbol;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.skills.effects.EffectTransformation;
import l2p.gameserver.skills.effects.EffectTurner;
import l2p.gameserver.skills.effects.EffectUnAggro;

public enum EffectType
{
	// Основные эффекты
	AddSkills(EffectAddSkills.class, null, false),
	Aggression(EffectAggression.class, null, true),
	Betray(EffectBetray.class, null, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	BlessNoblesse(EffectBlessNoblesse.class, null, true),
	BlockStat(EffectBlockStat.class, null, true),
	Buff(EffectBuff.class, null, false),
	BuffImmunity(EffectBuffImmunity.class, null, true),
	CallSkills(EffectCallSkills.class, null, false),
	CombatPointHealOverTime(EffectCombatPointHealOverTime.class, null, true),
	ConsumeSoulsOverTime(EffectConsumeSoulsOverTime.class, null, true),
	CharmOfCourage(EffectCharmOfCourage.class, null, true),
	CPDamPercent(EffectCPDamPercent.class, null, true),
	DamOverTime(EffectDamOverTime.class, null, false),
	DamOverTimeLethal(EffectDamOverTimeLethal.class, null, false),
	DestroySummon(EffectDestroySummon.class, null, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	Disarm(EffectDisarm.class, null, true),
	Discord(EffectDiscord.class, AbnormalEffect.CONFUSED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	Enervation(EffectEnervation.class, null, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, false),
	FakeDeath(EffectFakeDeath.class, null, true),
	Fear(EffectFear.class, AbnormalEffect.AFFRAID, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	Grow(EffectGrow.class, null, false),
	Heal(EffectHeal.class, null, false),
	HealBlock(EffectHealBlock.class, null, true),
	HealCPPercent(EffectHealCPPercent.class, null, true),
	HealOverTime(EffectHealOverTime.class, null, false),
	HealPercent(EffectHealPercent.class, null, false),
	ImobileBuff(EffectImobileBuff.class, null, true),
	Interrupt(EffectInterrupt.class, null, true),
	Invulnerable(EffectInvulnerable.class, null, false),
	Invisible(EffectInvisible.class, null, false),
	CurseOfLifeFlow(EffectCurseOfLifeFlow.class, null, true),
	LDManaDamOverTime(EffectLDManaDamOverTime.class, null, true),
	ManaDamOverTime(EffectManaDamOverTime.class, null, true),
	ManaHeal(EffectManaHeal.class, null, false),
	ManaHealOverTime(EffectManaHealOverTime.class, null, false),
	ManaHealPercent(EffectManaHealPercent.class, null, false),
	Meditation(EffectMeditation.class, null, false),
	Mute(EffectMute.class, AbnormalEffect.MUTED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	MuteAll(EffectMuteAll.class, AbnormalEffect.MUTED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	MuteAttack(EffectMuteAttack.class, AbnormalEffect.MUTED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	MutePhisycal(EffectMutePhisycal.class, AbnormalEffect.MUTED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	NegateEffects(EffectNegateEffects.class, null, false),
	NegateMusic(EffectNegateMusic.class, null, false),
	Paralyze(EffectParalyze.class, AbnormalEffect.HOLD_1, Stats.PARALYZE_RECEPTIVE, Stats.PARALYZE_POWER, true),
	Petrification(EffectPetrification.class, AbnormalEffect.HOLD_2, Stats.PARALYZE_RECEPTIVE, Stats.PARALYZE_POWER, true),
	Relax(EffectRelax.class, null, true),
	Root(EffectRoot.class, AbnormalEffect.ROOT, Stats.ROOT_RECEPTIVE, Stats.ROOT_POWER, true),
	Salvation(EffectSalvation.class, null, true),
	SilentMove(EffectSilentMove.class, AbnormalEffect.STEALTH, true),
	Sleep(EffectSleep.class, AbnormalEffect.SLEEP, Stats.SLEEP_RECEPTIVE, Stats.SLEEP_POWER, true),
	Stun(EffectStun.class, AbnormalEffect.STUN, Stats.STUN_RECEPTIVE, Stats.STUN_POWER, true),
	Symbol(EffectSymbol.class, null, false),
	Transformation(EffectTransformation.class, null, true),
	Turner(EffectTurner.class, AbnormalEffect.STUN, Stats.STUN_RECEPTIVE, Stats.STUN_POWER, true),
	UnAggro(EffectUnAggro.class, null, true),
	Vitality(EffectBuff.class, AbnormalEffect.VITALITY, true),

	// Производные от основных эффектов
	Poison(EffectDamOverTime.class, null, Stats.POISON_RECEPTIVE, Stats.POISON_POWER, false),
	PoisonLethal(EffectDamOverTimeLethal.class, null, Stats.POISON_RECEPTIVE, Stats.POISON_POWER, false),
	Bleed(EffectDamOverTime.class, null, Stats.BLEED_RECEPTIVE, Stats.BLEED_POWER, false),
	Debuff(EffectBuff.class, null, false),
	WatcherGaze(EffectBuff.class, null, false),
	TransferDam(EffectBuff.class, null, false);

	private final Class<? extends L2Effect> clazz;
	private final AbnormalEffect abnormal;
	private final Stats resistType;
	private final Stats attibuteType;
	private final boolean isRaidImmune;

	private EffectType(Class<? extends L2Effect> clazz, AbnormalEffect abnormal, boolean isRaidImmune)
	{
		this(clazz, abnormal, null, null, isRaidImmune);
	}

	private EffectType(Class<? extends L2Effect> clazz, AbnormalEffect abnormal, Stats resistType, Stats attibuteType, boolean isRaidImmune)
	{
		this.clazz = clazz;
		this.abnormal = abnormal;
		this.resistType = resistType;
		this.attibuteType = attibuteType;
		this.isRaidImmune = isRaidImmune;
	}

	public AbnormalEffect getAbnormal()
	{
		return abnormal;
	}

	public Stats getResistType()
	{
		return resistType;
	}

	public Stats getAttibuteType()
	{
		return attibuteType;
	}

	public boolean isRaidImmune()
	{
		return isRaidImmune;
	}

	public L2Effect makeEffect(Env env, EffectTemplate template)
	{
		try
		{
			Constructor<? extends L2Effect> c = clazz.getConstructor(Env.class, EffectTemplate.class);
			return c.newInstance(env, template);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}