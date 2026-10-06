package engine;

import java.util.Random;

/**
 * Decides, using a configurable probability, whether a destroyed enemy ship
 * drops a coin.
 *
 * The chance is intentionally low (10% by default, i.e. roughly 1 in 10
 * kills) and centralized in this single class so the whole team can balance
 * the in-game economy from one place instead of guaranteeing a coin on
 * every kill. Without this kind of gate, currency would flood in linearly
 * with kills and break the shop/reward balance (power creep).
 *
 * @author GoG - Currency System
 */
public final class CoinDropManager {

	/** Default chance of a coin dropping after a kill (1 in 10). */
	public static final double DEFAULT_DROP_CHANCE = 0.10d;

	/** Currently configured drop chance, between 0.0 and 1.0. */
	private double dropChance;
	/** Random source used to roll the chance. */
	private final Random random;

	/**
	 * Constructor, uses the default drop chance (10%) and a new random
	 * source.
	 */
	public CoinDropManager() {
		this(DEFAULT_DROP_CHANCE, new Random());
	}

	/**
	 * Constructor, allows a custom drop chance.
	 *
	 * @param dropChance
	 *            Probability of a drop, between 0.0 (never) and 1.0
	 *            (always).
	 */
	public CoinDropManager(final double dropChance) {
		this(dropChance, new Random());
	}

	/**
	 * Constructor mainly meant for testing: allows injecting a seeded
	 * Random so drop behaviour is deterministic and verifiable instead of
	 * relying on real randomness.
	 *
	 * @param dropChance
	 *            Probability of a drop, between 0.0 (never) and 1.0
	 *            (always).
	 * @param random
	 *            Random source to use.
	 */
	public CoinDropManager(final double dropChance, final Random random) {
		this.random = random;
		setDropChance(dropChance);
	}

	/**
	 * Rolls the dice to decide whether a coin should drop this time.
	 *
	 * @return true if a coin should be dropped.
	 */
	public boolean rollForDrop() {
		return this.random.nextDouble() < this.dropChance;
	}

	/**
	 * Updates the drop chance, e.g. for difficulty tuning or event
	 * boosts.
	 *
	 * @param dropChance
	 *            New probability, between 0.0 and 1.0.
	 */
	public final void setDropChance(final double dropChance) {
		if (dropChance < 0.0d || dropChance > 1.0d)
			throw new IllegalArgumentException(
					"dropChance must be between 0.0 and 1.0");
		this.dropChance = dropChance;
	}

	/**
	 * @return the currently configured drop chance.
	 */
	public final double getDropChance() {
		return this.dropChance;
	}
}
