package screen;

import java.util.Random;

/**
 * Slowly scrolling stars behind the main menu. Three layers move at
 * different speeds, so near stars pass faster than far ones.
 */
public class Starfield {

	/** Number of stars on screen. */
	private static final int STAR_COUNT = 70;
	/** Pixels per frame for the far, middle and near layers. */
	private static final float[] LAYER_SPEED = { 0.2f, 0.4f, 0.7f };
	/** Grey level for the far, middle and near layers. */
	private static final int[] LAYER_BRIGHTNESS = { 90, 150, 220 };

	/** Screen width the stars wrap around. */
	private final int width;
	/** Screen height the stars wrap around. */
	private final int height;
	/** Horizontal position of each star. */
	private final int[] x;
	/** Vertical position of each star, fractional so slow stars still move. */
	private final float[] y;
	/** Layer of each star: 0 far, 1 middle, 2 near. */
	private final int[] layer;

	/**
	 * Scatters the stars over the screen.
	 *
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 */
	public Starfield(final int width, final int height) {
		this.width = width;
		this.height = height;
		this.x = new int[STAR_COUNT];
		this.y = new float[STAR_COUNT];
		this.layer = new int[STAR_COUNT];

		Random random = new Random();
		for (int i = 0; i < STAR_COUNT; i++) {
			this.x[i] = random.nextInt(width);
			this.y[i] = random.nextInt(height);
			this.layer[i] = random.nextInt(LAYER_SPEED.length);
		}
	}

	/**
	 * Moves every star down by its layer's speed, wrapping to the top.
	 */
	public final void update() {
		for (int i = 0; i < STAR_COUNT; i++) {
			this.y[i] += LAYER_SPEED[this.layer[i]];
			if (this.y[i] >= this.height)
				this.y[i] -= this.height;
		}
	}

	/** @return Number of stars. */
	public final int getCount() {
		return STAR_COUNT;
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Horizontal position of the star.
	 */
	public final int getX(final int i) {
		return this.x[i];
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Vertical position of the star.
	 */
	public final int getY(final int i) {
		return (int) this.y[i];
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Grey level of the star, 0 to 255.
	 */
	public final int getBrightness(final int i) {
		return LAYER_BRIGHTNESS[this.layer[i]];
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Size of the star in pixels; near stars are larger.
	 */
	public final int getSize(final int i) {
		return this.layer[i] == LAYER_SPEED.length - 1 ? 2 : 1;
	}
}