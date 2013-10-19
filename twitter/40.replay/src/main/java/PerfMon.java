import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


class MonUserLat implements AutoCloseable {
	static List<Lat> r_lats = new ArrayList<Lat>();
	static List<Lat> w_lats = new ArrayList<Lat>();
	Lat l;

	MonUserLat(char op) {
		l = new Lat();
		if (op == 'R') r_lats.add(l);
		else if (op == 'W') w_lats.add(l);
		else throw new RuntimeException("Unknown op " + op);
	}

	@Override
	public void close() {
		l.End();
	}

	static void WriteResult(String dn) throws java.io.IOException {
		new File(dn).mkdirs();
		_WriteResult(dn + "/userlat-r", r_lats);
		_WriteResult(dn + "/userlat-w", w_lats);
	}

	static private void _WriteResult(String fn, List<Lat> lats) throws java.io.IOException {
		int size = lats.size();
		if (size == 0) return;
		Collections.sort(lats, Lat.ByTS);

		long min = 0;
		long max = 0;
		long sum = 0;
		double sum_sq = 0.0;
		boolean first = true;

		for (Lat l: lats) {
			if (first) {
				first = false;
				min = max = sum = l.dur;
				sum_sq = ((double)l.dur) * l.dur;
			} else {
				if (min > l.dur) min = l.dur;
				else if (max < l.dur) max = l.dur;
				sum += l.dur;
				sum_sq += (((double)l.dur) * l.dur);
			}
		}
		double avg = ((double) sum) / size;
		double sd = Math.sqrt(sum_sq / size - avg * avg);

		PrintWriter out = new PrintWriter(new FileWriter(fn));
		out.printf("# min max avg sd\n");
		out.printf("# %d %d %f %f\n", min, max, avg, sd);

		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss.SSS");
		for (Lat l: lats)
			out.printf("%s %d %d\n", sdf.format(l.timestamp), l.timestamp, l.dur);
		out.close();
	}

	static class Lat {
		long timestamp;
		long dur;

		Lat() {
			timestamp = System.currentTimeMillis();
		}

		void End() {
			dur = System.currentTimeMillis() - timestamp;
		}

		public static Comparator<Lat> ByTS = new Comparator<Lat>() {
			//@Override
			public int compare(Lat t1, Lat t2) {
				return (int)(t1.timestamp - t2.timestamp);
			}
		};
	}
}
