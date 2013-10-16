# Tested with gnuplot 4.6 patchlevel 1 on Ubuntu 13.04

set border back lc rgb "#808080"
set xlabel "tweet to retweet time interval (in sec)"

set grid back lt 0 lc rgb "#808080"
#set grid ytics back lt 0 lc rgb "#E0E0E0"

INFILE = system("echo $INFILE")

set format y "%.1f"

set xtics ( \
		"5 secs" 5, \
		"1 min" 60, \
		"1 hour" 60 * 60, \
		"1 day" 24 * 60 * 60, \
		"10 days" 10 * 24 * 60 * 60)

set logscale x

#set terminal pdf enhanced size 10in, 8in
set terminal pdf enhanced size 5.50in, 4.125in
OUTFILE = INFILE . ".pdf"
set output OUTFILE
set pointsize 0.1

plot \
INFILE u 1:2 w points pointtype 7 lc rgb "#FFA0A0" not, \
""     u 1:2:4 w labels font ",7" offset 0,0.5 tc rgb "blue" not, \
""     u 1:3 w points pointsize 0.2 pointtype 7 lc rgb "#0000FF" not
