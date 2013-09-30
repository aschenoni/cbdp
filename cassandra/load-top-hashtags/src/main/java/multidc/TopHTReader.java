package multidc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Query;
import com.datastax.driver.core.Session;


class TopHTReader {
	private BlockingQueue<HtCnt> _q;
	private final int Q_CAP;
	private Thread _reader;

	TopHTReader(String in_dir, int concurrency) {
		Q_CAP = 5000 * concurrency;
		_q = new ArrayBlockingQueue<HtCnt>(Q_CAP);
		_reader = new Thread(new Reader(in_dir, _q, concurrency));
		_reader.start();
	}

	HtCnt GetHtCnt() throws InterruptedException {
		HtCnt hc = _q.take();
		hc.ParseLine();
		return hc;
	}

	private class Reader implements Runnable {
		private long _tid;
		private String _in_dir;
		private BlockingQueue<HtCnt> _q;
		private int _num_consumers;

		public Reader(String in_dir, BlockingQueue<HtCnt> tweet_q, int concurrency) {
			_in_dir = in_dir;
			_q = tweet_q;
			_num_consumers = concurrency;
		}

    private void _read_day() throws FileNotFoundException, IOException, InterruptedException {
      List<String> fns = GetFileList(_in_dir + "/by-day");

      long start_time = System.nanoTime();
      int cnt_f = 0;
      for (String fn: fns) {
        String filepath = String.format("%s/%s/%s", _in_dir, "by-day", fn);

        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String line;
        while ((line = br.readLine()) != null) {
          _q.put(new HtCntByDay(line, fn));
        }
        br.close();

        ++ cnt_f;
        if (cnt_f % 20 == 0)
        {
          System.out.printf("by-day: %d/%d files. %.2f file/sec.\n",
              cnt_f, fns.size(),
              cnt_f / ((System.nanoTime() - start_time) / 1000000000.0));
        }
      }
      System.out.printf("by-day: %d/%d files. %.2f file/sec.\n",
          cnt_f, fns.size(),
          cnt_f / ((System.nanoTime() - start_time) / 1000000000.0));
    }

    private void _read_week() throws FileNotFoundException, IOException, InterruptedException {
      List<String> fns = GetFileList(_in_dir + "/by-week");

      long start_time = System.nanoTime();
      int cnt_f = 0;
      for (String fn: fns) {
        String filepath = String.format("%s/%s/%s", _in_dir, "by-week", fn);

        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String line;
        while ((line = br.readLine()) != null) {
          _q.put(new HtCntByWeek(line, fn));
        }
        br.close();

        ++ cnt_f;
        if (cnt_f % 20 == 0)
        {
          System.out.printf("by-week: %d/%d files. %.2f file/sec.\n",
              cnt_f, fns.size(),
              cnt_f / ((System.nanoTime() - start_time) / 1000000000.0));
        }
      }
      System.out.printf("by-week: %d/%d files. %.2f file/sec.\n",
          cnt_f, fns.size(),
          cnt_f / ((System.nanoTime() - start_time) / 1000000000.0));
    }

    public void run() {
      _tid = Thread.currentThread().getId();

      try {
        _read_day();
        _read_week();

        for (int i = 0; i < _num_consumers; ++ i)
          _q.put(HtCnt.END);
      } catch (Exception e) {
        System.err.println("Unexpected error: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
      }
		}

		private List<String> GetFileList(String dn) {
			try (Timing _ = new Timing()) {
				List<String> fns = new ArrayList<String>();
        for (File f0: new File(dn).listFiles()) {
          if (! f0.isFile())
            continue;
          String fn = f0.getName();
          // System.out.printf("%s\n", fn);
          if (fn.matches("^.*\\.swp$"))
            continue;
          fns.add(fn);
        }
				System.out.printf("  %d tweet files.\n", fns.size());
				return fns;
			}
		}
	}
}


class HtCnt {
	private static String END_MARKER = "END_MARKER";
	public static HtCnt END = new HtCnt(END_MARKER);

  String line;
  String ht;
  int cnt;

  static void PrepareStatement(Session sess) {
    HtCntByDay.PrepareStatement(sess);
    HtCntByWeek.PrepareStatement(sess);
  }

	HtCnt(String line_) {
		line = line_;
	}

	void ParseLine() {
		if (line == END_MARKER)
			return;

		int p0, p1;
		p0 = 0;
		p1 = line.indexOf(" ", p0);
		ht = line.substring(p0, p1);

		p0 = p1 + 1;
		cnt = Integer.parseInt(line.substring(p0));
	}

  Query GetCassQuery() {
    throw new RuntimeException("Can not be called");
  }
}


class HtCntByDay extends HtCnt {
  private static PreparedStatement _stmt;
  String day;

  static void PrepareStatement(Session sess) {
    _stmt = sess.prepare("INSERT INTO top_ht_day "
        + "(day, ht, cnt) "
        + "VALUES (?, ?, ?)");
  }

	HtCntByDay(String line_, String day_) {
    super(line_);
    day = day_;
	}

  Query GetCassQuery() {
    BoundStatement bs = _stmt.bind();
    bs.bind(day, ht, cnt);
    return bs;
  }
}


class HtCntByWeek extends HtCnt {
  private static PreparedStatement _stmt;
  String week;

  static void PrepareStatement(Session sess) {
    _stmt = sess.prepare("INSERT INTO top_ht_week "
        + "(week, ht, cnt) "
        + "VALUES (?, ?, ?)");
  }

	HtCntByWeek(String line_, String week_) {
    super(line_);
    week = week_;
	}

  Query GetCassQuery() {
    BoundStatement bs = _stmt.bind();
    bs.bind(week, ht, cnt);
    return bs;
  }
}
