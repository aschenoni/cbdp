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
set pointsize 0.4

set key under nobox maxrows 2

# Too many items. Hard to get.

plot \
INDIR . "/mdc-s60/userlat-w" u 1:3 w points lt 1 lc rgb "#FF0000" t "W s60", \
INDIR . "/mdc-s70/userlat-w" u 1:3 w points lt 2 lc rgb "#FF0000" t "W s70", \
INDIR . "/mdc-s80/userlat-w" u 1:3 w points lt 6 lc rgb "#FF0000" t "W s80", \
INDIR . "/mdc-p40/userlat-w" u 1:3 w points lt 4 lc rgb "#FF0000" t "W p40", \
INDIR . "/mdc-s60/userlat-r" u 1:3 w points lt 1 lc rgb "#0000FF" t "R s60", \
INDIR . "/mdc-s70/userlat-r" u 1:3 w points lt 2 lc rgb "#0000FF" t "R s70", \
INDIR . "/mdc-s80/userlat-r" u 1:3 w points lt 6 lc rgb "#0000FF" t "R s80", \
INDIR . "/mdc-p40/userlat-r" u 1:3 w points lt 4 lc rgb "#0000FF" t "R p40"

OUTFILE = INDIR . "/plot/userlat-timeseries-w.pdf"
set output OUTFILE
plot \
INDIR . "/mdc-s60/userlat-w" u 1:3 w points lt 1 lc rgb "#FF0000" t "s60", \
INDIR . "/mdc-s70/userlat-w" u 1:3 w points lt 2 lc rgb "#FF0000" t "s70", \
INDIR . "/mdc-s80/userlat-w" u 1:3 w points lt 6 lc rgb "#FF0000" t "s80", \
INDIR . "/mdc-p40/userlat-w" u 1:3 w points lt 4 lc rgb "#FF0000" t "p40"

OUTFILE = INDIR . "/plot/userlat-timeseries-r.pdf"
set output OUTFILE
plot \
INDIR . "/mdc-s60/userlat-r" u 1:3 w points lt 1 lc rgb "#0000FF" t "s60", \
INDIR . "/mdc-s70/userlat-r" u 1:3 w points lt 2 lc rgb "#0000FF" t "s70", \
INDIR . "/mdc-s80/userlat-r" u 1:3 w points lt 6 lc rgb "#0000FF" t "s80", \
INDIR . "/mdc-p40/userlat-r" u 1:3 w points lt 4 lc rgb "#0000FF" t "p40"
