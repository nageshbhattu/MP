#!/bin/bash -x
for k in 10 20 30 40 50 ; 
do 
~/Semi-Supervised/sgt_light_linux/sgt_buildknngraph -k "$k" "$1".txt bgout_"$1"_"$k".txt;
file=bgout_"$1"_"$k".txt
var="$(wc -l $file |cut -d' ' -f 1)"
var="$(expr $var - 2)"
echo $var
tail -n "$var" bgout_"$1"_"$k".txt | ./scr.pl | tee "$1"_"$k".txt > /dev/null
paste labelFile.txt "$1"_"$k".txt | tee "$1"-mp_"$k".txt > /dev/null
java -cp dist/MP.jar mp.MP "$1"-mp_"$k".txt 5000 3 4000 0.01 0.2 
done;
