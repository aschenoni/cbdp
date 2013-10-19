import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.*;

class CassClient {
	private Session _sess;
	private SimpleStatement _stmt;
	private PreparedStatement _stmt_write;
	private PreparedStatement _stmt_read;

	CassClient() throws java.net.SocketException {
		String ip = Util.GetEth0IP();
		//System.out.println(ip);
		Cluster cluster = new Cluster.Builder().addContactPoints(ip).build();
		_sess= cluster.connect();
		Metadata metadata = cluster.getMetadata();
		System.out.println(String.format("Connected to cluster '%s' on %s.", metadata.getClusterName(), metadata.getAllHosts()));
		_CreateSchema();
		_stmt_write = _sess.prepare("INSERT INTO tweet "
				+ "(tid, sn, created_at_st, created_at_rt, real_coord, longi, lati, text_) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?); ");
		_stmt_read = _sess.prepare("SELECT * from tweet where tid=?");
	}

	private void _CreateSchema() {
		try {
			_sess.execute("CREATE KEYSPACE pbdp WITH replication = { "
					+ "'class' : 'NetworkTopologyStrategy', "
					+ "'DC0' : 1, "
					+ "'DC1' : 1, "
					+ "'DC2' : 1, "
					+ "'DC3' : 1 }; ");
		} catch (AlreadyExistsException e) {}
		_sess.execute("use pbdp;");

		try {
			_sess.execute("CREATE TABLE tweet ("
					+ "tid bigint, "
					+ "sn text, "
					+ "created_at_st text, "
					+ "created_at_rt bigint, "
					+ "real_coord boolean, "
					+ "longi float, "
					+ "lati float, "
					+ "text_ text, "
					+ "PRIMARY KEY (tid) "
					+ "); ");
		} catch (AlreadyExistsException e) {}
	}

	void WriteParentTweet(TweetWriter.ParentTweet t) throws java.lang.InterruptedException {
		BoundStatement bs = _stmt_write.bind();
		bs.bind(t.tid, t.sn, t.created_at, System.currentTimeMillis(), t.real_coord, t.longi, t.lati, t.text);
		try (MonUserLat _ = new MonUserLat('W')) {
			_RunQuery(bs);
		}
	}

	List<TweetReader.ParentTweetFromCass> ReadParentTweet(long tid)
		throws java.lang.InterruptedException {
		BoundStatement bs = _stmt_read.bind();
		bs.bind(tid);
		ResultSet rs;
		try (MonUserLat _ = new MonUserLat('R')) {
			rs = _RunQuery(bs);
		}
		List<TweetReader.ParentTweetFromCass> rows
			= new ArrayList<TweetReader.ParentTweetFromCass>();
		for (Row r: rs.all())
			rows.add(new TweetReader.ParentTweetFromCass(r));
		return rows;
	}

	private ResultSet _RunQuery(BoundStatement bs) throws InterruptedException
	{
		while (true) {
			try {
				return _sess.execute(bs);
			} catch (DriverException e) {
				System.err.println("Error during query: " + e.getMessage());
				e.printStackTrace();
				System.out.printf("Retrying in 5 sec...\n");
				Thread.sleep(5000);
			}
		}
	}
}
