/*
 *      Copyright (C) 2012 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package multidc;

import java.io.IOException;
import java.util.List;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.*;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class LoadTweetAndIndex {
	private static final OptionParser parser = new OptionParser() {{
		accepts("h", "Show this help message");
		accepts("t", "Level of concurrency to use").withRequiredArg().ofType(Integer.class).defaultsTo(4);
		accepts("csv", "Save metrics into csv instead of displaying on stdout");
		accepts("ip", "The hosts ip to connect to").withRequiredArg().ofType(String.class).defaultsTo("127.0.0.1");
	}};

	private static void printHelp(OptionParser parser) {
		System.out.println("Usage: load-tweet-and-index [<option>]* tweet_dir tweet|index|both\n");

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

	private static void createSchema(Session sess) {
		System.out.println("Creating schema...");

		try {
			sess.execute("CREATE KEYSPACE tweet WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
		} catch (AlreadyExistsException e) { /* It's ok, ignore */ }

		sess.execute("USE tweet;");

		try {
			sess.execute("CREATE TABLE hashtag_createdat_loc ("
					+ "day_ht text, "
					+ "week_ht text, "
					+ "lati float, "
					+ "longi float, "
					+ "real_coord boolean, "
					+ "tid bigint, "
					+ "PRIMARY KEY (day_ht, tid) "
					+ "); ");
		} catch (AlreadyExistsException e) { /* It's ok, ignore */ }
		try {
			sess.execute("CREATE INDEX hashtag_createdat_loc_week_ht ON hashtag_createdat_loc(week_ht); ");
		} catch (InvalidQueryException e) {
			if (! e.getMessage().equals("Index already exists"))
				throw e;
		}

		try {
			sess.execute("CREATE TABLE tweet ("
					+ "tid bigint, "
					+ "sn text, "
					+ "created_at text, "
					+ "text text, "
					+ "PRIMARY KEY (tid) "
					+ "); ");
		} catch (AlreadyExistsException e) { /* It's ok, ignore */ }
	}

	public static void main(String[] args) throws Exception {
		OptionSet options = parseOptions(args);
		int concurrency = (Integer)options.valueOf("t");
		String input_dir = options.nonOptionArguments().get(0).toString();
		String which_table = options.nonOptionArguments().get(1).toString();

		boolean useCsv = options.has("csv");

		System.out.println("concurrency: " + concurrency);

		try {
			ConciseTweetReader ctr = new ConciseTweetReader(input_dir, concurrency);

			// Create session to hosts
			Cluster cluster = new Cluster.Builder().addContactPoints(String.valueOf(options.valueOf("ip"))).build();

			final int maxRequestsPerConnection = 128;
			int maxConnections = concurrency / maxRequestsPerConnection + 1;

			PoolingOptions pools = cluster.getConfiguration().getPoolingOptions();
			pools.setMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.LOCAL, concurrency);
			pools.setCoreConnectionsPerHost(HostDistance.LOCAL, maxConnections);
			pools.setMaxConnectionsPerHost(HostDistance.LOCAL, maxConnections);
			pools.setCoreConnectionsPerHost(HostDistance.REMOTE, maxConnections);
			pools.setMaxConnectionsPerHost(HostDistance.REMOTE, maxConnections);

			Session session = cluster.connect();

			Metadata metadata = cluster.getMetadata();
			System.out.println(String.format("Connected to cluster '%s' on %s.", metadata.getClusterName(), metadata.getAllHosts()));

			createSchema(session);

			Reporter reporter = new Reporter(useCsv);

			CassTweetInserter[] tis = new CassTweetInserter[concurrency];
			for (int i = 0; i < concurrency; i++) {
				tis[i] = new CassTweetInserter(session, ctr, which_table, reporter);
			}

			System.out.println("Inserting tweets...");

			for (CassTweetInserter ti : tis)
				ti.start();

			for (CassTweetInserter ti : tis)
				ti.join();

			System.exit(0);
		} catch (NoHostAvailableException e) {
			System.err.println("No alive hosts to use: " + e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
