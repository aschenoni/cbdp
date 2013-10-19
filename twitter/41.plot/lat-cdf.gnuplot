# Tested with gnuplot 4.6 patchlevel 1 on Ubuntu 13.04

set border back lc rgb "#808080"
set xlabel "User-perceived latencies (ms)"

INDIR = system("echo $INDIR")
INFILE = system("echo $INFILE")

set terminal pdf enhanced size 5.50in, 4.125in
OUTFILE = INDIR . "/plot/userlat-cdf.pdf"
set output OUTFILE

set key under nobox maxrows 1

#set logscale x

plot \
INFILE . "-w" u 1:2 w lines lc rgb "#FF0000" t "Write", \
INFILE . "-r" u 1:2 w lines lc rgb "#0000FF" t "Read" 
