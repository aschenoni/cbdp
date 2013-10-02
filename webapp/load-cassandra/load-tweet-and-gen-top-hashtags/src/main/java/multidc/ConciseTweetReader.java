package multidc;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;
import java.util.Calendar;


class ConciseTweetReader {
	private BlockingQueue<ConciseTweet> _q;
	// The sampled users have 1433 tweets each on average. Queue size of 2000 ( >
	// 1433) * num_consumers should be good.
	private final int TWEET_Q_CAP;
	private Thread _reader;

	ConciseTweetReader(String in_dir, int num_consumers) {
		TWEET_Q_CAP = 2000 * num_consumers;
		_q = new ArrayBlockingQueue<ConciseTweet>(TWEET_Q_CAP);
		_reader = new Thread(new Reader(in_dir, _q, num_consumers));
		_reader.start();
	}

	ConciseTweet GetTweet() throws InterruptedException {
		ConciseTweet ct = _q.take();
		ct.ParseLine();
		return ct;
	}

	private class Reader implements Runnable {
		private long _tid;
		private String _in_dir;
		private BlockingQueue<ConciseTweet> _q;
		private int _num_consumers;

		public Reader(String in_dir, BlockingQueue<ConciseTweet> tweet_q, int num_consumers) {
			_in_dir = in_dir;
			_q = tweet_q;
			_num_consumers = num_consumers;
		}

		public void run() {
			_tid = Thread.currentThread().getId();

			try {
				List<String> fns = GetFileList();

				long start_time = System.nanoTime();
				int cnt_t = 0;
				int cnt_f = 0;
				for (String fn: fns) {
					//System.out.printf("%s\n", fn);
					String sn = fn.split("\\.")[0];
					//System.out.printf("%s\n", sn);
					String filepath = String.format("%s/%s/%s", _in_dir, fn.substring(0, 2), fn);

					BufferedReader br = new BufferedReader(new FileReader(filepath));
					String line;
					while ((line = br.readLine()) != null) {
						//System.out.printf("    %s\n", line);
						_q.put(new ConciseTweet(line, sn));
						++ cnt_t;
					}
					br.close();

					++ cnt_f;
					if (cnt_f % 20 == 0)
					{
						System.out.printf("ConciseTweetReader: %d/%d files. %.2f file/sec. %d tweets\n",
								cnt_f, fns.size(),
								cnt_f / ((System.nanoTime() - start_time) / 1000000000.0),
								cnt_t);
					}
				}
				System.out.printf("ConciseTweetReader: %d/%d files. %.2f file/sec. %d tweets\n",
						cnt_f, fns.size(),
						cnt_f / ((System.nanoTime() - start_time) / 1000000000.0),
						cnt_t);

				for (int i = 0; i < _num_consumers; ++ i)
					_q.put(ConciseTweet.END);
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}

		private List<String> GetFileList() {
			try (Timing _ = new Timing()) {
				List<String> fns = new ArrayList<String>();

				for (File f0: new File(_in_dir).listFiles()) {
					if (! f0.isDirectory())
						continue;
					for (File f1: new File(f0.getPath()).listFiles()) {
						String fn = f1.getName();
						// System.out.printf("%s\n", fn);
						if (fn.matches("^.*\\.swp$"))
							continue;
						fns.add(fn);
					}
				}
				System.out.printf("  %d tweet files.\n", fns.size());

				return fns;
			}
		}
	}
}


class ConciseTweet {
	private static String END_MARKER = "END_MARKER";
	public static ConciseTweet END = new ConciseTweet(END_MARKER, "");
	static final int FIRST_DAY_OF_WEEK = Calendar.getInstance().getFirstDayOfWeek();

	String line;

	long tid;
	String created_at;
	String created_at_day;
	String created_at_week;
	Set<String> hashtags;
	float lati;
	float longi;
	boolean real_coord;
	String text;
	String sn;

	ConciseTweet(String line_, String sn_)
	{
		line = line_;
		sn = sn_;
	}

	@Override
	public String toString() {
		return String.format("%d %s %s %f %f %s", tid, created_at, sn, lati, longi, real_coord ? "T" : "F", text);
	}


  private String _GetFirstDayOfWeek(String day) {
    String yy = day.substring(0, 2);
    String mm = day.substring(2, 4);
    String dd = day.substring(4, 6);

    Calendar cal = Calendar.getInstance();
    cal.set(Integer.parseInt(yy) + 2000,
        Integer.parseInt(mm) - 1,
        Integer.parseInt(dd)); 
    while (cal.get(Calendar.DAY_OF_WEEK) > FIRST_DAY_OF_WEEK)
      cal.add(Calendar.DATE, -1);
    return String.format("%02d-%02d-%02d", cal.get(cal.YEAR) - 2000, cal.get(cal.MONTH) + 1, cal.get(cal.DATE));
  }


  String CreatedAtDay() {
		if (created_at_day == null)
      created_at_day = created_at.substring(0, 6);
    return created_at_day;
  }


  String CreatedAtWeek() {
		if (created_at_week == null) {
      String day = CreatedAtDay();
      String yy = day.substring(0, 2);
      String mm = day.substring(2, 4);
      String dd = day.substring(4, 6);

      Calendar cal = Calendar.getInstance();
      cal.set(Integer.parseInt(yy) + 2000,
          Integer.parseInt(mm) - 1,
          Integer.parseInt(dd)); 
      while (cal.get(Calendar.DAY_OF_WEEK) > FIRST_DAY_OF_WEEK)
        cal.add(Calendar.DATE, -1);
      created_at_week = String.format("%02d%02d%02d", cal.get(cal.YEAR) - 2000, cal.get(cal.MONTH) + 1, cal.get(cal.DATE));
    }
    return created_at_week;
  }


	void ParseLine() {
		if (line == END_MARKER)
			return;

		hashtags = new HashSet<String>();

		int p0, p1;
		p0 = 0;
		p1 = line.indexOf(" ", p0);
		tid = Long.parseLong(line.substring(p0, p1));
		p0 = p1 + 1;
		p1 = line.indexOf(" ", p0);
		created_at = line.substring(p0, p1);

		p0 = p1 + 1;
		p1 = line.indexOf(" ", p0);
		lati = Float.parseFloat(line.substring(p0, p1));
		p0 = p1 + 1;
		p1 = line.indexOf(" ", p0);
		longi = Float.parseFloat(line.substring(p0, p1));
		p0 = p1 + 1;
		p1 = line.indexOf(" ", p0);
		real_coord = (line.charAt(p0) == 'T');

		p0 = p1 + 1;
		text = line.substring(p0);

		// parse hashtags
		for (StringTokenizer st = new StringTokenizer(text, " "); st.hasMoreElements(); ) {
			String s = st.nextElement().toString();
			if (s.charAt(0) == '#' && s.length() > 1) {
				for (StringTokenizer st1 = new StringTokenizer(s, "#"); st1.hasMoreElements(); ) {
					// filter out non-word characters and underscores
					String ht = st1.nextElement().toString().replaceAll("[\\W]|_", "").toLowerCase();
					if (ht.length() == 0)
						continue;
					//System.out.printf("%s %s\n", s, ht);
					hashtags.add(ht);
				}
			}
		}
	}
}
