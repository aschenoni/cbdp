import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.StringTokenizer;

class TweetReader implements Runnable {
	private Replay _rp;
	static ChildTweetToRead _first_tweet = null;

	TweetReader(Replay rp) {
		_rp = rp;
	}

	public void run() {
		try {
			BlockingQueue<ChildTweetToRead> q =
				new ArrayBlockingQueue<ChildTweetToRead>(_rp._read_conc * 100);

			Thread file_reader = new Thread(new WhatToRead(_rp, q));
			file_reader.start();

			Thread[] cass_readers = new Thread[_rp._read_conc];
			for (int i = 0; i < _rp._read_conc; ++ i) {
				cass_readers[i] = new Thread(new ReadFromCass(_rp, q));
				cass_readers[i].start();
			}
			file_reader.join();
			for (Thread c: cass_readers)
				c.join();
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} 
	}

	static class ChildTweetToRead {
		long tid;
		String created_at;
		boolean real_coord;
		float lati;
		float longi;
		long r_tid;
		static final ChildTweetToRead END_MARKER = new ChildTweetToRead();

		ChildTweetToRead() {
		}

		ChildTweetToRead(String line0, String line1) {
			StringTokenizer st = new StringTokenizer(line0);
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			tid = Long.parseLong((String) st.nextElement());
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			// sn
			st.nextElement();
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
			// in_reply_to
			st.nextElement();
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			r_tid = Long.parseLong((String) st.nextElement());
		}
	}

	static class WhatToRead implements Runnable {
		private Replay _rp;
		private BlockingQueue<ChildTweetToRead> _q;

		WhatToRead(Replay rp,
				BlockingQueue<ChildTweetToRead> q) {
			_rp = rp;
			_q = q;
		}

		public void run() {
			try {
				String fn = "/mnt/multidc-data/twitter/raw-concise/to-replay/children";
				BufferedReader br = new BufferedReader(new FileReader(fn));
				while (true) {
					String line0 = br.readLine();
					if (line0 == null)
						break;
					String line1 = br.readLine();
					if (line1 == null)
						throw new RuntimeException("Unexpected end of file: [" + line0 + "]");
					ChildTweetToRead t = new ChildTweetToRead(line0, line1);
					if (! _rp._dc.IsLocal(t.lati, t.longi))
						continue;
					if (_first_tweet == null)
						_first_tweet = t;
					_q.put(t);
				}
				for (int i = 0; i < _rp._read_conc; ++ i)
					_q.put(ChildTweetToRead.END_MARKER);
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	static class ReadFromCass implements Runnable {
		private Replay _rp;
		private BlockingQueue<ChildTweetToRead> _q;

		ReadFromCass(Replay rp,
				BlockingQueue<ChildTweetToRead> q) {
			_rp = rp;
			_q = q;
		}

		public void run() {
			try {
				SimpleDateFormat sdf0 = new SimpleDateFormat("yyMMdd-hhmmss");

				while (true) {
					ChildTweetToRead t = _q.take();
					if (t == ChildTweetToRead.END_MARKER)
						break;
					long st = sdf0.parse(t.created_at).getTime();
					long rt = _rp.SimTimeToRealTimeMilli(st);
					long cur_time = System.currentTimeMillis();
					long sleep_time = rt - cur_time;
					// TODO:
					if (t == _first_tweet)
						System.out.println("TweetReader: waiting " + sleep_time + " ms for sync ...");
					if (sleep_time > 0)
						Thread.sleep(sleep_time);
					// TODO:
					//_rp._cc.ReadParentTweet(t.r_tid);
					if (sleep_time > 0) {
						System.out.print("r");
						System.out.flush();
					} else {
						System.out.print("R");
						System.out.flush();
					}

					// TODO: t.created_at. sleep this much.
					
					// _rp._cc.Read(t.r_tid);

					//ParentTweetRead pt;
					// compare pt.created_at_rt with current time;
					//
					// TODO: what if it's not there?

				}
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
