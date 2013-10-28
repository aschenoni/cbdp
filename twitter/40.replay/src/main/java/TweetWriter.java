import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


class TweetWriter implements Runnable {
	private Replay _rp;
	static ParentTweet _first_tweet = null;

	TweetWriter(Replay rp) {
		_rp = rp;
	}

	public void run() {
		try {
			BlockingQueue<ParentTweet> q = new ArrayBlockingQueue<ParentTweet>(_rp._write_conc * 100);
			Thread reader = new Thread(new ParentTweetReader(_rp, q));
			reader.start();
			Thread[] writers = new Thread[_rp._write_conc];
			for (int i = 0; i < _rp._write_conc; ++ i) {
				writers[i] = new Thread(new ParentTweetWriter(_rp, q));
				writers[i].start();
			}
			reader.join();
			for (Thread w: writers)
				w.join();
			System.out.println("\nWrite: " + ParentTweetWriter._cnt);
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	static class ParentTweetReader implements Runnable {
		private Replay _rp;
		private BlockingQueue<ParentTweet> _q;

		ParentTweetReader(Replay rp, BlockingQueue<ParentTweet> q) {
			_rp = rp;
			_q = q;
		}

		public void run() {
			try {
				String fn = "/mnt/multidc-data/twitter/to-replay/parents";
				BufferedReader br = new BufferedReader(new FileReader(fn));
				while (true) {
					String line0 = br.readLine();
					if (line0 == null)
						break;
					String line1 = br.readLine();
					if (line1 == null)
						throw new RuntimeException("Unexpected end of file: [" + line0 + "]");
					ParentTweet t = new ParentTweet(line0, line1);
					if (! _rp._dc.IsLocal(t.lati, t.longi))
						continue;
					if (_first_tweet == null)
						_first_tweet = t;
					_q.put(t);
				}
				for (int i = 0; i < _rp._write_conc; ++ i)
					_q.put(ParentTweet.END_MARKER);
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	static class ParentTweetWriter implements Runnable {
		private BlockingQueue<ParentTweet> _q;
		private Replay _rp;
		static private Integer _cnt = 0;	// successful write cnt

		ParentTweetWriter(Replay rp, BlockingQueue<ParentTweet> q) {
			_rp = rp;
			_q = q;
		}

		public void run() {
			try {
				SimpleDateFormat sdf0 = new SimpleDateFormat("yyMMdd-HHmmss");
				int cnt = 0;

				while (true) {
					ParentTweet t = _q.take();
					if (t == ParentTweet.END_MARKER)
						break;
					long st = sdf0.parse(t.created_at).getTime();
					long rt = _rp.SimTimeToRealTime(st);
					long cur_time = System.currentTimeMillis();
					long sleep_time = rt - cur_time;
					if (t == _first_tweet) {
						if (sleep_time <= 0)
							throw new RuntimeException("Synchronization failure. sleep_time=" + sleep_time);
						System.out.println("TweetWriter: waiting " + sleep_time + " ms for sync ...");
					}
					if (sleep_time > 0)
						Thread.sleep(sleep_time);
					_rp._cc.WriteParentTweet(t);
					cnt ++;
					if (sleep_time > 0) {
						System.out.print("w");
						System.out.flush();
					} else {
						System.out.print("W");
						System.out.flush();
					}
				}
				_UpdateCnt(cnt);
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}

		void _UpdateCnt(int cnt) {
			synchronized(_cnt) {
				_cnt += cnt;
			}
		}
	}

	static class ParentTweet {
		long tid;
		String sn;
		String created_at;
		boolean real_coord;
		float lati;
		float longi;
		String in_reply_to;
		String r_tid;
		Set<String> hashtags;
		String text;
		static final ParentTweet END_MARKER = new ParentTweet();

		ParentTweet() {
		}

		ParentTweet(String line0, String line1) {
			StringTokenizer st = new StringTokenizer(line0);
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			tid = Long.parseLong((String) st.nextElement());
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			sn = (String) st.nextElement();
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			created_at = (String) st.nextElement();
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			real_coord = ((String) st.nextElement()).equals("T");
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			lati = Float.parseFloat((String) st.nextElement());
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			longi = Float.parseFloat((String) st.nextElement());
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			in_reply_to = (String) st.nextElement();
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			r_tid = (String) st.nextElement();
			hashtags = new HashSet<String>();
			while (st.hasMoreElements())
				hashtags.add((String) st.nextElement());
			text = line1;
		}
	}
}
