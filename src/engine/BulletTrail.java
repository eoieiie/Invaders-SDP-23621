package engine;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Comet trail behind a player bullet: soft glow plus fading orange
 * blocks below it. Look only.
 * AUTHORED BY: VFX TEAM (Effection)
 */
public final class BulletTrail {

    /** Trail colours, hot to cool. */
    private static final Color[] COLORS = {
			new Color(255, 240, 150), new Color(255, 205, 60),
			new Color(255, 165, 30), new Color(255, 135, 20),
			new Color(250, 105, 10), new Color(230, 75, 10),
			new Color(190, 45, 5) };
    /** Height of one trail block in pixels. */
    private static final int SEGMENT = 4;
	/** Glow colour around the bullet. */
	private static final Color GLOW = new Color(255, 150, 40, 90);
	/** Ember colour. */
	private static final Color EMBER = new Color(255, 120, 20);
	/** Max embers per frame. */
	private static final int EMBERS = 3;

    /** Not instantiable. */
    private BulletTrail() {
    }

    /**
     * Draws the trail. Call before the bullet so the bullet stays on top.
     *
     * @param g      Graphics to draw on.
     * @param x      Bullet x.
     * @param y      Bullet y.
     * @param width  Bullet width.
     * @param height Bullet height.
     */
	public static void draw(final Graphics g, final int x, final int y,
                            final int width, final int height) {
		if (g == null)
			return;
		int n = COLORS.length;
		int centerX = x + width / 2;

		// Glow around the bullet.
		g.setColor(GLOW);
		g.fillRect(x - 3, y - 3, width + 6, height + 6);

		// Flame: wide orange outer layer + thin bright core.
		for (int i = 0; i < n; i++) {
			float k = i / (float) n;
			int ty = y + height - 2 + i * SEGMENT;
			int flicker = (int) (Math.random() * 3) - 1;
			int outerW = Math.max(2, (int) ((width + 4) * (1f - k)) + flicker);
			int innerW = Math.max(1, outerW / 2);
			Color outer = COLORS[Math.min(i + 1, n - 1)];
			Color inner = COLORS[i];
			g.setColor(alpha(outer, (int) (170 * (1f - k))));
			g.fillRect(centerX - outerW / 2 + flicker, ty, outerW, SEGMENT);
			g.setColor(alpha(inner, (int) (230 * (1f - k))));
			g.fillRect(centerX - innerW / 2, ty, innerW, SEGMENT);
		}

		// Embers: small sparks that drift off the trail.
		int trailLen = n * SEGMENT;
		for (int e = 0; e < EMBERS; e++) {
			if (Math.random() < 0.5)
				continue;
			int spread = width + 6;
			int ex = centerX - spread / 2 + (int) (Math.random() * spread);
			int dist = (int) (Math.random() * trailLen);
			int ey = y + height + dist;
			int a = (int) (255 * (1f - dist / (float) trailLen));
			g.setColor(alpha(EMBER, a));
			g.fillRect(ex, ey, 2, 2);
		}
	}

	/** Returns c with alpha a. */
    private static Color alpha(final Color c, final int a) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(),
				Math.max(0, Math.min(255, a)));
	}
}