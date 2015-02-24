package l2p.gameserver.skills.effects;

import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.skills.Env;

public final class EffectDisarm extends L2Effect
{
	public EffectDisarm(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(!_effected.isPlayer())
			return false;
		L2Player player = _effected.getPlayer();
		if(player == null)
			return false;
		// Нельзя снимать/одевать проклятое оружие и флаги
		if(player.isCursedWeaponEquipped() || player.isCombatFlagEquipped() || player.isTerritoryFlagEquipped())
			return false;
		if(player.getActiveWeaponInstance() == null)
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		L2Player player = _effected.getPlayer();
		if(player != null)
		{
			L2ItemInstance weapon = player.getActiveWeaponInstance();
			if(weapon != null)
				player.getInventory().unEquipItemInBodySlotAndNotify(weapon.getBodyPart(), weapon);
		}
	}

	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}