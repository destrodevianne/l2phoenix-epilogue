package l2p.debug.benchmark;

import l2p.Config;
import l2p.gameserver.geodata.GeoEngine;

public class GeoCheckOptimized
{
	public static void main(String[] args) throws Exception
	{
		common.init();
		Config.GEOFILES_PATTERN = "(\\d{2}_\\d{2})\\.l2j";
		Config.ALLOW_DOORS = false;
		Config.COMPACT_GEO = true;
		GeoEngine.loadGeo();
		common.PromptEnterToContinue();
	}
}