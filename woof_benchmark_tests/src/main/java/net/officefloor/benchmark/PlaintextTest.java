package net.officefloor.benchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.HttpClientRule;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.SystemPropertiesRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the plain text.
 */
public class PlaintextTest {

	@ClassRule
	public static SystemPropertiesRule systemProperties = new SystemPropertiesRule(HttpServer.PROPERTY_HTTP_SERVER_NAME,
			"OF", HttpServer.PROPERTY_HTTP_DATE_HEADER, "true", HttpServerLocation.PROPERTY_HTTP_PORT, "8181");

	@Rule
	public OfficeFloorRule server = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	protected String getServerName() {
		return "OF";
	}

	@Test
	public void validRequest() throws Exception {
		HttpResponse response = this.client.execute(new HttpGet("http://localhost:8181/plaintext"));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Should be successful:\n\n" + entity, 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content-type", "text/plain", response.getFirstHeader("content-type").getValue());
		assertEquals("Incorrect server", this.getServerName(), response.getFirstHeader("Server").getValue());
		assertNotNull("Should have date", response.getFirstHeader("date"));
		assertEquals("Incorrect content", "Hello, World!", entity);
	}

	@Test
	public void stress() throws Exception {
		BenchmarkEnvironment.doStressTest("http://localhost:8181/plaintext", 4, 10, 250);
	}

}