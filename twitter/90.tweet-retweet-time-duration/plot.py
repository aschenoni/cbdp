#! /usr/bin/python

import os
import sys
import time
import subprocess
import multiprocessing


def timing(f):
	def wrap(*args):
		time1 = time.time()
		ret = f(*args)
		time2 = time.time()
		print '%s(): %0.3f s' % (f.func_name, (time2 - time1))
		return ret
	return wrap


@timing
def _run_subprocess(cmd, env):
	subprocess.check_call(cmd, stderr=subprocess.STDOUT, shell=True, env=env)


def run_subprocess(cmd, env):
	p = multiprocessing.Process(target=_run_subprocess, args=(cmd, env,))
	p.start()
	return p


@timing
def Plot():
	jobs = []
	cmd = "gnuplot time-series.gnuplot"
	env = os.environ.copy()
	env["INFILE"] = "/mnt/multidc-data/twitter/stat/tweet-retweet-interval/time-series"
	jobs.append(run_subprocess(cmd, env));

	cmd = "gnuplot histo.gnuplot"
	env["INFILE"] = "/mnt/multidc-data/twitter/stat/tweet-retweet-interval/histo-24"
	env["BUCKET_SIZE"] = "24"
	jobs.append(run_subprocess(cmd, env));

	env["INFILE"] = "/mnt/multidc-data/twitter/stat/tweet-retweet-interval/histo-1"
	env["BUCKET_SIZE"] = "1"
	jobs.append(run_subprocess(cmd, env));

	cmd = "gnuplot cdf.gnuplot"
	env["INFILE"] = "/mnt/multidc-data/twitter/stat/tweet-retweet-interval/cdf"
	jobs.append(run_subprocess(cmd, env));

	for j in jobs:
		j.join();


def main(argv):
	Plot()


if __name__ == "__main__":
    sys.exit(main(sys.argv))
