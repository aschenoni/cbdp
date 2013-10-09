import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;


class TweetWriter implements Runnable {
	private Replay _rp;
	static Tweet _first_tweet = null;

	TweetWriter(Replay rp) {
		_rp = rp;
	}

	public void run() {
		try {
			BlockingQueue<Tweet> q = new ArrayBlockingQueue<Tweet>(_rp._write_conc * 100);
			Thread reader = new Thread(new ParentTweetReader(_rp, q));
			reader.start();
			Thread[] writers = new Thread[_rp._write_conc];
			for (int i = 0; i < _rp._write_conc; ++ i) {
				writers[i] = new Thread(new ParentTweetWriter(_rp, q));
				writers[i].start();
			}
			for (Thread w: writers)
				w.join();
			reader.join();
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	static class ParentTweetReader implements Runnable {
		private Replay _rp;
		private BlockingQueue<Tweet> _q;

		ParentTweetReader(Replay rp, BlockingQueue<Tweet> q) {
			_rp = rp;
			_q = q;
		}

		public void run() {
			try {
				String fn = "/mnt/multidc-data/twitter/raw-concise/to-replay/parents";
				BufferedReader br = new BufferedReader(new FileReader(fn));
				while (true) {
					String line0 = br.readLine();
					if (line0 == null)
						break;
					String line1 = br.readLine();
					if (line1 == null)
						throw new RuntimeException("Unexpected end of file: [" + line0 + "]");
					Tweet t = new Tweet(line0, line1);
					if (! _rp._dc.IsLocal(t.lati, t.longi))
						continue;
					if (_first_tweet == null)
						_first_tweet = t;
					_q.put(t);
				}
				for (int i = 0; i < _rp._write_conc; ++ i)
					_q.put(Tweet.END_MARKER);
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	static class ParentTweetWriter implements Runnable {
		private BlockingQueue<Tweet> _q;
		private Replay _rp;

		ParentTweetWriter(Replay rp, BlockingQueue<Tweet> q) {
			_rp = rp;
			_q = q;
		}

		public void run() {
			try {
				SimpleDateFormat sdf0 = new SimpleDateFormat("yyMMdd-hhmmss");

				while (true) {
					Tweet t = _q.take();
					if (t == Tweet.END_MARKER)
						break;
					long st = sdf0.parse(t.created_at).getTime();
					long rt = _rp.SimTimeToRealTimeMilli(st);
					long cur_time = System.currentTimeMillis();
					long sleep_time = rt - cur_time;
					if (t == _first_tweet)
						System.out.println("TweetWriter: waiting " + sleep_time + " ms for sync ...");
					if (sleep_time > 0)
						Thread.sleep(sleep_time);
					_rp._cc.WriteParentTweet(t);
					if (sleep_time > 0) {
						System.out.print("w");
						System.out.flush();
					} else {
						System.out.print("W");
						System.out.flush();
					}
				}
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
