package l2p.gameserver.serverpackets;

import javolution.util.FastTable;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.tables.SkillTreeTable;

/**
 * format   d (dddc)
 */
public class SkillList extends L2GameServerPacket
{
	private FastTable<L2Skill> _skills;
	private boolean canEnchant;

	public SkillList(L2Player p)
	{
		_skills = new FastTable<L2Skill>();
		_skills.addAll(p.getAllSkills());
		canEnchant = p.getTransformation() == 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x5f);
		writeD(_skills.size());

		for(L2Skill temp : _skills)
		{
			writeD(temp.isActive() || temp.isToggle() ? 0 : 1); // deprecated? клиентом игнорируется
			writeD(temp.getDisplayLevel());
			writeD(temp.getDisplayId());
			writeC(0x00); // иконка скилла серая если не 0
			writeC(canEnchant ? SkillTreeTable.isEnchantable(temp) : 0); // для заточки: если 1 скилл можно точить
		}
	}
}