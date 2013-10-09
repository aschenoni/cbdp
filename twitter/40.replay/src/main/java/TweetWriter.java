import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;


class TweetWriter implements Runnable {
	private Replay _rp;
	private BlockingQueue<Tweet> _q;
	static final Tweet END_MARKER = new Tweet();
	private int _num_writers;

	TweetWriter(Replay rp, int num_writers) {
		_rp = rp;
		_q = new ArrayBlockingQueue<Tweet>(1000);
		_num_writers = num_writers;
	}

	public void run() {
		try {
			Thread reader = new Thread(new ParentTweetReader(_rp, _q, _num_writers));
			reader.start();
			Thread[] writers = new Thread[_num_writers];
			for (int i = 0; i < _num_writers; ++ i) {
				writers[i] = new Thread(new ParentTweetWriter(_rp, _q));
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
		private int _num_writers;

		ParentTweetReader(Replay rp, BlockingQueue<Tweet> q, int num_writers) {
			_rp = rp;
			_q = q;
			_num_writers = num_writers;
		}

		public void run() {
			try {
				for (Tweet t: _rp._p_tweets)
					_q.put(t);
				for (int i = 0; i < _num_writers; ++ i)
					_q.put(END_MARKER);
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
					if (t == END_MARKER)
						break;
					long st = sdf0.parse(t.created_at).getTime();
					long rt = _rp.SimTimeToRealTimeMilli(st);
					long cur_time = System.currentTimeMillis();
					long sleep_time = rt - cur_time;
					if (t == _rp._p_tweets.get(0))
						System.out.println("waiting " + sleep_time + " ms for sync ...");
					if (sleep_time > 0)
						Thread.sleep(sleep_time);
					_rp._cc.WriteParentTweet(t);
					if (sleep_time > 0) {
						System.out.print("s");
						System.out.flush();
					} else {
						System.out.print(".");
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
