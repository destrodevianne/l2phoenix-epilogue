package l2p.loginserver.gameservercon.lspackets;

import javolution.util.FastList;
import l2p.loginserver.IpManager;
import l2p.util.BannedIp;

public class BanIPList extends ServerBasePacket
{
	public BanIPList()
	{
		FastList<BannedIp> baniplist = IpManager.getInstance().getBanList();
		writeC(0x05);
		writeD(baniplist.size());
		for(BannedIp ip : baniplist)
		{
			writeS(ip.ip);
			writeS(ip.admin);
		}
		FastList.recycle(baniplist);
	}
}
