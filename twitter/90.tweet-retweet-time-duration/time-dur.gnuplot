# Tested with gnuplot 4.6 patchlevel 3

set key under nobox

set border back lc rgb "#808080"
set ylabel "parent to child time durations (in sec)"
set xlabel "date time"

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
		"20 mins" 20 * 60, \
		"1 hour" 60 * 60, \
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

plot \
INFILE u 2:3 w points lt 7 lc rgb "#0000FF" not, \
""     u 2:4 w lines lw 5 lc rgb "#00FF00" smooth bezier title "24-h mean"

#""     u 2:3 w lines smooth bezier lw 5 lc rgb "#FF0000" title "mean", \
