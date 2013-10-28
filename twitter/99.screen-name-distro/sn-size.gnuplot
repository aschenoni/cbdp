# Tested with gnuplot 4.6 patchlevel 1 on Ubuntu 13.04

set border back lc rgb "#808080"
set ylabel "size (raw data dize, MB)"
set xlabel "screen name prefix (first 2 characters)"

IN_FN = "/mnt/multidc-data/twitter/stat/screen-name-distro/size-sn.by-name"
OUT_FN = "/mnt/multidc-data/twitter/stat/screen-name-distro/size-sn.by-name.pdf"

#set xtics font ",10"

set terminal pdf enhanced size 20in, 10in
set output OUT_FN
set pointsize 0.2

set key under nobox maxrows 1

set xrange[-1:2768]

set style fill solid 1 noborder
BOXWIDTH=1

plot \
IN_FN u 0:(0):xticlabel(1) w points pointsize 0.01 lc rgb "#E0E0E0" not, \
IN_FN u 0:($2/1024):(BOXWIDTH) w boxes lc rgb "#FF0000" not
