package engine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import entity.Bullet;
import entity.DiamondDrop;
import entity.EnemyShip;
import entity.Entity;
import entity.Meteor;
import entity.Ship;

/**
 * AUTHORED BY: VFX TEAM (Effection)
 * Runs and draws the bonus meteor for one level: spawns it, moves it,handles hits, moves the diamonds it drops, and draws the burning rock,
 * its fire and smoke tail, the burst and the diamonds. GameScreen and DrawManager only call these methods.
 */
public class MeteorManager {

	private static final int TOP_MARGIN = 4; // px below the HUD line
	private static final int PLAYER_MARGIN = 30; // px above the ship
	private final MeteorSpawner spawner;
	private final int width;
	private final int height;
	private final int topY;
	private final Logger logger = Core.getLogger();
	private Meteor meteor; // null when none on screen
	private final List<DiamondDrop> drops = new ArrayList<DiamondDrop>();

	/** Creates the meteor manager for a level. */
	public MeteorManager(final int level, final int width, final int height,final int hudLineY) {
		this.spawner = MeteorSpawner.forLevel(level);
		this.width = width;
		this.height = height;
		this.topY = hudLineY + TOP_MARGIN;
	}

	/** Spawns a meteor on a random path above the player, if it is time. */
	public final void trySpawn(final Iterable<EnemyShip> enemies,final Ship ship) {
		int enemiesLeft = 0;
		for (EnemyShip enemy : enemies)
			if (!enemy.isDestroyed())
				enemiesLeft++;
		long now = System.currentTimeMillis();
		if (!this.spawner.shouldSpawn(now, enemiesLeft, this.meteor != null))
			return;

		double startAngle = this.spawner.rollStartAngle();
		double endAngle = this.spawner.rollEndAngle(startAngle);
		// Flatten the path just enough to fit between the HUD and the player.
		int bottomY = ship.getPositionY() - PLAYER_MARGIN - Meteor.SIZE;
		int room = bottomY - this.topY;
		int drop = Meteor.getTotalDrop(this.width, startAngle, endAngle);
		if (drop > room) {
			double scale = (double) room / drop;
			startAngle = Math.toDegrees(Math.atan(scale * Math.tan(Math.toRadians(startAngle))));
			endAngle = Math.toDegrees(Math.atan(scale * Math.tan(Math.toRadians(endAngle))));
			drop = Meteor.getTotalDrop(this.width, startAngle, endAngle);
		}
		int maxStartY = Math.max(this.topY, bottomY - drop);
		this.meteor = new Meteor(this.spawner.rollFromLeft(),
				this.spawner.rollHeight(this.topY, maxStartY), this.width,
				this.spawner.getCrossTime(), startAngle, endAngle,
				this.spawner.getDiamondValue(), new Random());
		this.spawner.markSpawned(now);
		this.logger.info("A meteor appears");
	}

	/**
	 * Moves the meteor and its diamonds. Call every frame.
	 *
	 * @return Diamonds caught by the ship this frame.
	 */
	public final int update(final Ship ship, final boolean canCatch) {
		if (this.meteor != null) {
			this.meteor.update();
			if (this.meteor.isOffScreen(this.width)
					|| this.meteor.isBurstFinished())
				this.meteor = null;
		}
		int caught = 0;
		Iterator<DiamondDrop> it = this.drops.iterator();
		while (it.hasNext()) {
			DiamondDrop drop = it.next();
			drop.update();
			if (!drop.isCaught() && canCatch && !ship.isDestroyed()
					&& overlaps(drop, ship)) {
				drop.catchIt();
				caught += drop.getValue();
				this.logger.info("Meteor diamond caught, +" + drop.getValue());
			} else if (drop.isFinished()
					|| (!drop.isCaught() && drop.getPositionY() > this.height)) {
				it.remove();
			}
		}
		return caught;
	}

	/** @return True if a player bullet hit the meteor (it bursts and drops a diamond). */
	public final boolean tryHit(final Bullet bullet) {
		if (this.meteor == null || this.meteor.isBursting()
				|| !overlaps(bullet, this.meteor))
			return false;
		this.meteor.burst();
		this.drops.add(new DiamondDrop(
				this.meteor.getPositionX() + Meteor.SIZE / 2,
				this.meteor.getPositionY() + Meteor.SIZE / 2,
				this.meteor.getDiamondValue(), this.meteor.getSpeedX(),
				this.width));
		this.logger.info("Meteor shot, dropped a diamond");
		return true;
	}

	/** @return Diamonds still falling at level clear (given like leftover coins). */
	public final int collectRemaining() {
		int total = 0;
		for (DiamondDrop drop : this.drops)
			if (!drop.isCaught())
				total += drop.getValue();
		this.drops.clear();
		return total;
	}

	/** Removes the meteor and diamonds, e.g. on game over. */
	public final void clear() {
		this.meteor = null;
		this.drops.clear();
	}

	/** Draws the meteor and the falling diamonds. */
	public final void draw(final DrawManager drawManager) {
		if (this.meteor != null)
			drawManager.drawMeteor(this.meteor);
		for (DiamondDrop drop : this.drops)
			drawManager.drawDiamondDrop(drop);
	}

	/** @return True when two entities overlap (same test as GameScreen). */
	private static boolean overlaps(final Entity a, final Entity b) {
		return Math.abs((a.getPositionX() + a.getWidth() / 2) - (b.getPositionX() + b.getWidth() / 2)) < a.getWidth() / 2 + b.getWidth() / 2
				&& Math.abs((a.getPositionY() + a.getHeight() / 2) - (b.getPositionY() + b.getHeight() / 2))< a.getHeight() / 2 + b.getHeight() / 2;
	}
    
	// Lumpy rock, 9x9 cells of 2 px: # rock, o crater, . empty.
	private static final String[] SHAPE = { "...###...", ".######..",".##o####.", "###oo####", "####o####", "##o######", ".#####o#.","..#####..", "...###..." };
	private static final int MID = 4; // center cell
	private static final Color ROCK = new Color(124, 100, 84);
	private static final Color SHADOW = new Color(78, 60, 52);
	private static final Color SMOKE = new Color(120, 110, 105);
	// Flame: white-hot core, yellow, orange, red edge.
	private static final Color[] FLAME = { new Color(255, 250, 220),new Color(255, 225, 90), new Color(255, 150, 40),new Color(220, 60, 30) };
	private static final java.util.Random FLICKER = new java.util.Random();

	/** Draws the bonus meteor, flying or bursting. */
	public static void drawMeteor(final Graphics g, final Meteor meteor) {
		if (meteor.isBursting())
			drawBurst(g, meteor);
		else
			drawFlying(g, meteor);
		setAlpha(g, 1f);
	}

	/** Sets the opacity (0 to 1) of everything drawn next. */
	private static void setAlpha(final Graphics g, final float alpha) {
		((Graphics2D) g).setComposite(AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER,
						Math.max(0f, Math.min(1f, alpha))));
	}

	/** Draws a flying meteor: a burning rock with a fire and smoke tail. */
	private static void drawFlying(final Graphics g, final Meteor meteor) {
		int size = meteor.getWidth();
		int r = size / 2;
		int cx = meteor.getPositionX() + r;
		int cy = meteor.getPositionY() + r;
		int dir = meteor.getDirection();
		float slope = meteor.getFallSlope();

		// Red heat glow around the rock.
		setAlpha(g, 0.25f);
		g.setColor(FLAME[3]);
		g.fillOval(cx - r - 4, cy - r - 4, size + 8, size + 8);

		// Tail: hot fire near the rock, cooling to red, then grey smoke.
		for (int i = 24; i >= 1; i--) {
			int back = r / 3 + i * 3 + FLICKER.nextInt(4);
			int tx = cx - dir * back;
			int ty = cy - Math.round(slope * back) + FLICKER.nextInt(5) - 2;
			int outer = Math.max(2, r - i * r / 26 + FLICKER.nextInt(3));
			setAlpha(g, i > 16 ? 0.35f - (i - 16) * 0.04f : 0.95f - i * 0.03f);
			for (int layer = 3; layer >= 0; layer--) {
				int fr = outer * (layer + 1) / 4;
				g.setColor(i > 16 ? SMOKE
						: FLAME[Math.min(3, layer + i / 6)]);
				g.fillOval(tx - fr, ty - fr, fr * 2, fr * 2);
			}
		}
		setAlpha(g, 1f);

		// Spinning pixel rock: the front and bottom burn bright, the back
		// stays dark with glowing cracks.
		float cos = (float) Math.cos(meteor.getRotation());
		float sin = (float) Math.sin(meteor.getRotation());
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++) {
				char cell = cellAt(row, col, cos, sin);
				if (cell == '.')
					continue;
				int heat = dir * (col - MID) + (row - MID)
						+ FLICKER.nextInt(2);
				if (heat >= 5)
					g.setColor(FLAME[0]);
				else if (heat >= 2)
					g.setColor(FLAME[1 + (5 - heat) / 2]);
				else if (cell == 'o')
					g.setColor(FLAME[3]);
				else
					g.setColor(heat <= -4 ? SHADOW : ROCK);
				g.fillRect(cx - r + col * 2, cy - r + row * 2, 2, 2);
			}

		// Flames wrapping over the back of the rock.
		for (int i = 0; i < 4; i++) {
			int fr = 2 + FLICKER.nextInt(3);
			setAlpha(g, 0.5f + FLICKER.nextFloat() * 0.4f);
			g.setColor(FLAME[1 + FLICKER.nextInt(2)]);
			g.fillOval(cx - dir * FLICKER.nextInt(r) - fr,cy - FLICKER.nextInt(r) - fr, fr * 2, fr * 2);
		}
	}

	/** @return Rock cell shown at a screen cell after rotation, '.' if empty. */
	private static char cellAt(final int row, final int col, final float cos,final float sin) {
		int dx = col - MID;
		int dy = row - MID;
		int srcCol = MID + Math.round(cos * dx + sin * dy);
		int srcRow = MID + Math.round(-sin * dx + cos * dy);
		if (srcRow < 0 || srcRow > 8 || srcCol < 0 || srcCol > 8)
			return '.';
		return SHAPE[srcRow].charAt(srcCol);
	}

	/** Draws a falling meteor diamond, or its "+N" popup once caught. */
	public static void drawDiamond(final Graphics g, final DiamondDrop drop,final Font font) {
		int x = drop.getPositionX();
		int y = drop.getPositionY();
		int size = drop.getWidth();
		int cx = x + size / 2;

		if (drop.isCaught()) {
			float progress = Math.min(1f, (float) drop.getCaughtElapsed() / DiamondDrop.POPUP_DURATION);
			String popup = "+" + drop.getValue();
			int textX = cx - g.getFontMetrics(font).stringWidth(popup) / 2;
			int textY = y - (int) (progress * 28);
			g.setFont(font);
			setAlpha(g, 1f - progress * progress);
			g.setColor(Color.BLACK);
			g.drawString(popup, textX + 1, textY + 1);
			g.setColor(Color.CYAN);
			g.drawString(popup, textX, textY);
			setAlpha(g, 1f);
			return;
		}

		// Soft pulsing glow so it stands out while it falls.
		float pulse = 0.5f + 0.5f * (float) Math.sin(drop.getAge() / 120.0);
		setAlpha(g, 0.15f + 0.15f * pulse);
		g.setColor(Color.CYAN);
		g.fillOval(x - 4, y - 4, size + 8, size + 8);
		setAlpha(g, 1f);

		// Gem: darker lower half, bright upper half, white edge and sparkle.
		int[] gemX = { cx, x + size, cx, x };
		int[] gemY = { y, y + size / 2, y + size, y + size / 2 };
		g.setColor(new Color(0, 170, 200));
		g.fillPolygon(gemX, gemY, 4);
		g.setColor(Color.CYAN);
		g.fillPolygon(new int[] { cx, x + size, x },new int[] { y, y + size / 2, y + size / 2 }, 3);
		g.setColor(Color.WHITE);
		g.drawPolygon(gemX, gemY, 4);
		if (pulse > 0.6f)
			g.fillRect(cx - 3, y + 3, 2, 2);
	}

	/** Draws a shot meteor: flash, shockwave, rock pieces and sparks. */
	private static void drawBurst(final Graphics g, final Meteor meteor) {
		long elapsed = meteor.getBurstElapsed();
		float progress = Math.min(1f, (float) elapsed / Meteor.BURST_DURATION);
		int cx = meteor.getPositionX() + meteor.getWidth() / 2;
		int cy = meteor.getPositionY() + meteor.getHeight() / 2;
		int radius = 8 + (int) (progress * 40);

		// Flash, then a shockwave ring growing outward.
		setAlpha(g, 1f - progress);
		if (progress < 0.15f) {
			g.setColor(FLAME[0]);
			g.fillOval(cx - radius, cy - radius,radius * 2, radius * 2);
		}
		g.setColor(FLAME[2]);
		g.drawOval(cx - radius, cy - radius,radius * 2, radius * 2);

		// Even sparks: rock pieces that fall. Odd sparks: hot streaks.
		for (int i = 0; i < meteor.getSparkCount(); i++) {
			float sx = meteor.getSparkSpeedX(i) * elapsed;
			float sy = meteor.getSparkSpeedY(i) * elapsed;
			if (i % 2 == 0) {
				g.setColor(i % 4 == 0 ? ROCK : SHADOW);
				g.fillRect(cx + Math.round(sx * 0.45f) - 2,cy + Math.round(sy * 0.45f + 0.00006f * elapsed * elapsed) - 2, 4, 4);
			} else {
				g.setColor(FLAME[Math.min(3, 1 + (int) (progress * 3))]);
				g.drawLine(cx + Math.round(sx * 0.7f), cy + Math.round(sy * 0.7f),cx + Math.round(sx), cy + Math.round(sy));
				g.fillRect(cx + Math.round(sx) - 1, cy + Math.round(sy) - 1, 2, 2);
			}
		}
	}
}