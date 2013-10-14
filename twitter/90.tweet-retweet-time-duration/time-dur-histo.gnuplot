# Tested with gnuplot 4.6 patchlevel 3

#set terminal pdf enhanced size 10in, 8in
set terminal pdf enhanced size 5.50in, 4.125in
INFILE = system("echo $INFILE")
OUTFILE = INFILE . ".pdf"
set output OUTFILE
BS = system("echo $BUCKET_SIZE")

set key under nobox

set border back lc rgb "#808080"
set xlabel "parent/child tweet time durations (in hours)"
set ylabel "# of tweet pairs"

set grid ytics back lt 0 lc rgb "#E0E0E0"

set yrange [:1]
set pointsize 0.3
set style fill solid 0.4 noborder
BOXWIDTH=0.7 * BS

set xtics rotate by 90 right font ",5"

set title ""

plot \
INFILE u 1:(0):xticlabel(1) w points pointsize 0.01 lc rgb "#E0E0E0" not, \
INFILE u ($1 + BS * 0.5):2:(BOXWIDTH) w boxes lc rgb "#0000FF" not, \
INFILE u ($1 + BS * 0.5):2:(sprintf("%.3f", $2)) w labels font ",4.5" offset 0,0.5 tc rgb "blue" not
