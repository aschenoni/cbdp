package multidc;

import java.io.IOException;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class GenTopHashtags {
	private static final OptionParser parser = new OptionParser() {{
		accepts("h", "Show this help message");
		accepts("t", "Level of concurrency to use").withRequiredArg().ofType(Integer.class).defaultsTo(4);
		accepts("ip", "The hosts ip to connect to").withRequiredArg().ofType(String.class).defaultsTo("127.0.0.1");
	}};

	private static void printHelp(OptionParser parser) {
		System.out.println("Usage: GenTopHashtags [<option>]* concise_tweet_dir output_dir\n");

		try {
			parser.printHelpOn(System.out);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private static OptionSet parseOptions(String[] args) {
		try {
			OptionSet options = parser.parse(args);
			if (options.has("h")) {
				printHelp(parser);
				System.exit(0);
			}

			List<?> noop_args = options.nonOptionArguments();
			if (noop_args.size() != 2) {
				printHelp(parser);
				System.exit(1);
			}

			return options;
		} catch (Exception e) {
			System.err.println("Error parsing options: " + e.getMessage());
			printHelp(parser);
			System.exit(1);
			throw new AssertionError();
		}
	}


	public static void main(String[] args) throws Exception {
		try {
			OptionSet options = parseOptions(args);
			int concurrency = (Integer)options.valueOf("t");
			String input_dir = options.nonOptionArguments().get(0).toString();
			String output_dir = options.nonOptionArguments().get(1).toString();
			System.out.println("concurrency: " + concurrency);

			ConciseTweetReader ctr = new ConciseTweetReader(input_dir, concurrency);
			HashtagCounter hc = new HashtagCounter(ctr, concurrency);
			hc.Join();
			hc.GenOutput(output_dir);
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
