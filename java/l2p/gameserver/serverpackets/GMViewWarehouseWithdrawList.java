package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.model.items.L2ItemInstance.ItemClass;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.templates.L2Weapon;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private final L2ItemInstance[] _items;
	private String _charName;
	private long _charAdena;

	public GMViewWarehouseWithdrawList(L2Player cha)
	{
		_charName = cha.getName();
		_charAdena = cha.getAdena();
		_items = cha.getWarehouse().listItems(ItemClass.ALL);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9b);
		writeS(_charName);
		writeQ(_charAdena);
		writeH(_items.length);
		for(L2ItemInstance temp : _items)
		{
			writeH(temp.getItem().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2ForPackets());
			writeH(temp.getCustomType1());

			if(temp.isEquipable())
			{
				writeD(temp.getBodyPart());
				writeH(temp.getEnchantLevel());

				if(temp.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					writeH(((L2Weapon) temp.getItem()).getSoulShotCount());
					writeH(((L2Weapon) temp.getItem()).getSpiritShotCount());
				}
				else
				{
					writeH(0);
					writeH(0);
				}

				if(temp.isAugmented())
				{
					writeD(temp.getAugmentationId() & 0x0000FFFF);
					writeD(temp.getAugmentationId() >> 16);
				}
				else
				{
					writeD(0);
					writeD(0);
				}

				writeD(temp.getObjectId());
				writeItemElements(temp);
			}

			writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
			writeItemRev152(temp);
			writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0x00); // limited time item life remaining
		}
	}
}