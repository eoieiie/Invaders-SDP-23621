package engine;

import java.util.Random;

/**
 * AUTHORED BY: VFX TEAM (Effection)
 *
 * Short screen shake when the player destroys an enemy.
 * The shake starts at full power and fades to 0 over the duration.
 * A new trigger restarts the shake. It does not add power,
 * so many kills in a row give a light shake that does not grow.
 */
public class ScreenShake {

    /** Default max offset in pixels. */
    private static final int DEFAULT_POWER = 5;
    /** Default shake time in milliseconds. */
    private static final int DEFAULT_DURATION = 200;

    /** Max offset in pixels. */
    private final int power;
    /** Shake time in milliseconds. */
    private final int duration;
    /** Random source for the offset. */
    private final Random random;

    /** Time when the shake started. */
    private long startTime;
    /** True while the shake is on. */
    private boolean active;
    /** Current X offset. */
    private int offsetX;
    /** Current Y offset. */
    private int offsetY;

    /** Creates the shake with default values (5 px, 200 ms). */
    public ScreenShake() {
        this(DEFAULT_POWER, DEFAULT_DURATION, new Random());
    }

    /**
     * Creates the shake with custom values.
     *
     * @param power    Max offset in pixels (min 0).
     * @param duration Shake time in milliseconds (min 1).
     * @param random   Random source.
     */
    public ScreenShake(final int power, final int duration,
                       final Random random) {
        this.power = Math.max(0, power);
        this.duration = Math.max(1, duration);
        this.random = random == null ? new Random() : random;
        this.active = false;
    }

    /** Starts (or restarts) the shake at full power. */
    public void trigger() {
        this.startTime = System.currentTimeMillis();
        this.active = true;
    }

    /** Calculates the offset for this frame. Call once per frame. */
    public void update() {
        this.offsetX = 0;
        this.offsetY = 0;
        if (!this.active)
            return;

        long elapsed = System.currentTimeMillis() - this.startTime;
        if (elapsed >= this.duration) {
            this.active = false;
            return;
        }

        float strength = 1f - (float) elapsed / this.duration;
        this.offsetX = Math.round((this.random.nextFloat() * 2f - 1f)
                * this.power * strength);
        this.offsetY = Math.round((this.random.nextFloat() * 2f - 1f)
                * this.power * strength);
    }

    /** @return Current X offset in pixels. */
    public int getOffsetX() {
        return this.offsetX;
    }

    /** @return Current Y offset in pixels. */
    public int getOffsetY() {
        return this.offsetY;
    }

    /** @return True while the shake is on. */
    public boolean isActive() {
        return this.active;
    }
}