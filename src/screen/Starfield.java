package screen;

import java.awt.Color;
import java.util.Random;

/**
 * Stars behind the main menu that fly out from the centre of the screen,
 * as if the ship is travelling forward through space. Each star has a
 * depth; as the depth shrinks the star moves away from the centre and
 * grows brighter, and once it passes the viewer it starts again far away.
 */
public class Starfield {

	/** Number of stars on screen. */
	private static final int STAR_COUNT = 120;
	/** Depth at which a new star appears. */
	private static final float MAX_DEPTH = 1000f;
	/** Depth at which a star has passed the viewer. */
	private static final float MIN_DEPTH = 10f;
	/** How much nearer every star gets each frame. */
	private static final float SPEED = 4f;
	/** How strongly depth spreads the stars out from the centre. */
	private static final float FOCAL_LENGTH = 300f;
	/** How many frames back the tail of a star starts. */
	private static final float TAIL_FRAMES = 3f;
	/** Greys from far (dim) to near (bright), made once. */
	private static final Color[] SHADES = new Color[8];

	static {
		for (int i = 0; i < SHADES.length; i++) {
			int grey = 50 + i * (230 - 50) / (SHADES.length - 1);
			SHADES[i] = new Color(grey, grey, grey);
		}
	}

	/** Centre of the screen, where stars come from. */
	private final int centerX;
	/** Centre of the screen, where stars come from. */
	private final int centerY;
	/** Screen width, to tell when a star has left the screen. */
	private final int width;
	/** Screen height, to tell when a star has left the screen. */
	private final int height;
	/** Picks the starting point of each star. */
	private final Random random;
	/** Sideways offset of each star in space. */
	private final float[] spaceX;
	/** Up-down offset of each star in space. */
	private final float[] spaceY;
	/** Distance of each star from the viewer. */
	private final float[] depth;

	/**
	 * Scatters the stars at random depths so the screen is full at once.
	 *
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 */
	public Starfield(final int width, final int height) {
		this.width = width;
		this.height = height;
		this.centerX = width / 2;
		this.centerY = height / 2;
		this.random = new Random();
		this.spaceX = new float[STAR_COUNT];
		this.spaceY = new float[STAR_COUNT];
		this.depth = new float[STAR_COUNT];

		for (int i = 0; i < STAR_COUNT; i++) {
			placeFar(i);
			this.depth[i] = MIN_DEPTH
					+ this.random.nextFloat() * (MAX_DEPTH - MIN_DEPTH);
		}
	}

	/**
	 * Moves every star nearer, restarting the ones that passed the viewer
	 * or left the screen.
	 */
	public final void update() {
		for (int i = 0; i < STAR_COUNT; i++) {
			this.depth[i] -= SPEED;
			if (this.depth[i] < MIN_DEPTH || isOffScreen(i))
				placeFar(i);
		}
	}

	/**
	 * Puts a star far away at a random point, so it starts near the centre.
	 *
	 * @param i
	 *            Star index.
	 */
	private void placeFar(final int i) {
		this.spaceX[i] = (this.random.nextFloat() * 2 - 1) * this.width;
		this.spaceY[i] = (this.random.nextFloat() * 2 - 1) * this.height;
		this.depth[i] = MAX_DEPTH;
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Whether the star is outside the screen.
	 */
	private boolean isOffScreen(final int i) {
		int x = getX(i);
		int y = getY(i);
		return x < 0 || x >= this.width || y < 0 || y >= this.height;
	}

	/** @return Number of stars. */
	public final int getCount() {
		return STAR_COUNT;
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Horizontal position on screen; nearer stars are further out.
	 */
	public final int getX(final int i) {
		return this.centerX
				+ (int) (this.spaceX[i] * FOCAL_LENGTH / this.depth[i]);
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Vertical position on screen; nearer stars are further out.
	 */
	public final int getY(final int i) {
		return this.centerY
				+ (int) (this.spaceY[i] * FOCAL_LENGTH / this.depth[i]);
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Horizontal position of the star a few frames ago, where its
	 *         streak starts.
	 */
	public final int getTailX(final int i) {
		return this.centerX + (int) (this.spaceX[i] * FOCAL_LENGTH
				/ (this.depth[i] + SPEED * TAIL_FRAMES));
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Vertical position of the star a few frames ago, where its
	 *         streak starts.
	 */
	public final int getTailY(final int i) {
		return this.centerY + (int) (this.spaceY[i] * FOCAL_LENGTH
				/ (this.depth[i] + SPEED * TAIL_FRAMES));
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Colour of the star; nearer stars are brighter.
	 */
	public final Color getColor(final int i) {
		float nearness = 1 - this.depth[i] / MAX_DEPTH;
		int shade = Math.min(SHADES.length - 1,
				(int) (nearness * SHADES.length));
		return SHADES[shade];
	}

	/**
	 * @param i
	 *            Star index.
	 * @return Size of the star in pixels; the nearest stars are larger.
	 */
	public final int getSize(final int i) {
		return this.depth[i] < MAX_DEPTH / 4 ? 2 : 1;
	}
}
