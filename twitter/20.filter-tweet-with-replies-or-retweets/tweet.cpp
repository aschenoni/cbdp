#define BOOST_SPIRIT_THREADSAFE
#include <boost/algorithm/string/join.hpp>
#include <boost/foreach.hpp>
#include <boost/format.hpp>
#include <boost/locale.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <boost/regex.hpp>
#include "tweet.h"
#include "util.h"

using namespace std;


string concise_datetime(const string& dt) {
	// input format: Mon Mar 18 20:24:24 +0000 2013
	//               012345678901234567890123456789
	string mon_(dt.substr(4, 3));
	string mon;
	if (mon_ == "Jan") mon = "01";
	else if (mon_ == "Feb") mon = "02";
	else if (mon_ == "Mar") mon = "03";
	else if (mon_ == "Apr") mon = "04";
	else if (mon_ == "May") mon = "05";
	else if (mon_ == "Jun") mon = "06";
	else if (mon_ == "Jul") mon = "07";
	else if (mon_ == "Aug") mon = "08";
	else if (mon_ == "Sep") mon = "09";
	else if (mon_ == "Oct") mon = "10";
	else if (mon_ == "Nov") mon = "11";
	else if (mon_ == "Dec") mon = "12";
	else throw runtime_error(str(boost::format("unable to parse month %1%") % mon_));

	string day = dt.substr(8, 2);
	string hr = dt.substr(11, 2);
	string min = dt.substr(14, 2);
	string sec = dt.substr(17, 2);
	string year = dt.substr(28, 2);

	return year + mon + day + "-" + hr + min + sec;
}


Tweet::Tweet() {
}

Tweet::Tweet(boost::property_tree::ptree::value_type& v) {
	tid = 0;
	real_coord = false;
	r_tid = 0;

	using boost::property_tree::ptree;
	BOOST_FOREACH(ptree::value_type &v2, v.second.get_child("")) {
		if (strcmp(v2.first.data(), "id") == 0)
			tid = atol(v2.second.data().c_str());
		else if (strcmp(v2.first.data(), "retweeted_status") == 0) {
			BOOST_FOREACH(ptree::value_type &v3, v2.second.get_child("")) {
				if (strcmp(v3.first.data(), "id") == 0)
					r_tid = atol(v3.second.data().c_str());
			}
		} else _ParseJsonTree(v2);
	}
}


Tweet::Tweet(boost::property_tree::ptree::value_type& v, const long r_tid_) {
	tid = r_tid_;
	real_coord = false;
	r_tid = 0;	// we trace immediate parent only
	bool retweeted_status_exist = false;

	using boost::property_tree::ptree;
	BOOST_FOREACH(ptree::value_type &v2, v.second.get_child("")) {
		if (strcmp(v2.first.data(), "retweeted_status") == 0) {
			retweeted_status_exist = true;
			BOOST_FOREACH(ptree::value_type &v3, v2.second.get_child("")) {
				if (strcmp(v3.first.data(), "id") == 0) {
					long tid_ = atol(v3.second.data().c_str());
					if (tid != tid_)
						throw runtime_error(str(boost::format("unexpected. tid=%1% tid_=%2%") % tid % tid_));
				} else if (strcmp(v3.first.data(), "user") == 0) {
					BOOST_FOREACH(ptree::value_type &v4, v3.second.get_child("")) {
						if (strcmp(v4.first.data(), "screen_name") == 0)
							sn0 = v4.second.data();
					}
				} else _ParseJsonTree(v3);
			}
		}
	}
	if (! real_coord) {
		coord.push_back("0.0");
		coord.push_back("0.0");
	}
	if (! retweeted_status_exist)
		throw runtime_error(str(boost::format("retweeted_status does not exist?! r_tid_=%1%") % r_tid_));
}

void Tweet::_ParseJsonTree(boost::property_tree::ptree::value_type& v) {
	using boost::property_tree::ptree;

	if (strcmp(v.first.data(), "created_at") == 0)
		created_at = concise_datetime(v.second.data());
	else if (strcmp(v.first.data(), "text") == 0)
		text = v.second.data();
	else if (strcmp(v.first.data(), "entities") == 0) {
		BOOST_FOREACH(ptree::value_type &v2, v.second.get_child("")) {
			if (strcmp(v2.first.data(), "hashtags") == 0) {
				BOOST_FOREACH(ptree::value_type &v3, v2.second.get_child("")) {
					BOOST_FOREACH(ptree::value_type &v4, v3.second.get_child("")) {
						if (strcmp(v4.first.data(), "text") == 0)
							hashtags.insert(v4.second.data());
					}
				}
				break;
			}
		}
	} else if (strcmp(v.first.data(), "in_reply_to_status_id") == 0) {
		in_reply_to_status_id = (v.second.data() == "null" ? 0 : atol(v.second.data().c_str()));
	} else if (strcmp(v.first.data(), "coordinates") == 0) {
		BOOST_FOREACH(ptree::value_type &v2, v.second.get_child("")) {
			if (strcmp(v2.first.data(), "coordinates") == 0) {
				BOOST_FOREACH(ptree::value_type &v3, v2.second.get_child("")) {
					coord.push_back(v3.second.data());
					real_coord = true;
				}
			}
		}
	}
}


string const Tweet::to_str() const {
	return str(boost::format("tid: %1%\n"
				"created_at: %2%\n"
				"text: %3%\n"
				"hashtags: %4%\n")
			% tid % created_at % text % boost::algorithm::join(hashtags, " "));
}


string Tweet::concise_str() const {
	stringstream ss;
	ss << tid << " "
		<< created_at << " "
		<< (real_coord ? "T" : "F") << " ";
	if (coord.size() != 2)
		throw runtime_error(str(boost::format("coord needs to be set first. coord.size()=%1%") % coord.size()));
	ss << coord[0] << " " << coord[1] << " "
		<< in_reply_to_status_id << " " << r_tid << " "
		<< boost::algorithm::join(hashtags, " ") << "\n"
		<< boost::regex_replace(text, boost::regex("[\n|\r]"), " ") << "\n";
	// doesn't seem to generate utf8 string
	//ss << boost::locale::conv::to_utf<char>(boost::regex_replace(text, boost::regex("[\n|\r]"), " "), "Latin1") << "\n";
	return ss.str();
}


bool Tweet::operator < (const Tweet& t) const {
	return tid < t.tid;
}


std::ostream & operator<<(std::ostream &os, const Tweet& t) {
  return os << t.to_str();
}


void read_tweet_json(
		const string* raw_data,
		map<long, Tweet>& tweets,
		map<string, set<Tweet> >& p_tweets) {
  using boost::property_tree::ptree;
	stringstream ss(*raw_data);
  ptree pt;
  boost::property_tree::read_json(ss, pt);

  BOOST_FOREACH(ptree::value_type &v, pt.get_child("")) {
		Tweet t(v);
		tweets[t.tid] = t;

		// gather immediate paretent Tweet
		if (t.r_tid != 0) {
			Tweet re(v, t.r_tid);
			auto it = p_tweets.find(t.sn0);
			if (it == p_tweets.end()) {
				set<Tweet> tweet_set;
				tweet_set.insert(re);
				p_tweets[re.sn0] = tweet_set;
			} else {
				it->second.insert(re);
			}
		}
	}
}
