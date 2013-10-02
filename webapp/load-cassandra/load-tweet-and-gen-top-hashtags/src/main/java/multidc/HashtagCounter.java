package multidc;

import java.lang.InterruptedException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.Calendar;


class HashtagCounter {
	private final ConciseTweetReader _ctr;
	private ConcurrentMap<String, ConcurrentMap<String, AtomicInteger>> _ht_map_by_day;
	private Map<String, Map<String, Integer>> _ht_map_by_week;
	private Thread[] _hc_threads;
	static final int FIRST_DAY_OF_WEEK = Calendar.getInstance().getFirstDayOfWeek();

	public HashtagCounter(ConciseTweetReader ctr, int concurrency) {
		_ctr = ctr;
		_ht_map_by_day = new ConcurrentHashMap<String, ConcurrentMap<String, AtomicInteger>>();

		_hc_threads = new Thread[concurrency];
		//for (Thread t: _hc_threads)
		for (int i = 0; i < concurrency; ++ i)
			_hc_threads[i] = new Thread(new CounterThread());
		for (Thread t: _hc_threads)
			t.start();
	}

	public void Join() throws InterruptedException {
		for (Thread t: _hc_threads)
			t.join();
	}

	public void GenOutput(String out_dir) throws FileNotFoundException {
		System.out.printf("Generating output in %s ...\n", out_dir);

		_GenOutput(out_dir + "/by-day", _ht_map_by_day);
		
		_GenHTMapByWeek();
		_GenOutput(out_dir + "/by-week", _ht_map_by_week);
	}

	private void _GenOutput(String out_dir, ConcurrentMap<String, ConcurrentMap<String, AtomicInteger>> htmap)
		throws FileNotFoundException {
		new File(out_dir).mkdirs();
		for (ConcurrentMap.Entry<String, ConcurrentMap<String, AtomicInteger>> e: htmap.entrySet()) {
			//System.out.printf("  %s %d\n", e.getKey(), e.getValue().size());
			String fn = out_dir + "/" + e.getKey();

			List<HashtagCnt> hc_list = new ArrayList<HashtagCnt>();
			for (ConcurrentMap.Entry<String, AtomicInteger> e2: e.getValue().entrySet()) {
				//System.out.printf("  %s %d\n", e2.getKey(), e2.getValue().intValue());
				hc_list.add(new HashtagCnt(e2.getKey(), e2.getValue().intValue()));
			}
			Collections.sort(hc_list, new HashtagCntComparator());

			PrintWriter writer = new PrintWriter(fn);
			for (HashtagCnt hc: hc_list) {
				writer.printf("%s %d\n", hc.ht, hc.cnt);
			}
			writer.close();
		}
	}
	
	private void _GenOutput(String out_dir, Map<String, Map<String, Integer>> htmap)
		throws FileNotFoundException {
		new File(out_dir).mkdirs();
		for (Map.Entry<String, Map<String, Integer>> e: htmap.entrySet()) {
			//System.out.printf("  %s %d\n", e.getKey(), e.getValue().size());
			String fn = out_dir + "/" + e.getKey();

			List<HashtagCnt> hc_list = new ArrayList<HashtagCnt>();
			for (Map.Entry<String, Integer> e2: e.getValue().entrySet()) {
				//System.out.printf("  %s %d\n", e2.getKey(), e2.getValue());
				hc_list.add(new HashtagCnt(e2.getKey(), e2.getValue()));
			}
			Collections.sort(hc_list, new HashtagCntComparator());

			PrintWriter writer = new PrintWriter(fn);
			for (HashtagCnt hc: hc_list) {
				writer.printf("%s %d\n", hc.ht, hc.cnt);
			}
			writer.close();
		}
	}
	

  private String _CreatedAtWeek(String day)
  {
			String yy = day.substring(0, 2);
			String mm = day.substring(2, 4);
			String dd = day.substring(4, 6);

			Calendar cal = Calendar.getInstance();
			cal.set(Integer.parseInt(yy) + 2000,
					Integer.parseInt(mm) - 1,
					Integer.parseInt(dd)); 
			while (cal.get(Calendar.DAY_OF_WEEK) > FIRST_DAY_OF_WEEK)
				cal.add(Calendar.DATE, -1);
			return String.format("%02d%02d%02d", cal.get(cal.YEAR) - 2000, cal.get(cal.MONTH) + 1, cal.get(cal.DATE));
  }


	private void _GenHTMapByWeek() {
		_ht_map_by_week = new HashMap<String, Map<String, Integer>>();

		for (ConcurrentMap.Entry<String, ConcurrentMap<String, AtomicInteger>> e: _ht_map_by_day.entrySet()) {
      String created_at_week = _CreatedAtWeek(e.getKey());

			if (! _ht_map_by_week.containsKey(created_at_week))
				_ht_map_by_week.put(created_at_week, new HashMap<String, Integer>());
			Map<String, Integer> htmap = _ht_map_by_week.get(created_at_week);
			for (ConcurrentMap.Entry<String, AtomicInteger> e2: e.getValue().entrySet()) {
				String k = e2.getKey();
				int v = e2.getValue().intValue();
				if (htmap.containsKey(k))
					htmap.put(k, htmap.get(k) + v);
				else
					htmap.put(k, v);
			}
		}
	}
	
	
	private class HashtagCnt {
		String ht;
		int cnt;

		HashtagCnt(String ht_, int cnt_) {
			ht = ht_;
			cnt = cnt_;
		}
	}

	private class HashtagCntComparator implements Comparator<HashtagCnt> {
		public int compare(HashtagCnt h1, HashtagCnt h2) {
			if (h1.cnt > h2.cnt) return -1;
			if (h1.cnt < h2.cnt) return 1;
			return h1.ht.compareTo(h2.ht);
		}
	};

	private class CounterThread implements Runnable {
		private long _tid;

		public CounterThread() {
		}

		public void run() {
			_tid = Thread.currentThread().getId();

			try {
				while (true) {
					ConciseTweet ct = _ctr.GetTweet();
					if (ct == ConciseTweet.END)
						return;
					if (ct.hashtags.size() == 0)
						continue;

					_ht_map_by_day.putIfAbsent(ct.CreatedAtDay(), new ConcurrentHashMap<String, AtomicInteger>());

					for (String ht: ct.hashtags)
					{
						ConcurrentMap<String, AtomicInteger> htmap = _ht_map_by_day.get(ct.CreatedAtDay());
						htmap.putIfAbsent(ht, new AtomicInteger(0));
						htmap.get(ht).incrementAndGet();
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
