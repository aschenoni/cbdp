#include "childtweet.h"

using namespace std;


ChildTweet::ChildTweet(long tid_, const string& sn_, string& created_at_,
		bool real_coord_, string& lati_, string& longi_,
		string& in_reply_to_, long r_tid_, string& r_created_at_,
		string& hashtags_, string& text_)
	: tid(tid_), 
	sn(sn_),
	created_at(created_at_), 
	real_coord(real_coord_), 
	lati(lati_), 
	longi(longi_), 
	in_reply_to(in_reply_to_),
	r_tid(r_tid_),
	r_created_at(r_created_at_),
	hashtags(hashtags_),
	text(text_) {
}

bool ChildTweet::operator < (const ChildTweet& t) const {
	return tid < t.tid;
}


ostream& operator<< (ostream& os, const ChildTweet& t) {
	os << t.tid << " "
		<< t.sn << " "
		<< t.created_at << " "
		<< (t.real_coord ? "T" : "F") << " "
		<< t.lati << " "
		<< t.longi << " "
		<< t.in_reply_to << " "
		<< t.r_tid << " "
		<< t.r_created_at << " "
		<< t.hashtags << "\n"
		<< t.text << "\n";
	return os;
}
