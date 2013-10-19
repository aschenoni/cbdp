#! /usr/bin/python

import errno
import multiprocessing
import os
import string
import subprocess
import sys
import time


def timing(f):
	def wrap(*args):
		time1 = time.time()
		ret = f(*args)
		time2 = time.time()
		print '%s(): %0.3f s' % (f.func_name, (time2 - time1))
		return ret
	return wrap


def run_subprocess_fn(fn, args):
	p = multiprocessing.Process(target=fn, args=args)
	p.start()
	return p


def mkdir_p(path):
	try:
		os.makedirs(path)
	except OSError as e:
		if e.errno == errno.EEXIST and os.path.isdir(path):
			pass
		else:
			raise


@timing
def _PlotTimeSeries(dn):
	cmd = "gnuplot lat-time-series.gnuplot"
	env = os.environ.copy()
	env["INDIR"] = dn
	subprocess.check_call(cmd, stderr=subprocess.STDOUT, shell=True, env=env)


def _GenLatAll(dn):
	w_min = 0
	w_max = 0
	w_sum = 0.0
	w_first = True
	r_min = 0
	r_max = 0
	r_sum = 0.0
	r_first = True
	num_nodes = 0

	fo_out = open(os.path.join(dn, "userlat-all"), "w")
	fo_out.write("# node w_min w_max w_avg w_sd r_min r_max r_avg r_sd\n")
	for node in ("mdc-s60", "mdc-s70", "mdc-s80", "mdc-p40"):
		fo_out.write(node + " ")
		for m in ("w", "r"):
			fn = os.path.join(dn, node, m == "w" and "userlat-w" or "userlat-r")
			fo = open(fn)
			for i in range(2):
				line = fo.readline()
				if i != 1:
					continue
				line = line.rstrip()
				tokens = string.split(line, " ")
				if m == "w":
					if w_first:
						w_first = False
						w_min = int(tokens[1])
						w_max = int(tokens[2])
						w_sum = float(tokens[3])
					else:
						w_min = min(w_min, int(tokens[1]))
						w_max = max(w_max, int(tokens[2]))
						w_sum += float(tokens[3])
					num_nodes += 1
				else:
					if r_first:
						r_first = False
						r_min = int(tokens[1])
						r_max = int(tokens[2])
						r_sum = float(tokens[3])
					else:
						r_min = min(r_min, int(tokens[1]))
						r_max = max(r_max, int(tokens[2]))
						r_sum += float(tokens[3])
				fo_out.write(" ".join(tokens[1:5]))
				fo_out.write(m == "w" and " " or "\n")
	w_avg = w_sum / num_nodes
	r_avg = r_sum / num_nodes
	# avg of sd is omitted here
	fo_out.write("all %d %d %f 0.0 %d %d %f 0.0\n" %
			(w_min, w_max, w_avg, r_min, r_max, r_avg))


def __GenLatCDF(dn, op):
	lats = []
	for node in ("mdc-s60", "mdc-s70", "mdc-s80", "mdc-p40"):
		fn = os.path.join(dn, node, "userlat-" + op)
		fo = open(fn)
		while True:
			line = fo.readline()
			if len(line) == 0:
				break
			if line[0] == "#":
				continue
			line = line.rstrip()
			tokens = string.split(line, " ")
			if len(tokens) == 0:
				continue
			lats.append(int(tokens[2]))
	lats.sort()

	size = len(lats)
	fo_out = open(os.path.join(dn, "userlat-cdf-" + op), "w")
	fo_out.write("# lat_in_ms %\n")
	for i in range(size):
		fo_out.write("%d %f\n" % (lats[i], ((float)(i+1))/size))


def _GenLatCDF(dn):
	__GenLatCDF(dn, "r")
	__GenLatCDF(dn, "w")


@timing
def _PlotErrorbar(dn):
	_GenLatAll(dn)
	cmd = "gnuplot lat-errorbar.gnuplot"
	env = os.environ.copy()
	env["INDIR"] = dn
	env["INFILE"] = os.path.join(dn, "userlat-all")
	subprocess.check_call(cmd, stderr=subprocess.STDOUT, shell=True, env=env)


@timing
def _PlotCDF(dn):
	_GenLatCDF(dn)
	cmd = "gnuplot lat-cdf.gnuplot"
	env = os.environ.copy()
	env["INDIR"] = dn
	env["INFILE"] = os.path.join(dn, "userlat-cdf")
	subprocess.check_call(cmd, stderr=subprocess.STDOUT, shell=True, env=env)


@timing
def Plot(dn):
	mkdir_p(os.path.join(dn, "plot"))

	jobs = []
	jobs.append(run_subprocess_fn(_PlotTimeSeries, (dn,)));
	jobs.append(run_subprocess_fn(_PlotErrorbar, (dn,)));
	jobs.append(run_subprocess_fn(_PlotCDF, (dn,)));

	for j in jobs:
		j.join();


def GetLatestLogDir():
	log_dir = "/mnt/multidc-data/twitter/replay-log"
	cmd = "echo `ls -t %s` | awk '{print $1;}'" % log_dir
	dn = subprocess.check_output(cmd, stdin=None, stderr=subprocess.STDOUT, shell=True)
	dn = os.path.join(log_dir, dn.rstrip())
	#print dn
	return dn


def main(argv):
	dn = GetLatestLogDir()
	Plot(dn)


if __name__ == "__main__":
    sys.exit(main(sys.argv))
