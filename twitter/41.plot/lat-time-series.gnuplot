# Tested with gnuplot 4.6 patchlevel 1 on Ubuntu 13.04

set border back lc rgb "#808080"
set ylabel "user-perceived latencies (ms)"
set xlabel "date time"

INDIR = system("echo $INDIR")

set xdata time
set timefmt "%y%m%d-%H%M%S"
set format x "%H:%M:%S"

set xtics font ",10"

set terminal pdf enhanced size 5.50in, 4.125in
OUTFILE = INDIR . "/plot/userlat-timeseries.pdf"
set output OUTFILE
set pointsize 0.5

set key under nobox maxrows 1

# Too many items. Hard to get.

plot \
INDIR . "/mdc-s60/userlat-r" u 1:3 w points t "mdc-s60", \
INDIR . "/mdc-s60/userlat-w" u 1:3 w points t "mdc-s60", \
INDIR . "/mdc-s70/userlat-r" u 1:3 w points t "mdc-s70", \
INDIR . "/mdc-s70/userlat-w" u 1:3 w points t "mdc-s70", \
INDIR . "/mdc-s80/userlat-r" u 1:3 w points t "mdc-s80", \
INDIR . "/mdc-s80/userlat-w" u 1:3 w points t "mdc-s80", \
INDIR . "/mdc-p40/userlat-r" u 1:3 w points t "mdc-p40", \
INDIR . "/mdc-p40/userlat-w" u 1:3 w points t "mdc-p40"

#INDIR . "/mdc-s60/userlat-r" u 1:3 w points lt 1 lc rgb "#FF0000" t "mdc-s60", \
#INDIR . "/mdc-s60/userlat-w" u 1:3 w points lt 0 lc rgb "#FF0000" t "mdc-s60", \
#INDIR . "/mdc-s70/userlat-r" u 1:3 w points lt 1 lc rgb "#0000FF" t "mdc-s70", \
#INDIR . "/mdc-s70/userlat-w" u 1:3 w points lt 0 lc rgb "#0000FF" t "mdc-s70", \
#INDIR . "/mdc-s80/userlat-r" u 1:3 w points lt 1 lc rgb "#A52A2A" t "mdc-s80", \
#INDIR . "/mdc-s80/userlat-w" u 1:3 w points lt 0 lc rgb "#A52A2A" t "mdc-s80", \
#INDIR . "/mdc-p40/userlat-r" u 1:3 w points lt 1 lc rgb "#006400" t "mdc-p40", \
#INDIR . "/mdc-p40/userlat-w" u 1:3 w points lt 0 lc rgb "#006400" t "mdc-p40"
