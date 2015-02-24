package l2p.loginserver;

import java.nio.ByteBuffer;

import l2p.extensions.network.IPacketHandler;
import l2p.extensions.network.ReceivablePacket;
import l2p.loginserver.L2LoginClient.LoginClientState;
import l2p.loginserver.clientpackets.AuthGameGuard;
import l2p.loginserver.clientpackets.AuthHWID;
import l2p.loginserver.clientpackets.RequestAuthLogin;
import l2p.loginserver.clientpackets.RequestServerList;
import l2p.loginserver.clientpackets.RequestServerLogin;
import l2p.util.Util;

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		int opcode = buf.get() & 0xFF;

		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();

		switch(state)
		{
			case CONNECTED:
				if(opcode == 0x07)
					packet = new AuthGameGuard();
				else if(opcode == 0xF0)
					packet = new AuthHWID();
				else
					debugOpcode(opcode, state, client, buf);
				break;
			case AUTHED_GG:
				if(opcode == 0x00)
					packet = new RequestAuthLogin();
				else if(opcode == 0xF0)
					packet = new AuthHWID();
				else if(opcode != 0x05) //на случай когда клиент зажимает ентер
					debugOpcode(opcode, state, client, buf);
				break;
			case AUTHED_LOGIN:
				if(opcode == 0x05)
					packet = new RequestServerList();
				else if(opcode == 0x02)
					packet = new RequestServerLogin();
				else
					debugOpcode(opcode, state, client, buf);
				break;
		}
		return packet;
	}

	private void debugOpcode(int opcode, LoginClientState state, L2LoginClient client, ByteBuffer buf)
	{
		int sz = buf.remaining();
		byte[] arr = new byte[sz];
		buf.get(arr);
		System.out.println("Unknown Opcode: " + opcode + " for state: " + state.name() + " from IP: " + client);
		System.out.println(Util.printData(arr, sz));
	}
}