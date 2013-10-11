import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.List;
import java.util.StringTokenizer;

class TweetReader implements Runnable {
	private Replay _rp;
	static ChildTweet _first_tweet = null;

	TweetReader(Replay rp) {
		_rp = rp;
	}

	public void run() {
		try {
			BlockingQueue<ChildTweet> q =
				new ArrayBlockingQueue<ChildTweet>(_rp._read_conc * 100);

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

	static class ChildTweet {
		long tid;
		String created_at;
		boolean real_coord;
		float lati;
		float longi;
		long r_tid;
		static final ChildTweet END_MARKER = new ChildTweet();

		ChildTweet() {
		}

		ChildTweet(String line0, String line1) {
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
		private BlockingQueue<ChildTweet> _q;

		WhatToRead(Replay rp,
				BlockingQueue<ChildTweet> q) {
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
					ChildTweet t = new ChildTweet(line0, line1);
					if (! _rp._dc.IsLocal(t.lati, t.longi))
						continue;
					if (_first_tweet == null)
						_first_tweet = t;
					_q.put(t);
				}
				for (int i = 0; i < _rp._read_conc; ++ i)
					_q.put(ChildTweet.END_MARKER);
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	static class ReadFromCass implements Runnable {
		private Replay _rp;
		private BlockingQueue<ChildTweet> _q;

		ReadFromCass(Replay rp,
				BlockingQueue<ChildTweet> q) {
			_rp = rp;
			_q = q;
		}

		public void run() {
			try {
				SimpleDateFormat sdf0 = new SimpleDateFormat("yyMMdd-hhmmss");

				while (true) {
					ChildTweet t = _q.take();
					if (t == ChildTweet.END_MARKER)
						break;
					long st = sdf0.parse(t.created_at).getTime();
					long rt = _rp.SimTimeToRealTime(st);
					long cur_time = System.currentTimeMillis();
					long sleep_time = rt - cur_time;
					if (t == _first_tweet)
						System.out.println("TweetReader: waiting " + sleep_time + " ms for sync ...");
					if (sleep_time > 0)
						Thread.sleep(sleep_time);
					List<ParentTweetFromCass> rows = _rp._cc.ReadParentTweet(t.r_tid);
					if (rows.size() == 0) {
						// TODO:
						System.out.println("the record is not there yet");
					} else if (rows.size() == 1) {
						// TODO: compare the rt and current time.
						long created_at_rt = rows.get(0).created_at_rt;

						// created_at_rt should be younger than rt_begin_milli. Otherwise,
						// it must be from the previous experiments.
						if (created_at_rt < _rp._rt_begin_milli)
							System.out.println("the record is not there yet");
						else {
							// out of curiosity, how much time difference between current time and the read time
							//long c_rt = System.currentTimeMillis();
							//long diff = c_rt - rt;
							//long diff_st = (long)(diff * _rp._replay_time_sf);
							//float diff_st_s = (diff_st % 60000) / 1000.0f;
							//long diff_st_m = (diff_st / 60000) % 60;
							//long diff_st_h = (diff_st / 60000 / 60) % 24;
							//long diff_st_d = diff_st / 60000 / 60 / 24;
							//System.out.println("diff: rt: " + (diff / 1000.0) + "s "
							//		+ " st: " + diff_st_d + "d " + diff_st_h + "h " + diff_st_m + "m " + diff_st_s + "s");

//							if (false) {
//								long c_rt = System.currentTimeMillis();
//								long diff = c_rt - created_at_rt;
//								long diff_st = (long)(diff * _rp._replay_time_sf);
//								float diff_st_s = (diff_st % 60000) / 1000.0f;
//								long diff_st_m = (diff_st / 60000) % 60;
//								long diff_st_h = (diff_st / 60000 / 60) % 24;
//								long diff_st_d = diff_st / 60000 / 60 / 24;
//								System.out.println("rt: " + (diff / 1000.0) + "s "
//										+ " st: " + diff_st_d + "d " + diff_st_h + "h " + diff_st_m + "m " + diff_st_s + "s");
//							}
						}
					} else
						throw new RuntimeException("Unexpected: rows.size()=" + rows.size());

					if (sleep_time > 0) {
						System.out.print("r");
						System.out.flush();
					} else {
						System.out.print("R");
						System.out.flush();
					}

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

	static class ParentTweetFromCass {
		long tid;
		long created_at_rt;
		String created_at_st;
		float lati;
		float longi;
		boolean real_coord;
		String sn;
		String text;

		ParentTweetFromCass(com.datastax.driver.core.Row r) {
			tid = r.getLong("tid");
			created_at_rt = r.getLong("created_at_rt");
			created_at_st = r.getString("created_at_st");
			lati = r.getFloat("lati");
			longi = r.getFloat("longi");
			real_coord = r.getBool("real_coord");
			sn = r.getString("sn");
			text = r.getString("text_");
		}
	}
}
