# Tested with gnuplot 4.6 patchlevel 3

set terminal pdf enhanced size 10in, 8in
INFILE = system("echo $INFILE")
OUTFILE = INFILE . ".pdf"
set output OUTFILE

set key under nobox

set border back lc rgb "#808080"
set xlabel "parent/child tweet time durations (in secs)"
set ylabel "# of tweet pairs"

set grid back lt 0 lc rgb "#808080"
#set grid ytics back lt 0 lc rgb "#E0E0E0"

#set logscale xy

set pointsize 0.3

plot \
INFILE u 1:2 w points linetype 7 linecolor rgb "#FF0000"



#
#
#
## Tested with gnuplot 4.6 patchlevel 1
#
##set terminal pdf enhanced dashed size 5.50in, 4.125in
#
#set key under nobox
#
#set border back lc rgb "#808080"
#set ylabel "Time (sec)"
#set xlabel "File name\nsize" offset 0,-1
#
#set grid y back lt 0 lc rgb "#E0E0E0"
#
##set title TITLE
#
#set logscale xy
#
#X_BEGIN=-0.20
#X_END=0.20
#X_LEN=X_END-X_BEGIN
#
#I_MIN=0
#I_MAX=5
#I_LEN=I_MAX - I_MIN
#X_STEP=X_LEN/I_LEN
#
#_offset(i) = X_BEGIN + X_STEP * (i - I_MIN)
#
#set xrange [I_MIN - 0.5 : I_MAX + 0.5]
#set style fill solid 0.2 noborder
#BOXWIDTH=0.060
#
#plot \
#INFILE_SS3   u 0:(0):xticlabel(1) w points pointsize 0.01 lc rgb "#E0E0E0" not, \
#INFILE_SS3   u ($0 + _offset(0)):2:3:4:(BOXWIDTH) w boxerrorbars lt 1 lc rgb "blue" t "spark - s3n files. left - first, right - second and later", \
#INFILE_SS3   u ($0 + _offset(0)):2:(sprintf("%.2f", $2)) w labels right offset -0.75,0 tc rgb "blue" not, \
#INFILE_SS3   u ($0 + _offset(1)):5:6:7:(BOXWIDTH) w boxerrorbars lt 1 lc rgb "blue" not, \
#INFILE_SS3   u ($0 + _offset(1)):5:(sprintf("%.2f", $5)) w labels right offset -0.75,0 tc rgb "blue" not, \
#INFILE_STS3  u ($0 + _offset(2)):2:3:4:(BOXWIDTH) w boxerrorbars lt 1 lc rgb "red" t "spark - tachyon - s3n files. left - first, right - second and later", \
#INFILE_STS3  u ($0 + _offset(2)):2:(sprintf("%.2f", $2)) w labels right offset -0.75,0 tc rgb "red" not, \
#INFILE_STS3  u ($0 + _offset(3)):5:6:7:(BOXWIDTH) w boxerrorbars lt 1 lc rgb "red" not, \
#INFILE_STS3  u ($0 + _offset(3)):5:(sprintf("%.2f", $5)) w labels right offset -0.75,0 tc rgb "red" not, \
#INFILE_STPS3 u ($0 + _offset(4)):2:3:4:(BOXWIDTH) w boxerrorbars lt 1 lc rgb "#006400" t "spark - tachyon - peer tachyon - s3n files. left - first, right - second and later", \
#INFILE_STPS3 u ($0 + _offset(4)):2:(sprintf("%.2f", $2)) w labels left offset 0.75,0 tc rgb "#006400" not, \
#INFILE_STPS3 u ($0 + _offset(5)):5:6:7:(BOXWIDTH) w boxerrorbars lt 1 lc rgb "#006400" not, \
#INFILE_STPS3 u ($0 + _offset(5)):5:(sprintf("%.2f", $5)) w labels left offset 0.75,0 tc rgb "#006400" not
