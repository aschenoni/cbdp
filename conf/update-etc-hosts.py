#! /usr/bin/python

import os
import sys


def _read():
	fo = open("/etc/hosts")
	lines = []
	while True:
		line = fo.readline()
		if len(line) == 0:
			break
		#print line
		lines.append(line)
	fo.close()

	i_begin = None;
	i_end = None;
	for i in range(len(lines)):
		#print lines[i]
		if lines[i].startswith("# Multi-DC project. Begin."):
			i_begin = i;
		elif lines[i].startswith("# Multi-DC project. End."):
			i_end = i;
	#print i_begin, i_end
	return [lines, i_begin, i_end]
	

def _write(lines0, i_begin, i_end, fn):
	lines1 = []
	fo = open(fn)
	while True:
		line = fo.readline()
		if len(line) == 0:
			break
		lines1.append(line)
	fo.close()

	lines2 = []
	if i_begin is None and i_end is None:
		lines2 = lines0
		lines2 += "\n"
		lines2 += lines1
	elif i_begin is not None and i_end is not None:
		# replace the multi-dc block
		lines2 += lines0[0:i_begin]
		lines2 += lines1
		lines2 += lines0[i_end+1:]
	else:
		raise RuntimeError("Unexpected %s %s" % (i_begin, i_end))

	fo1 = open("/etc/hosts", "w")
	fo1.write("".join(lines2))
	fo1.close()


def main(argv):
	[lines, i_begin, i_end] = _read()
	fn = os.path.join(os.path.dirname(os.path.abspath(argv[0])), "etc-hosts")
	_write(lines, i_begin, i_end, fn)


if __name__ == "__main__":
	sys.exit(main(sys.argv))
