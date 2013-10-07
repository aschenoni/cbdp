import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


//import com.datastax.driver.core.*;
//import com.datastax.driver.core.exceptions.*;

public class Replay {
	String _hostname;
	DC _dc;
	List<Tweet> _p_tweets;

	Replay() throws java.net.UnknownHostException, java.io.FileNotFoundException, java.io.IOException {
		java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
		_hostname = addr.getHostName();
		//System.out.println(_hostname);
		_dc = new DC();
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

	long SimTimeToRealTimeMilli(long st_begin_milli, long rt_begin_milli, Date sim_time) {
		// 1 week : 1 min = 7 * 24 * 60 : 1 = 10080 : 1
		//double scaling_factor = 10080.0;
		final double scaling_factor = 100800.0;
		// sim_time.getTime() - st_begin.getTime() = scaling_factor * (rt - rt_begin.getTime());
		long rt = (long) ((sim_time.getTime() - st_begin_milli) / scaling_factor + rt_begin_milli);
		return rt;
	}

	void InsertParentTweets(String start_time) throws java.text.ParseException, java.lang.InterruptedException {
		// st_ : simulated time
		// rt_ : real time
		SimpleDateFormat sdf0 = new SimpleDateFormat("yyMMdd-hhmmss");
		long rt_begin_milli = sdf0.parse(start_time).getTime() + 1000L;
		long st_begin_milli = sdf0.parse("130407-000000").getTime();

		System.out.println("wait for sync ...");
		for (Tweet t: _p_tweets) {
			Date st = sdf0.parse(t.created_at);
			long rt = SimTimeToRealTimeMilli(st_begin_milli, rt_begin_milli, st);
			long sleep_time = rt - System.currentTimeMillis();
			if (sleep_time > 0)
				Thread.sleep(sleep_time);
			// TODO: write to cassandra
			System.out.println("Writing a parent tweet at " + sdf0.format(st) + " " + rt);
			//_WriteToCassandra(t);
		}
	}
	
	public static void main(String[] args) throws Exception {
		try {
			if (args.length != 1) {
				System.out.println("Usage: Replay cur_datetime");
				System.out.println("  e.g.: Replay 131007-134856");
				System.exit(1);
			}
			String start_time = args[0];
			Replay rp = new Replay();
			rp.ReadTweets();
			rp.InsertParentTweets(start_time);

//		} catch (NoHostAvailableException e) {
//			System.err.println("No alive hosts to use: " + e.getMessage());
//			System.exit(1);
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
