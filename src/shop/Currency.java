package shop;

/**
 * The two currencies a shop price can be paid in.
 *
 * @author GoG - Currency System
 */
public enum Currency {
	/** Main currency, earned from enemy coin drops. */
	COINS("coins"),
	/** Premium currency, earned by banking cleared levels. */
	DIAMONDS("diamonds");

	/** Short label shown next to prices. */
	private final String label;

	/**
	 * @param label
	 *            Short label shown next to prices.
	 */
	Currency(final String label) {
		this.label = label;
	}

	/**
	 * @return Short label shown next to prices.
	 */
	public String getLabel() {
		return this.label;
	}
}
