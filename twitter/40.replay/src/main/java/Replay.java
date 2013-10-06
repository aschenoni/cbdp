import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import java.util.StringTokenizer;

//import com.datastax.driver.core.*;
//import com.datastax.driver.core.exceptions.*;
//
//import joptsimple.OptionParser;
//import joptsimple.OptionSet;

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

	// TODO
	void InsertParentTweets() {
		// 1 week : 1 min = 7 * 24 * 60 : 1 = 10080 : 1
		double scaling_factor = 10080.0;

		// TODO: filter out tweets that have parents before the beginning of the
		// replay period from the filter tool.
		//
		for (Tweet t: _p_tweets) {
			System.out.println(t.created_at);
		}
	}
	
	public static void main(String[] args) throws Exception {
		try {
			Replay rp = new Replay();
			rp.ReadTweets();
			rp.InsertParentTweets();

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
