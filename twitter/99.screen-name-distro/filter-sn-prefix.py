#! /usr/bin/python

import string
import sys


def main(argv):
	in_fn = "/mnt/multidc-data/pbdp/twitter/stat/screen-name-distro/size-sn"
	out_fn = "/mnt/multidc-data/pbdp/twitter/stat/screen-name-distro/size-sn.by-name"

	sn_size = {}

	fo_in = open(in_fn)
	while True:
		line = fo_in.readline()
		if len(line) == 0:
			break
		line = line.rstrip()
		t = string.split(line, "\t")
		size = t[0]
		sn = t[1]
		sn_size[sn] = size
	
	fo_out = open(out_fn, "w")
	cur_sn_fc = ""
	for sn in sorted(sn_size.keys()):
		size = sn_size[sn]
		sn_fc = sn[0:1]
		if cur_sn_fc != sn_fc:
			cur_sn_fc = sn_fc
			#print "%s* %s" % (sn_fc, size)
			fo_out.write("\"%s\" %s\n" % (sn_fc, size))
		else:
			#print "- %s" % (size)
			fo_out.write("\"\" %s\n" % size)
	print "Created file %s" % out_fn

if __name__ == "__main__":
    sys.exit(main(sys.argv))
