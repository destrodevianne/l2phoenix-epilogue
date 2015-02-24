<?php

print "Thinking...\n";

$fp = @fopen("item_pch.txt", "r");
$fp2 = @fopen("wh_with_numbers.txt", "w");
$rez = @file("wh.txt");

while(!feof($fp))
{
	$data = @fgets($fp, 10000);
	$split = explode(" = ", $data);

	$split[1] = round($split[1]);

	for($i = 0; $i < count($rez); $i++)
		$rez[$i] = str_replace($split[0], $split[1], $rez[$i]);
}

for($i = 0; $i < count($rez); $i++)
	fwrite($fp2, $rez[$i]);

?>