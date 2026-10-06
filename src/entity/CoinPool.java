package entity;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a pool of recyclable coins, avoiding needless allocation each
 * time an enemy drops one. Mirrors {@link BulletPool}.
 *
 * @author GoG - Currency System
 */
public final class CoinPool {

	/** Set of already created, currently unused coins. */
	private static Set<Coin> pool = new HashSet<Coin>();

	/**
	 * Constructor, not called.
	 */
	private CoinPool() {

	}

	/**
	 * Returns a coin from the pool if one is available, a new one if there
	 * isn't.
	 *
	 * @param positionX
	 *            Requested position of the coin in the X axis.
	 * @param positionY
	 *            Requested position of the coin in the Y axis.
	 * @param value
	 *            Currency value the coin should award when collected.
	 * @return Requested coin.
	 */
	public static Coin getCoin(final int positionX, final int positionY,
			final int value) {
		Coin coin;
		if (!pool.isEmpty()) {
			coin = pool.iterator().next();
			pool.remove(coin);
			coin.reset(positionX, positionY, value);
		} else {
			coin = new Coin(positionX, positionY, value);
		}
		return coin;
	}

	/**
	 * Adds one or more coins to the list of available ones.
	 *
	 * @param coins
	 *            Coins to recycle.
	 */
	public static void recycle(final Set<Coin> coins) {
		pool.addAll(coins);
	}
}
