import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
			System.out.println("\nRead: total: " + (ReadFromCass._cnt_s + ReadFromCass._cnt_nt)
					+ " successful: " + ReadFromCass._cnt_s
					+ " not there yet: " + ReadFromCass._cnt_nt);
			String dn = _rp._logdir + "/" + _rp._rt_begin + "/" + Util.GetHostname();
			new File(dn).mkdirs();
			String fn = dn + "/user-lat-r";
			MonUserLatR.WriteResult(fn);
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} 
	}

	static class ChildTweet {
		long tid;
		String st_ca;	// created_at
		boolean real_coord;
		float lati;
		float longi;
		long r_tid;
		String r_created_at;
		long rt_ca;	// created_at in real time in millisecond

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
			st_ca = (String) st.nextElement();
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
			if (! st.hasMoreElements())
				throw new RuntimeException("Unexpected format line0: " + line0);
			r_created_at = (String) st.nextElement();
		}

		void CalcRT(Replay _rp, SimpleDateFormat sdf) throws java.text.ParseException {
			// sim time = to_sim_time(r_created_at) + diff(c time - p time)
			long p_st = sdf.parse(r_created_at).getTime();
			long p_rt = _rp.SimTimeToRealTime(p_st);
			long c_st = sdf.parse(st_ca).getTime();
			long diff_st = c_st - p_st;
			rt_ca = p_rt + diff_st;
			if (diff_st < 0) {
				System.out.printf("tid=    %d\n", tid);
				System.out.printf("r_tid=  %d\n", r_tid);
				System.out.printf("p_st=   %s %d %s\n", sdf.format(p_st), p_st, r_created_at);
				System.out.printf("p_rt=   %s %d\n", sdf.format(p_rt), p_rt);
				System.out.printf("c_st=   %s %d %s\n", sdf.format(c_st), c_st, st_ca);
				System.out.printf("diff_st=%d\n", diff_st);
				System.out.printf("rt_ca=  %s %d\n", sdf.format(rt_ca), rt_ca);
				System.out.printf("\n");
			}
		}

		public static Comparator<ChildTweet> ByRealTimeCreatedAt = new Comparator<ChildTweet>() {
			//@Override
			public int compare(ChildTweet t1, ChildTweet t2) {
				return (int)(t1.rt_ca - t2.rt_ca);
			}
		};
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
				// read all and reorder them in the order of replay
				List<ChildTweet> c_tweets = new ArrayList<ChildTweet>();
				SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss");
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
					t.CalcRT(_rp, sdf);
					c_tweets.add(t);
				}
				Collections.sort(c_tweets, ChildTweet.ByRealTimeCreatedAt);
				int replay_cnt = 0;
				for (ChildTweet t: c_tweets) {
					if (t.rt_ca < _rp._rt_end_inc_wait_milli)
						++ replay_cnt;
				}
				System.out.printf("Replaying %d retweets out of %d ...\n", replay_cnt, c_tweets.size());

				for (ChildTweet t: c_tweets) {
					if (t.rt_ca >= _rp._rt_end_inc_wait_milli)
						break;
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
		private SimpleDateFormat _sdf;
		static private Integer _cnt_s = 0;	// successful read cnt
		static private Integer _cnt_nt = 0;	// requested tweet not there yet

		ReadFromCass(Replay rp,
				BlockingQueue<ChildTweet> q) {
			_rp = rp;
			_q = q;
			_sdf = new SimpleDateFormat("yyMMdd-HHmmss");
		}

		public void run() {
			try {
				int cnt_s = 0;	// successful read cnt
				int cnt_nt = 0;	// requested tweet not there yet

				while (true) {
					ChildTweet t = _q.take();
					if (t == ChildTweet.END_MARKER)
						break;
					long cur_time = System.currentTimeMillis();
					long sleep_time = t.rt_ca - cur_time;
					if (t == _first_tweet) {
						if (sleep_time <= 0) {
							System.out.printf("Synchronization failure. Initialization took more than %d ms.\n", _rp.RT_BEGIN_OFFSET);
							System.exit(0);
						}
						//System.out.printf("cur_time: %s %d\n", _sdf.format(cur_time), cur_time);
						//System.out.printf("rt_ca:    %s %d\n", _sdf.format(t.rt_ca), t.rt_ca);
						System.out.println("TweetReader: waiting " + sleep_time + " ms for sync ...");
					}
					if (sleep_time > 0)
						Thread.sleep(sleep_time);
					List<ParentTweetFromCass> rows = _rp._cc.ReadParentTweet(t.r_tid);
					if (rows.size() == 0) {
						// the record is not there yet
						cnt_nt ++;
						System.out.printf("no such record yet? %d\n", t.r_tid);
					} else if (rows.size() == 1) {
						long created_at_rt = rows.get(0).created_at_rt;

						// created_at_rt should be younger than rt_begin_milli. Otherwise,
						// it must be from the previous experiments.
						if (created_at_rt < _rp._rt_begin_milli) {
							// the record is not there yet
							cnt_nt ++;
							System.out.printf(" tid=%d r_tid=%d created_at_rt(%d) -  _rp._rt_begin_milli(%d) = %d\n",
									t.tid, t.r_tid,
									created_at_rt, _rp._rt_begin_milli,
									created_at_rt - _rp._rt_begin_milli);
						}
						else {
							cnt_s ++;
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
				}
				_UpdateCnt(cnt_s, cnt_nt);
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}

		void _UpdateCnt(int cnt_s, int cnt_nt) {
			synchronized(_cnt_s) {
				_cnt_s += cnt_s;
			}
			synchronized(_cnt_nt) {
				_cnt_nt += cnt_nt;
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
