package entity;

import java.awt.Color;

/**
 * AUTHORED BY: VFX TEAM (Effection)
 * Diamond dropped by a shot meteor. It drifts sideways while falling and bounces off the screen edges; the player must catch it with the ship.
 * After being caught it stays briefly for the "+N" popup.
 */
public class DiamondDrop extends Entity {

	public static final int SIZE = 12;
	public static final int POPUP_DURATION = 800; // ms
	private static final float FALL_SPEED = 0.1f; // px per ms
	public static final float DRIFT = 0.35f; // part of the meteor's sideways speed kept
	private static final long MAX_STEP = 50; // ms, avoids jumps on lag

	private final int value;
	private float exactX;
	private float exactY;
	private float speedX;
	private final int screenWidth;
	private long lastUpdate;
	private final long createdAt;
	private boolean caught;
	private long caughtAt;

	/** Creates a diamond centred on a point, drifting with the meteor. */
	public DiamondDrop(final int centerX, final int centerY, final int value, final float meteorSpeedX, final int screenWidth) {
		super(centerX - SIZE / 2, centerY - SIZE / 2, SIZE, SIZE, Color.CYAN);
		this.value = value;
		this.exactX = this.positionX;
		this.exactY = this.positionY;
		this.speedX = meteorSpeedX * DRIFT;
		this.screenWidth = screenWidth;
		this.createdAt = System.currentTimeMillis();
		this.lastUpdate = this.createdAt;
	}

	/** Makes the drop fall while it has not been caught. Call once per frame. */
	public final void update() {
		long now = System.currentTimeMillis();
		long step = Math.min(MAX_STEP, now - this.lastUpdate);
		this.lastUpdate = now;
		if (this.caught)
			return;
		this.exactY += FALL_SPEED * step;
		this.exactX += this.speedX * step;
		// Bounce off the side edges so it never leaves the screen.
		if (this.exactX < 0) {
			this.exactX = -this.exactX;
			this.speedX = -this.speedX;
		} else if (this.exactX > this.screenWidth - SIZE) {
			this.exactX = 2 * (this.screenWidth - SIZE) - this.exactX;
			this.speedX = -this.speedX;
		}
		this.positionX = Math.round(this.exactX);
		this.positionY = Math.round(this.exactY);
	}

	/** Marks the drop as caught and starts its "+N" popup. */
	public final void catchIt() {
		if (this.caught)
			return;
		this.caught = true;
		this.caughtAt = System.currentTimeMillis();
	}

	/** @return True once the drop has been caught. */
	public final boolean isCaught() {
		return this.caught;
	}

	/** @return True when the drop was caught and its popup has finished. */
	public final boolean isFinished() {
		return this.caught && getCaughtElapsed() >= POPUP_DURATION;
	}

	/** @return Milliseconds since the drop was caught, 0 before that. */
	public final long getCaughtElapsed() {
		if (!this.caught)
			return 0;
		return System.currentTimeMillis() - this.caughtAt;
	}

	/** @return Milliseconds since the drop appeared. */
	public final long getAge() {
		return System.currentTimeMillis() - this.createdAt;
	}

	/** @return Diamonds given when caught. */
	public final int getValue() {
		return this.value;
	}
}
