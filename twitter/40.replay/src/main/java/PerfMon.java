import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


class MonUserLatR implements AutoCloseable {
	static List<Lat> lats = new ArrayList<Lat>();
	Lat l;

	MonUserLatR() {
		l = new Lat();
		lats.add(l);
	}

	@Override
	public void close() {
		l.End();
	}

	static void WriteResult(String fn) throws java.io.IOException {
		int size = lats.size();
		if (size == 0) return;

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
			}
			if (min > l.dur) min = l.dur;
			else if (max < l.dur) max = l.dur;
			sum += l.dur;
			sum_sq += (((double)l.dur) * l.dur);
		}
		double avg = ((double) sum) / size;
		double sd = Math.sqrt(avg * avg - sum_sq / size);

		PrintWriter out = new PrintWriter(new FileWriter(fn));
		out.printf("# min max avg sd\n");
		out.printf("# %d %d %f %f\n", min, max, avg, sd);
		for (Lat l: lats) {
			out.printf("%d %d\n", l.timestamp, l.dur);
		}
		out.close();
	}

	class Lat {
		long timestamp;
		long dur;

		Lat() {
			timestamp = System.currentTimeMillis();
		}

		void End() {
			dur = System.currentTimeMillis() - timestamp;
		}
	}
}
