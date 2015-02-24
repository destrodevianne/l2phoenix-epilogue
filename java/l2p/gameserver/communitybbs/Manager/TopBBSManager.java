package l2p.gameserver.communitybbs.Manager;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2p.Config;
import l2p.gameserver.model.L2Player;
import l2p.util.Files;

public class TopBBSManager extends BaseBBSManager
{
	private static final Pattern callsPattern = Pattern.compile("\\{@(.+?)\\((.*?)\\)}");

	public void showTopPage(L2Player activeChar, String page, String subcontent)
	{
		showTopPage(activeChar, page, subcontent, null);
	}

	public void showTopPage(L2Player activeChar, String page, String subcontent, String className)
	{
		if(page == null || page.isEmpty())
			page = "index";
		else
			page = page.replace("../", "").replace("..\\", "");

		page = Config.COMMUNITYBOARD_HTML_ROOT + page + ".htm";

		String content = Files.read(page, activeChar);
		if(content == null)
			if(subcontent == null)
				content = "<html><body><br><br><center>404 Not Found: " + page + "</center></body></html>";
			else
				content = "<html><body>%content%</body></html>";
		if(subcontent != null)
			content = content.replace("%content%", subcontent);

		Matcher m = callsPattern.matcher(content);
		if(m.find())
		{
			StringBuffer sb = new StringBuffer();
			m.reset();
			String method, methodclassName;
			String[] method_args;
			Object ret_subcontent;
			HashMap<String, Object> variables;
			while(m.find())
			{
				method = m.group(1);
				method_args = method.split(":");
				method = method_args[0];
				methodclassName = method_args.length > 1 ? method_args[1] : className;
				method_args = m.group(2).split(",");
				variables = new HashMap<String, Object>();
				variables.put("npc", null);
				ret_subcontent = activeChar.callScripts(methodclassName, method, method_args.length == 0 || m.group(2).isEmpty() ? new Object[] {} : new Object[] { method_args }, variables);
				m.appendReplacement(sb, ret_subcontent.toString());
			}
			m.appendTail(sb);
			content = sb.toString();
		}
		
		separateAndSend(content, activeChar);
	}

	@Override
	public void parsecmd(String command, L2Player activeChar)
	{
		if(command.equals("_bbstop") || command.equals("_bbshome"))
			showTopPage(activeChar, "index", null);
		else if(command.startsWith("_bbstop;"))
			showTopPage(activeChar, command.replaceFirst("_bbstop;", ""), null);
		else
			separateAndSend("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", activeChar);
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player activeChar)
	{}

	private static TopBBSManager _Instance = new TopBBSManager();

	public static TopBBSManager getInstance()
	{
		return _Instance;
	}
}