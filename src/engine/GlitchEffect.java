package engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Glitch effect for low player health.
 * While enabled: shows light scanlines all the time, and short random
 * "bursts" that shift horizontal slices of the screen, add red/cyan
 * lines and noise blocks.
 * AUTHORED BY: VFX TEAM (Effection)
 * Any further inquiries please contact us.
 * Team Effection - Visual Effects.
 */
public class GlitchEffect {

    /** Minimum time between bursts (ms). */
    private static final int MIN_GAP = 400;
    /** Maximum time between bursts (ms). */
    private static final int MAX_GAP = 1500;
    /** Minimum burst length (ms). */
    private static final int MIN_BURST = 80;
    /** Maximum burst length (ms). */
    private static final int MAX_BURST = 220;
    /** Maximum number of shifted slices per frame. */
    private static final int MAX_SLICES = 5;
    /** Maximum horizontal shift of a slice (px). */
    private static final int MAX_SHIFT = 14;
    /** Maximum height of a slice (px). */
    private static final int MAX_SLICE_HEIGHT = 18;
    /** Number of noise blocks per burst frame. */
    private static final int NOISE_BLOCKS = 12;
    /** Space between scanlines (px). */
    private static final int SCANLINE_GAP = 3;

    /** Scanline color. */
    private static final Color SCANLINE = new Color(0, 0, 0, 70);
    /** Red split color. */
    private static final Color RED = new Color(255, 0, 60, 110);
    /** Cyan split color. */
    private static final Color CYAN = new Color(0, 255, 255, 110);
    /** Noise color. */
    private static final Color NOISE = new Color(255, 255, 255, 120);

    /** Random source. */
    private final Random random;
    /** True while player health is low. */
    private boolean enabled;
    /** Time when next burst starts. */
    private long nextBurstTime;
    /** Time when current burst ends. */
    private long burstEndTime;

    /** Creates the effect. */
    public GlitchEffect() {
        this(new Random());
    }

    /**
     * Creates the effect with a given random source (for tests).
     *
     * @param random Random source.
     */
    public GlitchEffect(final Random random) {
        this.random = random == null ? new Random() : random;
        this.enabled = false;
    }

    /**
     * Turns the effect on or off. First burst starts at once when
     * the effect turns on.
     *
     * @param enabled True for low health.
     */
    public void setEnabled(final boolean enabled) {
        if (enabled && !this.enabled) {
            this.nextBurstTime = System.currentTimeMillis();
            this.burstEndTime = 0;
        }
        this.enabled = enabled;
    }

    /** @return True if the effect is on. */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @param now Current time (ms).
     * @return True if a burst is visible now.
     */
    public boolean isBursting(final long now) {
        return this.enabled && now < this.burstEndTime;
    }

    /**
     * Starts a new burst when its time comes.
     *
     * @param now Current time (ms).
     */
    private void updateBurst(final long now) {
        if (now >= this.nextBurstTime) {
            this.burstEndTime = now + range(MIN_BURST, MAX_BURST);
            this.nextBurstTime = this.burstEndTime + range(MIN_GAP, MAX_GAP);
        }
    }

    /**
     * Draws the glitch on the back buffer.
     *
     * @param image Back buffer image.
     * @param g     Graphics of the back buffer.
     */
    public void draw(final BufferedImage image, final Graphics g) {
        if (!this.enabled || image == null || g == null) {
            return;
        }
        long now = System.currentTimeMillis();
        updateBurst(now);

        int width = image.getWidth();
        int height = image.getHeight();

        drawScanlines(g, width, height);

        if (now < this.burstEndTime) {
            shiftSlices(image, g, width, height);
            drawNoise(g, width, height);
        }
    }

    /** Draws dark scanlines over the screen. */
    private void drawScanlines(final Graphics g, final int width,
            final int height) {
        g.setColor(SCANLINE);
        for (int y = 0; y < height; y += SCANLINE_GAP) {
            g.drawLine(0, y, width, y);
        }
    }

    /** Moves random horizontal slices left or right. */
    private void shiftSlices(final BufferedImage image, final Graphics g,
            final int width, final int height) {
        int count = 1 + this.random.nextInt(MAX_SLICES);
        for (int i = 0; i < count; i++) {
            int sliceHeight = 2 + this.random.nextInt(MAX_SLICE_HEIGHT);
            int y = this.random.nextInt(Math.max(1, height - sliceHeight));
            int dx = this.random.nextInt(MAX_SHIFT * 2 + 1) - MAX_SHIFT;
            if (dx == 0) {
                continue;
            }

            // Copy slice first, so we do not read pixels we already moved.
            BufferedImage slice = new BufferedImage(width, sliceHeight,
                    BufferedImage.TYPE_INT_RGB);
            Graphics sg = slice.getGraphics();
            sg.drawImage(image, 0, 0, width, sliceHeight,
                    0, y, width, y + sliceHeight, null);
            sg.dispose();

            g.drawImage(slice, dx, y, null);
            // Wrap the part that goes off screen to the other side.
            g.drawImage(slice, dx > 0 ? dx - width : dx + width, y, null);

            // Color split line on slice edge.
            g.setColor(this.random.nextBoolean() ? RED : CYAN);
            g.fillRect(0, y, width, 1);
            g.fillRect(0, y + sliceHeight - 1, width, 1);
        }
    }

    /** Draws small random noise blocks. */
    private void drawNoise(final Graphics g, final int width,
            final int height) {
        for (int i = 0; i < NOISE_BLOCKS; i++) {
            int w = 2 + this.random.nextInt(20);
            int h = 1 + this.random.nextInt(3);
            int x = this.random.nextInt(Math.max(1, width - w));
            int y = this.random.nextInt(Math.max(1, height - h));
            int pick = this.random.nextInt(3);
            g.setColor(pick == 0 ? RED : pick == 1 ? CYAN : NOISE);
            g.fillRect(x, y, w, h);
        }
    }

    /** @return Random int from min to max (inclusive). */
    private int range(final int min, final int max) {
        return min + this.random.nextInt(max - min + 1);
    }
}
