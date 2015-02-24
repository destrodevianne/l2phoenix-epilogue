package l2p.gameserver.serverpackets;

import java.util.Collection;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.tables.SkillTable;

public class GMViewSkillInfo extends L2GameServerPacket
{
	private String _charName;
	private Collection<L2Skill> _skills;
	private boolean _isClanSkillsDisabled;

	public GMViewSkillInfo(L2Player cha)
	{
		_charName = cha.getName();
		_skills = cha.getAllSkills();
		_isClanSkillsDisabled = cha.getClan() != null && cha.getClan().getReputationScore() < 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x97);
		writeS(_charName);
		writeD(_skills.size());
		for(L2Skill skill : _skills)
		{
			// Сомнительное условие
			if(skill.getId() > 9000)
				continue; // fake skills to change base stats

			writeD(skill.isLikePassive() ? 1 : 0);
			writeD(skill.getDisplayLevel());
			writeD(skill.getId());
			writeC(_isClanSkillsDisabled && skill.isClanSkill() ? 1 : 0);
			writeC(SkillTable.getInstance().getMaxLevel(skill.getId()) > 100 ? 1 : 0);
		}
	}
}