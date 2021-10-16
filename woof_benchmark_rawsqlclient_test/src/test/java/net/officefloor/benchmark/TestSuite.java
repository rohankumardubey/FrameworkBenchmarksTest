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

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.officefloor.vertx.OfficeFloorVertx;

/**
 * Tests.
 */
@RunWith(Suite.class)
@SuiteClasses({ TestSuite.SqlClientJsonTest.class, TestSuite.SqlClientPlaintextTest.class,
		TestSuite.SqlClientDbTest.class, TestSuite.SqlClientQueriesTest.class, TestSuite.SqlClientFortunesTest.class,
		TestSuite.SqlClientUpdateTest.class })
public class TestSuite {

	public static void start() throws Throwable {
		OfficeFloorVertx.setVertx(null); // reset between tests
		RawSqlClientOfficeFloorMain.main(new String[] { "8181" });
	}

	public static void stop() throws Exception {
		if (RawWoof.socketManager != null) {
			RawWoof.socketManager.shutdown();
		}
	}

	public static class SqlClientJsonTest extends JsonTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class SqlClientPlaintextTest extends PlaintextTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class SqlClientDbTest extends DbTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class SqlClientQueriesTest extends QueriesTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class SqlClientFortunesTest extends FortunesTest {

		public SqlClientFortunesTest() {
			this.isGuavaEscaping = true;
		}

		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class SqlClientUpdateTest extends UpdateTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

}