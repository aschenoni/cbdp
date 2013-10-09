import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

class DC {
	List<Entry> _dcs = null;
	String _hostname;

	DC() throws java.io.FileNotFoundException, java.io.IOException {
		_hostname = Util.GetHostname();
		_ReadDCLocs();
	}

	boolean IsLocal(double lati, double longi) {
		DC.Entry dc_e = _GetClosest(lati, longi);
		return _hostname.equals(dc_e.hostname);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry e: _dcs) {
			sb.append(e);
			sb.append("\n");
		}
		return sb.toString();
	}

	private void _ReadDCLocs()
		throws java.io.FileNotFoundException, java.io.IOException {
		String fn = "../../conf/dc-coord";
		BufferedReader br = new BufferedReader(new FileReader(fn));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			if (line.length() == 0)
				continue;
			if (line.charAt(0) == '#')
				continue;
			Entry e = new Entry(line);
			if (_dcs == null)
				_dcs = new ArrayList<Entry>();
			_dcs.add(e);
		}
	}

	/*
	private double _Dist(
			double lati0, double longi0,
			double lati1, double longi1) {
		return Math.sqrt((lati1 - lati0) * (lati1 - lati0)
				+ (longi1 - longi0) * (longi1 - longi0));
	}
	*/

	// http://stackoverflow.com/questions/120283/working-with-latitude-longitude-values-in-java
	private double _Dist(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
			* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;
		return dist;
	}

	private Entry _GetClosest(double lati, double longi) {
		Entry s_e = null;
		double s_dist = 0.0;
		for (Entry e: _dcs) {
			double d = _Dist(lati, longi, e.lati, e.longi);
			if (s_e == null) {
				s_e = e;
				s_dist = d;
			} else {
				if (s_dist > d) {
					s_e = e;
					s_dist = d;
				}
			}
		}
		return s_e;
	}

	class Entry {
		String hostname;
		double longi;
		double lati;

		Entry(String line) {
			String[] tokens = line.split(" |,");
			if (tokens.length != 3)
				throw new RuntimeException("Unexpected DC loc format: [" + line + "]");
			hostname = tokens[0];
			longi = Double.parseDouble(tokens[1]);
			lati = Double.parseDouble(tokens[2]);
		}

		public String toString() {
			//return hostname + " " + lati + " " + longi;
			return hostname + " " + longi + " " + lati;
		}
	}

}
