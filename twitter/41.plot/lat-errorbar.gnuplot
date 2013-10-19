# Tested with gnuplot 4.6 patchlevel 1 on Ubuntu 13.04

set border back lc rgb "#808080"
set ylabel "user-perceived latencies (ms)"
set xlabel "date time"

INDIR = system("echo $INDIR")
INFILE = system("echo $INFILE")

set xtics font ",10"

set terminal pdf enhanced size 5.50in, 4.125in
OUTFILE = INDIR . "/plot/userlat-errorbar.pdf"
set output OUTFILE
set pointsize 0.2

set key under nobox maxrows 1

set style fill solid 0.3 noborder
BOXWIDTH=0.1

plot \
INFILE u 0:(0):xticlabel(1) w points pointsize 0.01 lc rgb "#E0E0E0" not, \
INFILE u ($0-0.08):4:(BOXWIDTH) w boxes lc rgb "#FF0000" t "Write", \
INFILE u ($0-0.08):4:2:3 w errorbars lt 7 lc rgb "#FF0000" not, \
INFILE u ($0+0.08):8:(BOXWIDTH) w boxes lc rgb "#0000FF" t "Read", \
INFILE u ($0+0.08):8:6:7 w errorbars lt 7 lc rgb "#0000FF" not, \
INFILE u ($0-0.08):4:(sprintf("%.2f", $4)) w labels font ",7" right offset -0.8,0 tc rgb "#FF0000" not, \
INFILE u ($0+0.08):8:(sprintf("%.2f", $8)) w labels font ",7" left offset 0.8,0 tc rgb "#0000FF" not
