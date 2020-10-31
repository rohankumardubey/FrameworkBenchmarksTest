package net.officefloor.benchmark;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides ability to have shared code between the Framework code and tests.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofBenchmarkShared {

	/**
	 * Allows turning off validation for all tests.
	 */
	private static final boolean isValidationActive = false;

	/**
	 * Shared counter.
	 */
	public static final AtomicInteger counter = new AtomicInteger(0);

	/**
	 * Able to turn off validation when not using.
	 */
	private static volatile boolean isValidate = isValidationActive;

	/**
	 * Test operation.
	 */
	public static interface TestOperation<R, E extends Throwable> {
		R test() throws E;
	}

	/**
	 * Runs test without validation.
	 * 
	 * @param <R>  Return type.
	 * @param <E>  Possible exception.
	 * @param test {@link TestOperation}.
	 * @return Result.
	 * @throws E Possible exception.
	 */
	public static <R, E extends Throwable> R runWithoutValidation(TestOperation<R, E> test) throws E {
		try {
			// Undertake with validation off
			isValidate = false;
			return test.test();

		} finally {
			// Reset to default
			isValidate = isValidationActive;
		}
	}

	/**
	 * Undertakes validation.
	 * 
	 * @param <E>        Possible exception.
	 * @param validation {@link TestOperation}.
	 * @throws E Possible exception.
	 */
	public static <E extends Throwable> void doValidation(TestOperation<Void, E> validation) throws E {

		// Avoid validation if not active
		if (!isValidate) {
			return;
		}

		// Undertake validation
		validation.test();
	}

	/**
	 * Asserts the counter value taking into account whether to validate.
	 * 
	 * @param expectedCounterValue Expected counter value.
	 * @param message              Failure message if counter not equal expected.
	 */
	public static void assertCounter(int expectedCounterValue, String message) {
		doValidation(() -> {
			int counterValue = counter.get();
			if (counterValue != expectedCounterValue) {
				throw new AssertionError(message + ". Expected " + expectedCounterValue + " but Actual " + counterValue
						+ " (difference of " + (expectedCounterValue - counterValue) + ")");
			}
			return null;
		});
	}

}