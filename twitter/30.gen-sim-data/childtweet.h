#include <string>
#include <iostream>

class ChildTweet {
public:
	long tid;
	std::string sn;
	std::string created_at;
	bool real_coord;
	std::string lati;
	std::string longi;
	std::string in_reply_to;
	long r_tid;
	std::string r_created_at;
	std::string hashtags;
	std::string text;

	ChildTweet(long tid_, const std::string& sn_, std::string& created_at_,
			bool real_coord_, std::string& lati_, std::string& longi_,
			std::string& in_reply_to_, long r_tid_, std::string& r_created_at_,
			std::string& hashtags_, std::string& text_);

	bool operator < (const ChildTweet& t) const;
};


std::ostream& operator<< (std::ostream& os, const ChildTweet& t);
