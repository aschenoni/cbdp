package multidc;

import java.lang.InterruptedException;

import com.google.common.util.concurrent.Uninterruptibles;
import com.yammer.metrics.core.TimerContext;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.BoundStatement;


class CassTweetInserter {
	private final Reporter _reporter;
	private final Runner _runner = new Runner();

	private final Session _sess;
	private final ConciseTweetReader _ctr;
	private final String _which_table;

	private PreparedStatement _stmt_ht_idx;
	private PreparedStatement _stmt_tweet;

	public CassTweetInserter(
			Session sess,
			ConciseTweetReader ctr,
			String which_table,
			Reporter reporter) {
		_sess = sess;
		_ctr = ctr;

		if (! (which_table.equals("tweet") || which_table.equals("index") || which_table.equals("both")))
				throw new RuntimeException("unexpected which_table " + which_table);
		_which_table = which_table;

		_stmt_ht_idx = _sess.prepare("INSERT INTO hashtag_createdat_loc "
				+ "(day_ht, week_ht, lati, longi, real_coord, tid) "
				+ "VALUES (?, ?, ?, ?, ?, ?)");
		_stmt_tweet = _sess.prepare("INSERT INTO tweet "
				+ "(tid, sn, created_at, text) "
				+ "VALUES (?, ?, ?, ?)");
		_reporter = reporter;
	}

	public void start() {
		_runner.start();
	}

	public void join() {
		Uninterruptibles.joinUninterruptibly(_runner);
	}

	private class Runner extends Thread {
		public Runner() {
			super("CassTweetInserter Threads");
		}

		public void run() {
			try {
				while (true)
				{
					ConciseTweet ct = _ctr.GetTweet();
					if (ct == ConciseTweet.END)
						break;
					if (ct.hashtags.size() == 0)
						continue;

					if (_which_table.equals("index") || _which_table.equals("both")) {
						for (String ht: ct.hashtags)
						{
							BoundStatement bs = _stmt_ht_idx.bind();
							bs.bind(
									ct.CreatedAtDay() + " " + ht,
									ct.CreatedAtWeek() + " " + ht,
									ct.lati, ct.longi, ct.real_coord,
									ct.tid);
							_RunQuery(bs);
						}
					}
					if (_which_table.equals("tweet") || _which_table.equals("both")) {
						BoundStatement bs = _stmt_tweet.bind();
						bs.bind(
								ct.tid,
								ct.sn,
								ct.created_at,
								ct.text);
						_RunQuery(bs);
					}
				}
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}

		private void _RunQuery(BoundStatement bs) throws InterruptedException
		{
			boolean succeeded = false;
			do {
				TimerContext context = _reporter.latencies.time();
				try {
					_sess.execute(bs);
					succeeded = true;
				} catch (DriverException e) {
					System.err.println("Error during query: " + e.getMessage());
					e.printStackTrace();
					System.out.printf("Retrying in 5 sec...\n");
					Thread.sleep(5000);
				}
				context.stop();
				_reporter.requests.mark();
			} while (! succeeded);
		}
	}
}
