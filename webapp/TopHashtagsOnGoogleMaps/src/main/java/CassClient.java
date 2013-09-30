import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.*;

import org.eclipse.jetty.util.ajax.JSON;

class CassClient {
	private Session _sess;
	private SimpleStatement _stmt;

	CassClient() {
		Cluster cluster = new Cluster.Builder().addContactPoints("127.0.0.1").build();
		_sess= cluster.connect();
		Metadata metadata = cluster.getMetadata();
		System.out.println(String.format("Connected to cluster '%s' on %s.", metadata.getClusterName(), metadata.getAllHosts()));
		_sess.execute("use tweet;");
	}

	String GetTopHashtags(String day_or_week, String date, int max) throws InterruptedException {
		SimpleStatement stmt = new SimpleStatement(
				String.format("SELECT cnt, ht FROM top_ht_%s WHERE %s='%s' order by cnt desc limit %d;",
					day_or_week, day_or_week, date, max));
		ResultSet rs = _RunQuery(stmt);
		List<Row> rows = rs.all();
		List<Map<String, Integer>> ht_cnt_list = new ArrayList<Map<String, Integer>>();
		for (Row r: rows) {
			Map<String, Integer> si_map = new HashMap<String, Integer>();
			si_map.put(r.getString("ht"), r.getInt("cnt"));
			ht_cnt_list.add(si_map);
		}
		return JSON.toString(ht_cnt_list);
	}

	String GetCoordTid(String day_or_week, String date, String ht) throws InterruptedException {
		SimpleStatement stmt = new SimpleStatement(
				String.format("SELECT lati, longi, tid FROM hashtag_createdat_loc WHERE %s_ht='%s %s';",
					day_or_week, date, ht));
		ResultSet rs = _RunQuery(stmt);
		List<Row> rows = rs.all();
		List<Map<String, String>> coord_tid_list = new ArrayList<Map<String, String>>();
		for (Row r: rows) {
			Map<String, String> kv_map = new HashMap<String, String>();
			kv_map.put("lati", Float.toString(r.getFloat("lati")));
			kv_map.put("longi", Float.toString(r.getFloat("longi")));
			kv_map.put("tid", Long.toString(r.getLong("tid")));
			coord_tid_list.add(kv_map);
		}
		return JSON.toString(coord_tid_list);
	}

	String GetTweet(long tid) throws InterruptedException {
		SimpleStatement stmt = new SimpleStatement(
				String.format("SELECT created_at, sn, text FROM tweet WHERE tid=%d;", tid));
		ResultSet rs = _RunQuery(stmt);
		List<Row> rows = rs.all();
		Map<String, String> kv_map = new HashMap<String, String>();
		for (Row r: rows) {
			kv_map.put("created_at", r.getString("created_at"));
			kv_map.put("sn", r.getString("sn"));
			kv_map.put("text", r.getString("text"));
			break;
		}
		return JSON.toString(kv_map);
	}

	private ResultSet _RunQuery(Query q) throws InterruptedException {     
		while (true) {
			try {
				return _sess.execute(q);
			} catch (DriverException e) {
				System.err.println("Error during query: " + e.getMessage());
				e.printStackTrace();
				System.out.printf("Retrying in 5 sec...\n");
				Thread.sleep(5000);
			}
		}
	}
}
