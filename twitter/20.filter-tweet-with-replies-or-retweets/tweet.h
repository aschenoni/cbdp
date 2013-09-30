#include <map>
#include <set>
#include <string>
#include <vector>
#include <boost/property_tree/ptree.hpp>


class Tweet {
public:
  long tid;
	std::string created_at;
	std::string text;
	std::set<std::string> hashtags;
	bool real_coord;
	std::vector<std::string> coord;	// longi, lati
	long in_reply_to_status_id;

	//long retweet_count;	// not needed for now
	// retweeted (src tweet)
	long r_tid;

	// screen_name. used only for parent tweets.
	std::string sn0;

  Tweet();
  Tweet(boost::property_tree::ptree::value_type& v);
	Tweet(boost::property_tree::ptree::value_type& v, const long r_tid_);
	void _ParseJsonTree(boost::property_tree::ptree::value_type& v);

	std::string concise_str() const;
	std::string const to_str() const;

	bool operator < (const Tweet& t) const;
};

std::ostream & operator<<(std::ostream &os, const Tweet& t);


void read_tweet_json(
		const std::string* raw_data,
		std::map<long, Tweet>& tweets,
		std::map<std::string, std::set<Tweet> >& p_tweets);
