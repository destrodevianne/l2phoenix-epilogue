package l2p.gameserver.skills.conditions;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.skills.Env;

final class ConditionSlotItemType extends ConditionInventory
{
	private final int _mask;

	ConditionSlotItemType(short slot, int mask)
	{
		super(slot);
		_mask = mask;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		Inventory inv = ((L2Player) env.character).getInventory();
		L2ItemInstance item = inv.getPaperdollItem(_slot);
		if(item == null)
			return false;
		return (item.getItem().getItemMask() & _mask) != 0;
	}
}