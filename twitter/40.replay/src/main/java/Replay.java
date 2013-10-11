import java.text.SimpleDateFormat;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Replay {
	long _rt_begin_milli;
	long _st_begin_milli;
	long _st_end_milli;
	double _replay_time;
	double _replay_time_sf;	// speed-up factor
	int _write_conc;
	int _read_conc;
	DC _dc;
	CassClient _cc;

	Replay(String[] args)
		throws java.net.UnknownHostException, java.io.FileNotFoundException,
										java.io.IOException, java.text.ParseException {
		_ParseOptions(args);
		_dc = new DC();
		_cc = new CassClient();
	}

	void Start() throws java.lang.InterruptedException {
		Thread w = new Thread(new TweetWriter(this));
		Thread r = new Thread(new TweetReader(this));
		w.start();
		r.start();
		w.join();
		r.join();
	}
	
	long SimTimeToRealTime(long sim_time_milli) {
		// st_dur = st_end - st_begin = "130428-000000" - "130407-000000"
		// rt_dur = rt_end - rt_begin = replay_time
		//
		// (st - st_begin) / st_dur = (rt - rt_begin) / rt_dur
		//
		// rt = (st - st_begin) / st_dur * rt_dur + rt_begin

		//System.out.println(_st_begin_milli + " " + sim_time_milli + " " + _rt_begin_milli + " " + System.currentTimeMillis());

		long rt = (long) ( ((double) (sim_time_milli - _st_begin_milli))
				/ (_st_end_milli - _st_begin_milli) * _replay_time + _rt_begin_milli );
		return rt;
	}
	
	private static final OptionParser _opt_parser = new OptionParser() {{
		accepts("h", "Show this help message");
		accepts("stbegin", "Simulation begin datetime")
			.withRequiredArg().defaultsTo("130407-000000");
		accepts("stend", "Simulation end datetime")
			.withRequiredArg().defaultsTo("130428-000000");
		accepts("replaytime", "Replay time in sec")
			.withRequiredArg().ofType(Integer.class).defaultsTo(10);
		accepts("wc", "Write concurrency: Number of Tweet writer threads")
			.withRequiredArg().ofType(Integer.class).defaultsTo(10);
		accepts("rc", "Read concurrency: Number of Tweet reader threads")
			.withRequiredArg().ofType(Integer.class).defaultsTo(10);
	}};

	void _PrintHelp() throws java.io.IOException {
		String class_name = this.getClass().getSimpleName();
		System.out.println("Usage: " + class_name + " [<option>]* ctime");
		System.out.println("  ctime: Current datetime for synchronizing multiple nodes. In the format of YYMMDD-HHMMSS\n");
		_opt_parser.printHelpOn(System.out);
	}

	private void _ParseOptions(String[] args)
		throws java.io.IOException, java.text.ParseException {
		OptionSet options = _opt_parser.parse(args);
		SimpleDateFormat sdf0 = new SimpleDateFormat("yyMMdd-hhmmss");
		if (options.has("h")) {
			_PrintHelp();
			System.exit(0);
		}
		List<?> nonop_args = options.nonOptionArguments();
		if (nonop_args.size() != 1) {
			_PrintHelp();
			System.exit(1);
		}
		String ctime = (String) nonop_args.get(0);
		System.out.println(ctime);

		_rt_begin_milli = sdf0.parse((String) nonop_args.get(0)).getTime() + 4000L;
		_st_begin_milli = sdf0.parse((String) options.valueOf("stbegin")).getTime();
		_st_end_milli = sdf0.parse((String) options.valueOf("stend")).getTime();
		_replay_time = (Integer)options.valueOf("replaytime") * 1000.0;
		_replay_time_sf = (_st_end_milli - _st_begin_milli) / _replay_time;
		_write_conc = (Integer)options.valueOf("wc");
		_read_conc = (Integer)options.valueOf("rc");
		long cur_time = System.currentTimeMillis();
		System.out.printf("_st_begin_milli: %s %d\n", sdf0.format(_st_begin_milli), _st_begin_milli);
		System.out.printf("_st_end_milli:   %s %d\n", sdf0.format(_st_end_milli), _st_end_milli);
		System.out.printf("_rt_begin_milli: %s %d\n", sdf0.format(_rt_begin_milli), _rt_begin_milli);
		System.out.printf("cur time:        %s %d\n", sdf0.format(cur_time), cur_time);
		System.out.printf("_replay_time:    %f\n", _replay_time);
	}

	public static void main(String[] args) throws Exception {
		try {
			// It has to be here. Doesn't work if put in the function GetEth0IP.
			System.setProperty("java.net.preferIPv4Stack", "true");
			Replay rp = new Replay(args);
			rp.Start();
			System.exit(0);
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
