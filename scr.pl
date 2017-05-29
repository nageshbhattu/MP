#!/usr/bin/perl
$prev = 1;
$str = "";
while(<>){
@a = split /\s+/;
$node = $a[1]-1;
if($a[0] == $prev) { $str = $str." $node:$a[2]"; } 
else { print $str."\n"; $str = "$node:$a[2]"; $prev = $a[0];}
}
print $str."\n";
