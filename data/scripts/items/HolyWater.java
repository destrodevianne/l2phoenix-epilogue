package items;

import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.IItemHandler;
import l2p.gameserver.handler.ItemHandler;
import l2p.gameserver.model.L2Playable;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2MonsterInstance;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.serverpackets.MagicSkillUse;
import npc.model.HellboundRemnantInstance;

public class HolyWater implements IItemHandler, ScriptFile
{
	private static final int[] _itemIds = { 9673 };
	L2Player player;
	L2MonsterInstance target;

	public void useItem(L2Playable playable, L2ItemInstance _item, Boolean ctrl)
	{
		if(playable == null)
			return;

		// Цель не выделена, цель не недоупокоеный
		if(playable.getTarget() == null || !(playable.getTarget() instanceof HellboundRemnantInstance))
		{
			playable.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return;
		}

		HellboundRemnantInstance target = (HellboundRemnantInstance) playable.getTarget();

		// Моб уже мертвый
		if(target.isDead() || target.isDying())
		{
			playable.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return;
		}

		playable.broadcastPacket(new MagicSkillUse(playable, target, 2358, 1, 0, 0));
		target.onUseHolyWater(playable);
	}

	public final int[] getItemIds()
	{
		return _itemIds;
	}

	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}