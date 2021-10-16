package net.officefloor.benchmark;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.VertxBuilder;
import io.vertx.core.impl.VertxThread;
import io.vertx.core.spi.VertxThreadFactory;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.impl.PgConnectionImpl;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;
import net.officefloor.test.system.SystemPropertiesRule;
import net.officefloor.vertx.OfficeFloorVertx;

/**
 * Ensure raw SQL Client interaction works (as using lower layer API that may
 * change).
 * 
 * @author Daniel Sagenschneider
 */
public class RawSqlClientTest {

	public static final SystemPropertiesRule systemProperties = BenchmarkEnvironment.createSystemProperties();

	public static final PostgreSqlRule dataSource = BenchmarkEnvironment.createPostgreSqlRule();

	public static final SetupWorldTableRule setupWorldTable = new SetupWorldTableRule(dataSource);

	public static final SetupFortuneTableRule setupFortuneTable = new SetupFortuneTableRule(dataSource);

	@ClassRule
	public static final RuleChain order = RuleChain.outerRule(systemProperties).around(dataSource)
			.around(setupWorldTable).around(setupFortuneTable);

	private static PgConnectionImpl conn;

	@BeforeClass
	public static void setupConnection() throws Exception {

		// Create connection
		PgConnectOptions connectOptions = new PgConnectOptions().setHost("localhost").setPort(5432)
				.setDatabase("hello_world").setUser("benchmarkdbuser").setPassword("benchmarkdbpass")
				.setCachePreparedStatements(true).setTcpNoDelay(true).setTcpQuickAck(true);

		// Setup Vertx for connection
		VertxOptions options = new VertxOptions().setPreferNativeTransport(true).setEventLoopPoolSize(1)
				.setWorkerPoolSize(1).setInternalBlockingPoolSize(1);
		VertxBuilder builder = new VertxBuilder(options).threadFactory(new VertxThreadFactory() {
			@Override
			public VertxThread newVertxThread(Runnable target, String name, boolean worker, long maxExecTime,
					TimeUnit maxExecTimeUnit) {
				return VertxThreadFactory.INSTANCE.newVertxThread(() -> {
					target.run();
				}, name, worker, maxExecTime, maxExecTimeUnit);
			}
		});
		Vertx vertx = builder.init().vertx();

		// Obtain the connection
		PgConnection connection = OfficeFloorVertx.block(PgConnection.connect(vertx, connectOptions));

		// Specify connection
		conn = (PgConnectionImpl) connection;
	}

	@Test
	public void db() throws Throwable {
		CollectHandler collect = new CollectHandler();
		Completion completion = new Completion(1);
		completion.schedule(createQuery("SELECT ID, RANDOMNUMBER FROM WORLD WHERE ID=$1",
				Tuple.of(ThreadLocalRandom.current().nextInt(1, 10001)), collect));
		completion.await();
		System.out.println("==== Db");
		for (Row row : collect.rows) {
			System.out.println("\t" + row.getInteger(0) + ": " + row.getInteger(1));
		}
		assertEquals("Should have single row", 1, collect.rows.size());
	}

	@Test
	public void queries() throws Throwable {
		final int COUNT = 20;
		CollectHandler collect = new CollectHandler();
		Completion completion = new Completion(COUNT);
		for (int i = 0; i < COUNT; i++) {
			completion.schedule(createQuery("SELECT ID, RANDOMNUMBER FROM WORLD WHERE ID=$1",
					Tuple.of(ThreadLocalRandom.current().nextInt(1, 10001)), collect));
		}
		completion.await();
		System.out.println("==== Queries");
		for (Row row : collect.rows) {
			System.out.println("\t" + row.getInteger(0) + ": " + row.getInteger(1));
		}
		assertEquals("Should have multiple rows", COUNT, collect.rows.size());
	}

	@Test
	public void fortunes() throws Throwable {
		CollectHandler collect = new CollectHandler();
		Completion completion = new Completion(1);
		completion.schedule(createQuery("SELECT ID, MESSAGE FROM FORTUNE", Tuple.tuple(), collect));
		completion.await();
		System.out.println("==== Fortunes");
		for (Row row : collect.rows) {
			System.out.println("\t" + row.getInteger(0) + ": " + row.getString(1));
		}
		assertEquals("Should have all rows", 12, collect.rows.size());
	}

	@Test
	public void update() throws Throwable {
		final int COUNT = 20;
		CollectHandler collect = new CollectHandler();
		Completion completion = new Completion(COUNT * 2);
		List<Row> rows = new ArrayList<>();
		Handler handler = (result, failure) -> {
			rows.addAll(result);
			if (rows.size() >= COUNT) {
				for (Row row : rows) {
					completion.schedule(createQuery("UPDATE world SET randomnumber=$1 WHERE id=$2",
							Tuple.of(ThreadLocalRandom.current().nextInt(1, 10001), row.getInteger(0)), collect));
				}
			}
		};
		for (int i = 0; i < COUNT; i++) {
			completion.schedule(createQuery("SELECT ID, RANDOMNUMBER FROM WORLD WHERE ID=$1",
					Tuple.of(ThreadLocalRandom.current().nextInt(1, 10001)), handler));
		}
		completion.await();
		System.out.println("==== Update");
		for (Row row : rows) {
			System.out.println("\t" + row.getInteger(0) + ": " + row.getInteger(1));
		}
		assertEquals("Should get not rows on update", 0, collect.rows.size());
	}

	private static ExtendedQueryCommand<List<Row>> createQuery(String sql, Tuple values, Handler handler) {
		return ExtendedQueryCommand.createQuery(sql, null, values, true, Collectors.toList(), handler);
	}

	public class CollectHandler implements Handler {

		private List<Row> rows = new ArrayList<>();

		@Override
		public void handle(List<Row> result, Throwable failure) {
			rows.addAll(result);
		}
	}

	private static interface Handler extends QueryResultHandler<List<Row>> {

		void handle(List<Row> result, Throwable failure);

		@Override
		public default <V> void addProperty(PropertyKind<V> property, V value) {
			// Do nothing
		}

		@Override
		public default void handleResult(int updatedCount, int size, RowDesc desc, List<Row> result,
				Throwable failure) {
			this.handle(result, failure);
		}
	}

	private static class Completion implements Promise<Boolean> {

		private final CountDownLatch latch;

		private volatile Throwable failure = null;

		private Completion(int requiredComplete) {
			this.latch = new CountDownLatch(requiredComplete);
		}

		public void schedule(ExtendedQueryCommand<?> query) {
			conn.schedule(query, this);
		}

		public void await() throws Throwable {
			this.latch.await(10, TimeUnit.SECONDS);
			if (this.failure != null) {
				throw this.failure;
			}
		}

		@Override
		public boolean tryComplete(Boolean result) {
			this.latch.countDown();
			return true;
		}

		@Override
		public boolean tryFail(Throwable cause) {
			this.failure = cause;
			this.latch.countDown();
			return true;
		}

		@Override
		public Future<Boolean> future() {
			Assert.fail("future should not be required");
			return null;
		}
	}
}