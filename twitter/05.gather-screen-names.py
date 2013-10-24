#! /usr/bin/python

# https://github.com/sixohsix/twitter
# https://dev.twitter.com/docs/api/1.1/post/statuses/filter

import datetime
import dateutil.parser
import errno
import json
import os
import sys
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


def get_twitter_stream():
	fo = open(os.path.expanduser("~/.twitter_auth"))
	data = json.load(fo)
	auth = twitter.OAuth(data["TOKEN"], data["TOKEN_SECRET"], data["CONSUMER_KEY"], data["CONSUMER_SECRET"])
	ts = twitter.TwitterStream(auth=auth)
	return ts


def main(argv):
	if len(argv) != 2:
		sys.exit("Usage: %s screen_name_dir\n"
				"  Ex: %s /mnt/multidc-data/twitter/screen-names"
				% (argv[0], argv[0]))

	out_dn = argv[1]
	out_fn = os.path.join(out_dn, datetime.datetime.now().strftime("%y%m%d-%H%M%S"))

	fo = open(out_fn, "w")

	wait_period = 1.5
	try:
		ts = get_twitter_stream()
		iterator = ts.statuses.filter(track="4sq")
		cnt = 0
		for t in iterator:
			sn = t["user"]["screen_name"]
			# http://stackoverflow.com/questions/1101508/how-to-parse-dates-with-0400-timezone-string-in-python
			#   Thu Oct 24 19:48:28 +0000 2013
			ca = dateutil.parser.parse(t["created_at"]).strftime("%Y%m%d-%H%M%S")
			line = "%s %s %s" % (ca, sn, t["coordinates"]["coordinates"] if t["coordinates"] else None)
			fo.write(line + "\n")
			fo.flush()
			print cnt, line
			cnt += 1
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


if __name__ == "__main__":
	sys.exit(main(sys.argv))
