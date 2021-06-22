package net.officefloor.benchmark;
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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests.
 */
@RunWith(Suite.class)
@SuiteClasses({ TestSuite.UndertowJsonTest.class, TestSuite.UndertowDbTest.class, TestSuite.UndertowQueriesTest.class,
		TestSuite.UndertowCachedTest.class, TestSuite.UndertowFortunesTest.class, TestSuite.UndertowUpdateTest.class,
		TestSuite.UndertowPlaintextTest.class })
public class TestSuite {

	private static final String SERVER_NAME = "O Undertow";

	public static class UndertowJsonTest extends JsonTest {
		@Override
		protected String getServerName() {
			return SERVER_NAME;
		}
	}

	public static class UndertowDbTest extends DbTest {
		@Override
		protected String getServerName() {
			return SERVER_NAME;
		}
	}

	public static class UndertowQueriesTest extends QueriesTest {
		@Override
		protected String getServerName() {
			return SERVER_NAME;
		}
	}

	public static class UndertowCachedTest extends CachedTest {
		@Override
		protected String getServerName() {
			return SERVER_NAME;
		}
	}

	public static class UndertowFortunesTest extends FortunesTest {
		@Override
		protected String getServerName() {
			return SERVER_NAME;
		}
	}

	public static class UndertowUpdateTest extends UpdateTest {
		@Override
		protected String getServerName() {
			return SERVER_NAME;
		}
	}

	public static class UndertowPlaintextTest extends PlaintextTest {
		@Override
		protected String getServerName() {
			return SERVER_NAME;
		}
	}

}