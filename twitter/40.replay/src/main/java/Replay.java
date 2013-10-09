import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class Replay {
	final double _replay_time;
	final long _st_begin_milli;
	final long _st_end_milli;
	final long _rt_begin_milli;
	String _hostname;
	DC _dc;
	List<Tweet> _p_tweets;
	CassClient _cc;

	Replay(long replay_time, String rt_start_time)
		throws java.net.UnknownHostException, java.io.FileNotFoundException,
										java.io.IOException, java.text.ParseException {
		_replay_time = replay_time * 1000.0;

		SimpleDateFormat sdf0 = new SimpleDateFormat("yyMMdd-hhmmss");
		_st_begin_milli = sdf0.parse("130407-000000").getTime();
		_st_end_milli = sdf0.parse("130428-000000").getTime();
		_rt_begin_milli = sdf0.parse(rt_start_time).getTime() + 4000L;

		java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
		_hostname = addr.getHostName();
		//System.out.println(_hostname);
		_dc = new DC();
		_cc = new CassClient();
	}

	List<Tweet> _ReadTweets(String fn)
		throws java.io.FileNotFoundException, java.io.IOException {
		List<Tweet> tweets = new ArrayList<Tweet>();
		BufferedReader br = new BufferedReader(new FileReader(fn));
		while (true) {
			String line0 = br.readLine();
			if (line0 == null)
				break;
			String line1 = br.readLine();
			if (line1 == null)
				throw new RuntimeException("Unexpected end of file: [" + line0 + "]");
			tweets.add(new Tweet(line0, line1));
		}
		//System.out.println(tweets.size());
		return tweets;
	}

	List<Tweet> _FilterLocalDCTweets(List<Tweet> tweets) {
		// tweets from the local DC
		List<Tweet> l_tweets = new ArrayList<Tweet>();
		for (Tweet t: tweets) {
			DC.Entry dc_e = _dc.GetClosest(t);
			if (_hostname.equals(dc_e.hostname))
				l_tweets.add(t);
		}
		System.out.println(tweets.size() + " parent tweets. " + l_tweets.size() + " local.");
		return l_tweets;
	}
	
	void ReadTweets() throws java.io.FileNotFoundException, java.io.IOException {
		_p_tweets = _ReadTweets("/mnt/multidc-data/twitter/raw-concise/to-replay/parents");
		// List<Tweet> c_tweets = _ReadTweets("/mnt/multidc-data/twitter/raw-concise/to-replay/children");;
		_p_tweets = _FilterLocalDCTweets(_p_tweets);
	}		

	long SimTimeToRealTimeMilli(long sim_time_milli) {
		// st_dur = st_end - st_begin = "130428-000000" - "130407-000000"
		// rt_dur = rt_end - rt_begin = replay_time
		//
		// (st - st_begin) / st_dur = (rt - rt_begin) / rt_dur
		//
		// rt = (st - st_begin) / st_dur * rt_dur + rt_begin
		long rt = (long) ( ((double) (sim_time_milli - _st_begin_milli))
				/ (_st_end_milli - _st_begin_milli) * _replay_time + _rt_begin_milli );
		return rt;
	}
	
	static class TweetReader implements Runnable {
		public void run() {
			try {
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			} 
		}
	}
	
	public static void main(String[] args) throws Exception {
		try {
			if (args.length != 2) {
				System.out.println("Usage: Replay cur_datetime replay_time_in_sec");
				System.out.println("  e.g.: Replay 131007-134856 10");
				System.exit(1);
			}

			// It has to be here. Doesn't work if put in the function GetEth0IP.
			System.setProperty("java.net.preferIPv4Stack", "true");

			String start_time = args[0];
			int replay_time = Integer.parseInt(args[1]);
			Replay rp = new Replay(replay_time, start_time);
			rp.ReadTweets();

			Thread w = new Thread(new TweetWriter(rp, 10));
			Thread r = new Thread(new TweetReader());
			w.start();
			r.start();
			w.join();
			r.join();

			System.exit(0);
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
