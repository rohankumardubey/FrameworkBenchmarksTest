package net.officefloor.benchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;
import net.officefloor.test.system.SystemPropertiesRule;

/**
 * Tests multiple queries.
 */
public class UpdateTest {

	public static final String URL = "http://localhost:8181/update?queries=20";

	public static final SystemPropertiesRule systemProperties = BenchmarkEnvironment.createSystemProperties();

	public static final PostgreSqlRule dataSource = BenchmarkEnvironment.createPostgreSqlRule();

	public static final SetupWorldTableRule setupWorldTable = new SetupWorldTableRule(dataSource);

	public static final OfficeFloorRule server = new OfficeFloorRule();

	public static final HttpClientRule client = new HttpClientRule();

	@ClassRule
	public static final RuleChain order = RuleChain.outerRule(systemProperties).around(dataSource)
			.around(setupWorldTable).around(server).around(client);

	protected String getServerName() {
		return "O";
	}

	@Test
	public void validRequest() throws Exception {
		HttpResponse response = client.execute(new HttpGet(URL));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Should be successful:\n\n" + entity, 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content-type", "application/json", response.getFirstHeader("content-type").getValue());
		assertEquals("Incorrect server", this.getServerName(), response.getFirstHeader("Server").getValue());
		assertNotNull("Should have date", response.getFirstHeader("date"));
		WorldResponse[] worlds = new ObjectMapper().readValue(entity, WorldResponse[].class);
		assertEquals("Incorrect number of worlds", 20, worlds.length);

		// Create map of results
		Map<Integer, WorldResponse> uniqueResponses = new HashMap<>();
		for (WorldResponse world : worlds) {
			uniqueResponses.put(world.getId(), world);
		}

		// Ensure database updated to the values
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement statement = connection
					.prepareStatement("SELECT ID, RANDOMNUMBER FROM WORLD WHERE ID = ?");
			for (WorldResponse world : uniqueResponses.values()) {
				statement.setInt(1, world.getId());
				ResultSet resultSet = statement.executeQuery();
				assertTrue("Should find row", resultSet.next());
				assertEquals("Should update row " + world.getId(), world.getRandomNumber(), resultSet.getInt(2));
			}
		}
	}

	@Test
	public void validate() throws Exception {
		BenchmarkEnvironment.doValidateTest(URL);
	}

	@Test
	public void stress() throws Exception {
		BenchmarkEnvironment.doRequestResponseStressTest(URL);
	}

	@Data
	public static class WorldResponse {
		private int id;
		private int randomNumber;
	}

}