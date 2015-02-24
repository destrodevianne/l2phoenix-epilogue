package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.ExShowBaseAttributeCancelWindow;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.templates.L2Item;
import l2p.util.Log;

/**
 * @author SYS
 */
public class RequestExRemoveItemAttribute extends L2GameClientPacket
{
	// Format: chd
	private int _objectId;
	public static final int UNENCHANT_PRICE = 50000;

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

		if(activeChar.isOutOfControl() || activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		L2ItemInstance itemToUnnchant = inventory.getItemByObjectId(_objectId);

		if(itemToUnnchant == null || itemToUnnchant.getAttributeElementValue() == 0 || activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getAdena() < UNENCHANT_PRICE)
		{
			activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			activeChar.sendActionFailed();
			return;
		}

		activeChar.reduceAdena(UNENCHANT_PRICE, true);
		itemToUnnchant.setAttributeElement(L2Item.ATTRIBUTE_NONE, 0, true);
		activeChar.getInventory().refreshListeners();

		activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToUnnchant));
		activeChar.sendChanges();
		activeChar.sendPacket(new ExShowBaseAttributeCancelWindow(activeChar, UNENCHANT_PRICE));

		Log.add(activeChar.getName() + "|Successfully unenchanted attribute|" + itemToUnnchant.getItemId(), "enchants");
		Log.LogItem(activeChar, Log.EnchantItem, itemToUnnchant);
	}
}