#include <exception>
#include <fstream>
#include <iostream>
#include <map>
#include <string>
#include <vector>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/format.hpp>
#include <boost/scoped_ptr.hpp>
#include <boost/timer/timer.hpp>
#include <signal.h>
#include <execinfo.h>

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


string sec_to_datetime(int s) {
	stringstream ss;
	int d = s / (24 * 60 * 60);
	if (d >= 1) {
		ss << d << "d ";
		s = s % (24 * 60 * 60);
	}
	int h = s / (60 * 60);
	if (h >= 1) {
		ss << h << "h ";
		s = s % (60 * 60);
	}
	int m = s / 60;
	if (m >= 1) {
		ss << m << "m ";
		s = s % 60;
	}
	ss << s << "s";
	return ss.str();
}


class Tweet {
public:
	long tid;
	string sn;
	string created_at;
	bool real_coord;
	string lati;
	string longi;
	string in_reply_to;
	long r_tid;
	string hashtags;
	string text;

	Tweet(long tid_, const string& sn_, string& created_at_, bool real_coord_, string& lati_, string& longi_,
			string& in_reply_to_, long r_tid_, string& hashtags_, string& text_)
		: tid(tid_), 
		sn(sn_),
		created_at(created_at_), 
		real_coord(real_coord_), 
		lati(lati_), 
		longi(longi_), 
		in_reply_to(in_reply_to_),
		r_tid(r_tid_),
		hashtags(hashtags_),
		text(text_) {
	}

	//std::string const to_str() const;

	bool operator < (const Tweet& t) const {
		return tid < t.tid;
	}
};


ostream& operator<< (ostream& os, const Tweet& t) {
	os << t.tid << " "
		<< t.sn << " "
		<< t.created_at << " "
		<< (t.real_coord ? "T" : "F") << " "
		<< t.lati << " "
		<< t.longi << " "
		<< t.in_reply_to << " "
		<< t.r_tid << " "
		<< t.hashtags << "\n"
		<< t.text << "\n";
	return os;
}


map<long, Tweet*> read_parent_tweets(string fn) {
	map<long, Tweet*> tweets;
	ifstream ifs(fn.c_str());
	if (! ifs.good())
		throw runtime_error(str(boost::format("unable to read file %1%") % fn));
	string line;
	long tid = 0;
	string sn;
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
			p = p1 + 1; p1 = line.find(" ", p); sn = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); created_at = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); real_coord = (line.substr(p, p1 - p) == "T");
			p = p1 + 1; p1 = line.find(" ", p); lati = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); longi = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); in_reply_to = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); r_tid = atol(line.substr(p, p1 - p).c_str());
			p = p1 + 1; hashtags = line.substr(p);
		} else {
			tweets[tid] = new Tweet(tid, sn, created_at, real_coord, lati, longi, in_reply_to, r_tid, hashtags, line);
		}
	}
	cout << "read " << tweets.size() << " parent tweets.\n";
	return tweets;
}


vector<Tweet*> read_child_tweets(string fn) {
	vector<Tweet*> tweets;
	ifstream ifs(fn.c_str());
	if (! ifs.good())
		throw runtime_error(str(boost::format("unable to read file %1%") % fn));
	string line;
	long tid = 0;
	string sn;
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
			p = p1 + 1; p1 = line.find(" ", p); sn = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); created_at = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); real_coord = (line.substr(p, p1 - p) == "T");
			p = p1 + 1; p1 = line.find(" ", p); lati = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); longi = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); in_reply_to = line.substr(p, p1 - p);
			p = p1 + 1; p1 = line.find(" ", p); r_tid = atol(line.substr(p, p1 - p).c_str());
			p = p1 + 1; hashtags = line.substr(p);
		} else {
			tweets.push_back(new Tweet(tid, sn, created_at, real_coord, lati, longi, in_reply_to, r_tid, hashtags, line));
		}
	}
	cout << "read " << tweets.size() << " child tweets.\n";
	return tweets;
}


class TimeDurs {
	class Entry {
	friend std::ostream& operator<< (std::ostream& os, const Entry& e);

	public:
		long p_tid;
		string p_ca;	// created_at
		boost::posix_time::ptime p_ca_ptime;
		vector<long> durs;	// in sec
		string ravg; // running avg
		string r95; // running 95th percentile
		string r99;

	private:
		boost::posix_time::ptime ptime_(int y, int m, int d, int h, int min, int s) {
			return boost::posix_time::ptime(
					boost::gregorian::date(y + 2000, m, d), 
					boost::posix_time::hours(h)
					+ boost::posix_time::minutes(min)
					+ boost::posix_time::seconds(s));
		}

		boost::posix_time::ptime ptime_(
				const string& y, const string& m, const string& d,
				const string& h, const string& min, const string& s) {
			return ptime_(
					atoi(y.c_str()),
					atoi(m.c_str()),
					atoi(d.c_str()),
					atoi(h.c_str()),
					atoi(min.c_str()),
					atoi(s.c_str()));
		}

	public:
		Entry(
				long p_tid_,
				const string& p_ca_,
				const string& c_ca)
			: p_tid(p_tid_), p_ca(p_ca_), ravg("-"), r95("-"), r99("-") {
				p_ca_ptime = ptime_(
						p_ca.substr(0, 2),
						p_ca.substr(2, 2),
						p_ca.substr(4, 2),
						p_ca.substr(7, 2),
						p_ca.substr(9, 2),
						p_ca.substr(11, 2));
				boost::posix_time::ptime c_ca_ptime = ptime_(
						c_ca.substr(0, 2),
						c_ca.substr(2, 2),
						c_ca.substr(4, 2),
						c_ca.substr(7, 2),
						c_ca.substr(9, 2),
						c_ca.substr(11, 2));
				boost::posix_time::time_duration td = c_ca_ptime - p_ca_ptime;
				durs.push_back(td.total_seconds());
		}

		void Add(const string& c_ca) {
			boost::posix_time::ptime c_ca_ptime = ptime_(
					c_ca.substr(0, 2),
					c_ca.substr(2, 2),
					c_ca.substr(4, 2),
					c_ca.substr(7, 2),
					c_ca.substr(9, 2),
					c_ca.substr(11, 2));
			boost::posix_time::time_duration td = c_ca_ptime - p_ca_ptime;
			durs.push_back(td.total_seconds());
		}
	};

private:
	friend std::ostream& operator<< (std::ostream& os, const Entry& e);
	map<long, Entry*> _entries;

	void _Add(long p_tid, const string& p_ca, const string& c_ca) {
		auto it = _entries.find(p_tid);
		if (it == _entries.end()) {
			_entries[p_tid] = new Entry(p_tid, p_ca, c_ca);
		} else {
			it->second->Add(c_ca);
		}
	}

	void _WriteStat(ofstream& ofs) {
		int min = 0;
		int max = 0;
		int sum = 0;
		float sum_sq = 0;
		int cnt = 0;
		float avg;
		//float sd;
		bool first = true;
		for (auto e: _entries) {
			for (long d: e.second->durs) {
				++ cnt;
				if (first) {
					first = false;
					min = max = sum = d;
					sum_sq = d * d;
				} else {
					if (min > d) min = d;
					else if (max < d) max = d;
					sum += d;
					sum_sq += (d * d);
				}
			}
		}
		avg = (float) sum / cnt;
		float sd = sqrt(sum_sq / cnt - (avg * avg));

		ofs
			<< "#"
			<< " cnt=" << cnt
			<< " min=" << min
			<< " max=" << max
			<< " avg=" << avg
			<< " sd=" << sd 
			<< "\n";
	}

	void _CalcStat(const vector<int>& durs_, string& ravg, string& r95, string& r99) {
		vector<int> durs(durs_);
		sort(durs.begin(), durs.end());

		int cnt = 0;
		int sum = 0;
		// not the most efficient way. can be optimized later.
		for (int d: durs) {
			++ cnt;
			sum += d;
		}
		float avg = (float) sum / cnt;
		//cout << cnt << " " << sum << " " << avg << "\n";
		stringstream ss;
		ss.setf(ios::fixed, ios::floatfield);
		ss.precision(0);
		ss << avg;
		ravg = ss.str();

		int cnt90 = (int) (cnt * 0.95);
		ss.str(string());
		ss << durs[cnt90];
		r95 = ss.str();
		int cnt99 = (int) (cnt * 0.99);
		ss.str(string());
		ss << durs[cnt99];
		r99 = ss.str();
	}

	// 1-hr running avg, 90, 95, 99 percentile.
	void _GenRunningStat() {
		bool first = true;
		boost::posix_time::ptime start_ca;
		int time_window_hour = 12;

		for (auto e: _entries) {
			if (first) {
				first = false;
				start_ca = e.second->p_ca_ptime + boost::posix_time::hours(time_window_hour);
			}
			const boost::posix_time::ptime& cur_ca = e.second->p_ca_ptime;
			if (start_ca > cur_ca)
				continue;
			boost::posix_time::ptime cur_ca_minus_time_window = cur_ca - boost::posix_time::hours(time_window_hour);

			vector<int> durs;
			for (auto e1: _entries) {
				if (cur_ca_minus_time_window <= e1.second->p_ca_ptime
					&& e1.second->p_ca_ptime < cur_ca) {
					for (long d: e1.second->durs)
						durs.push_back(d);
				}
			}
			_CalcStat(durs, e.second->ravg, e.second->r95, e.second->r99);
		}
	}
	
public:
	TimeDurs(
			const map<long, Tweet*>& p_tweets,
			const vector<Tweet*>& c_tweets) {
		for (auto c: c_tweets) {
			// find the matching parent tweet
			auto it = p_tweets.find(c->r_tid);
			if (it == p_tweets.end())
				throw runtime_error(str(boost::format("unable to read parent tweet %1%") % c->r_tid));
			_Add(it->first, it->second->created_at, c->created_at);
		}
		_GenRunningStat();
	}

	~TimeDurs() {
		for (auto i: _entries)
			delete i.second;
	}

	void WriteAll(const string& fn) {
		ofstream ofs(fn.c_str());
		if (! ofs.is_open())
			throw runtime_error(str(boost::format("unable to open file %1%") % fn));
		_WriteStat(ofs);
		ofs << "#\n"
			<< "# parent_tid p_created_at time_dir_in_sec\n";
		for (auto e: _entries)
			ofs << *(e.second);
		cout << "created " << fn << "\n";
	}

	void WriteHisto(
			const string& fn_,
			const int bucket_size_in_hour,
			const int max_num_buckets = -1) {
		int bs = bucket_size_in_hour * 3600;
		// range, cnt
		//   range: [i*bs, (i+1)*bs)
		map<int, double> histo;
		int total_cnt = 0;
		for (auto e: _entries) {
			for (long d: e.second->durs) {
				int bucket = d / bs;
				auto it = histo.find(bucket);
				if (it == histo.end()) {
					histo[bucket] = 1.0;
				} else {
					it->second += 1.0;
				}
				++ total_cnt;
			}
		}
		for (auto& h: histo)
			h.second /= total_cnt;
		string fn = str(boost::format("%1%-%2%") % fn_ % bucket_size_in_hour);
		ofstream ofs(fn.c_str());
		if (! ofs.is_open())
			throw runtime_error(str(boost::format("unable to open file %1%") % fn));
		ofs << "# bucket size=" << bucket_size_in_hour << "\n";
		for (auto h: histo) {
			if (max_num_buckets == -1 || h.first < max_num_buckets)
				ofs << (h.first * bucket_size_in_hour) << " " << h.second << "\n";
		}
		cout << "created " << fn << "\n";
	}

	void WriteCDF(const string& fn) {
		ofstream ofs(fn.c_str());
		if (! ofs.is_open())
			throw runtime_error(str(boost::format("unable to open file %1%") % fn));
		vector<long> durs;
		for (auto e: _entries)
			for (long d: e.second->durs)
				durs.push_back(d);
		sort(durs.begin(), durs.end());
		int size = durs.size();
		// label points
		float lp[] = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95, 0.99, 1.0};
		int lp_size = sizeof(lp) / sizeof(float);
		int lp_i = 0;
		bool print_label = false;
		for (int i = 0 ; i < size; ++ i) {
			float y = (float) (i+1) / size;
			if (lp_i < lp_size && y >= lp[lp_i]) {
				print_label = true;
				++ lp_i;
			}
			ofs << durs[i] << " " << y << " ";
			if (print_label) {
				ofs << y << " \"" << sec_to_datetime(durs[i]) << "\"";
				print_label = false;
			} else
				ofs << "- \"\"";
			ofs << "\n";
		}
		cout << "created " << fn << "\n";
	}
};

std::ostream& operator<< (std::ostream& os, const TimeDurs::Entry& e) {
	os.precision(0);
	os.setf(ios::fixed, ios::floatfield);
	for (auto d: e.durs)
		os << e.p_tid << " " << e.p_ca << " " << d << " " << e.ravg << " " << e.r95 << " " << e.r99 << "\n";
	return os;
}


int main(int argc, char* argv[]) {
  try {
		signal(SIGSEGV, on_signal);

		string p_fn = "/mnt/multidc-data/twitter/raw-concise/to-replay/parents";
		string c_fn = "/mnt/multidc-data/twitter/raw-concise/to-replay/children";
		string out_fn = "/mnt/multidc-data/twitter/stat/tweet-retweet-interval/time-series";
		string out_hist_fn = "/mnt/multidc-data/twitter/stat/tweet-retweet-interval/histo";
		string out_cdf_fn = "/mnt/multidc-data/twitter/stat/tweet-retweet-interval/cdf";

		map<long, Tweet*> p_tweets = read_parent_tweets(p_fn);
		vector<Tweet*> c_tweets = read_child_tweets(c_fn);
		TimeDurs td(p_tweets, c_tweets);
		td.WriteAll(out_fn);
		td.WriteHisto(out_hist_fn, 24, 24);
		td.WriteHisto(out_hist_fn, 1, 24);
		td.WriteCDF(out_cdf_fn);

		// free p_tweets and c_tweets. okay for a one-time tool.
		return 0;
  } catch (exception const& e) {
    cerr << typeid(e).name() << ": " << e.what() << endl;
  } catch (...) {
    cerr << "Unexpected exception" << endl;
	}
	return 1;
}
