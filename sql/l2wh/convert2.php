<?php

$br="
";

print "Thinking...\n";

$query = "";

$fp = @fopen("wh_with_numbers.txt", "r");
$fp2 = @fopen("query.sql", "w");

while(!feof($fp))
{
	$data = @fgets($fp, 10000);

	$split = explode("::", $data);

	$npc_id = $split[0];
	$drop_list = $split[1];

	if(strlen($split[1]) > 0)
	{
		$groups = explode("};{{{", $drop_list);
	
		$group_id = 0;
	
		for($i = 0; $i < count($groups); $i++)
		{
			$group = $groups[$i];
			$group = str_replace("{{{", "", $group);
			$group = explode("}};", $group);
			
			$items = explode("};{", $group[0]);
	
			$group_chance = $group[1] * 10000;
			
			for($j = 0; $j < count($items); $j++)
			{
				$item = explode(";", $items[$j]);
	
				$item_id = $item[0];
				$item_min = $item[1];
				$item_max = $item[2];
				$item_chance = $item[3] * 10000;
				
				$query .= "INSERT INTO `droplist` (`mobId`,`itemId`,`min`,`max`,`sweep`,`chance`,`gid`,`gchance`) VALUES (".$npc_id.",".$item_id.",".$item_min.",".$item_max.",0,".$item_chance.",".$group_id.",".$group_chance.");".$br;
			}
	
			$group_id++;
		}
	}
	

	if(strlen($split[2]) > 0)
	{
		//echo $split[2]."\n";
		$items = explode("};{", $split[2]);

		for($j = 0; $j < count($items); $j++)
		{
			$item = $items[$j];

			$item = str_replace("{", "", $item);
			$item = str_replace("}", "", $item);

			
			
			$item = explode(";", $item);
	
			$item_id = $item[0];
			$item_min = $item[1];
			$item_max = $item[2];
			$item_chance = $item[3] * 10000;

			if($item_chance == 0)
				continue;
				
			$query .= "INSERT INTO `droplist` (`mobId`,`itemId`,`min`,`max`,`sweep`,`chance`,`gid`,`gchance`) VALUES (".$npc_id.",".$item_id.",".$item_min.",".$item_max.",1,".$item_chance.",0,0);".$br;
		}
	}
}

fwrite($fp2, $query);

?>