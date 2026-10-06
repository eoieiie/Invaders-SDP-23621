package entity;

import java.awt.Color;

/**
 * Implements a coin dropped by a destroyed enemy ship. Coins fall slowly
 * down the screen and can be collected by the player's ship for currency.
 *
 * @author GoG - Currency System
 */
public class Coin extends Entity {

	/** Default coin size, in pixels. */
	private static final int SIDE = 3 * 2;
	/** Default falling speed of a coin. */
	private static final int DEFAULT_SPEED = 2;
	/** Default currency value of a coin. */
	private static final int DEFAULT_VALUE = 1;

	/** Vertical speed at which the coin falls. */
	private int speed;
	/** Amount of currency this coin is worth when collected. */
	private int value;

	/**
	 * Constructor, establishes the coin's properties with the default
	 * value.
	 *
	 * @param positionX
	 *            Initial position of the coin in the X axis.
	 * @param positionY
	 *            Initial position of the coin in the Y axis.
	 */
	public Coin(final int positionX, final int positionY) {
		this(positionX, positionY, DEFAULT_VALUE);
	}

	/**
	 * Constructor, establishes the coin's properties with a custom value.
	 *
	 * @param positionX
	 *            Initial position of the coin in the X axis.
	 * @param positionY
	 *            Initial position of the coin in the Y axis.
	 * @param value
	 *            Currency value awarded when the coin is collected.
	 */
	public Coin(final int positionX, final int positionY, final int value) {
		super(positionX, positionY, SIDE, SIDE, Color.YELLOW);

		this.speed = DEFAULT_SPEED;
		this.value = value;
	}

	/**
	 * Updates the coin's position, making it fall.
	 */
	public final void update() {
		this.positionY += this.speed;
	}

	/**
	 * @return the currency value of this coin.
	 */
	public final int getValue() {
		return this.value;
	}

	/**
	 * Reconfigures a recycled coin coming from the pool.
	 *
	 * @param positionX
	 *            New X position.
	 * @param positionY
	 *            New Y position.
	 * @param value
	 *            New currency value.
	 */
	final void reset(final int positionX, final int positionY,
			final int value) {
		this.positionX = positionX;
		this.positionY = positionY;
		this.value = value;
	}
}
