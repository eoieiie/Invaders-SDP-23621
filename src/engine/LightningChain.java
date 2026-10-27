package engine;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * AUTHORED BY: VFX TEAM (Effection)
 * Lightning Chain kill effect.
 * When an enemy dies, a forked bolt jumps from it to the nearest alive
 * enemies. Struck enemies spark and crackle for a moment.
 * A kill streak raises the tier: more jumps, thicker bolts, more sparks,
 * and the color changes from blue to violet to gold.
 * Look only: no damage, score or hitbox change.
 *
 * Team Effection - Visual Effects.
 */
public class LightningChain implements GameEvents.Listener {

    /** Time between kills that keeps the streak alive (ms). */
    private static final int STREAK_WINDOW = 1500;
    /** Kills needed for tier 2. */
    private static final int TIER_2_KILLS = 3;
    /** Kills needed for tier 3. */
    private static final int TIER_3_KILLS = 6;
    /** Time a strike stays on screen (ms). */
    private static final int STRIKE_TIME = 500;
    /** Time between two jumps of one chain (ms). */
    private static final int JUMP_DELAY = 60;
    /** Time the static shock stays on a struck enemy (ms). */
    private static final int SHOCK_TIME = 400;
    /** Max distance of one jump (px). */
    private static final int MAX_RANGE = 140;
    /** Number of points on one bolt path. */
    private static final int BOLT_POINTS = 8;
    /** Max side offset of a bolt point (px). */
    private static final float BOLT_JITTER = 18f;
    /** Spark life (frames). */
    private static final int SPARK_LIFE = 14;
    /** Max sparks on screen (performance limit). */
    private static final int MAX_SPARKS = 300;

    /** Outer color per tier. */
    private static final Color[] OUTER = {
        new Color(79, 184, 255),
        new Color(176, 107, 255),
        new Color(255, 200, 61)
    };
    /** Core color per tier. */
    private static final Color[] CORE = {
        new Color(230, 246, 255),
        new Color(243, 232, 255),
        new Color(255, 248, 224)
    };
    /** Number of jumps per tier. */
    private static final int[] JUMPS = {2, 3, 4};

    /** One chain of bolts made by one kill. */
    private static final class Strike {
        /** Chain points: dead enemy first, then struck enemies. */
        private final List<Point> chain;
        /** Tier when the strike was made (0, 1 or 2). */
        private final int tier;
        /** Time when the strike was made. */
        private final long born;

        Strike(final List<Point> chain, final int tier, final long born) {
            this.chain = chain;
            this.tier = tier;
            this.born = born;
        }
    }

    /** One impact spark. */
    private static final class Spark {
        /** Position x. */
        private float x;
        /** Position y. */
        private float y;
        /** Speed x per frame. */
        private float vx;
        /** Speed y per frame. */
        private float vy;
        /** Frames left. */
        private int life;
        /** Tier color index. */
        private int tier;
    }

    /** Live strikes. */
    private final List<Strike> strikes = new ArrayList<Strike>();
    /** Live sparks. */
    private final List<Spark> sparks = new ArrayList<Spark>();
    /** Random source. */
    private final Random random;
    /** Frame counter (for flicker). */
    private long frame;

    /** Kills in the current streak. */
    private int streak;
    /** Time of the last kill. */
    private long lastKillTime;

    /** Creates the effect. */
    public LightningChain() {
        this(new Random());
    }

    /**
     * Creates the effect with a given random source (for tests).
     *
     * @param random Random source.
     */
    public LightningChain(final Random random) {
        this.random = random == null ? new Random() : random;
    }

    /**
     * Call when an enemy is destroyed.
     *
     * @param x     Center x of the dead enemy.
     * @param y     Center y of the dead enemy.
     * @param alive Centers of the enemies that are still alive.
     */
    public void onKill(final int x, final int y, final List<Point> alive) {
        long now = System.currentTimeMillis();
        if (now - this.lastKillTime > STREAK_WINDOW) {
            this.streak = 0;
        }
        this.streak++;
        this.lastKillTime = now;

        int tier = getTier();
        List<Point> chain = new ArrayList<Point>();
        Point current = new Point(x, y);
        chain.add(current);

        List<Point> left = new ArrayList<Point>();
        if (alive != null) {
            for (Point p : alive) {
                if (p != null && !(p.x == x && p.y == y)) {
                    left.add(p);
                }
            }
        }
        for (int i = 0; i < JUMPS[tier] && !left.isEmpty(); i++) {
            Point next = nearest(current, left);
            if (next == null) {
                break;
            }
            left.remove(next);
            chain.add(next);
            current = next;
        }
        this.strikes.add(new Strike(chain, tier, now));
    }

    /** @return Current tier: 0 (blue), 1 (violet) or 2 (gold). */
    public int getTier() {
        if (this.streak >= TIER_3_KILLS) {
            return 2;
        }
        if (this.streak >= TIER_2_KILLS) {
            return 1;
        }
        return 0;
    }

    /** @return Kills in the current streak. */
    public int getStreak() {
        if (System.currentTimeMillis() - this.lastKillTime > STREAK_WINDOW) {
            return 0;
        }
        return this.streak;
    }

    /**
     * Resets the streak when the player is hit.
     *
     * @param type      Event type.
     * @param livesLeft Lives left after the hit.
     */
    @Override
    public void onEvent(final GameEvents.Type type, final int livesLeft) {
        if (type == GameEvents.Type.PLAYER_HIT) {
            this.streak = 0;
        }
    }

    /** Removes all strikes and sparks and resets the streak. */
    public void reset() {
        this.strikes.clear();
        this.sparks.clear();
        this.streak = 0;
    }

    /** Moves sparks and removes old strikes. Call once per frame. */
    public void update() {
        this.frame++;
        long now = System.currentTimeMillis();
        Iterator<Strike> it = this.strikes.iterator();
        while (it.hasNext()) {
            if (now - it.next().born >= STRIKE_TIME) {
                it.remove();
            }
        }
        Iterator<Spark> sp = this.sparks.iterator();
        while (sp.hasNext()) {
            Spark s = sp.next();
            s.life--;
            if (s.life <= 0) {
                sp.remove();
            } else {
                s.x += s.vx;
                s.y += s.vy;
            }
        }
    }

    /**
     * Draws all strikes, shocks and sparks.
     *
     * @param g Graphics to draw on (back buffer).
     */
    public void draw(final Graphics g) {
        if (!(g instanceof Graphics2D)
                || (this.strikes.isEmpty() && this.sparks.isEmpty())) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        Composite oldComp = g2.getComposite();
        Stroke oldStroke = g2.getStroke();
        Object oldAa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        long now = System.currentTimeMillis();
        for (Strike s : this.strikes) {
            drawStrike(g2, s, now - s.born);
        }
        drawSparks(g2);

        g2.setComposite(oldComp);
        g2.setStroke(oldStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                oldAa != null ? oldAa : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    /** Draws one strike at the given age. */
    private void drawStrike(final Graphics2D g2, final Strike s,
            final long age) {
        Color outer = OUTER[s.tier];
        Color core = CORE[s.tier];
        Point origin = s.chain.get(0);

        // Flash at the dead enemy (bigger on tier 3).
        if (age < 100) {
            float a = 0.3f * (1f - age / 100f);
            int r = s.tier == 2 ? 48 : 32;
            alpha(g2, a);
            g2.setColor(core);
            g2.fillOval(origin.x - r, origin.y - r, r * 2, r * 2);
        }

        int jumps = Math.min(s.chain.size() - 1, (int) (age / JUMP_DELAY) + 1);
        boolean flickerOff = this.frame % 3 == 2;

        for (int i = 0; i < jumps; i++) {
            Point a = s.chain.get(i);
            Point b = s.chain.get(i + 1);
            if (distance(a, b) > MAX_RANGE) {
                break;
            }
            if (!flickerOff && age < STRIKE_TIME - 80) {
                drawBolt(g2, a, b, outer, core, s.tier);
            }
            long hitAge = age - (long) i * JUMP_DELAY;
            if (hitAge >= 0 && hitAge < SHOCK_TIME) {
                drawShock(g2, b, outer);
                if (hitAge < 20 || this.frame % 4 == 0) {
                    addSparks(b, s.tier);
                }
            }
        }
    }

    /** Draws one forked bolt with glow, body and core. */
    private void drawBolt(final Graphics2D g2, final Point a, final Point b,
            final Color outer, final Color core, final int tier) {
        int[][] p = boltPath(a.x, a.y, b.x, b.y, BOLT_JITTER);

        alpha(g2, 0.35f);
        line(g2, p, outer, 5f + tier * 2f);
        alpha(g2, 1f);
        line(g2, p, outer, 2f + tier * 0.5f);
        line(g2, p, core, 1f);

        // Small side forks.
        alpha(g2, 0.7f);
        for (int i = 2; i < p.length - 2; i += 3) {
            if (this.random.nextBoolean()) {
                int fx = p[i][0] + this.random.nextInt(31) - 15;
                int fy = p[i][1] + this.random.nextInt(31) - 15;
                line(g2, boltPath(p[i][0], p[i][1], fx, fy, 6f), outer, 1f);
            }
        }
        alpha(g2, 1f);
    }

    /** Draws a short crackle over a struck enemy. */
    private void drawShock(final Graphics2D g2, final Point p,
            final Color color) {
        if (this.random.nextInt(10) < 4) {
            return;
        }
        int dy1 = this.random.nextInt(9) - 4;
        int dy2 = this.random.nextInt(9) - 4;
        alpha(g2, 0.9f);
        line(g2, boltPath(p.x - 10, p.y + dy1, p.x + 10, p.y + dy2, 5f),
                color, 1f);
        alpha(g2, 1f);
    }

    /** Adds impact sparks at a point. */
    private void addSparks(final Point p, final int tier) {
        int count = 3 + tier * 2;
        for (int i = 0; i < count && this.sparks.size() < MAX_SPARKS; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            float speed = 1f + this.random.nextFloat() * 2f;
            Spark s = new Spark();
            s.x = p.x;
            s.y = p.y;
            s.vx = (float) Math.cos(angle) * speed;
            s.vy = (float) Math.sin(angle) * speed;
            s.life = SPARK_LIFE;
            s.tier = tier;
            this.sparks.add(s);
        }
    }

    /** Draws all sparks. */
    private void drawSparks(final Graphics2D g2) {
        for (Spark s : this.sparks) {
            alpha(g2, s.life / (float) SPARK_LIFE);
            g2.setColor(CORE[s.tier]);
            g2.fillRect(Math.round(s.x) - 1, Math.round(s.y) - 1, 2, 2);
        }
        alpha(g2, 1f);
    }

    /** @return Jagged path from (x1,y1) to (x2,y2), bigger in the middle. */
    private int[][] boltPath(final int x1, final int y1, final int x2,
            final int y2, final float jitter) {
        int[][] p = new int[BOLT_POINTS + 1][2];
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.max(1.0, Math.hypot(dx, dy));
        for (int i = 0; i <= BOLT_POINTS; i++) {
            float k = i / (float) BOLT_POINTS;
            float off = 0f;
            if (i > 0 && i < BOLT_POINTS) {
                off = (this.random.nextFloat() - 0.5f) * jitter
                        * (float) Math.sin(k * Math.PI);
            }
            p[i][0] = Math.round(x1 + dx * k - dy / len * off);
            p[i][1] = Math.round(y1 + dy * k + dx / len * off);
        }
        return p;
    }

    /** Draws a polyline. */
    private static void line(final Graphics2D g2, final int[][] p,
            final Color color, final float width) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        for (int i = 0; i < p.length - 1; i++) {
            g2.drawLine(p[i][0], p[i][1], p[i + 1][0], p[i + 1][1]);
        }
    }

    /** Sets the draw opacity (0..1). */
    private static void alpha(final Graphics2D g2, final float a) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0f, Math.min(1f, a))));
    }

    /** @return Nearest point to from, or null. */
    private static Point nearest(final Point from, final List<Point> points) {
        Point best = null;
        double bestD = Double.MAX_VALUE;
        for (Point p : points) {
            double d = distance(from, p);
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    /** @return Distance between two points. */
    private static double distance(final Point a, final Point b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }
}
