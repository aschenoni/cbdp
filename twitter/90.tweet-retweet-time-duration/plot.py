#! /usr/bin/python

import os
import sys
import time
import subprocess


def timing(f):
	def wrap(*args):
		time1 = time.time()
		ret = f(*args)
		time2 = time.time()
		print '%s(): %0.3f s' % (f.func_name, (time2 - time1))
		return ret
	return wrap


@timing
def Plot():
	cmd = "gnuplot time-dur.gnuplot"
	env = os.environ.copy()
	env["INFILE"] = "/mnt/multidc-data/twitter/raw-concise/to-replay/tweet-retweet-time-duration"
	subprocess.check_call(cmd, stderr=subprocess.STDOUT, shell=True, env=env)

	cmd = "gnuplot time-dur-histo.gnuplot"
	env = os.environ.copy()
	env["INFILE"] = "/mnt/multidc-data/twitter/raw-concise/to-replay/tweet-retweet-time-duration-histo"
	subprocess.check_call(cmd, stderr=subprocess.STDOUT, shell=True, env=env)


def main(argv):
	Plot()


if __name__ == "__main__":
    sys.exit(main(sys.argv))
