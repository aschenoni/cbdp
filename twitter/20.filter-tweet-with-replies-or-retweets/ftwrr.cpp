#include <exception>
#include <fstream>
#include <iostream>
#include <map>
#include <set>
#include <string>
#include <thread>
#include <boost/algorithm/string/predicate.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/filesystem.hpp>
#include <boost/foreach.hpp>
#include <boost/format.hpp>
#include <boost/scoped_ptr.hpp>
#include <boost/thread/thread.hpp>
#include <boost/timer/timer.hpp>
#include <signal.h>
#include <execinfo.h>
#include "tweet.h"
#include "util.h"
#include "concurrent_queue.h"

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


class SnRawData {
public:
	string sn;
	map<string, string*> fn_data_map;

	SnRawData(const string& sn_,
			const map<string, string*>& fn_data_map_)
	: sn(sn_), fn_data_map(fn_data_map_) {
	}
};


void _get_existing_out_sn(const string& out_dn_, set<string>& sns) {
	string out_dn = out_dn_ + "/tweets";
	namespace fs = boost::filesystem;
	if (! fs::exists(out_dn))
		return;
	fs::directory_iterator end_iter;
	for (fs::directory_iterator di(out_dn); di != end_iter; ++ di) {
		if (! fs::is_directory(di->status()))
			throw runtime_error(str(boost::format("%1% is not a directory") % *di));
		for (fs::directory_iterator di2(*di); di2 != end_iter; ++ di2) {
			string fn = di2->path().filename().string();
			if (boost::ends_with(fn, ".tmp")) {
				cout << "found tmp file " << fn << " deleting...\n";
				boost::filesystem::remove(di2->path());
			} else {
				string key = ".no_tweets";
				size_t found = fn.rfind(key);
				if (found != string::npos)
					fn.replace(found, key.length(), "");
				sns.insert(fn);
			}
		}
	}
	cout << sns.size() << " screen names already exist in dir " << out_dn << "\n";
}


void _get_tweet_fns(
		const char* in_dir,
		const set<string>& exist_sns,
		map<string, set<string> >& sn_fns,
		int num_consumers) {
	namespace fs = boost::filesystem;
	fs::path path_(in_dir);
	fs::directory_iterator end_iter;

	for (fs::directory_iterator di(path_); di != end_iter; ++ di) {
		if (! fs::is_directory(di->status()))
			throw runtime_error(str(boost::format("%1% is not a directory") % *di));
		for (fs::directory_iterator di2(*di); di2 != end_iter; ++ di2) {
			//cout << di2->path().parent_path().string() << " " << di2->path().filename().string() << "\n";
			string fn = di2->path().filename().string();
			if (boost::ends_with(fn, ".not_authorized")
					|| boost::ends_with(fn, ".not_exist")
					|| boost::ends_with(fn, ".tmp"))
				continue;
			string sn = fn.substr(0, fn.find('.'));
			// skip already existing sns
			if (exist_sns.find(sn) != exist_sns.end())
				continue;
			if (sn_fns.find(sn) == sn_fns.end()) {
				set<string> ss;
				ss.insert(fn);
				sn_fns[sn] = ss;
			} else {
				sn_fns[sn].insert(fn);
			}
		}
	}
	cout << "Found " << sn_fns.size() << " new screen names in dir " << in_dir << ".\n";
}


void _read_raw_files(
		const char* in_dir,
		const map<string, set<string> >& sn_fns,
		ConcurrentQueue<SnRawData*>& q,
		int num_consumers) {
	int cnt = 0;
	int total_cnt = sn_fns.size();
	boost::timer::cpu_timer tmr;
	typedef map<string, set<string> > map_type;
	BOOST_FOREACH(const map_type::value_type& v, sn_fns) {
		const string& sn = v.first;
		map<string, string*> fn_data_map;
		BOOST_FOREACH(const string& s, v.second) {
			const string fn = string(in_dir) + "/" + s.substr(0, 2) + "/" + s;
			string* data = read_file_raw(fn.c_str());
			fn_data_map[fn] = data;
		}
		q.put(new SnRawData(sn, fn_data_map));

		++ cnt;
		if (cnt % 50 == 0) {
			double laptime = tmr.elapsed().wall / 1000000000.0;
			cout << cur_datetime() << " read " << cnt << "/" << total_cnt << " " << cnt / laptime << " sn/sec" << endl;
		}
	}
	double laptime = tmr.elapsed().wall / 1000000000.0;
	cout << cur_datetime() << " read " << cnt << "/" << total_cnt << " " << cnt / laptime << " sn/sec" << endl;

	// put end markers
	for (int i = 0; i < num_consumers; ++ i)
		q.put(NULL);
}


void raw_input_reader(
		const char* in_dir,
		const string& out_dir,
		ConcurrentQueue<SnRawData*>& q,
		int num_consumers) {
	try {
		set<string> exist_sns;
		_get_existing_out_sn(out_dir, exist_sns);
		map<string, set<string> > sn_fns;
		_get_tweet_fns(in_dir, exist_sns, sn_fns, num_consumers);
		_read_raw_files(in_dir, sn_fns, q, num_consumers);
		return;
  } catch (std::exception const& e) {
    cerr << typeid(e).name() << " " << e.what() << endl;
  } catch (...) {
    cerr << "Unexpected exception" << endl;
	}
	exit(1);
}


void _fill_in_missing_coord(map<long, Tweet>& tweets) {
	vector<string>* first_real_coord = NULL;
	vector<string>* cur_coord = NULL;

	for (map<long, Tweet>::iterator i = tweets.begin(); i != tweets.end(); ++ i) {
		if (i->second.real_coord) {
			if (! first_real_coord)
				first_real_coord = &(i->second.coord);
			cur_coord = &(i->second.coord);
		} else {
			if (cur_coord) {
				i->second.coord = *cur_coord;
			}
		}
	}

	if (! first_real_coord)
		throw runtime_error("NO_REAL_COORD");

	for (map<long, Tweet>::iterator i = tweets.begin(); i != tweets.end(); ++ i) {
		if (! i->second.real_coord)
			i->second.coord = *first_real_coord;
		else
			break;
	}
}


string date_plus_day(const string& d, int offset) {
	boost::gregorian::date date_(
			atoi(d.substr(0, 2).c_str()) + 2000,
			atoi(d.substr(2, 2).c_str()),
			atoi(d.substr(4, 2).c_str()));
	date_ += boost::gregorian::date_duration(offset);
	return boost::gregorian::to_iso_string(date_).substr(2);
}


// filter by date range. filter tweets with hashtags and retweets.
// TODO: keep tweets with replies too
void _filter(map<long, Tweet>& tweets, const string& date_o, const string& date_y) {
	string date_y_next_day = date_plus_day(date_y, 1);

	map<long, Tweet> filtered1;
	for (auto i: tweets) {
		long tid = i.first;
		Tweet& t = i.second;
		if (date_o > t.created_at || date_y_next_day < t.created_at)
			continue;
		filtered1[tid] = t;
	}

	map<long, Tweet> filtered2;
	for (auto i: filtered1) {
		long tid = i.first;
		Tweet& t = i.second;
		if (t.hashtags.size() > 0 && (t.in_reply_to_status_id != 0 || t.r_tid != 0))
			filtered2[tid] = t;
	}

	tweets = filtered2;
}


void _filter_by_date_range(map<string, set<Tweet> >& tweets, const string& date_o, const string& date_y) {
	string date_y_next_day = date_plus_day(date_y, 1);
	map<string, set<Tweet> > tweets_in_range;
	for (auto kv: tweets) {
		for (auto t: kv.second) {
			if (date_o > t.created_at || date_y_next_day < t.created_at)
				continue;
			auto j = tweets_in_range.find(kv.first);
			if (j == tweets_in_range.end()) {
				set<Tweet> t_set;
				t_set.insert(t);
				tweets_in_range[kv.first] = t_set;
			} else {
				j->second.insert(t);
			}
		}
	}
	tweets = tweets_in_range;
}


void _filter_parents_with_children(
		map<string, set<Tweet> >& parents,
		const map<long, Tweet>& children) {
	set<long> p_tids_in_children;
	for (auto c: children)
		p_tids_in_children.insert(c.second.r_tid);

	map<string, set<Tweet> > parents_with_children;
	for (auto p_kv: parents) {
		for (auto t: p_kv.second) {
			auto i = p_tids_in_children.find(t.tid);
			if (i != p_tids_in_children.end()) {
				auto j = parents_with_children.find(p_kv.first);
				if (j == parents_with_children.end()) {
					set<Tweet> t_set;
					t_set.insert(t);
					parents_with_children[p_kv.first] = t_set;
				} else {
					j->second.insert(t);
				}
			}
		}
	}
	//cout_ << parents.size() << " " << parents_with_children.size() << "\n";
	parents = parents_with_children;
}


void _filter_children_with_parents(
		const map<string, set<Tweet> >& parents,
		map<long, Tweet>& children) {
	set<long> p_tids;
	for (auto p_kv: parents)
		for (auto t: p_kv.second)
			p_tids.insert(t.tid);

	map<long, Tweet> c_with_parents;
	for (auto c_kv: children) {
		if (p_tids.find(c_kv.second.r_tid) != p_tids.end())
			c_with_parents[c_kv.first] = c_kv.second;
	}
	//cout_ << children.size() << " " << c_with_parents.size() << "\n";
	children = c_with_parents;
}


void _write_concise_tweets(
		const string& out_dn_,
		const string& sn,
		map<long, Tweet>& tweets) {
	string out_dn = out_dn_ + "/tweets/" + sn.substr(0, 2);
	boost::filesystem::create_directories(out_dn);
	string out_fn = out_dn + "/" + sn;
	string out_fn_tmp = out_fn + ".tmp";

	if (tweets.size() == 0) {
		// touch file sn.no_tweets
		string fn = out_fn + ".no_tweets";
		ofstream ofs(fn.c_str(), std::fstream::trunc);
		return;
	}
	ofstream ofs(out_fn_tmp.c_str());
	if (! ofs.is_open())
		throw runtime_error(str(boost::format("unable to open file %1%") % out_fn_tmp));
	for (auto i: tweets)
		ofs << i.second.concise_str();
	ofs.close();
	boost::filesystem::rename(out_fn_tmp, out_fn);
}


map<string, set<Tweet> > p_tweets;
boost::mutex p_tweets_merge_lock;
void merge_p_tweets_per_thread(const map<string, set<Tweet> >& tw) {
	boost::mutex::scoped_lock lock(p_tweets_merge_lock);
	for (const auto& kv: tw) {
		auto it = p_tweets.find(kv.first);
		if (it == p_tweets.end())
			p_tweets[kv.first] = kv.second;
		else
			it->second.insert(kv.second.begin(), kv.second.end());
	}
}


void parse_json_tweets_write_output(
		const string& out_dir,
		ConcurrentQueue<SnRawData*>& q,
		const char* date_o,
		const char* date_y) {
	try {
		//boost::thread::id th_id = boost::this_thread::get_id();

		map<string, set<Tweet> > p_tweets_1;	// all parent tweets gathered by this thread
		while (true) {
			boost::scoped_ptr<SnRawData> srd(q.get());
			if (! srd.get())
				break;
			string& sn = srd->sn;
			//cout_ << th_id << " " << sn << endl;
			map<string, string*>& fn_data_map = srd->fn_data_map;
			map<long, Tweet> tweets;
			map<string, set<Tweet> > p_tweets_2;	// parent tweets from sn
			for (map<string, string*>::iterator i = fn_data_map.begin(); i != fn_data_map.end(); ++ i) {
				boost::scoped_ptr<string> data(i->second);
				read_tweet_json(data.get(), tweets, p_tweets_2);
			}

			try {
				_fill_in_missing_coord(tweets);
				_filter(tweets, date_o, date_y);

				_filter_by_date_range(p_tweets_2, date_o, date_y);
				_filter_parents_with_children(p_tweets_2, tweets);
				_filter_children_with_parents(p_tweets_2, tweets);
				
				_write_concise_tweets(out_dir, sn, tweets);
				//cout_ << th_id << " " << sn << endl;

				// merge the inner map to the outer map. the inner map is used for
				// easily discard parents tweets with child tweets with no coordinates.
				for (const auto& kv: p_tweets_2) {
					auto it = p_tweets_1.find(kv.first);
					if (it == p_tweets_1.end())
						p_tweets_1[kv.first] = kv.second;
					else
						it->second.insert(kv.second.begin(), kv.second.end());
				}
				// cout << p_tweets_2.size() << "/" << p_tweets_1.size() << " parent tweets" << endl;
			} catch (runtime_error& e) {
				if (boost::starts_with(e.what(), "NO_REAL_COORD")) {
					cout << (boost::format("NO_REAL_COORD. ignoring. sn: %1%") % sn) << "\n";
					// touch file sn.no_tweets
					string out_dn = out_dir + "/tweets/" + sn.substr(0, 2);
					boost::filesystem::create_directories(out_dn);
					string fn = out_dn + "/" + sn + ".no_tweets";
					ofstream ofs(fn.c_str(), std::fstream::trunc);
					ofs.close();
				} else
					throw;
			}
		}

		merge_p_tweets_per_thread(p_tweets_1);
		return;
  } catch (std::exception const& e) {
    cerr << typeid(e).name() << " " << e.what() << endl;
  } catch (...) {
    cerr << "Unexpected exception" << endl;
	}
	exit(1);
}


void _get_existing_parent_tweets(
		const string& fn,
		map<long, string>& existing_tweets) {
	ifstream ifs(fn);
	if (ifs.good()) {
		string lines;
		string line;
		long tid = 0;
		for (int i = 0; getline(ifs, line); ++ i) {
			if (i % 2 == 0) {
				size_t space = line.find(" ");
				if (space == string::npos)
					throw runtime_error(str(boost::format("unexpected format %1% in file %2%") % line % fn));
				tid = atol(line.substr(0, space).c_str());
				lines = line + "\n";
			} else {
				lines += (line + "\n");
				existing_tweets[tid] = lines;
			}
		}
	}
}


void write_parent_tweets(const string& out_dir) {
	boost::timer::cpu_timer tmr;
	cout << "Writing parent tweets ... " << flush;
	string out_dn0 = out_dir + "/parent";
	for (const auto& kv: p_tweets) {
		const string& sn = kv.first;
		string out_dn = out_dn0 + "/" + sn.substr(0, 2);
		boost::filesystem::create_directories(out_dn);
		string out_fn = out_dn + "/" + sn;

		// do not overwrite existing parent tweets. some of them may have already
		// had their coord interpolated.
		map<long, string> tweets_to_be_written;
		_get_existing_parent_tweets(out_fn, tweets_to_be_written);

		// merge new tweets
		for (const auto& t: kv.second)
			if (tweets_to_be_written.find(t.tid) == tweets_to_be_written.end())
				tweets_to_be_written[t.tid] = t.concise_str();

		ofstream ofs(out_fn.c_str(), std::fstream::trunc);
		for (auto t: tweets_to_be_written)
			ofs << t.second;
	}
	cout << tmr.elapsed().wall / 1000000000.0 << "sec\n";
}


int main(int argc, char* argv[]) {
  try {
		if (argc != 4) {
			cout << "usage: " << argv[0] << " raw_tweet_dir date_oldest date_youngest\n" <<
				"  e.g.: " << argv[0] << " /mnt/multidc-data/twitter/raw 130407 130427";
			exit(1);
		}
		const char* in_dir = argv[1];
		const char* date_o = argv[2];
		const char* date_y = argv[3];
		string out_dir = string(argv[1]) + "-concise";
		signal(SIGSEGV, on_signal);
		
		int num_consumers = thread::hardware_concurrency();
		ConcurrentQueue<SnRawData*> q(num_consumers * 20);
		boost::thread th_p(&raw_input_reader, in_dir, out_dir, boost::ref(q), num_consumers);

		vector<boost::thread*> th_c;
		for (int i = 0; i < num_consumers; ++ i)
			th_c.push_back(new boost::thread(&parse_json_tweets_write_output, out_dir, boost::ref(q), date_o, date_y));

		th_p.join();
		for (int i = 0; i < num_consumers; ++ i) {
			th_c[i]->join();
			delete th_c[i];
		}

		write_parent_tweets(out_dir);
  } catch (std::exception const& e) {
    cerr << typeid(e).name() << " " << e.what() << endl;
  } catch (...) {
    cerr << "Unexpected exception" << endl;
	}
	return 0;
}
