package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2ShortCut;

public class ShortCutRegister extends L2GameServerPacket
{
	private L2ShortCut sc;

	public ShortCutRegister(L2ShortCut _sc)
	{
		sc = _sc;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x44);

		writeD(sc.type);
		writeD(sc.slot + sc.page * 12); // номер слота
		writeD(sc.id); // id скилла или object id вещи

		switch(sc.type)
		{
			case L2ShortCut.TYPE_ITEM:
				//ddddhh
				writeD(1); // неизвестно, если не 1 то черный квадрат
				writeD(-1); // если больше чем -1 то показывать реюз 
				writeD(0); // оставшееся время реюза в секундах
				writeD(0); // реюз в секундах
				writeH(0); // неизвестно, на изменение клиент не реагирует 
				writeH(0); // неизвестно, на изменение клиент не реагирует
				break;
			case L2ShortCut.TYPE_SKILL:
				//dcd
				writeD(sc.level);
				writeC(0); // неизвестно, на изменение клиент не реагирует
				writeD(1); // неизвестно, на изменение клиент не реагирует
				break;
			default:
				writeD(1); // неизвестно, на изменение клиент не реагирует
				break;
		}
	}
}