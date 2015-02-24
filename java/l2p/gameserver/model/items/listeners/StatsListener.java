package l2p.gameserver.model.items.listeners;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.skills.funcs.Func;

public final class StatsListener implements PaperdollListener
{
	private Inventory _inv;

	public StatsListener(Inventory inv)
	{
		_inv = inv;
	}

	public void notifyUnequipped(int slot, L2ItemInstance item)
	{
		L2Character owner = _inv.getOwner();
		owner.removeStatsOwner(item);
		owner.updateStats();
		if(owner.getPet() != null)
		{
			owner.getPet().removeStatsOwner(item);
			owner.getPet().updateStats();
		}
	}

	public void notifyEquipped(int slot, L2ItemInstance item)
	{
		L2Character owner = _inv.getOwner();
		owner.addStatFuncs(item.getStatFuncs());
		owner.updateStats();
		if(owner.getPet() != null && item.getAttributeFuncTemplate() != null)
		{
			Func f = item.getAttributeFuncTemplate().getFunc(item);
			owner.getPet().addStatFunc(f);
			owner.getPet().updateStats();
		}
	}
}