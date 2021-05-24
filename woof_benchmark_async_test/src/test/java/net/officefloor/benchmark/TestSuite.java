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

import static org.junit.Assert.fail;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.officefloor.server.http.HttpClientRule;

/**
 * Tests.
 */
@RunWith(Suite.class)
@SuiteClasses({ JsonTest.class, TestSuite.AsyncDbTest.class, TestSuite.AsyncQueriesTest.class,
		TestSuite.AsyncFortunesTest.class, TestSuite.AsyncUpdateTest.class, PlaintextTest.class })
public class TestSuite {

	public static void warmup(HttpClientRule client, String url) throws Exception {
		HttpResponse response = null;
		for (int i = 0; i < 10; i++) {
			response = client.execute(new HttpGet(url));
			if (response.getStatusLine().getStatusCode() == 200) {
				return; // warmed up
			}
		}
		String entity = EntityUtils.toString(response.getEntity());
		fail("Failed to warm up\n\tstatus=" + response.getStatusLine().getStatusCode() + "\n\tentity=" + entity);
	}

	public static class AsyncDbTest extends DbTest {
		@Before
		public void warmup() throws Exception {
			TestSuite.warmup(client, URL);
		}
	}

	public static class AsyncQueriesTest extends QueriesTest {
		@Before
		public void warmup() throws Exception {
			TestSuite.warmup(client, URL + "1");
		}
	}

	public static class AsyncFortunesTest extends FortunesTest {
		@Before
		public void warmup() throws Exception {
			TestSuite.warmup(client, URL);
		}
	}

	public static class AsyncUpdateTest extends UpdateTest {
		@Before
		public void warmup() throws Exception {
			TestSuite.warmup(client, URL);
		}
	}

}