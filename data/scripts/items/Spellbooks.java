package items;

import java.util.ArrayList;

import l2p.extensions.scripts.ScriptFile;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.IItemHandler;
import l2p.gameserver.handler.ItemHandler;
import l2p.gameserver.model.L2Playable;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.SkillSpellbookTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTreeTable;

public class Spellbooks implements IItemHandler, ScriptFile
{
	private static int[] _itemIds = null;

	public Spellbooks()
	{
		_itemIds = new int[SkillSpellbookTable.getSpellbookHandlers().size()];
		int i = 0;
		for(int id : SkillSpellbookTable.getSpellbookHandlers().keySet())
		{
			_itemIds[i] = id;
			i++;
		}
	}

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2Player player = (L2Player) playable;

		if(item == null || item.getCount() < 1)
		{
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			return;
		}

		ArrayList<Integer> skill_ids = SkillSpellbookTable.getSpellbookHandlers().get(item.getItemId());

		for(int skill_id : skill_ids)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skill_id, 1);
			if(skill == null)
				continue;

			if(player.getSkillLevel(skill_id) > 0)
				continue;

			if(!(skill.isCommon() || SkillTreeTable.getInstance().isSkillPossible(player, skill_id, 1)))
				continue;

			if(player.getLevel() < SkillSpellbookTable.getMinLevel(item.getItemId()))
				return;

			L2ItemInstance ri = player.getInventory().destroyItem(item, 1, true);
			player.addSkill(skill, true);
			player.updateStats();
			player.sendChanges();
			player.sendPacket(SystemMessage.removeItems(ri.getItemId(), 1), new SkillList(player));

			// Анимация изучения книги над головой чара (на самом деле, для каждой книги своя анимация, но они одинаковые)
			player.broadcastPacket(new MagicSkillUse(player, player, 2790, 1, 1, 0));
		}
	}

	public int[] getItemIds()
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