package l2p.gameserver.clientpackets;

import l2p.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.L2Item;
import l2p.util.Log;
import l2p.util.Rnd;

/**
 * @author SYS
 * Format: d
 */
public class RequestEnchantItemAttribute extends L2GameClientPacket
{
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_objectId == -1)
		{
			activeChar.setEnchantScroll(null);
			activeChar.sendPacket(Msg.ELEMENTAL_POWER_ENCHANCER_USAGE_HAS_BEEN_CANCELLED);
			return;
		}

		if(activeChar.isOutOfControl() || activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		L2ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		L2ItemInstance stone = activeChar.getEnchantScroll();
		activeChar.setEnchantScroll(null);

		if(itemToEnchant == null || stone == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Item item = itemToEnchant.getItem();

		if(!itemToEnchant.canBeEnchanted() || item.getCrystalType().cry < L2Item.CRYSTAL_S)
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, Msg.ActionFail);
			return;
		}

		if(itemToEnchant.getLocation() != L2ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, Msg.ActionFail);
			return;
		}

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP, Msg.ActionFail);
			return;
		}

		if(itemToEnchant.isStackable() || (stone = inventory.getItemByObjectId(stone.getObjectId())) == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		int itemType = item.getType2();

		if(itemToEnchant.getAttributeElement() != L2Item.ATTRIBUTE_NONE && itemToEnchant.getAttributeElement() != stone.getEnchantAttributeStoneElement(itemType == L2Item.TYPE2_SHIELD_ARMOR))
		{
			activeChar.sendPacket(Msg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, Msg.ActionFail);
			return;
		}

		if(item.isUnderwear() || item.isCloak() || item.isBracelet() || item.isBelt() || item.isPvP())
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, Msg.ActionFail);
			return;
		}

		int maxValue = itemType == L2Item.TYPE2_WEAPON ? 150 : 60;

		if(stone.isAttributeCrystal())
			maxValue += itemType == L2Item.TYPE2_WEAPON ? 150 : 60;

		if(itemToEnchant.getAttributeElementValue() >= maxValue)
		{
			activeChar.sendPacket(Msg.ELEMENTAL_POWER_ENCHANCER_USAGE_HAS_BEEN_CANCELLED, Msg.ActionFail);
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, Msg.ActionFail);
			return;
		}

		Log.add(activeChar.getName() + "|Trying to attribute enchant|" + itemToEnchant.getItemId() + "|attribute:" + stone.getEnchantAttributeStoneElement(itemType == L2Item.TYPE2_SHIELD_ARMOR) + "|" + itemToEnchant.getObjectId(), "enchants");

		L2ItemInstance removedStone;
		synchronized (inventory)
		{
			removedStone = inventory.destroyItem(stone.getObjectId(), 1, true);
		}

		if(removedStone == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(Rnd.chance(stone.isAttributeCrystal() ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE : Config.ENCHANT_ATTRIBUTE_STONE_CHANCE))
		{
			if(itemToEnchant.getEnchantLevel() == 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.S2_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1);
				sm.addItemName(itemToEnchant.getItemId());
				sm.addItemName(stone.getItemId());
				activeChar.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessage.S3_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO__S1S2);
				sm.addNumber(itemToEnchant.getEnchantLevel());
				sm.addItemName(itemToEnchant.getItemId());
				sm.addItemName(stone.getItemId());
				activeChar.sendPacket(sm);
			}

			int value = itemType == L2Item.TYPE2_WEAPON ? 5 : 6;

			// Для оружия 1й камень дает +20 атрибута
			if(itemToEnchant.getAttributeElementValue() == 0 && itemType == L2Item.TYPE2_WEAPON)
				value = 20;

			itemToEnchant.setAttributeElement(stone.getEnchantAttributeStoneElement(itemType == L2Item.TYPE2_SHIELD_ARMOR), itemToEnchant.getAttributeElementValue() + value, true);
			inventory.refreshListeners();

			activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnchant));

			Log.add(activeChar.getName() + "|Successfully enchanted by attribute|" + itemToEnchant.getItemId() + "|to+" + itemToEnchant.getAttributeElementValue() + "|" + (stone.isAttributeCrystal() ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE : Config.ENCHANT_ATTRIBUTE_STONE_CHANCE), "enchants");
			Log.LogItem(activeChar, Log.EnchantItem, itemToEnchant);
		}
		else
		{
			activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_ADD_ELEMENTAL_POWER);
			Log.add(activeChar.getName() + "|Failed to enchant attribute|" + itemToEnchant.getItemId() + "|+" + itemToEnchant.getAttributeElementValue() + "|" + (stone.isAttributeCrystal() ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE : Config.ENCHANT_ATTRIBUTE_STONE_CHANCE), "enchants");
		}

		activeChar.setEnchantScroll(null);
		activeChar.sendChanges();
	}
}