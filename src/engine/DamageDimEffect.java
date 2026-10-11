package engine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *  AUTHORED BY: VFX TEAM (Effection)
 * 	Any further inquiries please contact us.
 * Dims the screen for a short time when the player ship is hit.
 * The overlay starts at maxAlpha and fades to 0 over the duration.
 *
 * Team Effection - Visual Effects.
 */
public class DamageDimEffect implements GameEvents.Listener {

    /** Default fade time in milliseconds. */
    private static final int DEFAULT_DURATION = 600;
    /** Default start opacity (0.0 = none, 1.0 = full black). */
    private static final float DEFAULT_MAX_ALPHA = 0.6f;

    /** Fade time in milliseconds. */
    private final int duration;
    /** Start opacity. */
    private final float maxAlpha;
    /** Overlay color. */
    private final Color color;

    /** Time when the effect started. */
    private long startTime;
    /** True while the effect is visible. */
    private boolean active;

    /** Strength of the dim effect. */
    private float strength = 1.0f;

    /** Creates the effect with default values. */
    public DamageDimEffect() {
        this(DEFAULT_DURATION, DEFAULT_MAX_ALPHA, Color.BLACK);
    }

    /**
     * Creates the effect with custom values.
     *
     * @param duration Fade time in milliseconds (min 1).
     * @param maxAlpha Start opacity, clamped to 0.0 - 1.0.
     * @param color    Overlay color.
     */
    public DamageDimEffect(final int duration, final float maxAlpha,
            final Color color) {
        this.duration = Math.max(1, duration);
        this.maxAlpha = Math.max(0f, Math.min(1f, maxAlpha));
        this.color = color == null ? Color.BLACK : color;
        this.active = false;
    }


   /** Starts the effect at full strength. */
    public void trigger() {
        trigger(1f);
    }

    /**
     * Starts the effect with a strength.
     *
     * @param strength 0.0 - 1.0, multiplies the start opacity.
     */
    public void trigger(final float strength) {
        this.strength = Math.max(0f, Math.min(1f, strength));
        this.startTime = System.currentTimeMillis();
        this.active = true;
    }

    /** Stops the effect at once (for example, on level end). */
    public void reset() {
        this.active = false;


    }
    /**
     * Reacts to PLAYER_HIT.
     *
     * @param type      Event type.
     * @param livesLeft Lives left after the hit.
     */
    @Override
    public void onEvent(final GameEvents.Type type, final int livesLeft) {
        if (type == GameEvents.Type.PLAYER_HIT)
            trigger(livesLeft <= 1 ? 1f : 0.35f);
    }


    /** @return True while the effect is visible. */
    public boolean isActive() {
        if (this.active
                && System.currentTimeMillis() - this.startTime >= this.duration) {
            this.active = false;
        }
        return this.active;
    }

    /**
     * Calculates the opacity for a given time.
     *
     * @param now Current time in milliseconds.
     * @return Opacity from maxAlpha down to 0.
     */
    public float getAlpha(final long now) {
        if (!this.active) {
            return 0f;
        }
        float progress = (float) (now - this.startTime) / this.duration;
        if (progress >= 1f) {
            this.active = false;
            return 0f;
        }
        if (progress < 0f) {
            progress = 0f;
        }
        return this.maxAlpha * this.strength * (1f - progress);
    }

    /** @return Current opacity. */
    public float getAlpha() {
        return getAlpha(System.currentTimeMillis());
    }

    /**
     * Draws the dim overlay.
     *
     * @param g      Graphics to draw on (back buffer).
     * @param width  Screen width.
     * @param height Screen height.
     */
    public void draw(final Graphics g, final int width, final int height) {
        float alpha = getAlpha();
        if (alpha <= 0f || !(g instanceof Graphics2D)) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        Composite old = g2.getComposite();
        g2.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(this.color);
        g2.fillRect(0, 0, width, height);
        g2.setComposite(old);
    }
}
