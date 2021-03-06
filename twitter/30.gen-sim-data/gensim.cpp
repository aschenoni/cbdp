#include <exception>
#include <fstream>
#include <iostream>
#include <map>
#include <set>
#include <string>
#include <vector>
#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string/predicate.hpp>
#include <boost/filesystem.hpp>
#include <boost/format.hpp>
#include <boost/timer/timer.hpp>
#include <signal.h>
#include <execinfo.h>
#include "childtweet.h"
#include "parenttweet.h"
#include "util.h"

using namespace std;


void on_signal(int sig) {
  void *array[50];
  size_t size;

  // get void*'s for all entries on the stack
  size = backtrace(array, 50);

  // print out all the frames to stderr
	cerr << "Error: signal " << sig << endl;
  backtrace_symbols_fd(array, size, STDERR_FILENO);
  exit(1);
}


void _read_parent_tweets(
		const string& fn,
		const string& sn,
		vector<ParentTweet*>& t_set) {
	ifstream ifs(fn);
	if (! ifs.good())
		throw runtime_error(str(boost::format("unable to read file %1%") % fn));
	
	string line;
	long tid = 0;
	string created_at;
	bool real_coord = false;
	string lati;
	string longi;
	string in_reply_to;
	long r_tid = 0;
	string hashtags;
	for (int i = 0; getline(ifs, line); ++ i) {
		if (i % 2 == 0) {
			size_t p, p1 = -1;
			p = p1 + 1; p1 = line.find(" ", p); tid = atol(line.substr(p, p1).c_str());
			p = p1 + 1; p1 = line.find(" ", p); created_at = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); real_coord = (line.substr(p, p1 - p) == "T");
			p = p1 + 1; p1 = line.find(" ", p); lati = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); longi = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); in_reply_to = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); r_tid = atol(line.substr(p, p1 - p).c_str());
			p = p1 + 1; hashtags = line.substr(p);
		} else {
			t_set.push_back(new ParentTweet(tid, sn, created_at, real_coord, lati, longi, in_reply_to, r_tid, hashtags, line));
		}
	}
}


void read_parent_tweets(
		const string& in_dn,
		map<string, vector<ParentTweet*> >& parent_tweets) {
	boost::timer::cpu_timer tmr;
	cout << "Reading parent tweets " << flush;
	namespace fs = boost::filesystem;
	if (! fs::exists(in_dn))
		throw runtime_error(str(boost::format("%1% not exist") % in_dn));
	size_t num_tweets = 0;
	fs::directory_iterator end_iter;
	for (fs::directory_iterator di(in_dn); di != end_iter; ++ di) {
		if (! fs::is_directory(di->status()))
			throw runtime_error(str(boost::format("%1% is not a directory") % *di));
		for (fs::directory_iterator di2(*di); di2 != end_iter; ++ di2) {
			string sn = di2->path().filename().string();
			string fn = di2->path().string();
			if (boost::ends_with(fn, ".tmp"))
				continue;
			vector<ParentTweet*> t_set;
			_read_parent_tweets(fn, sn, t_set);
			if (t_set.size() == 0) {
				continue;
			}
			num_tweets += t_set.size();
			parent_tweets[sn] = t_set;
		}
		if (parent_tweets.size() % 100 == 0)
			cout << "." << flush;
	}
	cout << "\n";
	cout << "  " << tmr.elapsed().wall / 1000000000.0 << " sec.";
	cout << " parents: " << parent_tweets.size() << " sns. " << num_tweets << " tweets.\n";
}


// interpolate missing coords and filter only those with coords
void interpolate_coords_filter(map<string, vector<ParentTweet*> >& parent_tweets) {
	boost::timer::cpu_timer tmr;
	cout << "intepolating coords " << flush;
	map<string, vector<ParentTweet*> > tweets_with_coord;
	int num_tweets = 0;

	for (auto sn_tws: parent_tweets) {
		const string& sn = sn_tws.first;
		string first_lati;
		string first_longi;
		string cur_lati;
		string cur_longi;
		for (auto t: sn_tws.second) {
			if (t->real_coord) {
				if (first_lati.size() == 0) {
					first_lati = t->lati;
					first_longi = t->longi;
				}
				cur_lati = t->lati;
				cur_longi = t->longi;
			} else {
				if (cur_lati.size() != 0) {
					t->lati = cur_lati;
					t->longi = cur_longi;
					t->real_coord = false;
					cout << "F";
				}
			}
		}

		if (first_lati.size() == 0) {
			// no coord. discard the parent tweets.
			for (auto t: sn_tws.second)
				delete t;
			continue;
		}

		for (auto t: sn_tws.second) {
			if (t->real_coord)
				break;
			t->lati = first_lati;
			t->longi = first_longi;
			cout << "F";
		}

		tweets_with_coord[sn] = sn_tws.second;
		num_tweets += sn_tws.second.size();
		if (tweets_with_coord.size() % 30 == 0)
			cout << "." << flush;
	}
	parent_tweets = tweets_with_coord;
	cout << "\n";
	cout << "  " << tmr.elapsed().wall / 1000000000.0 << " sec.";
	cout << " parents: " << parent_tweets.size() << " sns. " << num_tweets << " tweets.\n";
}


void get_parents_by_tid(
		const map<string, vector<ParentTweet*> >& parent_tweets_by_sn,
		map<long, ParentTweet*>& parent_tweets) {
	for (auto pt: parent_tweets_by_sn)
		for (auto t: pt.second)
			parent_tweets[t->tid] = t;
}


void _read_child_tweets_in_file(
		const string& fn,
		const string& sn,
		map<long, ChildTweet*>& child_tweets,
		map<long, ParentTweet*>& parent_tweets) {
	ifstream ifs(fn);
	if (! ifs.good())
		throw runtime_error(str(boost::format("unable to read file %1%") % fn));
	
	string line;
	long tid = 0;
	string created_at;
	bool real_coord = false;
	string lati;
	string longi;
	string in_reply_to;
	long r_tid = 0;
	string r_created_at;
	string hashtags;
	bool has_parent = false;
	for (int i = 0; getline(ifs, line); ++ i) {
		if (i % 2 == 0) {
			size_t p, p1 = -1;
			p = p1 + 1; p1 = line.find(" ", p); tid = atol(line.substr(p, p1).c_str());
			p = p1 + 1; p1 = line.find(" ", p); created_at = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); real_coord = (line.substr(p, p1 - p) == "T");
			p = p1 + 1; p1 = line.find(" ", p); lati = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); longi = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); in_reply_to = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); r_tid = atol(line.substr(p, p1 - p).c_str());
			auto it = parent_tweets.find(r_tid);
			if (it == parent_tweets.end()) {
				has_parent = false;
			} else {
				has_parent = true;
				p = p1 + 1; hashtags = line.substr(p);
				r_created_at = it->second->created_at;
			}
		} else {
			if (has_parent)
				child_tweets[tid] = new ChildTweet(tid, sn, created_at,
						real_coord, lati, longi, in_reply_to,
						r_tid, r_created_at, hashtags, line);
		}
	}
}


// load children with parents
void load_child_tweets(
		const string& in_dn,
		map<long, ChildTweet*>& child_tweets,
		map<long, ParentTweet*>& parent_tweets) {
	boost::timer::cpu_timer tmr;
	cout << "Loading child tweets " << flush;
	namespace fs = boost::filesystem;
	if (! fs::exists(in_dn))
		throw runtime_error(str(boost::format("%1% not exist") % in_dn));
	size_t num_tweets = 0;
	fs::directory_iterator end_iter;
	for (fs::directory_iterator di(in_dn); di != end_iter; ++ di) {
		if (! fs::is_directory(di->status()))
			throw runtime_error(str(boost::format("%1% is not a directory") % *di));
		for (fs::directory_iterator di2(*di); di2 != end_iter; ++ di2) {
			string sn = di2->path().filename().string();
			string fn = di2->path().string();
			if (boost::ends_with(fn, ".tmp"))
				continue;
			_read_child_tweets_in_file(fn, sn, child_tweets, parent_tweets);
		}
		if (child_tweets.size() - num_tweets >= 50) {
			cout << "." << flush;
			num_tweets = child_tweets.size();
		}
	}
	cout << "\n";
	cout << "  " << tmr.elapsed().wall / 1000000000.0 << " sec.";
	cout << " children: " << child_tweets.size() << " tweets.\n";
}


void _write_output(const string& fn, const map<long, ParentTweet*>& tweets) {
	string fn_tmp = fn + ".tmp";
	ofstream ofs(fn_tmp.c_str());
	if (! ofs.is_open())
		throw runtime_error(str(boost::format("unable to open file %1%") % fn_tmp));
	for (auto t: tweets)
		ofs << *(t.second);
	ofs.close();
	boost::filesystem::rename(fn_tmp, fn);
}


void _write_output(const string& fn, const map<long, ChildTweet*>& tweets) {
	string fn_tmp = fn + ".tmp";
	ofstream ofs(fn_tmp.c_str());
	if (! ofs.is_open())
		throw runtime_error(str(boost::format("unable to open file %1%") % fn_tmp));
	for (auto t: tweets)
		ofs << *(t.second);
	ofs.close();
	boost::filesystem::rename(fn_tmp, fn);
}


// generate one file each for all parents and all children sorted by datetime
void write_output(
		const map<long, ParentTweet*>& parent_tweets,
		const map<long, ChildTweet*>& child_tweets,
		const string& out_dir) {
	boost::timer::cpu_timer tmr;
	cout << "Generating output files " << flush;
	namespace fs = boost::filesystem;
	fs::create_directories(out_dir);
	_write_output(out_dir + "/parents", parent_tweets);
	_write_output(out_dir + "/children", child_tweets);
	cout << "\n";
	cout << "  " << tmr.elapsed().wall / 1000000000.0 << " sec.";
}


int main(int argc, char* argv[]) {
  try {
		if (argc != 2) {
			cout << "usage: " << argv[0] << " concise_tweet_dir\n" <<
				"  e.g.: " << argv[0] << " /mnt/multidc-data/pbdp/twitter/raw-concise\n";
			exit(1);
		}
		string in_dir = argv[1];
		string in_p_dir = in_dir + "/parent";
		string in_c_dir = in_dir + "/tweets";
		string out_dir = in_dir + "/../to-replay";
		signal(SIGSEGV, on_signal);

		map<string, vector<ParentTweet*> > parent_tweets_by_sn;
		read_parent_tweets(in_p_dir, parent_tweets_by_sn);
		interpolate_coords_filter(parent_tweets_by_sn);
		map<long, ParentTweet*> parent_tweets;
		get_parents_by_tid(parent_tweets_by_sn, parent_tweets);
		map<long, ChildTweet*> child_tweets;
		load_child_tweets(in_c_dir, child_tweets, parent_tweets);
		write_output(parent_tweets, child_tweets, out_dir);

		for (auto pt: parent_tweets)
			delete pt.second;
		for (auto ct: child_tweets)
			delete ct.second;
  } catch (exception const& e) {
    cerr << typeid(e).name() << " " << e.what() << endl;
  } catch (...) {
    cerr << "Unexpected exception" << endl;
	}
	return 0;
}
