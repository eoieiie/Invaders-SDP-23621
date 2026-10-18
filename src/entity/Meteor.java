package entity;

import java.awt.Color;
import java.util.Random;

/**
 * AUTHORED BY: VFX TEAM (Effection)
 * Rare bonus meteor that flies across the screen on a curved path. When shot
 * it bursts into sparks and drops a diamond. It never harms the player.
 */
public class Meteor extends Entity {

	public static final int SIZE = 9 * 2; // 9x9 sprite at 2-px scale
	public static final int BURST_DURATION = 900; // ms
	private static final int SPARK_COUNT = 12;
	private static final float SPARK_MIN_SPEED = 0.04f; // px per ms
	private static final float SPARK_SPEED_VARIANCE = 0.06f;
	private static final long MAX_STEP = 50; // ms, avoids jumps on lag
	private static final float SPIN_MIN = 0.003f; // radians per ms
	private static final float SPIN_VARIANCE = 0.006f;

	private final int direction; // 1 = right, -1 = left
	private final float speed; // signed sideways speed, px per ms
	private final float fallSpeedStart; // downward speed when entering
	private final float fallSpeedEnd; // downward speed when leaving
	private final float flightTime;
	private float flightElapsed;
	private float fallSpeed;
	private final float spinSpeed;
	private float rotation;
	private float exactX;
	private float exactY;
	private long lastUpdate;
	private final int diamondValue;
	private boolean bursting;
	private long burstStart;
	private final float[] sparkSpeedX;
	private final float[] sparkSpeedY;
	private final Random random;

	/** Creates a meteor just outside the left or right side of the screen. */
	public Meteor(final boolean fromLeft, final int positionY,
			final int screenWidth, final int crossTime,
			final double startAngle, final double endAngle,
			final int diamondValue, final Random random) {
		super(fromLeft ? -SIZE : screenWidth, positionY, SIZE, SIZE,new Color(120, 100, 90));
		this.direction = fromLeft ? 1 : -1;
		this.speed = this.direction * (float) (screenWidth + SIZE)/ Math.max(1, crossTime);
		this.fallSpeedStart = Math.abs(this.speed) * (float) Math.tan(Math.toRadians(startAngle));
		this.fallSpeedEnd = Math.abs(this.speed) * (float) Math.tan(Math.toRadians(endAngle));
		this.fallSpeed = this.fallSpeedStart;
		this.flightTime = Math.max(1, crossTime);
		this.exactX = this.positionX;
		this.exactY = this.positionY;
		this.lastUpdate = System.currentTimeMillis();
		this.diamondValue = diamondValue;
		this.random = random == null ? new Random() : random;
		// Rolls forward in its flight direction, at a random speed.
		this.spinSpeed = this.direction * (SPIN_MIN + this.random.nextFloat() * SPIN_VARIANCE);
		this.rotation = this.random.nextFloat() * (float) (2 * Math.PI);
		this.sparkSpeedX = new float[SPARK_COUNT];
		this.sparkSpeedY = new float[SPARK_COUNT];
	}

	/** Moves the meteor while it is flying. Call once per frame. */
	public final void update() {
		long now = System.currentTimeMillis();
		long step = Math.min(MAX_STEP, now - this.lastUpdate);
		this.lastUpdate = now;
		if (this.bursting)
			return;
		// Fall speed grows over the flight, so the path bends downward.
		this.flightElapsed += step;
		float progress = Math.min(1f, this.flightElapsed / this.flightTime);
		this.fallSpeed = this.fallSpeedStart + (this.fallSpeedEnd - this.fallSpeedStart) * progress;
		this.exactX += this.speed * step;
		this.exactY += this.fallSpeed * step;
		this.rotation += this.spinSpeed * step;
		this.positionX = Math.round(this.exactX);
		this.positionY = Math.round(this.exactY);
	}

	/** @return Total drop in pixels while crossing the screen. */
	public static int getTotalDrop(final int screenWidth,
			final double startAngle, final double endAngle) {
		// The fall speed changes evenly, so the drop uses the average slope.
		return (int) Math.ceil((screenWidth + SIZE) * (Math.tan(Math.toRadians(startAngle)) + Math.tan(Math.toRadians(endAngle))) / 2);
	}

	/** @return Current rotation of the rock, in radians. */
	public final float getRotation() {
		return this.rotation;
	}

	/** @return Downward speed divided by sideways speed (tail slope). */
	public final float getFallSlope() {
		return this.speed == 0 ? 0 : this.fallSpeed / Math.abs(this.speed);
	}

	/** Starts the burst: the meteor stops and its sparks fly out. */
	public final void burst() {
		if (this.bursting)
			return;
		this.bursting = true;
		this.burstStart = System.currentTimeMillis();
		for (int i = 0; i < SPARK_COUNT; i++) {
			// Even angles with a little jitter: round but never identical.
			double angle = 2 * Math.PI * (i + this.random.nextDouble() * 0.6) / SPARK_COUNT;
			float sparkSpeed = SPARK_MIN_SPEED + this.random.nextFloat() * SPARK_SPEED_VARIANCE;
			this.sparkSpeedX[i] = (float) Math.cos(angle) * sparkSpeed;
			this.sparkSpeedY[i] = (float) Math.sin(angle) * sparkSpeed;
		}
	}

	/** @return True once the meteor has been shot. */
	public final boolean isBursting() {
		return this.bursting;
	}

	/** @return Milliseconds since the burst started, 0 while flying. */
	public final long getBurstElapsed() {
		if (!this.bursting)
			return 0;
		return System.currentTimeMillis() - this.burstStart;
	}

	/** @return True when the burst animation has fully played. */
	public final boolean isBurstFinished() {
		return this.bursting && getBurstElapsed() >= BURST_DURATION;
	}

	/** @return True when a flying meteor has left the far side. */
	public final boolean isOffScreen(final int screenWidth) {
		if (this.bursting)
			return false;
		return this.direction > 0 ? this.positionX > screenWidth
				: this.positionX + this.width < 0;
	}

	/** @return Sideways speed in px per ms (sign = direction). */
	public final float getSpeedX() {
		return this.speed;
	}

	/** @return 1 when flying right, -1 when flying left. */
	public final int getDirection() {
		return this.direction;
	}

	/** @return Diamonds dropped when shot. */
	public final int getDiamondValue() {
		return this.diamondValue;
	}

	/** @return Number of sparks in the burst. */
	public final int getSparkCount() {
		return SPARK_COUNT;
	}

	/** @return Horizontal speed of a spark, in px per ms. */
	public final float getSparkSpeedX(final int index) {
		return this.sparkSpeedX[index];
	}

	/** @return Vertical speed of a spark, in px per ms. */
	public final float getSparkSpeedY(final int index) {
		return this.sparkSpeedY[index];
	}
}
