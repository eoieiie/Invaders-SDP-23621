package engine;

/**
 * Tracks the player's diamond balance and keeps it persisted to disk, the
 * same way {@link CurrencyManager} does for coins.
 *
 * Diamonds are the secondary, premium currency. They are only earned by
 * clearing levels: clearing level N earns N diamonds, but they stay
 * "pending" (carried in {@link GameState}, not added to this balance) until
 * the player deliberately banks them on screen.CashOutScreen, or clears the
 * final level. Dying before banking forfeits them. That risk is what keeps
 * diamonds from being handed out for free just by playing.
 *
 * @author GoG - Currency System
 */
public final class DiamondManager {

	/** Singleton instance. */
	private static DiamondManager instance;

	/** Current diamond balance, persisted to disk. Never negative. */
	private int diamonds;

	/**
	 * Private constructor, loads whatever balance was last saved.
	 */
	private DiamondManager() {
		this.diamonds = Math.max(0, FileManager.getInstance().loadDiamonds());
	}

	/**
	 * Controls access to the diamond manager.
	 *
	 * @return shared instance of DiamondManager.
	 */
	public static DiamondManager getInstance() {
		if (instance == null)
			instance = new DiamondManager();
		return instance;
	}

	/**
	 * Banks diamonds into the permanent balance, e.g. when the player
	 * cashes out a run's pending diamonds, and immediately persists the
	 * new balance. Capped at Integer.MAX_VALUE instead of overflowing.
	 *
	 * @param amount
	 *            Amount of diamonds to add. Ignored if not positive.
	 */
	public void addDiamonds(final int amount) {
		if (amount <= 0)
			return;
		this.diamonds = (int) Math.min(Integer.MAX_VALUE,
				(long) this.diamonds + amount);
		save();
	}

	/**
	 * @return current diamond balance.
	 */
	public int getDiamonds() {
		return this.diamonds;
	}

	/**
	 * Checks whether the balance covers a price, without changing it.
	 *
	 * @param amount
	 *            Price to check.
	 * @return true if the player currently has at least that many diamonds.
	 */
	public boolean canAfford(final int amount) {
		return amount >= 0 && this.diamonds >= amount;
	}

	/**
	 * Attempts to spend diamonds, e.g. buying a skin. Checks-then-spends in
	 * one call so a caller never needs to call getDiamonds() first. Persists
	 * the new balance immediately on success.
	 *
	 * @param amount
	 *            Amount of diamonds to spend. Must be positive.
	 * @return true if the balance had enough diamonds and the amount was
	 *         deducted; false if funds were insufficient and nothing
	 *         changed.
	 */
	public boolean trySpend(final int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException("amount must be positive");
		if (this.diamonds < amount)
			return false;
		this.diamonds -= amount;
		save();
		return true;
	}

	/**
	 * Resets the balance to zero and persists it. Useful for tests; not
	 * meant to be called during normal play.
	 */
	public void reset() {
		this.diamonds = 0;
		save();
	}

	/**
	 * Writes the current balance to disk.
	 */
	private void save() {
		FileManager.getInstance().saveDiamonds(this.diamonds);
	}
}
