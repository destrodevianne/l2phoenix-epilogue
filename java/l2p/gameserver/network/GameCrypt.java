package l2p.gameserver.network;

import l2p.util.Protection;

public class GameCrypt
{
	private final byte[] _inKey = new byte[16], _outKey = new byte[16];
	private boolean _isEnabled = false, useprotect = false;
	private int _inCount = 0, _outCount = 0, sessionKey;

	public void setKey(byte[] key)
	{
		System.arraycopy(key, 0, _inKey, 0, 16);
		System.arraycopy(key, 0, _outKey, 0, 16);
	}

	public void setKey(byte[] key, boolean _useprotect)
	{
		if((useprotect = _useprotect) == true)
			sessionKey = Protection.initSession(key);
		setKey(key);
	}

	public boolean decrypt(byte[] raw, final int offset, final int size)
	{
		if(!_isEnabled)
			return true;

		if(useprotect)
		{
			if(size > 0)
				_inCount++;
			if(Protection.decrypt(raw, offset, size, _inKey, _inCount))
				return Protection.check(raw, offset, size, sessionKey);
		}

		int temp = 0;
		for(int i = 0; i < size; i++)
		{
			int temp2 = raw[offset + i] & 0xFF;
			raw[offset + i] = (byte) (temp2 ^ _inKey[i & 15] ^ temp);
			temp = temp2;
		}

		int old = _inKey[8] & 0xff;
		old |= _inKey[9] << 8 & 0xff00;
		old |= _inKey[10] << 0x10 & 0xff0000;
		old |= _inKey[11] << 0x18 & 0xff000000;

		old += size;

		_inKey[8] = (byte) (old & 0xff);
		_inKey[9] = (byte) (old >> 0x08 & 0xff);
		_inKey[10] = (byte) (old >> 0x10 & 0xff);
		_inKey[11] = (byte) (old >> 0x18 & 0xff);
		return true;
	}

	public void encrypt(byte[] raw, final int offset, final int size)
	{
		if(!_isEnabled)
		{
			_isEnabled = true;
			return;
		}

		if(useprotect)
		{
			if(size > 0)
				_outCount++;
			if(Protection.encrypt(raw, offset, size, _outKey, _outCount))
				return;
		}

		int temp = 0;
		for(int i = 0; i < size; i++)
		{
			int temp2 = raw[offset + i] & 0xFF;
			temp = temp2 ^ _outKey[i & 15] ^ temp;
			raw[offset + i] = (byte) temp;
		}

		int old = _outKey[8] & 0xff;
		old |= _outKey[9] << 8 & 0xff00;
		old |= _outKey[10] << 0x10 & 0xff0000;
		old |= _outKey[11] << 0x18 & 0xff000000;

		old += size;

		_outKey[8] = (byte) (old & 0xff);
		_outKey[9] = (byte) (old >> 0x08 & 0xff);
		_outKey[10] = (byte) (old >> 0x10 & 0xff);
		_outKey[11] = (byte) (old >> 0x18 & 0xff);
	}
}