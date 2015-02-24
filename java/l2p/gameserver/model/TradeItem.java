package l2p.gameserver.model;

import java.sql.ResultSet;

import l2p.database.DatabaseUtils;
import l2p.database.FiltredPreparedStatement;
import l2p.database.L2DatabaseFactory;
import l2p.database.ThreadConnection;
import l2p.gameserver.model.items.L2ItemInstance;
import l2p.gameserver.model.items.L2ItemInstance.ItemLocation;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.templates.L2Weapon.WeaponType;

/**
 * Абстрактное описание предмета, безопасное для любых операций. Может использоваться как ссылка на уже существующий предмет либо как набор информации для создания нового.
 */
public final class TradeItem
{
	private int _objectId;
	private int _itemId;
	private long _price;
	private long _storePrice;
	private long _count;
	private int _enchantLevel;

	private int[] _attackElement;
	private int _defenceFire;
	private int _defenceWater;
	private int _defenceWind;
	private int _defenceEarth;
	private int _defenceHoly;
	private int _defenceUnholy;

	private long _currentvalue;
	private int _lastRechargeTime;
	private int _rechargeTime;

	public TradeItem()
	{}

	public TradeItem(L2ItemInstance original)
	{
		_objectId = original.getObjectId();
		_itemId = original.getItemId();
		_count = original.getCount();
		_enchantLevel = original.getEnchantLevel();
		_attackElement = original.getAttackElement();
		_defenceFire = original.getDefenceFire();
		_defenceWater = original.getDefenceWater();
		_defenceWind = original.getDefenceWind();
		_defenceEarth = original.getDefenceEarth();
		_defenceHoly = original.getDefenceHoly();
		_defenceUnholy = original.getDefenceUnholy();
	}

	public void setObjectId(int id)
	{
		_objectId = id;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public void setItemId(int id)
	{
		_itemId = id;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public L2Item getItem()
	{
		return ItemTable.getInstance().getTemplate(_itemId);
	}

	public void setOwnersPrice(long price)
	{
		_price = price;
	}

	public long getOwnersPrice()
	{
		return _price;
	}

	public void setStorePrice(long price)
	{
		_storePrice = price;
	}

	public long getStorePrice()
	{
		return _storePrice;
	}

	public void setCount(long count)
	{
		_count = count;
	}

	public long getCount()
	{
		return _count;
	}

	public void setEnchantLevel(int enchant)
	{
		_enchantLevel = enchant;
	}

	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	public void setCurrentValue(long tempvalue)
	{
		_currentvalue = tempvalue;
	}

	public long getCurrentValue()
	{
		return _currentvalue;
	}

	@Override
	public int hashCode()
	{
		return _objectId + _itemId;
	}

	public int[] getAttackElement()
	{
		if(_attackElement == null)
			return new int[] { L2Item.ATTRIBUTE_NONE, 0 };
		return _attackElement;
	}

	public void setAttackElement(int[] attackElement)
	{
		_attackElement = attackElement;
	}

	public int getDefenceFire()
	{
		return _defenceFire;
	}

	public void setDefenceFire(int defenceFire)
	{
		_defenceFire = defenceFire;
	}

	public int getDefenceWater()
	{
		return _defenceWater;
	}

	public void setDefenceWater(int defenceWater)
	{
		_defenceWater = defenceWater;
	}

	public int getDefenceWind()
	{
		return _defenceWind;
	}

	public void setDefenceWind(int defenceWind)
	{
		_defenceWind = defenceWind;
	}

	public int getDefenceEarth()
	{
		return _defenceEarth;
	}

	public void setDefenceEarth(int defenceEarth)
	{
		_defenceEarth = defenceEarth;
	}

	public int getDefenceHoly()
	{
		return _defenceHoly;
	}

	public void setDefenceHoly(int defenceHoly)
	{
		_defenceHoly = defenceHoly;
	}

	public int getDefenceUnholy()
	{
		return _defenceUnholy;
	}

	public void setDefenceUnholy(int defenceUnholy)
	{
		_defenceUnholy = defenceUnholy;
	}

	/**
	 * Устанавливает время респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @param rechargeTime : unixtime в минутах
	 */
	public void setRechargeTime(int rechargeTime)
	{
		_rechargeTime = rechargeTime;
	}

	/**
	 * Возвращает время респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @return unixtime в минутах
	 */
	public int getRechargeTime()
	{
		return _rechargeTime;
	}

	/**
	 * Возвращает ограничен ли этот предмет в количестве, используется в NPC магазинах с ограниченным количеством.
	 * @return true, если ограничен
	 */
	public boolean isCountLimited()
	{
		return _currentvalue > 0;
	}

	/**
	 * Устанавливает время последнего респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @param lastRechargeTime : unixtime в минутах
	 */
	public void setLastRechargeTime(int lastRechargeTime)
	{
		_lastRechargeTime = lastRechargeTime;
	}

	/**
	 * Возвращает время последнего респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @return unixtime в минутах
	 */
	public int getLastRechargeTime()
	{
		return _lastRechargeTime;
	}

	public static TradeItem restoreFromDb(int objectId, ItemLocation loc)
	{
		TradeItem inst = null;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet item_rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM items WHERE object_id=? AND loc=? LIMIT 1");
			statement.setLong(1, objectId);
			statement.setString(2, loc.name());
			item_rset = statement.executeQuery();
			if(item_rset.next())
			{
				L2Item item = ItemTable.getInstance().getTemplate(item_rset.getInt("item_id"));
				if(item == null)
					return null;

				if(item.isTemporal() && item_rset.getInt("shadow_life_time") <= 0)
					return null;

				inst = new TradeItem();
				inst.setObjectId(objectId);
				inst.setItemId(item.getItemId());
				inst.setCount(item_rset.getLong("count"));
				inst.setEnchantLevel(item_rset.getInt("enchant_level"));

				// load augmentation and elemental enchant
				if(item.isEquipable())
					inst.restoreAttributes(con);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, item_rset);
		}
		return inst;
	}

	public void restoreAttributes(ThreadConnection con)
	{
		FiltredPreparedStatement statement = null;
		try
		{
			statement = con.prepareStatement("SELECT elemType,elemValue FROM item_attributes WHERE itemId=? LIMIT 1");
			statement.setInt(1, getObjectId());
			ResultSet rs = statement.executeQuery();
			rs = statement.executeQuery();
			if(rs.next())
			{
				byte elem_type = rs.getByte(1);
				int elem_value = rs.getInt(2);
				if(getItem().getItemType() instanceof WeaponType)
					setAttackElement(new int[] { elem_type, elem_value });
				else
					switch(elem_type)
					{
						case 0:
							setDefenceFire(elem_value);
							break;
						case 1:
							setDefenceWater(elem_value);
							break;
						case 2:
							setDefenceWind(elem_value);
							break;
						case 3:
							setDefenceEarth(elem_value);
							break;
						case 4:
							setDefenceHoly(elem_value);
							break;
						case 5:
							setDefenceUnholy(elem_value);
							break;
					}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeStatement(statement);
		}
	}
}