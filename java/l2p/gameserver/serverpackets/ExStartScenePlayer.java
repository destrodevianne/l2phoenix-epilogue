package l2p.gameserver.serverpackets;

public class ExStartScenePlayer extends L2GameServerPacket
{
	private final int _sceneId;

	public static final int SCENE_LINDVIOR = 1;
	public static final int SCENE_ECHMUS_OPENING = 2;
	public static final int SCENE_ECHMUS_SUCCESS = 3;
	public static final int SCENE_ECHMUS_FAIL = 4;
	public static final int SCENE_TIAT_OPENING = 5;
	public static final int SCENE_TIAT_SUCCESS = 6;
	public static final int SCENE_TIAT_FAIL = 7;
	public static final int SCENE_SSQ_SUSPICIOUS_DEATH = 8;
	public static final int SCENE_SSQ_DYING_MASSAGE = 9;
	public static final int SCENE_SSQ_CONTRACT_OF_MAMMON = 10;
	public static final int SCENE_SSQ_RITUAL_OF_PRIEST = 11;
	public static final int SCENE_SSQ_SEALING_EMPEROR_1ST = 12;
	public static final int SCENE_SSQ_SEALING_EMPEROR_2ND = 13;
	public static final int SCENE_SSQ_EMBRYO = 14;
	public static final int SCENE_LAND_KSERTH_A = 1000;
	public static final int SCENE_LAND_KSERTH_B = 1001;
	public static final int SCENE_LAND_UNDEAD_A = 1002;
	public static final int SCENE_LAND_DISTRUCTION_A = 1003;

	public ExStartScenePlayer(int sceneId)
	{
		_sceneId = sceneId;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x99);
		writeD(_sceneId);
	}
}