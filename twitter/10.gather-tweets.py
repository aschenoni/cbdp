#! /usr/bin/python

# Referenced:
#   http://www.pythondiary.com/blog/May.06,2012/getting-started-python-and-twitter.html

import datetime
import dateutil.parser
import errno
import fnmatch
import json
import os
import sys
import time
import twitter
import urllib2
import traceback
import httplib


def mkdir_p(path):
	try:
		os.makedirs(path)
	except OSError as exc: # Python >2.5
		if exc.errno == errno.EEXIST and os.path.isdir(path):
			pass
		else: raise


def touch(fname, times=None):
	with file(fname, 'a'):
		os.utime(fname, times)


def file_exists(fname):
	try:
		with open(fname):
			return True
	except IOError:
		return False


def get_twitter_client():
	fo = open(os.path.expanduser("~/.twitter_auth"))
	data = json.load(fo)
	auth = twitter.OAuth(data["TOKEN"], data["TOKEN_SECRET"], data["CONSUMER_KEY"], data["CONSUMER_SECRET"])
	tc = twitter.Twitter(auth=auth)
	tc.account.verify_credentials()
	return tc


def _get_statuses_user_timeline(tc, sn, max_id_):
	wait_period = 1.5
	while True:
		try:
			# https://dev.twitter.com/docs/api/1.1/get/statuses/user_timeline
			# count=200 is the maximum.
			r = tc.statuses.user_timeline(count=200, exclude_replies=False,
					include_rts=True, max_id=max_id_, screen_name=sn)
			return r
		# referenced http://www.people.fas.harvard.edu/~astorer/twitter/twitter.org
		except twitter.api.TwitterHTTPError as e:
			if e.e.code == 401:
				raise RuntimeError("NOT_AUTHORIZED sn=%s" % sn)
			elif e.e.code == 404:
				raise RuntimeError("NOT_EXIST sn=%s" % sn)
			elif e.e.code == 429:
				# you are being rate limited
				sys.stdout.write("Rate limiting. sleep for 15.5 mins ")
				for i in range(15 * 6 + 3):
					sys.stdout.write(".")
					sys.stdout.flush()
					time.sleep(10)
				print ""
			elif e.e.code in (500, 502, 503):
				# these errors are Twitter's fault!
				print "Encountered %i Error. Will retry in %d seconds" % (e.e.code, wait_period)
				time.sleep(wait_period)
				wait_period *= 1.5
			else:
				print str(e)
				raise e
		except urllib2.URLError as e:
			print "URLError: %s. Will retry in %d seconds" % (e.strerror, wait_period)
			time.sleep(wait_period)
			wait_period *= 1.5
		except httplib.HTTPException as e:
			print "HTTPException: %s. Will retry in %d seconds" % (e, wait_period)
			time.sleep(wait_period)
			wait_period *= 1.5
		except:
			print "".join(traceback.format_exception(sys.exc_info()[0], sys.exc_info()[1], sys.exc_info()[2]))
			raise


def get_statuses_user_timeline(tc, sn, out_dir_root, req_max_tid, req_o_dt):
	out_dn = os.path.join(out_dir_root, sn[0:2])
	mkdir_p(out_dn)

	exist_fns = []
	for fn in os.listdir(out_dn):
		# delete tmp file
		if fnmatch.fnmatch(fn, "%s.*.tmp" % sn):
			print "deleting a tmp file %s." % os.path.join(out_dn, fn)
			os.remove(os.path.join(out_dn, fn))
		elif fnmatch.fnmatch(fn, "%s.not_authorized" % sn) or fnmatch.fnmatch(fn, "%s.not_exist" % sn):
			sys.stdout.write("already checked. ")
			return
		elif fnmatch.fnmatch(fn, "%s.*" % sn):
			exist_fns.append(fn)
	exist_y_tid = None
	if len(exist_fns) > 0:
		exist_fns.sort()
		tokens = exist_fns[-1].split(".")
		exist_y_tid = long(tokens[3])
		if (req_max_tid <= exist_y_tid):
			sys.stdout.write("%s young enough. " % exist_fns[-1])
			return

	tmp_fns = []
	max_tid = req_max_tid
	while True:
		try:
			r = _get_statuses_user_timeline(tc, sn, max_tid)
			sys.stdout.write("%d " % len(r))
			sys.stdout.flush()
			if len(r) == 0:
				# no tweets in a given date range. touch a not_exist file to
				# prevent recrawling the data.
				if max_tid == req_max_tid:
					open(os.path.join(out_dn, sn + ".not_exist"), "w").close()
				# else, no more tweets. either way, break.
				break
			# get youngest/oldest datatime/tweet ids
			#y_tid = r[0]["id"]
			o_tid = long(r[-1]["id"])
			y_dt = dateutil.parser.parse(r[0]["created_at"]).strftime("%y%m%d-%H%M%S")
			o_dt = dateutil.parser.parse(r[-1]["created_at"]).strftime("%y%m%d-%H%M%S")
			if exist_y_tid >= o_tid:
				raise RuntimeError("TO_IMPLEMENT handle overlapping tids %d >= %d" % (exist_y_tid, o_tid))
			# out file name format:
			#   sn.youngest_datetime.oldest_datetime.youngest_tid.oldest_tid
			#   Jessi_Alvarenga.130914-010203.121229-020304.288306461497491458.198306461497491458
			# Note that the max_tid here is the requested one. not the youngest one
			# in the output file.
			out_fn = os.path.join(out_dn, "%s.%s.%s.%d.%d" % (sn, y_dt, o_dt, max_tid, o_tid))
			out_fn_tmp = out_fn + ".tmp"
			mkdir_p(out_dn)
			fo = open(out_fn_tmp, "w")
			# json.dumps(r) generates smaller output then str(r), which is an AST
			# format.  specifying the option indent makes it more readible, but makes
			# the output bigger.  you can use external tool to indent it, e.g., cat
			# filename | python -mjson.tool
			fo.write(json.dumps(r))
			fo.close()
			tmp_fns.append(out_fn_tmp)

			# we've gone back far enough
			if req_o_dt >= o_dt:
				break
			max_tid = o_tid - 1
		except RuntimeError as e:
			if len(e.args) > 0:
				if e.args[0].startswith("NOT_AUTHORIZED"):
					open(os.path.join(out_dn, sn + ".not_authorized"), "w").close()
					return
				elif e.args[0].startswith("NOT_EXIST"):
					open(os.path.join(out_dn, sn + ".not_exist"), "w").close()
					return
			raise
	
	for tfn in tmp_fns:
		os.rename(tfn, tfn[:-4])


def read_input_screen_names(screenname_filename):
	sn_set = set()
	fo = open(screenname_filename)
	while True:
		line = fo.readline()
		if len(line) == 0:
			break
		sn_set.add(line.split()[1])
	print "loaded %d screen names from file %s" % (len(sn_set), screenname_filename)
	return sn_set


def get_tweets(sn_set, tc, out_dir_root, req_max_tid, req_o_dt):
	num_sns = len(sn_set)
	i = 0
	for sn in sn_set:
		i += 1
		sys.stdout.write("%s %d/%d %s " %
				(datetime.datetime.now().strftime("%y%m%d-%H%M%S"), i, num_sns, sn))
		sys.stdout.flush()
		get_statuses_user_timeline(tc, sn, out_dir_root, req_max_tid, req_o_dt)
		sys.stdout.write("\n")
		sys.stdout.flush()


def main(argv):
	if len(argv) != 3:
		sys.exit("Usage: %s screen_name_file output_dir\n"
				"  Ex: %s /mnt/multidc-data/twitter/screen-names/131024-163428 /mnt/multidc-data/twitter/raw"
				% (argv[0], argv[0]))

	screenname_filename = argv[1]
	out_dir_root = argv[2]
	# 130407-000000 minus 30 days
	req_o_dt = "130307-000000"
	req_max_tid = 328297506721103874L

	tc = get_twitter_client()
	sn_set = read_input_screen_names(screenname_filename)
	get_tweets(sn_set, tc, out_dir_root, req_max_tid, req_o_dt)


if __name__ == "__main__":
	sys.exit(main(sys.argv))
