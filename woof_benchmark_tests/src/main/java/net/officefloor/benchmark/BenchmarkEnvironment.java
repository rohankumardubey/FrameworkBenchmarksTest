/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.benchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.NumberFormat;
import java.util.concurrent.CompletableFuture;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.jdbc.postgresql.test.AbstractPostgreSqlJUnit.Configuration;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.test.system.SystemPropertiesRule;

/**
 * Provides benchmark environment.
 * 
 * @author Daniel Sagenschneider
 */
public class BenchmarkEnvironment {

	/**
	 * Timeout on requests.
	 */
	private static final int TIMEOUT = 5 * 60 * 1000;

	/**
	 * Starts PostgreSql for manual testing.
	 * 
	 * @param args Command line arguments.
	 * @throws Exception If fails to start.
	 */
	public static void main(String[] args) throws Exception {

		// Start PostgreSql
		createPostgreSqlRule().startPostgreSql();

		// Create the tables
		DbTest.setupDatabase();
		FortunesTest.setupDatabase();
	}

	/**
	 * Creates the {@link SystemPropertiesRule} for running testing.
	 * 
	 * @return {@link SystemPropertiesRule} for running testing.
	 */
	public static SystemPropertiesRule createSystemProperties() {
		return new SystemPropertiesRule(HttpServer.PROPERTY_HTTP_SERVER_NAME, "OF",
				HttpServer.PROPERTY_HTTP_DATE_HEADER, "true", HttpServer.PROPERTY_INCLUDE_STACK_TRACE, "false",
				HttpServerLocation.PROPERTY_HTTP_PORT, "8181",
				"OFFICE.net_officefloor_jdbc_DataSourceManagedObjectSource.server", "localhost",
				"OFFICE.net_officefloor_r2dbc_R2dbcManagedObjectSource.host", "localhost", "spring.datasource.url",
				"jdbc:postgresql://localhost:5432/hello_world");
	}

	/**
	 * Creates {@link PostgreSqlRule} for benchmark.
	 * 
	 * @return {@link PostgreSqlRule}.
	 */
	public static PostgreSqlRule createPostgreSqlRule() {
		return new PostgreSqlRule(new Configuration().server("localhost").port(5432).database("hello_world")
				.username("benchmarkdbuser").password("benchmarkdbpass").maxConnections(2000));
	}

	/**
	 * Undertakes validate test with similar load as benchmark validations.
	 * 
	 * @param url URL to send requests.
	 * @throws Exception If fail in validate test.
	 */
	public static void doValidateTest(String url) throws Exception {

		// Obtain the default validate details
		int clients = Integer.parseInt(System.getProperty("validate.clients", "512"));
		int iterations = Integer.parseInt(System.getProperty("validate.iterations", "1"));
		int pipelineBatchSize = Integer.parseInt(System.getProperty("validate.pipeline", "1"));

		// Undertake validate test
		doStressTest(url, clients, iterations, pipelineBatchSize, true);
	}

	/**
	 * Undertakes stress test with similar loads as benchmark validations.
	 * 
	 * @param url URL to send requests.
	 * @throws Exception If fail in stress test.
	 */
	public static void doStressTest(String url) throws Exception {

		// Obtain the default stress details
		int clients = Integer.parseInt(System.getProperty("stress.clients", "512"));
		int iterations = Integer.parseInt(System.getProperty("stress.iterations", "10"));
		int pipelineBatchSize = Integer.parseInt(System.getProperty("stress.pipeline", "10"));

		// Undertake stress test
		doStressTest(url, clients, iterations, pipelineBatchSize, false);
	}

	/**
	 * <p>
	 * Undertakes a pipelined stress test.
	 * <p>
	 * This is similar requesting as per the Tech Empower benchmarks.
	 * 
	 * @param url               URL to send requests.
	 * @param clients           Number of clients.
	 * @param iterations        Number of iterations.
	 * @param pipelineBatchSize Pipeline batch size (maximum number of requests
	 *                          pipelined together).
	 * @param isMimicValidate   Indicates to mimic validate. No overload responses.
	 * @throws Exception If failure in stress test.
	 */
	public static void doStressTest(String url, int clients, int iterations, int pipelineBatchSize,
			boolean isMimicValidate) throws Exception {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> {

			// Create configuration
			DefaultAsyncHttpClientConfig.Builder configuration = new DefaultAsyncHttpClientConfig.Builder()
					.setConnectTimeout(TIMEOUT).setReadTimeout(TIMEOUT);

			// Run load
			AsyncHttpClient[] warmupClients = new AsyncHttpClient[] { Dsl.asyncHttpClient(configuration) };
			AsyncHttpClient[] asyncClients = new AsyncHttpClient[clients];
			for (int i = 0; i < asyncClients.length; i++) {
				asyncClients[i] = Dsl.asyncHttpClient(configuration);
			}
			try {

				// Indicate test
				System.out.println();
				System.out.println((isMimicValidate ? "VALIDATE" : "STRESS") + ": " + url + " (with " + clients
						+ " clients, " + pipelineBatchSize + " pipeline for " + iterations + " iterations)");

				// Log the memory
				logMemory();

				// Undertake the warm up
				if (!isMimicValidate) {
					WoofBenchmarkShared.counter.set(0);
					doStressRequests(url, iterations, pipelineBatchSize, "warm up", warmupClients, isMimicValidate);
					WoofBenchmarkShared.assertCounter(iterations * pipelineBatchSize,
							"Incorrect number of warm up calls");

					// Log the memory
					logMemory();
				}

				// Capture the start time
				long startTime = System.currentTimeMillis();

				// Undertake the stress test
				WoofBenchmarkShared.counter.set(0);
				int overloadCount = doStressRequests(url, iterations, pipelineBatchSize, "iteration", asyncClients,
						isMimicValidate);
				WoofBenchmarkShared.assertCounter(clients * iterations * pipelineBatchSize,
						"Incorrect number of stress run calls");

				// Capture the completion time
				long endTime = System.currentTimeMillis();

				// Log the ending memory
				logMemory();

				// Indicate performance
				int totalSuccessfulRequests = (clients * iterations * pipelineBatchSize) - overloadCount;
				long totalTime = endTime - startTime;
				int requestsPerSecond = (int) ((totalSuccessfulRequests) / (((float) totalTime) / 1000.0));
				System.out.println("\tRequests: " + totalSuccessfulRequests + " (overload: " + overloadCount + ")");
				System.out.println("\tTime: " + totalTime + " milliseconds");
				System.out.println("\tReq/Sec: " + requestsPerSecond);
				System.out.println();

			} finally {
				// Close the clients
				for (AsyncHttpClient warmupClient : warmupClients) {
					warmupClient.close();
				}
				for (AsyncHttpClient asyncClient : asyncClients) {
					asyncClient.close();
				}
			}
		});
	}

	/**
	 * Undertakes running the requests.
	 * 
	 * @param url               URL to send requests.
	 * @param iterations        Number of iterations.
	 * @param pipelineBatchSize Pipeline batch size (maximum number of requests
	 *                          pipelined together).
	 * @param progressPrefix    Prefix to print out to indicate progress.
	 * @param clients           {@link AsyncHttpClient} instances.
	 * @param isMimicValidate   Indicates to mimic validate. No overload responses.
	 * @return Number of overload responses.
	 * @throws Exception If failure in stress test.
	 */
	@SuppressWarnings("unchecked")
	private static int doStressRequests(String url, int iterations, int pipelineBatchSize, String progressPrefix,
			AsyncHttpClient[] clients, boolean isMimicValidate) throws Exception {

		// capture number of overloads
		int overloadCount = 0;

		// Provide line to show progress
		System.out.println();

		// Run the iterations
		CompletableFuture<Response>[] futures = new CompletableFuture[clients.length * pipelineBatchSize];
		for (int i = 0; i < iterations; i++) {

			// Indicate progress (on same line)
			System.out.println("\033[F\r " + progressPrefix + " " + (i + 1));

			// Run the iteration
			for (int p = 0; p < pipelineBatchSize; p++) {
				for (int c = 0; c < clients.length; c++) {

					// Determine the index
					int index = (c * pipelineBatchSize) + p;

					// Undertake the request
					futures[index] = clients[c].prepareGet(url).setRequestTimeout(TIMEOUT).execute()
							.toCompletableFuture();
				}
			}

			// Ensure all responses are valid
			CompletableFuture.allOf(futures).get();
			for (CompletableFuture<Response> future : futures) {
				Response response = future.get();
				int statusCode = response.getStatusCode();
				assertTrue("Invalid response status code " + statusCode + "\n" + response.getResponseBody(),
						(statusCode == 200) || (statusCode == 503));
				if (statusCode == 503) {
					overloadCount++;
				}
			}
		}

		// Ensure no overload in validate
		if (isMimicValidate) {
			assertEquals("Should be no overload on initial validation", 0, overloadCount);
		}

		// Return the overload count
		return overloadCount;
	}

	/**
	 * Log memory.
	 */
	private static void logMemory() {

		// Obtain the memory management bean
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

		// Obtain the heap diagnosis details
		MemoryUsage heap = memoryBean.getHeapMemoryUsage();
		float usedPercentage = (heap.getUsed() / (float) heap.getMax());

		// Print the results
		NumberFormat format = NumberFormat.getPercentInstance();
		System.out.println("    HEAP: " + format.format(usedPercentage) + " (used=" + getMemorySize(heap.getUsed())
				+ ", max=" + getMemorySize(heap.getMax()) + ", init=" + getMemorySize(heap.getInit()) + ", commit="
				+ getMemorySize(heap.getCommitted()) + ", fq=" + memoryBean.getObjectPendingFinalizationCount() + ")");
	}

	/**
	 * Obtains the memory size.
	 * 
	 * @param memorySize Memory size.
	 * @return Formated memory size.
	 */
	private static String getMemorySize(long memorySize) {

		final long gigabyteSize = 1 << 30;
		final long megabyteSize = 1 << 20;
		final long kilobyteSize = 1 << 10;

		if (memorySize >= gigabyteSize) {
			return (memorySize / gigabyteSize) + "g";
		} else if (memorySize >= megabyteSize) {
			return (memorySize / megabyteSize) + "m";
		} else if (memorySize >= kilobyteSize) {
			return (memorySize / kilobyteSize) + "k";
		} else {
			return memorySize + "b";
		}
	}

}