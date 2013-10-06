import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

class Tweet {
	String tid;
	String sn;
	String created_at;
	String real_coord;
	double lati;
	double longi;
	String in_reply_to;
	String r_tid;
	Set<String> hashtags;
	String text;

	Tweet(String line0, String line1) {
		StringTokenizer st = new StringTokenizer(line0);
		if (! st.hasMoreElements())
			throw new RuntimeException("Unexpected format line0: " + line0);
		tid = (String) st.nextElement();
		if (! st.hasMoreElements())
			throw new RuntimeException("Unexpected format line0: " + line0);
		sn = (String) st.nextElement();
		if (! st.hasMoreElements())
			throw new RuntimeException("Unexpected format line0: " + line0);
		created_at = (String) st.nextElement();
		if (! st.hasMoreElements())
			throw new RuntimeException("Unexpected format line0: " + line0);
		real_coord = (String) st.nextElement();
		if (! st.hasMoreElements())
			throw new RuntimeException("Unexpected format line0: " + line0);
		lati = Double.parseDouble((String) st.nextElement());
		if (! st.hasMoreElements())
			throw new RuntimeException("Unexpected format line0: " + line0);
		longi = Double.parseDouble((String) st.nextElement());
		if (! st.hasMoreElements())
			throw new RuntimeException("Unexpected format line0: " + line0);
		in_reply_to = (String) st.nextElement();
		if (! st.hasMoreElements())
			throw new RuntimeException("Unexpected format line0: " + line0);
		r_tid = (String) st.nextElement();
		hashtags = new HashSet<String>();
		while (st.hasMoreElements())
			hashtags.add((String) st.nextElement());
		text = line1;
	}
}

