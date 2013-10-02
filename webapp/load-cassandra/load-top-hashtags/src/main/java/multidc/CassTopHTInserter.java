package multidc;

import java.lang.InterruptedException;

import com.google.common.util.concurrent.Uninterruptibles;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.Query;


class CassTopHTInserter {
	private final Runner _runner = new Runner();

	private final Session _sess;
	private final TopHTReader _thr;

	public CassTopHTInserter(
			Session sess,
			TopHTReader thr) {
		_sess = sess;
		_thr = thr;
	}

	public void start() {
		_runner.start();
	}

	public void join() {
		Uninterruptibles.joinUninterruptibly(_runner);
	}

	private class Runner extends Thread {
		public Runner() {
			super("CassTopHTInserter Threads");
		}

		public void run() {
			try {
				while (true)
				{
					HtCnt hc = _thr.GetHtCnt();
					if (hc == HtCnt.END)
						break;
          Query q = hc.GetCassQuery();
          _RunQuery(q);
				}
			} catch (Exception e) {
				System.err.println("Unexpected error: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}

		private void _RunQuery(Query q) throws InterruptedException
		{
			boolean succeeded = false;
			do {
				try {
					_sess.execute(q);
					succeeded = true;
				} catch (DriverException e) {
					System.err.println("Error during query: " + e.getMessage());
					e.printStackTrace();
					System.out.printf("Retrying in 5 sec...\n");
					Thread.sleep(5000);
				}
			} while (! succeeded);
		}
	}
}
