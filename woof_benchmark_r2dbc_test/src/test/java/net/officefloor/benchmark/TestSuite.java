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

/**
 * Tests.
 */
@RunWith(Suite.class)
@SuiteClasses({ TestSuite.R2dbcJsonTest.class, TestSuite.R2dbcPlaintextTest.class, TestSuite.R2dbcDbTest.class,
		TestSuite.R2dbcQueriesTest.class, TestSuite.R2dbcCachedTest.class, TestSuite.R2dbcFortunesTest.class,
		TestSuite.R2dbcUpdateTest.class })
public class TestSuite {

	public static void start() throws Throwable {
		R2dbcOfficeFloorMain.main(new String[] { "8181" });
	}

	public static void stop() throws Exception {
		if (RawWoof.socketManager != null) {
			RawWoof.socketManager.shutdown();
		}
	}

	public static class R2dbcJsonTest extends JsonTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class R2dbcPlaintextTest extends PlaintextTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class R2dbcDbTest extends DbTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class R2dbcQueriesTest extends QueriesTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class R2dbcCachedTest extends CachedTest {
		@Before
		public void start() throws Throwable {
			TestSuite.start();
		}

		@After
		public void stop() throws Exception {
			TestSuite.stop();
		}
	}

	public static class R2dbcFortunesTest extends FortunesTest {

		public R2dbcFortunesTest() {
			super(true);
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

	public static class R2dbcUpdateTest extends UpdateTest {
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