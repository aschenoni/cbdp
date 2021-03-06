# Tested with gnuplot 4.6 patchlevel 1 on Ubuntu 13.04

set border back lc rgb "#808080"
set ylabel "tweet to retweet time interval (in sec)"
set xlabel "date time (original tweets)"

set grid back lt 0 lc rgb "#808080"
#set grid ytics back lt 0 lc rgb "#E0E0E0"

INFILE = system("echo $INFILE")

set xdata time
set timefmt "%y%m%d-%H%M%S"
set format x "%m/%d"

set ytics ( \
		"5 sec" 5, \
		"30 secs" 30, \
		"1 min" 60, \
		"10 mins" 10 * 60, \
		"1 hour" 60 * 60, \
		"3 hours" 3 * 60 * 60, \
		"12 hours" 12 * 60 * 60, \
		"1 day" 24 * 60 * 60, \
		"5 days" 5 * 24 * 60 * 60, \
		"10 days" 10 * 24 * 60 * 60)

set xrange ["130407-000000":"130428-000000"]

set logscale y

set samples 1000

#set terminal pngcairo enhanced size 2000,2000
#set terminal pdf enhanced size 10in, 8in
set terminal pdf enhanced size 5.50in, 4.125in
OUTFILE = INFILE . ".pdf"
set output OUTFILE
set pointsize 0.1

set key under nobox width -6 maxrows 1 font ",8"

plot \
INFILE u 2:3 w points lt 7 lc rgb "#00FF00" not, \
""     u 2:4 w lines lw 5 lc rgb "#FF0000" smooth bezier title "24-h mean", \
""     u 2:5 w lines lw 5 lc rgb "#228B22" smooth bezier title "24-h 95th percentile", \
""     u 2:6 w lines lw 5 lc rgb "#0000FF" smooth bezier title "24-h 99th percentile"
