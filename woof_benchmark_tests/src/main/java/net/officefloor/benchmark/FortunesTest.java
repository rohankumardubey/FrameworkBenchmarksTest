package net.officefloor.benchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;
import net.officefloor.test.system.SystemPropertiesRule;

/**
 * Tests multiple queries.
 */
public class FortunesTest {

	public static final String URL = "http://localhost:8181/fortunes";

	public static final SystemPropertiesRule systemProperties = BenchmarkEnvironment.createSystemProperties();

	public static final PostgreSqlRule dataSource = BenchmarkEnvironment.createPostgreSqlRule();

	public static final OfficeFloorRule server = new OfficeFloorRule();

	public static final HttpClientRule client = new HttpClientRule();

	@ClassRule
	public static final RuleChain order = RuleChain.outerRule(systemProperties).around(dataSource).around(server)
			.around(client);

	public static void setupDatabase(Connection connection) throws Exception {
		try {
			connection.createStatement().executeQuery("SELECT * FROM Fortune");
		} catch (SQLException ex) {
			connection.createStatement()
					.executeUpdate("CREATE TABLE Fortune ( id INT PRIMARY KEY, message VARCHAR(100))");
			PreparedStatement insert = connection.prepareStatement("INSERT INTO Fortune (id, message) VALUES (?, ?)");
			int id = 1;
			for (String message : new String[] { "fortune: No such file or directory",
					"A computer scientist is someone who fixes things that aren't broken.",
					"After enough decimal places, nobody gives a damn.",
					"A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1",
					"A computer program does what you tell it to do, not what you want it to do.",
					"Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen",
					"Any program that runs right is obsolete.",
					"A list is only as strong as its weakest link. — Donald Knuth", "Feature: A bug with seniority.",
					"Computers make very fast, very accurate mistakes.",
					"<script>alert(\"This should not be displayed in a browser alert box.\");</script>",
					"フレームワークのベンチマーク" }) {
				insert.setInt(1, id++);
				insert.setString(2, message);
				insert.executeUpdate();
			}
		}
	}

	@BeforeClass
	public static void setupDatabase() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			setupDatabase(connection);
		}
	}

	protected String getServerName() {
		return "O";
	}

	@Test
	public void validRequest() throws Exception {
		HttpResponse response = client.execute(new HttpGet(URL));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Should be successful:\n\n" + entity, 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content-type", "text/html;charset=utf-8",
				response.getFirstHeader("content-type").getValue());
		assertEquals("Incorrect server", this.getServerName(), response.getFirstHeader("Server").getValue());
		assertNotNull("Should have date", response.getFirstHeader("date"));
		assertEquals("Incorrect content",
				"<!DOCTYPE html><html><head><title>Fortunes</title></head><body><table><tr><th>id</th><th>message</th></tr>"
						+ "<tr><td>11</td><td>&lt;script&gt;alert(&quot;This should not be displayed in a browser alert box.&quot;);&lt;/script&gt;</td></tr>"
						+ "<tr><td>4</td><td>A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1</td></tr>"
						+ "<tr><td>5</td><td>A computer program does what you tell it to do, not what you want it to do.</td></tr>"
						+ "<tr><td>2</td><td>A computer scientist is someone who fixes things that aren't broken.</td></tr>"
						+ "<tr><td>8</td><td>A list is only as strong as its weakest link. &mdash; Donald Knuth</td></tr>"
						+ "<tr><td>0</td><td>Additional fortune added at request time.</td></tr>"
						+ "<tr><td>3</td><td>After enough decimal places, nobody gives a damn.</td></tr>"
						+ "<tr><td>7</td><td>Any program that runs right is obsolete.</td></tr>"
						+ "<tr><td>10</td><td>Computers make very fast, very accurate mistakes.</td></tr>"
						+ "<tr><td>6</td><td>Emacs is a nice operating system, but I prefer UNIX. &mdash; Tom Christaensen</td></tr>"
						+ "<tr><td>9</td><td>Feature: A bug with seniority.</td></tr>"
						+ "<tr><td>1</td><td>fortune: No such file or directory</td></tr>"
						+ "<tr><td>12</td><td>フレームワークのベンチマーク</td></tr>" + "</table></body></html>",
				entity);
	}

	@Test
	public void validate() throws Exception {
		BenchmarkEnvironment.doValidateTest(URL);
	}

	@Test
	public void stress() throws Exception {
		BenchmarkEnvironment.doStressTest(URL);
	}

}