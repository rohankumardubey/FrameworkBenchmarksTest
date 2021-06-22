package net.officefloor.benchmark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;

/**
 * Sets up the Fortune database table.
 * 
 * @author Daniel Sagenschneider
 */
public class SetupFortuneTableRule implements TestRule {

	/**
	 * {@link PostgreSqlRule}.
	 */
	private final PostgreSqlRule dataSource;

	/**
	 * Instantiate.
	 * 
	 * @param dataSource {@link PostgreSqlRule}.
	 */
	public SetupFortuneTableRule(PostgreSqlRule dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Sets up the Fortune table.
	 * 
	 * @throws Exception If fails to create Fortune table.
	 */
	public void setupFortuneTable() throws Exception {

		// Setup the database (must be done before starting OfficeFloor)
		try (Connection connection = this.dataSource.getConnection()) {
			try {
				connection.createStatement().executeQuery("SELECT * FROM Fortune");
			} catch (SQLException ex) {
				connection.createStatement()
						.executeUpdate("CREATE TABLE Fortune ( id INT PRIMARY KEY, message VARCHAR(100))");
				PreparedStatement insert = connection
						.prepareStatement("INSERT INTO Fortune (id, message) VALUES (?, ?)");
				int id = 1;
				for (String message : new String[] { "fortune: No such file or directory",
						"A computer scientist is someone who fixes things that aren't broken.",
						"After enough decimal places, nobody gives a damn.",
						"A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1",
						"A computer program does what you tell it to do, not what you want it to do.",
						"Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen",
						"Any program that runs right is obsolete.",
						"A list is only as strong as its weakest link. — Donald Knuth",
						"Feature: A bug with seniority.", "Computers make very fast, very accurate mistakes.",
						"<script>alert(\"This should not be displayed in a browser alert box.\");</script>",
						"フレームワークのベンチマーク" }) {
					insert.setInt(1, id++);
					insert.setString(2, message);
					insert.executeUpdate();
				}
			}
		}
	}

	/*
	 * ======================== TestRule ==========================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Set up Fortune table
				SetupFortuneTableRule.this.setupFortuneTable();

				// Continue evaluation
				base.evaluate();
			}
		};
	}

}