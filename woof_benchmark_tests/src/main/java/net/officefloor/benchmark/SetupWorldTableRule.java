package net.officefloor.benchmark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;

/**
 * Sets up the World database table.
 * 
 * @author Daniel Sagenschneider
 */
public class SetupWorldTableRule implements TestRule {

	/**
	 * {@link PostgreSqlRule}.
	 */
	private final PostgreSqlRule dataSource;

	/**
	 * Instantiate.
	 * 
	 * @param dataSource {@link PostgreSqlRule}.
	 */
	public SetupWorldTableRule(PostgreSqlRule dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Sets up the World table.
	 * 
	 * @throws Exception If fails to create World table.
	 */
	public void setupWorldTable() throws Exception {

		// Setup the database (must be done before starting OfficeFloor)
		try (Connection connection = this.dataSource.getConnection()) {
			try {
				connection.createStatement().executeQuery("SELECT * FROM World");
			} catch (SQLException ex) {
				connection.createStatement()
						.executeUpdate("CREATE TABLE World ( id INT PRIMARY KEY, randomNumber INT)");
				PreparedStatement insert = connection
						.prepareStatement("INSERT INTO World (id, randomNumber) VALUES (?, ?)");
				for (int i = 0; i < 10000; i++) {
					insert.setInt(1, i + 1);
					insert.setInt(2, ThreadLocalRandom.current().nextInt(1, 10000));
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

				// Set up World table
				SetupWorldTableRule.this.setupWorldTable();

				// Continue evaluation
				base.evaluate();
			}
		};
	}

}