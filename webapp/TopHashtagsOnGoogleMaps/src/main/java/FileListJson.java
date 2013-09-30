import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.jetty.util.ajax.JSON;

class FileListJson {
	static private String _r_week_list;
	static private String _r_day_list;

	static private final String BY_WEEK_DN = "/mnt/multidc-data/twitter/top-hashtags.130914/by-week";
	static private final String BY_DAY_DN = "/mnt/multidc-data/twitter/top-hashtags.130914/by-day";

	static String GetDateList(String day_or_week) {
		if (day_or_week.equals("day"))
			return _GetDayList();
		else if (day_or_week.equals("week"))
			return _GetWeekList();
		else throw new RuntimeException("invalid arg " + day_or_week);
	}

	static private String _GetWeekList() {
		if (_r_week_list == null) {
			List<String> fns = _GetFileList(BY_WEEK_DN);
			Collections.sort(fns);
			_r_week_list = JSON.toString(fns);
		}
		return _r_week_list;
	}

	static private String _GetDayList() {
		if (_r_day_list == null) {
			List<String> fns = _GetFileList(BY_DAY_DN);
			Collections.sort(fns);
			_r_day_list = JSON.toString(fns);
		}
		return _r_day_list;
	}

	static private List<String> _GetFileList(String dn) {
		List<String> fns = new ArrayList<String>();
		for (File f0: new File(dn).listFiles()) {
			if (! f0.isFile())
				continue;
			String fn = f0.getName();
			if (fn.matches("^.*\\.swp$"))
				continue;
			fns.add(fn);
		}
		return fns;
	}
}
