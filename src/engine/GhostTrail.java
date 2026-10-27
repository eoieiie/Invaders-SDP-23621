package engine;

import java.awt.Color;

import engine.DrawManager.SpriteType;
import entity.Ship;
import item.ItemAPI;
import item.ItemSystem;
import shop.Skin;

/**
 * Ghost Trail ship effect (shop item "effect_ghost_trail"). While the
 * player's ship moves, a few fading copies of it follow behind. When the
 * ship stops, the copies disappear. Look only, no gameplay change.
 * <p>
 * How it works: every frame {@link #draw} remembers the ship's position
 * and skin colour in a small ring buffer, then draws ghosts at the
 * positions from {@code GHOST_FRAMES} frames ago, each a bit more
 * transparent ({@code GHOST_ALPHA}). Faster movement gives stronger
 * ghosts. While the boost item is active the trail surges: two extra
 * ghosts and a brighter glow fade in. {@link #clear} empties the
 * buffer; GameScreen calls it on pause, game over and level clear, and
 * the trail clears itself while the ship is destroyed.
 * <p>
 * To tune the look, change the constants at the top of the class.
 * AUTHORED BY: VFX TEAM (Effection)
 */
public final class GhostTrail {

	/** How many past positions are remembered (one per frame). */
	private static final int HISTORY = 25;
	/** How many frames back each ghost is drawn, nearest first. */
	private static final int[] GHOST_FRAMES = { 4, 8, 12, 16 };
	/** Opacity of each ghost (0-255), nearest first. */
	private static final int[] GHOST_ALPHA = { 150, 115, 80, 45 };
	/** Extra ghosts while surging (boost item): frames back, farther out. */
	private static final int[] SURGE_FRAMES = { 20, 24 };
	/** Opacity of each extra surge ghost (0-255). */
	private static final int[] SURGE_ALPHA = { 40, 25 };
	/** How far ghosts are lightened toward white while surging (0-1). */
	private static final double SURGE_GLOW = 0.4;
	/** Frames the surge takes to fade fully in or out. */
	private static final int SURGE_RAMP_FRAMES = 12;
	/** Frames looked back to decide if the ship is moving. */
	private static final int MOVE_FRAMES = 16;
	/** Pixels the ship must move over the history to count as moving. */
	private static final int MIN_MOVE = 4;
	/** Frames looked back to measure the ship's speed. */
	private static final int SPEED_FRAMES = 4;
	/** Speed (pixels per frame) at which ghosts are fully visible. */
	private static final double FULL_SPEED = 4.0;
	/** How visible ghosts are at the slowest speed (0-1). */
	private static final double MIN_STRENGTH = 0.4;
	/** Shop preview: size of one sprite pixel on screen. */
	private static final int PREVIEW_PIXEL = 3;
	/** Shop preview: milliseconds per game frame (60 fps). */
	private static final int PREVIEW_FRAME_MS = 16;
	/** Shop preview: gap between the preview's top and the ship. */
	private static final int PREVIEW_OFFSET_Y = 20;
	/** Shop preview: time of one left-right slide, in milliseconds. */
	private static final int PREVIEW_CYCLE_MS = 2400;
	/** Shop preview: how far the ship slides each way, in pixels. */
	private static final int PREVIEW_SWING = 36;

	/** Past ship x positions, a ring buffer (no new objects per frame). */
	private final int[] historyX = new int[HISTORY];
	/** Past ship y positions, same slots as {@link #historyX}. */
	private final int[] historyY = new int[HISTORY];
	/** Past ship colours as RGB, same slots as {@link #historyX}. */
	private final int[] historyColor = new int[HISTORY];
	/** Slot of the newest position. */
	private int newest = -1;
	/** How many positions are stored (at most HISTORY). */
	private int count;
	/** How far ghosts are lightened this frame (0 = normal colour). */
	private double glow;
	/** How far the surge has faded in (0 = off, 1 = full). */
	private double surgeLevel;

	/**
	 * Remembers where the ship is this frame, and its colour. Call once
	 * per frame.
	 *
	 * @param x
	 *            Ship x.
	 * @param y
	 *            Ship y.
	 * @param color
	 *            Ship colour this frame (skin colour).
	 */
	public void record(final int x, final int y, final Color color) {
		this.newest = (this.newest + 1) % HISTORY;
		this.historyX[this.newest] = x;
		this.historyY[this.newest] = y;
		this.historyColor[this.newest] = color.getRGB();
		if (this.count < HISTORY)
			this.count++;
	}

	/** @return True if the ship moved enough lately to show ghosts. */
	public boolean isMoving() {
		if (this.count < 2)
			return false;
		int oldest = slotFramesAgo(Math.min(this.count - 1, MOVE_FRAMES));
		return Math.abs(this.historyX[this.newest]
				- this.historyX[oldest]) >= MIN_MOVE;
	}

	/** Forgets every remembered position, so no ghosts show. */
	public void clear() {
		this.count = 0;
		this.surgeLevel = 0;
	}

	/**
	 * @param items
	 *            Item system of the run (Team CS), may be null.
	 * @return True while the boost item is active.
	 */
	public static boolean isBoosting(final ItemSystem items) {
		if (items == null)
			return false;
		for (ItemAPI.EffectView effect : items.api().getView().effects)
			if (effect.item.effectKind == ItemAPI.EffectKind.BOOST)
				return true;
		return false;
	}

	/**
	 * Records the ship's position, then draws its ghosts behind it.
	 * Call every frame, just before the ship is drawn. While surging,
	 * extra ghosts make the trail longer and brighter.
	 *
	 * @param drawManager
	 *            Draw manager.
	 * @param ship
	 *            Player ship.
	 * @param shipColor
	 *            Skin colour of the ship this frame; ghosts keep it.
	 * @param surging
	 *            True while the ship is boosted.
	 */
	public void draw(final DrawManager drawManager, final Ship ship,
			final Color shipColor, final boolean surging) {
		// No ghosts while the ship is destroyed; start fresh after.
		if (ship.isDestroyed()) {
			clear();
			return;
		}
		record(ship.getPositionX(), ship.getPositionY(), shipColor);
		// Fade the surge in or out a little every frame.
		double step = 1.0 / SURGE_RAMP_FRAMES;
		this.surgeLevel = surging ? Math.min(1, this.surgeLevel + step)
				: Math.max(0, this.surgeLevel - step);
		if (!isMoving())
			return;
		double strength = speedStrength();
		this.glow = SURGE_GLOW * this.surgeLevel;
		// Surge ghosts are the farthest, so they are drawn first.
		if (this.surgeLevel > 0)
			for (int k = SURGE_FRAMES.length - 1; k >= 0; k--)
				drawGhost(drawManager, ship, SURGE_FRAMES[k],
						SURGE_ALPHA[k] * strength * this.surgeLevel);
		// Farthest ghost first, so nearer ghosts are drawn on top.
		for (int k = GHOST_FRAMES.length - 1; k >= 0; k--)
			drawGhost(drawManager, ship, GHOST_FRAMES[k],
					GHOST_ALPHA[k] * strength);
	}

	/**
	 * Draws one ghost, if that frame is remembered.
	 *
	 * @param drawManager
	 *            Draw manager.
	 * @param ship
	 *            Player ship (for its sprite).
	 * @param frames
	 *            How many frames back the ghost is.
	 * @param alpha
	 *            Opacity (0-255).
	 */
	private void drawGhost(final DrawManager drawManager, final Ship ship,
			final int frames, final double alpha) {
		int slot = slotFramesAgo(frames);
		if (slot < 0)
			return;
		Color base = new Color(this.historyColor[slot]);
		drawManager.drawSprite(ship.getSpriteType(),
				this.historyX[slot], this.historyY[slot],
				new Color(lighten(base.getRed()), lighten(base.getGreen()),
						lighten(base.getBlue()), (int) Math.round(alpha)));
	}

	/**
	 * @param channel
	 *            One colour channel (0-255).
	 * @return The channel moved toward white by the current glow.
	 */
	private int lighten(final int channel) {
		return (int) Math.round(channel + (255 - channel) * this.glow);
	}

	/**
	 * @return How visible the ghosts are (0-1): faster ship, stronger
	 *         ghosts.
	 */
	private double speedStrength() {
		int older = slotFramesAgo(SPEED_FRAMES);
		if (older < 0)
			return 1;
		double speed = Math.abs(this.historyX[this.newest]
				- this.historyX[older]) / (double) SPEED_FRAMES;
		return MIN_STRENGTH
				+ (1 - MIN_STRENGTH) * Math.min(1, speed / FULL_SPEED);
	}

	/**
	 * @param frames
	 *            Frames back (0 = now).
	 * @return Ring buffer slot of that frame, or -1 if not stored yet.
	 */
	private int slotFramesAgo(final int frames) {
		if (frames >= this.count)
			return -1;
		return (this.newest - frames + HISTORY) % HISTORY;
	}

	/**
	 * Shop preview in the skin's colours: each ghost uses the colour the
	 * skin had when the ship was there, so animated skins flow along it.
	 *
	 * @param drawManager
	 *            Draw manager.
	 * @param sprite
	 *            Ship sprite to show.
	 * @param skin
	 *            Selected skin.
	 * @param centerX
	 *            Horizontal centre of the preview.
	 * @param top
	 *            Top of the preview.
	 */
	public static void drawPreview(final DrawManager drawManager,
			final SpriteType sprite, final Skin skin, final int centerX,
			final int top) {
		long now = System.currentTimeMillis();
		int width = drawManager.getSpriteSize(sprite)[0] * PREVIEW_PIXEL;
		int shipY = top + PREVIEW_OFFSET_Y;
		for (int k = GHOST_FRAMES.length - 1; k >= 0; k--) {
			long then = now - GHOST_FRAMES[k] * PREVIEW_FRAME_MS;
			Color base = skin.colorAt(then);
			drawManager.drawSpriteScaled(sprite, previewX(centerX, width,
					then), shipY, PREVIEW_PIXEL,
					new Color(base.getRed(), base.getGreen(),
							base.getBlue(), GHOST_ALPHA[k]));
		}
		drawManager.drawSpriteScaled(sprite, previewX(centerX, width, now),
				shipY, PREVIEW_PIXEL, skin.colorAt(now));
	}

	/**
	 * @return Left edge of the preview ship at a given time: it slides
	 *         PREVIEW_SWING px each way and pauses at the ends.
	 */
	private static int previewX(final int centerX, final int width,
			final long time) {
		double phase = (time % PREVIEW_CYCLE_MS)
				/ (double) PREVIEW_CYCLE_MS * 2 * Math.PI;
		double s = Math.max(-1, Math.min(1, Math.sin(phase) * 1.4));
		return centerX - width / 2 + (int) Math.round(s * PREVIEW_SWING);
	}
}