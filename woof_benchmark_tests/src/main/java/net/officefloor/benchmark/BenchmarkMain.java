package net.officefloor.benchmark;

import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;

/**
 * Enables running tests separately to enable profiling of main code in another
 * process.
 * 
 * @author Daniel Sagenschneider
 */
public class BenchmarkMain {

	/**
	 * Main method.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) throws Exception {

		// Obtain operation to run
		if (args.length == 0) {
			throw new IllegalArgumentException("Must always provide one argument being operation");
		}
		String operation = args[0].toLowerCase();
		switch (operation) {

		case "setup":
			setup();
			break;

		case "test":
			test(args);
			break;

		default:
			throw new IllegalArgumentException("Unknown operation " + operation);
		}
	}

	/**
	 * Undertakes setup of the database.
	 */
	private static void setup() throws Exception {

		// Start the database
		PostgreSqlRule rule = BenchmarkEnvironment.createPostgreSqlRule();
		rule.startPostgreSql();

		// Run setup of the tables
		new SetupWorldTableRule(rule).setupWorldTable();
		new SetupFortuneTableRule(rule).setupFortuneTable();
	}

	/**
	 * Undertakes the testing.
	 * 
	 * @param args Command line arguments.
	 */
	private static void test(String... args) throws Exception {

		// Determine test
		String test = args.length >= 2 ? args[1].toLowerCase() : "all";
		switch (test) {

		case "json":
			new JsonTest().stress();
			break;

		case "db":
			new DbTest().stress();
			break;

		case "queries":
			new QueriesTest().stress();
			break;

		case "fortunes":
			new FortunesTest().stress();
			break;

		case "update":
			new UpdateTest().stress();
			break;

		case "plaintext":
			new PlaintextTest().stress();
			break;

		case "validate":
			new JsonTest().validate();
			new DbTest().validate();
			new QueriesTest().validate();
			new FortunesTest().validate();
			new UpdateTest().validate();
			new PlaintextTest().validate();
			break;

		case "all":
			test("test", "validate");
			test("test", "json");
			test("test", "db");
			test("test", "queries");
			test("test", "fortunes");
			test("test", "update");
			test("test", "plaintext");
			break;

		default:
			throw new IllegalArgumentException("Unknown test " + test);
		}
	}

}
