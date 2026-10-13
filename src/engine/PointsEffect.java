package engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * AUTHORED BY: VFX TEAM (Effection)
 * Any further inquiries please contact us.
 * Shows the points earned when an enemy is destroyed.
 *
 * - Regular enemies: the number pops in just above the explosion, drifts
 *   up a little, hovers so it can be read, then blinks out with a few
 *   sparks. It never covers the explosion or the enemies around it.
 * - Bonus ship: the number streaks sideways like a meteor with a burning
 *   tail, starting to the right of the explosion so it never covers it.
 * - Color shows the value: +10 white, +20 light orange, +30 orange-red,
 *   +100 hot red. No yellow (coins) and no green (player / HUD).
 * - Kills close together make the number slightly bigger. This is visual
 *   only; the score itself is not changed.
 * - The number has a dark outline, so it stays readable over sprites.
 * - It never moves downward (falling things look like enemy bullets) and
 *   never crosses the HUD line.
 * - A fixed pool is reused, so no objects are created while playing.
 *   Finished popups are removed automatically.
 *
 * Team Effection - Visual Effects.
 */
public class PointsEffect {

    /** Maximum popups on screen; the oldest is reused when full. */
    private static final int MAX_POPUPS = 16;
    /** Time the popup moves before stopping (ms). */
    private static final int FLY_TIME = 400;
    /** Time it hovers so the number can be read (ms). */
    private static final int HOLD_TIME = 350;
    /** Time it blinks and bursts into sparks (ms). */
    private static final int BURST_TIME = 200;
    /** Total life of a popup (ms). */
    private static final int LIFETIME = FLY_TIME + HOLD_TIME + BURST_TIME;
    /** Time of each step of the "pop" when the number appears (ms). */
    private static final int POP_STEP = 50;
    /** Time between two tail points (ms). */
    private static final int TRAIL_STEP = 22;
    /** Blink speed while bursting (ms). */
    private static final int BLINK = 50;
    /** Size of one retro pixel, same as the sprites (px). */
    private static final int PIXEL = 2;
    /** Space kept between a popup and the HUD line (px). */
    private static final int CEILING_MARGIN = 4;
    /** Space kept from the screen sides (px). */
    private static final int SIDE_MARGIN = 6;
    /** Rough half height of the text, used before fonts are known (px). */
    private static final int TEXT_HALF_HEIGHT = 8;

    /** Half height of an enemy explosion sprite (px). */
    private static final int EXPLOSION_HALF_HEIGHT = 8;
    /** Half width of the bonus ship explosion sprite (px). */
    private static final int BONUS_EXPLOSION_HALF_WIDTH = 16;
    /** Gap kept between a number and its explosion (px). */
    private static final int EXPLOSION_GAP = 3;
    /** How far a regular number drifts up (px). */
    private static final int DRIFT = 4;
    /** How far the bonus meteor streaks sideways (px). */
    private static final int STREAK = 70;
    /** How far the bonus meteor rises (px). */
    private static final int STREAK_RISE = 6;
    /** Distance from the bonus explosion edge to where the meteor starts. */
    private static final int STREAK_START_OFFSET = 26;

    /** Font size of regular points (same as the HUD score). */
    private static final float FONT_SIZE = 14f;
    /** Font size of bonus points. */
    private static final float BONUS_FONT_SIZE = 20f;
    /** Biggest regular font size, so numbers fit between enemy rows. */
    private static final float MAX_FONT_SIZE = 24f;
    /** Size steps of the "pop": big, medium, then normal. */
    private static final float[] POP_SCALES = { 1.6f, 1.3f, 1f };
    /** Kills closer together than this count as a streak (ms). */
    private static final int STREAK_WINDOW = 1000;
    /** Biggest streak that still makes the number bigger. */
    private static final int MAX_STREAK_LEVEL = 4;
    /** Extra size per kill in a streak. */
    private static final float STREAK_GROWTH = 0.15f;

    /** Bonus tail length. */
    private static final int TRAIL_LENGTH = 12;
    /** Sparks per tier (10, 20, 30, bonus). */
    private static final int[] SPARKS = { 4, 5, 6, 10 };

    /** Number colors per tier (10, 20, 30, bonus). */
    private static final Color[] TEXT = { Color.WHITE,
            new Color(255, 190, 110), new Color(255, 120, 60),
            new Color(255, 75, 55) };
    /** Glow colors per tier (10, 20, 30, bonus). */
    private static final Color[] GLOW = { new Color(110, 110, 130),
            new Color(190, 90, 20), new Color(170, 40, 10),
            new Color(255, 200, 120) };
    /** Dark outline around the number. */
    private static final Color OUTLINE = Color.BLACK;
    /** Hottest tail color, next to the number. */
    private static final Color TAIL_HOT = new Color(255, 160, 40);
    /** Middle tail color. */
    private static final Color TAIL_MID = new Color(235, 75, 20);
    /** Coldest tail color, end of the tail. */
    private static final Color TAIL_COLD = new Color(120, 25, 25);

    /** Number of precomputed shades, so no colors are created per frame. */
    private static final int SHADES = 16;
    /** Tail shades from the number (hot) to the end (cold). */
    private static final Color[] TAIL_SHADES = new Color[SHADES];
    /** Spark shades from the start (hot) to the end (cold) of a burst. */
    private static final Color[] SPARK_SHADES = new Color[SHADES];

    static {
        for (int i = 0; i < SHADES; i++) {
            float p = (float) i / (SHADES - 1);
            TAIL_SHADES[i] = p < 0.5f
                    ? mix(TAIL_HOT, TAIL_MID, p * 2f)
                    : mix(TAIL_MID, TAIL_COLD, (p - 0.5f) * 2f);
            SPARK_SHADES[i] = mix(TAIL_HOT, TAIL_COLD, p);
        }
    }

    /** One popup in the pool. */
    private static final class Popup {
        /** True while on screen. */
        private boolean active;
        /** Spawn number, used for small visual variation. */
        private int id;
        /** Spawn time (ms). */
        private long startTime;
        /** Start position, center of the text (px). */
        private double startX;
        /** Start position, center of the text (px). */
        private double startY;
        /** Total sideways travel (px). */
        private double travelX;
        /** Total vertical travel, negative is up (px). */
        private double travelY;
        /** 0 = 10 pts, 1 = 20 pts, 2 = 30 pts, 3 = bonus. */
        private int tier;
        /** Kills in a row when this one appeared. */
        private int streak;
        /** Text shown, for example "+30". */
        private String text;
        /** Center Y of the destroyed enemy (regular popups). */
        private double enemyY;
        /** Right edge of the bonus explosion, -1 for regular popups. */
        private double explosionRight;

        /**
         * @param t Time since spawn (ms).
         * @return How far along the movement it is, from 0 to 1. It starts
         *         fast and slows down to a stop.
         */
        private double travel(final double t) {
            double u = Math.max(0, Math.min(1, t / FLY_TIME));
            return 1 - (1 - u) * (1 - u);
        }

        /** @return X position after t ms. */
        private double x(final double t) {
            return this.startX + this.travelX * travel(t);
        }

        /** @return Y position after t ms. */
        private double y(final double t) {
            return this.startY + this.travelY * travel(t);
        }

        /** @return True for the bonus ship meteor. */
        private boolean isBonus() {
            return this.tier == 3;
        }
    }

    /** Pool of popups. */
    private final Popup[] popups;
    /** Lowest Y the top of a number may reach (just under the HUD line). */
    private final int ceilingY;
    /** Counts spawned popups. */
    private int spawnCount;
    /** Time of the last kill, for streaks (ms). */
    private long lastKillTime;
    /** Kills in the current streak. */
    private int streak;
    /** Font the sizes below were made from. */
    private Font baseFont;
    /** Regular fonts for each streak level and pop step. */
    private final Font[][] fonts =
            new Font[MAX_STREAK_LEVEL][POP_SCALES.length];
    /** Bonus fonts for each pop step. */
    private final Font[] bonusFonts = new Font[POP_SCALES.length];

    /**
     * Creates the effect.
     *
     * @param hudLineY Y of the HUD separation line; popups stay below it.
     */
    public PointsEffect(final int hudLineY) {
        this.ceilingY = hudLineY + CEILING_MARGIN;
        this.popups = new Popup[MAX_POPUPS];
        for (int i = 0; i < MAX_POPUPS; i++) {
            this.popups[i] = new Popup();
        }
        this.lastKillTime = Long.MIN_VALUE / 2;
    }

    /**
     * Shows the points of a regular enemy. Call when it is destroyed.
     *
     * @param centerX Center X of the destroyed enemy.
     * @param centerY Center Y of the destroyed enemy.
     * @param points  Points earned.
     */
    public void spawn(final int centerX, final int centerY, final int points) {
        spawn(centerX, centerY, points, System.currentTimeMillis());
    }

    /**
     * Shows the points of a regular enemy at a given time (for tests).
     *
     * @param centerX Center X of the destroyed enemy.
     * @param centerY Center Y of the destroyed enemy.
     * @param points  Points earned.
     * @param now     Current time (ms).
     */
    public void spawn(final int centerX, final int centerY, final int points,
            final long now) {
        Popup m = take(now);
        start(m, centerX, centerY, points, now);
        // Start just above the explosion, then drift up a little.
        m.enemyY = centerY;
        m.startY = centerY - EXPLOSION_HALF_HEIGHT - 8;
        double room = m.startY - TEXT_HALF_HEIGHT - this.ceilingY;
        m.travelX = 0;
        m.travelY = -Math.max(0, Math.min(DRIFT, room));
    }

    /**
     * Shows the points of the bonus ship. Call when it is destroyed.
     *
     * @param centerX Center X of the destroyed bonus ship.
     * @param centerY Center Y of the destroyed bonus ship.
     * @param points  Points earned.
     */
    public void spawnBonus(final int centerX, final int centerY,
            final int points) {
        spawnBonus(centerX, centerY, points, System.currentTimeMillis());
    }

    /**
     * Shows the points of the bonus ship at a given time (for tests).
     *
     * @param centerX Center X of the destroyed bonus ship.
     * @param centerY Center Y of the destroyed bonus ship.
     * @param points  Points earned.
     * @param now     Current time (ms).
     */
    public void spawnBonus(final int centerX, final int centerY,
            final int points, final long now) {
        Popup m = take(now);
        start(m, centerX, centerY, points, now);
        m.tier = 3;
        // Start to the right of the explosion; the bonus ship flies right,
        // so the meteor keeps going that way.
        m.explosionRight = centerX + BONUS_EXPLOSION_HALF_WIDTH
                + EXPLOSION_GAP;
        m.startX = centerX + BONUS_EXPLOSION_HALF_WIDTH + STREAK_START_OFFSET;
        double room = centerY - TEXT_HALF_HEIGHT - this.ceilingY;
        m.travelX = STREAK;
        m.travelY = -Math.max(0, Math.min(STREAK_RISE, room));
    }

    /** Removes every popup at once (for example, on level end). */
    public void clear() {
        for (Popup m : this.popups) {
            m.active = false;
        }
    }

    /**
     * @param now Current time (ms).
     * @return Number of popups still visible.
     */
    public int activeCount(final long now) {
        int count = 0;
        for (Popup m : this.popups) {
            if (m.active && now - m.startTime < LIFETIME) {
                count++;
            }
        }
        return count;
    }

    /**
     * Draws every popup on the back buffer.
     *
     * @param g        Graphics of the back buffer.
     * @param width    Screen width, to keep the numbers on screen.
     * @param baseFont Game font; the popup sizes are made from it once.
     */
    public void draw(final Graphics g, final int width, final Font baseFont) {
        draw(g, width, baseFont, System.currentTimeMillis());
    }

    /**
     * Draws every popup at a given time (for tests).
     *
     * @param g        Graphics of the back buffer.
     * @param width    Screen width, to keep the numbers on screen.
     * @param baseFont Game font; the popup sizes are made from it once.
     * @param now      Current time (ms).
     */
    public void draw(final Graphics g, final int width, final Font baseFont,
            final long now) {
        if (g == null || baseFont == null) {
            return;
        }
        prepareFonts(baseFont);
        for (Popup m : this.popups) {
            if (!m.active) {
                continue;
            }
            long t = now - m.startTime;
            if (t >= LIFETIME) {
                m.active = false; // finished: removed automatically
                continue;
            }
            drawPopup(g, m, Math.max(0, t), width, now);
        }
    }

    /** Makes the font sizes once, the first time a font is given. */
    private void prepareFonts(final Font font) {
        if (font == this.baseFont) {
            return;
        }
        this.baseFont = font;
        for (int i = 0; i < POP_SCALES.length; i++) {
            for (int level = 0; level < MAX_STREAK_LEVEL; level++) {
                float size = FONT_SIZE * (1 + STREAK_GROWTH * level)
                        * POP_SCALES[i];
                this.fonts[level][i] =
                        font.deriveFont(Math.min(MAX_FONT_SIZE, size));
            }
            this.bonusFonts[i] =
                    font.deriveFont(BONUS_FONT_SIZE * POP_SCALES[i]);
        }
    }

    /** Gets a free popup, or the oldest one when all are busy. */
    private Popup take(final long now) {
        Popup oldest = this.popups[0];
        for (Popup m : this.popups) {
            if (!m.active || now - m.startTime >= LIFETIME) {
                return m;
            }
            if (m.startTime < oldest.startTime) {
                oldest = m;
            }
        }
        return oldest;
    }

    /** Fills the values shared by every popup and updates the streak. */
    private void start(final Popup m, final int centerX, final int centerY,
            final int points, final long now) {
        this.streak = now - this.lastKillTime <= STREAK_WINDOW
                ? this.streak + 1 : 1;
        this.lastKillTime = now;

        m.active = true;
        m.id = this.spawnCount++;
        m.startTime = now;
        m.startX = centerX;
        m.startY = centerY;
        m.tier = points >= 30 ? 2 : points >= 20 ? 1 : 0;
        m.streak = this.streak;
        m.text = "+" + points;
        m.explosionRight = -1;
    }

    /** Draws one popup: tail, sparks, then the number on top. */
    private void drawPopup(final Graphics g, final Popup m, final long t,
            final int width, final long now) {
        int burstStart = FLY_TIME + HOLD_TIME;
        boolean bursting = t >= burstStart;
        float burn = bursting ? (float) (t - burstStart) / BURST_TIME : 0f;

        // Pop: starts big and snaps down to the normal size.
        int popStep = (int) Math.min(POP_SCALES.length - 1, t / POP_STEP);
        Font font = m.isBonus() ? this.bonusFonts[popStep]
                : this.fonts[Math.min(m.streak, MAX_STREAK_LEVEL) - 1][popStep];
        FontMetrics metrics = g.getFontMetrics(font);
        int halfW = metrics.stringWidth(m.text) / 2;
        int halfH = metrics.getAscent() / 2;

        // Position, kept on screen and under the HUD line.
        double headX = clamp(m.x(t), SIDE_MARGIN + halfW,
                width - SIDE_MARGIN - halfW);
        double headY = Math.max(m.y(t), this.ceilingY + halfH);
        if (m.isBonus()) {
            // The whole number stays right of the bonus explosion.
            headX = Math.max(headX, m.explosionRight + 2 + halfW);
        } else {
            // The bottom of the number (with outline) stays above the
            // explosion.
            headY = Math.min(headY, m.enemyY - EXPLOSION_HALF_HEIGHT
                    - EXPLOSION_GAP - 2 - (metrics.getAscent() - halfH));
        }

        if (m.isBonus()) {
            drawTail(g, m, t, headX, headY, halfW, halfH, now);
        }
        if (bursting) {
            drawSparks(g, m, headX, headY, burn);
        }

        // Number: dark outline, glow, then the number itself.
        // It blinks while bursting.
        boolean visible = !bursting || ((t - burstStart) / BLINK) % 2 == 0;
        if (visible) {
            int textX = snap(headX - halfW);
            int textY = snap(headY + halfH);
            g.setFont(font);
            g.setColor(OUTLINE);
            for (int dx = -2; dx <= 2; dx += 2) {
                for (int dy = -2; dy <= 2; dy += 2) {
                    if (dx != 0 || dy != 0) {
                        g.drawString(m.text, textX + dx, textY + dy);
                    }
                }
            }
            g.setColor(GLOW[m.tier]);
            g.drawString(m.text, textX - 1, textY);
            g.drawString(m.text, textX + 1, textY);
            g.drawString(m.text, textX, textY - 1);
            g.drawString(m.text, textX, textY + 1);
            g.setColor(TEXT[m.tier]);
            g.drawString(m.text, textX, textY);
        }
    }

    /**
     * Draws the burning tail of the bonus meteor from its old positions.
     * The tail dies down as the meteor slows and never covers the explosion.
     */
    private void drawTail(final Graphics g, final Popup m, final long t,
            final double headX, final double headY, final int halfW,
            final int halfH, final long now) {
        double flight = Math.min(1, (double) t / FLY_TIME);
        int shown = (int) Math.round(TRAIL_LENGTH * (1 - flight));
        if (shown <= 0) {
            return;
        }

        // Start the tail just behind the number, opposite to its travel.
        double dist = Math.sqrt(m.travelX * m.travelX
                + m.travelY * m.travelY);
        double ux = dist == 0 ? 0 : m.travelX / dist;
        double uy = dist == 0 ? -1 : m.travelY / dist;
        double backX = headX - ux * (Math.abs(ux) * halfW + 2);
        double backY = headY - uy * (Math.abs(uy) * halfH + 2);
        long flicker = now / 50;

        for (int k = shown; k >= 1; k--) {
            double pastT = t - k * TRAIL_STEP;
            if (pastT < 0) {
                continue; // the tail grows during the first moments
            }
            float along = (float) k / TRAIL_LENGTH; // 0 near number, 1 end
            double px = backX + (m.x(pastT) - m.x(t));
            double py = backY + (m.y(pastT) - m.y(t));

            // Small flame wobble, sideways to the travel direction.
            int wobble = (int) ((k * 7 + flicker * 3 + m.id) % 3) - 1;
            px += -uy * wobble;
            py += ux * wobble;

            int size = along < 0.34f ? 4 * PIXEL
                    : along < 0.5f ? 3 * PIXEL
                    : along < 0.67f ? 2 * PIXEL : PIXEL;
            if (px - size / 2.0 < m.explosionRight) {
                continue; // never draw the tail over the explosion
            }
            g.setColor(shade(TAIL_SHADES, along));
            g.fillRect(snap(px - size / 2.0), snap(py - size / 2.0),
                    size, size);
        }
    }

    /** Draws sparks flying out from the number while it bursts. */
    private void drawSparks(final Graphics g, final Popup m,
            final double headX, final double headY, final float burn) {
        int count = SPARKS[m.tier];
        double reach = m.isBonus() ? 26 * burn + 6 : 10 * burn + 4;
        Color color = shade(SPARK_SHADES, burn);
        g.setColor(color);
        for (int i = 0; i < count; i++) {
            if (burn > 0.6f && (i + m.id) % 2 == 0) {
                continue; // sparks die out one by one
            }
            double angle = 2 * Math.PI * i / count + m.id * 0.7;
            int x = snap(headX + Math.cos(angle) * reach);
            int y = snap(headY + Math.sin(angle) * reach);
            g.fillRect(x, y, PIXEL, PIXEL);
        }
    }

    /** @return Value kept between min and max. */
    private static double clamp(final double v, final double min,
            final double max) {
        return v < min ? min : v > max ? max : v;
    }

    /** @return Value snapped to the retro pixel grid. */
    private static int snap(final double v) {
        return ((int) Math.round(v) / PIXEL) * PIXEL;
    }

    /** @return Precomputed shade for an amount from 0 to 1. */
    private static Color shade(final Color[] shades, final float amount) {
        int i = Math.round(Math.max(0f, Math.min(1f, amount)) * (SHADES - 1));
        return shades[i];
    }

    /** @return Color between a and b (amount 0 = a, 1 = b). */
    private static Color mix(final Color a, final Color b, final float amount) {
        float p = Math.max(0f, Math.min(1f, amount));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * p),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * p),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * p));
    }
}