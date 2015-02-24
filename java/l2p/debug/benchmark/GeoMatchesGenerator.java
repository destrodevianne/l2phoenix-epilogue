package l2p.debug.benchmark;

import l2p.Config;
import l2p.gameserver.geodata.GeoEngine;

public class GeoMatchesGenerator
{
	public static void main(String[] args) throws Exception
	{
		common.init();
		Config.GEOFILES_PATTERN = "(\\d{2}_\\d{2})\\.l2j";
		Config.ALLOW_DOORS = false;
		Config.COMPACT_GEO = false;
		GeoEngine.loadGeo();
		common.log.info("Goedata loaded");
		common.GC();
		GeoEngine.genBlockMatches(0); //TODO
		if(common.YesNoPrompt("Do you want to delete temproary geo checksums files?"))
			GeoEngine.deleteChecksumFiles();
		common.PromptEnterToContinue();
	}
}