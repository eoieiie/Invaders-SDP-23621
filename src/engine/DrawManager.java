package engine;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import entity.EnemyShip;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import screen.MenuItem;
import screen.Screen;
import screen.Starfield;
import entity.Boss;
import entity.Coin;
import entity.DiamondDrop;
import entity.Entity;
import entity.Meteor;
import entity.Particle;
import entity.Ship;
import item.ItemAPI;
import item.ItemSystem;

/**
 * Manages screen drawing.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class DrawManager {

	/** Singleton instance of the class. */
	private static DrawManager instance;
	/** Current frame. */
	private static Frame frame;
	/** FileManager instance. */
	private static FileManager fileManager;
	/** Application logger. */
	private static Logger logger;
	/** Graphics context. */
	private static Graphics graphics;
	/** Buffer Graphics. */
	private static Graphics backBufferGraphics;
	/** Buffer image. */
	private static BufferedImage backBuffer;
	/** Top of the item panel, right below the HUD line (Team CS). */
	private static final int ITEM_PANEL_TOP = 43;
	private static final int ITEM_SLOT_HEIGHT = 16;
	/** Item panel font size: small and fixed so the panel fits one line. */
	private static final float ITEM_FONT_SIZE = 9f;
	/** Item notice baseline, measured up from the bottom of the screen. */
	private static final int ITEM_NOTICE_BOTTOM_OFFSET = 60;
	/** The shield bubble blinks during this many last milliseconds. */
	private static final long SHIELD_BLINK_MILLIS = 2000;
	/** Current world offset X (screen shake). */
	private static int worldOffsetX;
	/** Current world offset Y (screen shake). */
	private static int worldOffsetY;
	/** Normal sized font. */
	private static Font fontRegular;
	/** Normal sized font properties. */
	private static FontMetrics fontRegularMetrics;
	/** Big sized font. */
	private static Font fontBig;
	/** Big sized font properties. */
	private static FontMetrics fontBigMetrics;

	/** Font used for the highlighted menu item. */
	private static Font fontSelected;
	/** Highlighted menu item font properties. */
	private static FontMetrics fontSelectedMetrics;

	/** Sprite types mapped to their images. */
	private static Map<SpriteType, boolean[][]> spriteMap;

	/** Sprite types. */
	public static enum SpriteType {
		/** Player ship. */
		Ship,
		/** Destroyed player ship. */
		ShipDestroyed,
		/** Player bullet. */
		Bullet,
		/** Enemy bullet. */
		EnemyBullet,
		/** First enemy ship - first form. */
		EnemyShipA1,
		/** First enemy ship - second form. */
		EnemyShipA2,
		/** Second enemy ship - first form. */
		EnemyShipB1,
		/** Second enemy ship - second form. */
		EnemyShipB2,
		/** Third enemy ship - first form. */
		EnemyShipC1,
		/** Third enemy ship - second form. */
		EnemyShipC2,
		/** Bonus ship. */
		EnemyShipSpecial,
		/** Destroyed enemy ship. */
		Explosion,
		/** Player ship with a high movement speed (Fast Move). */
		ShipFastMove,
		/** Player ship that shoots from two barrels (Two-Way Shot). */
		ShipTwoWay,
		/** Fast Attack player ship. */
		ShipFastAttack,
		/** Big Bullet player ship. */
		ShipBigBullet,

		/** First Flight achievement icon. */
		FirstFlight,
		/** Fleet Master achievement icon. */
		FleetMaster,
		/** Weakestship sprite. */
		Weakestship,
		/** Infinity Void achievement icon. */
		InfinityVoid,
		/** First Boss Kill achievement icon. */
		BossKill,
		/** Fast Attack skin A1 (Arrow). */
		ShipSkinA1,
		/** Fast Attack skin A2 (Twin Fang). */
		ShipSkinA2,
		/** Fast Attack skin A3 (Stealth). */
		ShipSkinA3,
		/** Big Bullet skin B1 (Cannon). */
		ShipSkinB1,
		/** Big Bullet skin B2 (Mortar). */
		ShipSkinB2,
		/** Big Bullet skin B3 (Fortress). */
		ShipSkinB3,
		/** Fast Move skin C1 (Jet). */
		ShipSkinC1,
		/** Fast Move skin C2 (Triple Thruster). */
		ShipSkinC2,
		/** Fast Move skin C3 (Stealth). */
		ShipSkinC3,
		/** Two-Way Shot skin D1 (Long Barrels). */
		ShipSkinD1,
		/** Two-Way Shot skin D2 (Twin Spike). */
		ShipSkinD2,
		/** Two-Way Shot skin D3 (Hollow). */
		ShipSkinD3,

		/** Achievement trophy icon. */
		Trophy,
		/** Enemies killed achievement icon. */
		Hunter,
		/** Accuracy target achievement icon. */
		Accuracytarget,
        /** Currency collected in one run achievement icon**/
        Looter,
        /** Item used achievement icon. */
		ItemCol,
		/** Bouncing boss animation frames. */
		MidBoss1,
		MidBoss2,
		FinalBoss1,
		FinalBoss2
	};

	/**
	 * Private constructor.
	 */
	private DrawManager() {
		fileManager = Core.getFileManager();
		logger = Core.getLogger();
		logger.info("Started loading resources.");

		try {
			spriteMap = new LinkedHashMap<SpriteType, boolean[][]>();

			// Order must match res/graphics exactly. Keep the same groups in
			// both files, and add a new sprite to the end of its team's group.

			// Base game
			spriteMap.put(SpriteType.Ship, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipDestroyed, new boolean[13][8]);
			spriteMap.put(SpriteType.Bullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyBullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyShipA1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipA2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipSpecial, new boolean[16][7]);
			spriteMap.put(SpriteType.Explosion, new boolean[13][7]);

			// Player ships (A1)
			spriteMap.put(SpriteType.ShipFastMove, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipTwoWay, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipFastAttack, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipBigBullet, new boolean[13][8]);

			// Achievements (Mantis)
			spriteMap.put(SpriteType.FirstFlight, new boolean[11][8]);
			spriteMap.put(SpriteType.FleetMaster, new boolean[23][23]);
			spriteMap.put(SpriteType.Weakestship, new boolean[11][11]);
			spriteMap.put(SpriteType.InfinityVoid, new boolean[11][11]);
			spriteMap.put(SpriteType.BossKill, new boolean[11][11]);
			spriteMap.put(SpriteType.Hunter, new boolean[13][12]);
			spriteMap.put(SpriteType.Trophy, new boolean[11][8]);
			spriteMap.put(SpriteType.Accuracytarget, new boolean[11][11]);
            spriteMap.put(SpriteType.Looter, new boolean[11][11]);
			spriteMap.put(SpriteType.ItemCol, new boolean[12][12]);
			spriteMap.put(SpriteType.MidBoss1, new boolean[16][10]);
			spriteMap.put(SpriteType.MidBoss2, new boolean[16][10]);
			spriteMap.put(SpriteType.FinalBoss1, new boolean[24][16]);
			spriteMap.put(SpriteType.FinalBoss2, new boolean[24][16]);

			// Player ship skins (A1)
			spriteMap.put(SpriteType.ShipSkinA1, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinA2, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinA3, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinB1, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinB2, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinB3, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinC1, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinC2, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinC3, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinD1, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinD2, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipSkinD3, new boolean[13][8]);

			fileManager.loadSprite(spriteMap);
			logger.info("Finished loading the sprites.");

			// Font loading.
			fontRegular = fileManager.loadFont(14f);
			fontBig = fileManager.loadFont(24f);
			logger.info("Finished loading the fonts.");

		} catch (IOException e) {
			logger.warning("Loading failed: " + e.getMessage());
			fontRegular = new Font(Font.MONOSPACED, Font.PLAIN, 14);
			fontBig = new Font(Font.MONOSPACED, Font.PLAIN, 24);
		} catch (FontFormatException e) {
			logger.warning("Font formating failed.");
			fontRegular = new Font(Font.MONOSPACED, Font.PLAIN, 14);
			fontBig = new Font(Font.MONOSPACED, Font.PLAIN, 24);
		}

		// Derived outside the try so it is set on the fallback path too.
		// While it was inside, a missing font file left it null and the game
		// died on the first frame.
		fontSelected = fontRegular.deriveFont(17f);
	}

	/**
	 * Returns shared instance of DrawManager.
	 *
	 * @return Shared instance of DrawManager.
	 */
	protected static DrawManager getInstance() {
		if (instance == null)
			instance = new DrawManager();
		return instance;
	}

	/**
	 * Sets the frame to draw the image on.
	 *
	 * @param currentFrame
	 *            Frame to draw on.
	 */
	public void setFrame(final Frame currentFrame) {
		frame = currentFrame;
	}

	/**
	 * First part of the drawing process. Initialices buffers, draws the
	 * background and prepares the images.
	 *
	 * @param screen
	 *            Screen to draw in.
	 */
	public void initDrawing(final Screen screen) {

		if (fontRegular == null) {
			fontRegular = new Font(Font.MONOSPACED, Font.PLAIN, 14);
		}
		if (fontBig == null) {
			fontBig = new Font(Font.MONOSPACED, Font.PLAIN, 24);
		}
		if (fontSelected == null) {
			fontSelected = fontRegular;
		}
		backBuffer = new BufferedImage(screen.getWidth(), screen.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		graphics = frame.getGraphics();
		backBufferGraphics = backBuffer.getGraphics();
		worldOffsetX = 0;
		worldOffsetY = 0;

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics
				.fillRect(0, 0, screen.getWidth(), screen.getHeight());

		fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
		fontBigMetrics = backBufferGraphics.getFontMetrics(fontBig);
		fontSelectedMetrics = backBufferGraphics.getFontMetrics(fontSelected);

		// drawBorders(screen);
		// drawGrid(screen);
	}

	/**
	 * Draws the completed drawing on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void completeDrawing(final Screen screen) {
		setWorldOffset(0, 0);
		graphics.drawImage(backBuffer, frame.getInsets().left,
				frame.getInsets().top, frame);
	}

	/**
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * Moves everything drawn after this call by the given offset.
	 * Used for screen shake. Call setWorldOffset(0, 0) before the HUD.
	 *
	 * @param offsetX
	 *            X offset in pixels.
	 * @param offsetY
	 *            Y offset in pixels.
	 */
	public void setWorldOffset(final int offsetX, final int offsetY) {
		backBufferGraphics.translate(offsetX - worldOffsetX,
				offsetY - worldOffsetY);
		worldOffsetX = offsetX;
		worldOffsetY = offsetY;
	}

	/**
	 * Draws an entity, using the apropiate image.
	 *
	 * @param entity
	 *            Entity to be drawn.
	 * @param positionX
	 *            Coordinates for the left side of the image.
	 * @param positionY
	 *            Coordinates for the upper side of the image.
	 */
	public void drawEntity(final Entity entity, final int positionX,
						   final int positionY) {
		// Enemy ships can be see-through while flickering / fading out.
		float alpha = 1f;
		if (entity instanceof EnemyShip)
			alpha = ((EnemyShip) entity).getAlpha();
		if (alpha <= 0f)
			return;

		Graphics2D g2d = (Graphics2D) backBufferGraphics;
		g2d.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, alpha));

		// If the entity is a Bullet fired by the PLAYER (speed < 0), stretch its sprite to match its actual dimensions.
		if (entity instanceof entity.Bullet && ((entity.Bullet) entity).getSpeed() < 0) {
			boolean[][] image = spriteMap.get(entity.getSpriteType());
			if (image != null && image.length > 0) {
				// Calculate how much to stretch each pixel of the sprite
				double scaleX = (double) entity.getWidth() / image.length;
				double scaleY = (double) entity.getHeight() / image[0].length;

				backBufferGraphics.setColor(entity.getColor());
				for (int i = 0; i < image.length; i++) {
					for (int j = 0; j < image[i].length; j++) {
						if (image[i][j]) {
							backBufferGraphics.fillRect(
									positionX + (int)(i * scaleX),
									positionY + (int)(j * scaleY),
									(int)Math.ceil(scaleX),
									(int)Math.ceil(scaleY));
						}
					}
				}
			}
		} else {
			// For all other entities (ships, enemies, and ENEMY bullets), use the standard 2x pixel drawing
			drawSprite(entity.getSpriteType(), positionX, positionY,
					entity.getColor());
		}

		g2d.setComposite(AlphaComposite.SrcOver); // back to normal
	}

	/**
	 * Draws a sprite using the game's standard two-pixel scale.
	 *
	 * @param spriteType Sprite to draw.
	 * @param positionX Coordinates for the left side of the image.
	 * @param positionY Coordinates for the upper side of the image.
	 * @param color Color used for filled pixels.
	 */
	public void drawSprite(final SpriteType spriteType, final int positionX,
						   final int positionY, final Color color) {
		boolean[][] image = spriteMap.get(spriteType);

		backBufferGraphics.setColor(color);

		for (int i = 0; i < image.length; i++)
			for (int j = 0; j < image[i].length; j++)
				if (image[i][j])
					backBufferGraphics.drawRect(
							positionX + i * 2,
							positionY + j * 2,
							1, 1);
	}
	/**
	 * Draws regular text at an exact position, left aligned.
	 *
	 * @param string    Text to draw.
	 * @param positionX Horizontal position of the left edge.
	 * @param positionY Vertical position of the baseline.
	 * @param color     Colour of the text.
	 */
	public void drawRegularString(final String string, final int positionX,
								  final int positionY, final Color color) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(color);
		backBufferGraphics.drawString(string, positionX, positionY);
	}

	/**
	 * Draws an empty rectangle outline.
	 *
	 * @param positionX Horizontal position of the left edge.
	 * @param positionY Vertical position of the top edge.
	 * @param width     Width of the box.
	 * @param height    Height of the box.
	 * @param color     Colour of the outline.
	 */
	public void drawBox(final int positionX, final int positionY,
						final int width, final int height, final Color color) {
		backBufferGraphics.setColor(color);
		backBufferGraphics.drawRect(positionX, positionY, width, height);
	}

	/**
	 * Draws a dropped coin as a filled circle (GoG - Currency System).
	 * Coins have no entry in the shared sprite file, so they are drawn
	 * here instead of through drawEntity().
	 *
	 * @param coin
	 *            Coin to be drawn.
	 * @param positionX
	 *            Coordinates for the left side of the coin.
	 * @param positionY
	 *            Coordinates for the upper side of the coin.
	 */
	public void drawCoin(final Coin coin, final int positionX,
						 final int positionY) {
		backBufferGraphics.setColor(coin.getColor());
		backBufferGraphics.fillOval(positionX, positionY, coin.getWidth(),
				coin.getHeight());
	}

	/** Draws the bonus meteor. AUTHORED BY: VFX TEAM (Effection) */
	public void drawMeteor(final Meteor meteor) {
		MeteorManager.drawMeteor(backBufferGraphics, meteor);
	}

	/** Draws a meteor diamond. AUTHORED BY: VFX TEAM (Effection) */
	public void drawDiamondDrop(final DiamondDrop drop) {
		MeteorManager.drawDiamond(backBufferGraphics, drop, fontRegular);
	}

	/**
	 * Draws the item drops on the field (Team CS - Item System). Drops have
	 * no entry in the shared sprite file yet, so the item system draws
	 * placeholder shapes on the back buffer.
	 *
	 * @param items
	 *            Item system of the current run.
	 */
	public void drawItemDrops(final ItemSystem items) {
		backBufferGraphics.setFont(fontRegular);
		items.drawDrops(backBufferGraphics);
	}

	/**
	 * Draws the shield item around the player ship while it is active
	 * (Team CS - Item System): a cyan bubble that blinks during its last
	 * seconds.
	 *
	 * @param ship
	 *            Player ship.
	 * @param items
	 *            Item system of the current run.
	 */
	public void drawItemShield(final Entity ship, final ItemSystem items) {
		ItemAPI.EffectView shield = items.shield();
		if (shield == null)
			return;
		long remaining = shield.remainingMillis == null ? Long.MAX_VALUE
				: shield.remainingMillis;
		if (remaining < SHIELD_BLINK_MILLIS
				&& (System.currentTimeMillis() / 150) % 2 == 0)
			return;
		int padding = 6;
		int x = ship.getPositionX() - padding;
		int y = ship.getPositionY() - padding;
		int width = ship.getWidth() + padding * 2;
		int height = ship.getHeight() + padding * 2;
		Color color = ItemSystem.colorOf(shield.item.effectKind);
		backBufferGraphics.setColor(new Color(color.getRed(),
				color.getGreen(), color.getBlue(), 60));
		backBufferGraphics.fillOval(x, y, width, height);
		backBufferGraphics.setColor(color);
		backBufferGraphics.drawOval(x, y, width, height);
	}

	/**
	 * Draws the item panel right below the HUD line (Team CS - Item System):
	 * inventory slots on the left, each with the number key that uses it,
	 * and running effects on the right with stacks or seconds left. The
	 * latest item notice is shown above the player ship.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param items
	 *            Item system of the current run.
	 */
	public void drawItemHud(final Screen screen, final ItemSystem items) {
		ItemAPI.View view = items.api().getView();
		// Small fixed font with two-letter labels, so four slots and every
		// effect fit one line even with the wide pixel font.
		Font font = fontRegular.deriveFont(ITEM_FONT_SIZE);
		FontMetrics metrics = backBufferGraphics.getFontMetrics(font);
		backBufferGraphics.setFont(font);
		int baseline = ITEM_PANEL_TOP + ITEM_SLOT_HEIGHT - 4;
		int boxWidth = metrics.stringWidth("WW") + 8;

		// Slots: "1 [SH]" ... an empty slot is a dark box.
		int x = 6;
		for (int i = 0; i < view.slots.size(); i++) {
			ItemAPI.ItemInfo item = view.slots.get(i);
			backBufferGraphics.setColor(Color.GRAY);
			backBufferGraphics.drawString(Integer.toString(i + 1), x, baseline);
			x += metrics.stringWidth("0") + 3;
			backBufferGraphics.setColor(item == null ? Color.DARK_GRAY
					: ItemSystem.colorOf(item.effectKind));
			backBufferGraphics.drawRect(x, ITEM_PANEL_TOP, boxWidth,
					ITEM_SLOT_HEIGHT);
			if (item != null) {
				String label = shortItemName(item.effectKind);
				backBufferGraphics.drawString(label, x + (boxWidth
						- metrics.stringWidth(label)) / 2, baseline);
			}
			x += boxWidth + 6;
		}

		// Effects, right-aligned and compact: "RFx2 SH8s". The equipped item is
		// not listed here; the countdown hint names it.
		int right = screen.getWidth() - 6;
		for (int i = view.effects.size() - 1; i >= 0; i--) {
			ItemAPI.EffectView effect = view.effects.get(i);
			if (effect.item.activationMode == ItemAPI.ActivationMode.EQUIPPED)
				continue;
			String label = shortItemName(effect.item.effectKind);
			if (effect.stacks != null)
				label += "x" + effect.stacks;
			else if (effect.remainingMillis != null)
				label += (effect.remainingMillis + 999) / 1000 + "s";
			right -= metrics.stringWidth(label);
			backBufferGraphics.setColor(ItemSystem
					.colorOf(effect.item.effectKind));
			backBufferGraphics.drawString(label, right, baseline);
			right -= 8;
		}

		String notice = items.getNotice();
		if (notice != null) {
			backBufferGraphics.setColor(Color.YELLOW);
			drawCenteredRegularString(screen, notice, screen.getHeight()
					- ITEM_NOTICE_BOTTOM_OFFSET);
		}
	}

	/**
	 * Explains the items while the level countdown runs and names the
	 * equipped item (Team CS - Item System).
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param items
	 *            Item system of the current run.
	 */
	public void drawItemHint(final Screen screen, final ItemSystem items) {
		int slots = items.api().getView().slots.size();
		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, "Catch item drops with your ship",
				screen.getHeight() * 2 / 3);
		drawCenteredRegularString(screen, "Keys 1-" + slots
				+ " use stored items", screen.getHeight() * 2 / 3
				+ fontRegularMetrics.getHeight());
		ItemAPI.ItemInfo equipped = items.equippedThisRun();
		if (equipped != null)
			drawCenteredRegularString(screen, "Equipped: " + equipped.displayName,
					screen.getHeight() * 2 / 3 + fontRegularMetrics.getHeight() * 2);
	}

	/**
	 * Short label for a running item effect in the HUD.
	 *
	 * @param kind
	 *            Effect kind.
	 * @return Two-letter label.
	 */
	private static String shortItemName(final ItemAPI.EffectKind kind) {
		switch (kind) {
			case RAPID_FIRE:
				return "RF";
			case BULLET_SPEED:
				return "BS";
			case SHIELD:
				return "SH";
			case FREEZE:
				return "FZ";
			case BOOST:
				return "BO";
			case SCORE_BOOST:
				return "SC";
			default:
				return kind.name();
		}
	}

	/**
	 * Draws a coin balance as a small coin icon followed by the amount,
	 * centered along the top bar of the screen (GoG - Currency System).
	 * Used by the in-game HUD and the shop.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param coins
	 *            Coin balance to display.
	 */
	public void drawCoinBalance(final Screen screen, final int coins) {
		drawCoinBalance(screen, coins, 25);
	}

	/**
	 * Draws a coin balance as a small coin icon followed by the amount,
	 * centered horizontally at the given baseline (GoG - Currency System).
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param coins
	 *            Coin balance to display.
	 * @param positionY
	 *            Baseline Y coordinate of the text.
	 */
	public void drawCoinBalance(final Screen screen, final int coins,
								final int positionY) {
		final int iconSize = 14;
		final int iconTextGap = 6;

		backBufferGraphics.setFont(fontRegular);
		String balanceString = Integer.toString(coins);
		int totalWidth = iconSize + iconTextGap
				+ fontRegularMetrics.stringWidth(balanceString);
		int startX = (screen.getWidth() - totalWidth) / 2;

		backBufferGraphics.setColor(Color.YELLOW);
		backBufferGraphics.fillOval(startX, positionY - iconSize + 1,
				iconSize, iconSize);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(balanceString, startX + iconSize
				+ iconTextGap, positionY);
	}

	/**
	 * Draws a diamond balance as a small diamond icon followed by the
	 * amount, centered horizontally at the given baseline, so it can be
	 * stacked with the coin balance (GoG - Currency System).
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param diamonds
	 *            Diamond balance to display.
	 * @param positionY
	 *            Baseline Y coordinate of the text.
	 */
	public void drawDiamondBalance(final Screen screen, final int diamonds,
								   final int positionY) {
		final int iconSize = 14;
		final int iconTextGap = 6;

		backBufferGraphics.setFont(fontRegular);
		String balanceString = Integer.toString(diamonds);
		int totalWidth = iconSize + iconTextGap
				+ fontRegularMetrics.stringWidth(balanceString);
		int startX = (screen.getWidth() - totalWidth) / 2;
		int iconTop = positionY - iconSize + 1;

		int[] xPoints = { startX + iconSize / 2, startX + iconSize,
				startX + iconSize / 2, startX };
		int[] yPoints = { iconTop, iconTop + iconSize / 2,
				iconTop + iconSize, iconTop + iconSize / 2 };

		backBufferGraphics.setColor(Color.CYAN);
		backBufferGraphics.fillPolygon(xPoints, yPoints, 4);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(balanceString, startX + iconSize
				+ iconTextGap, positionY);
	}

/** Width of the boss health bar, as a fraction of the screen width. */
	private static final double BOSS_BAR_WIDTH_RATIO = 0.5;
	/** Height, in pixels, of the boss health bar. */
	private static final int BOSS_BAR_HEIGHT = 8;
	/** Gap between the "BOSS" label baseline and the top of the bar. */
	private static final int BOSS_BAR_GAP = 4;
	
	/** Draws the current boss's remaining health as a labelled bar below the item HUD, so the player can track a Stage 5 (MidBoss) or Stage 10 (FinalBoss) encounter at a glance (Requirement 8 - Gameplay HUD). Boss may be null when the stage has no boss or it has already been defeated. */
	public void drawBossHealthBar(final Screen screen, final Boss boss, final int positionY) {
		if (boss == null || boss.isDestroyed())
			return;
		
		int maxHp = Math.max(1, boss.getMaxHp());
		int hp = Math.max(0, Math.min(boss.getHp(), maxHp));
		
		int barWidth = (int) (screen.getWidth() * BOSS_BAR_WIDTH_RATIO);
		int barX = (screen.getWidth() - barWidth) / 2;
		int barY = positionY + BOSS_BAR_GAP;
		int filledWidth = barWidth * hp / maxHp;
		
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, "BOSS", positionY);
		
		backBufferGraphics.setColor(Color.RED);
		backBufferGraphics.drawRect(barX, barY, barWidth, BOSS_BAR_HEIGHT);
		if (filledWidth > 0)
			backBufferGraphics.fillRect(barX, barY, filledWidth, BOSS_BAR_HEIGHT);
	}
	
	/**
	 * Draws a sparkle, see-through according to its age.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * @param particle
	 *            Particle to draw.
	 */
	public void drawParticle(final Particle particle) {
		java.awt.Graphics2D g2d = (java.awt.Graphics2D) backBufferGraphics;
		g2d.setComposite(java.awt.AlphaComposite.getInstance(
				java.awt.AlphaComposite.SRC_OVER, particle.getAlpha()));
		g2d.setColor(particle.getColor());
		g2d.fillRect(particle.getX(), particle.getY(), particle.getSize(),
				particle.getSize());
		g2d.setComposite(java.awt.AlphaComposite.SrcOver);
	}
	/**
	 * Draws all sparkles, clipped to the play area below the HUD line.
	 * Call while the world offset is applied so they shake with the world.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param particles
	 *            Particles to draw.
	 * @param topY
	 *            Y of the HUD separation line; nothing is drawn above it.
	 */
	public void drawParticles(final Screen screen,
							  final java.util.List<Particle> particles, final int topY) {
		java.awt.Shape oldClip = backBufferGraphics.getClip();
		// Undo the shake offset so the clip stays fixed on screen.
		backBufferGraphics.clipRect(-worldOffsetX, topY - worldOffsetY,
				screen.getWidth(), screen.getHeight() - topY);
		for (Particle particle : particles)
			drawParticle(particle);
		backBufferGraphics.setClip(oldClip);
	}

	/**
	 * For debugging purpouses, draws the canvas borders.
	 *
	 * @param screen
	 *            Screen to draw in.
	 */
	@SuppressWarnings("unused")
	private void drawBorders(final Screen screen) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(0, 0, screen.getWidth() - 1, 0);
		backBufferGraphics.drawLine(0, 0, 0, screen.getHeight() - 1);
		backBufferGraphics.drawLine(screen.getWidth() - 1, 0,
				screen.getWidth() - 1, screen.getHeight() - 1);
		backBufferGraphics.drawLine(0, screen.getHeight() - 1,
				screen.getWidth() - 1, screen.getHeight() - 1);
	}

	/**
	 * For debugging purpouses, draws a grid over the canvas.
	 *
	 * @param screen
	 *            Screen to draw in.
	 */
	@SuppressWarnings("unused")
	private void drawGrid(final Screen screen) {
		backBufferGraphics.setColor(Color.DARK_GRAY);
		for (int i = 0; i < screen.getHeight() - 1; i += 2)
			backBufferGraphics.drawLine(0, i, screen.getWidth() - 1, i);
		for (int j = 0; j < screen.getWidth() - 1; j += 2)
			backBufferGraphics.drawLine(j, 0, j, screen.getHeight() - 1);
	}

	/** Margin between the score text and the right edge of the screen. */
	private static final int SCORE_ZONE_MARGIN = 8;
	/** Horizontal gap between the "SCORE" label and the value. */
	private static final int SCORE_LABEL_GAP = 8;

	/**
	 * Draws the current score in the top-right corner, arcade-style: no
	 * border, just the "SCORE" label in white followed by the value in
	 * green, right-aligned so it stays anchored to the same edge as the
	 * digit count grows.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param score
	 *            Current score.
	 */
	public void drawScore(final Screen screen, final int score) {
		int textY = 25;

		backBufferGraphics.setFont(fontRegular);
		String label = "SCORE";
		String scoreString = Integer.toString(score);

		int scoreWidth = fontRegularMetrics.stringWidth(scoreString);
		int labelWidth = fontRegularMetrics.stringWidth(label);
		int scoreX = screen.getWidth() - SCORE_ZONE_MARGIN - scoreWidth;
		int labelX = scoreX - SCORE_LABEL_GAP - labelWidth;

		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(label, labelX, textY);

		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawString(scoreString, scoreX, textY);
	}

	/**
	 * Draws number of remaining lives on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param lives
	 *            Current lives.
	 */
	/** Horizontal space reserved for the "LIVES" label. */
	private static final int LIVES_LABEL_WIDTH = 46;
	/** Horizontal space reserved per life icon. */
	private static final int LIVES_ICON_SPACING = 35;

	public void drawLives(final Screen screen, final int lives) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString("LIVES", 20, 25);

		Ship dummyShip = new Ship(0, 0, Core.getSelectedShipType());
		// Life icons use the ship look chosen in Customize (A1).
		shop.ShapeSkin shape = PurchaseManager.getInstance()
				.getSelectedShape(dummyShip.getType());
		dummyShip.setShapeSprite(shape == null ? null : shape.getSprite());
		for (int i = 0; i < lives; i++)
			drawEntity(dummyShip, 20 + LIVES_LABEL_WIDTH
					+ LIVES_ICON_SPACING * i, 10);
	}

	/** Baseline Y of the top-centre level indicator, kept off y=25 so it
	 * doesn't collide with the Currency team's coin balance, which is
	 * also centred there. */
	private static final int LEVEL_INDICATOR_Y = 14;

	/**
	 * Draws the current wave/level number on screen, top-centre, so the
	 * player can see it at a glance during play.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param level
	 *            Current game level.
	 */
	public void drawLevel(final Screen screen, final int level) {
		String label = "LEVEL";
		String valueString = Integer.toString(level);

		backBufferGraphics.setFont(fontRegular);
		int labelWidth = fontRegularMetrics.stringWidth(label);
		int valueWidth = fontRegularMetrics.stringWidth(valueString);
		int gap = 6;
		int totalWidth = labelWidth + gap + valueWidth;
		int labelX = (screen.getWidth() - totalWidth) / 2;
		int valueX = labelX + labelWidth + gap;

		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(label, labelX, LEVEL_INDICATOR_Y);
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawString(valueString, valueX, LEVEL_INDICATOR_Y);
	}
	/** Y offset of the diamond counter below the score line. */
	private static final int DIAMONDS_Y_OFFSET = 11;
	/** Size, in pixels, of the diamond icon drawn next to the count. */
	private static final int DIAMOND_ICON_SIZE = 10;
	/** Gap between the diamond icon and its number. */
	private static final int DIAMOND_ICON_GAP = 4;

	/**
	 * Draws the player's current diamond balance, left-aligned under the
	 * score (same start X), as a small diamond icon followed by the amount
	 * - pure display, no game logic (the Currency team's DiamondManager /
	 * CashOutScreen own the actual Cash Out / Continue choice and the
	 * persisted balance).
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param diamonds
	 *            Current diamond balance to display.
	 */
	public void drawDiamonds(final Screen screen, final int diamonds) {
		String valueString = Integer.toString(diamonds);
		int textY = 25 + DIAMONDS_Y_OFFSET;
		int startX = screen.getWidth() - 60;
		int iconTop = textY - DIAMOND_ICON_SIZE + 1;

		int[] xPoints = { startX + DIAMOND_ICON_SIZE / 2, startX + DIAMOND_ICON_SIZE,
				startX + DIAMOND_ICON_SIZE / 2, startX };
		int[] yPoints = { iconTop, iconTop + DIAMOND_ICON_SIZE / 2,
				iconTop + DIAMOND_ICON_SIZE, iconTop + DIAMOND_ICON_SIZE / 2 };

		backBufferGraphics.setColor(Color.CYAN);
		backBufferGraphics.fillPolygon(xPoints, yPoints, 4);
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(valueString, startX + DIAMOND_ICON_SIZE
				+ DIAMOND_ICON_GAP, textY);
	}

	/**
	 * Draws the in-game pause menu: a boxed panel over the frozen game,
	 * same look as the exit confirmation, with a "PAUSED" title and a
	 * vertical list of options. The selected option is green with a
	 * leading cursor, like the main menu.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param options
	 *            Option labels, in the order they are listed.
	 * @param selectedIndex
	 *            Index of the option the cursor is on.
	 */
	public void drawPauseMenu(final Screen screen, final String[] options,
							  final int selectedIndex) {
		String title = "PAUSED";

		int spacing = menuItemSpacing();
		int boxWidth = screen.getWidth() * 3 / 4;
		int boxHeight = spacing * (options.length + 3);
		int boxX = (screen.getWidth() - boxWidth) / 2;
		int boxY = (screen.getHeight() - boxHeight) / 2;

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(boxX, boxY, boxWidth, boxHeight);
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawRect(boxX, boxY, boxWidth, boxHeight);

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, title, boxY + spacing * 3 / 2);

		for (int i = 0; i < options.length; i++) {
			boolean selected = i == selectedIndex;
			String line = (selected ? "> " : " ") + options[i];
			backBufferGraphics.setColor(selected ? Color.GREEN : Color.WHITE);
			drawCenteredRegularString(screen, line,
					boxY + spacing * (i + 3));
		}
	}

	/**
	 * Draws a thick line from side to side of the screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param positionY
	 *            Y coordinate of the line.
	 */
	public void drawHorizontalLine(final Screen screen, final int positionY) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(0, positionY, screen.getWidth(), positionY);
		backBufferGraphics.drawLine(0, positionY + 1, screen.getWidth(),
				positionY + 1);
	}

	/**
	 * Draws game title.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawTitle(final Screen screen) {
		String titleString = "Invaders";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, titleString, screen.getHeight() / 3);
	}

	/**
	 * Draws the main menu, one row per item, through drawMenuRow so the
	 * main menu and every other screen share one selected-row style.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param selected
	 *            Item the cursor is on.
	 */
	public void drawMenu(final Screen screen, final MenuItem selected) {
		for (MenuItem item : MenuItem.values())
			drawMenuRow(screen, item.getTitle(),
					menuItemBaseline(screen, item.ordinal()),
					item == selected, item.isEnabled());
	}

	/**
	 * Finds the menu item drawn at a given height. Each item owns a full-width
	 * row as tall as the spacing between items, so the whole row is clickable,
	 * not just the text.
	 *
	 * @param screen
	 *            Screen the menu is drawn on.
	 * @param positionY
	 *            Height to check, in screen coordinates.
	 * @return Item on that row, or null if there is none or nothing has been
	 *         drawn yet.
	 */
	public MenuItem menuItemAt(final Screen screen, final int positionY) {
		if (fontRegularMetrics == null)
			return null;
		int spacing = menuItemSpacing();
		for (MenuItem item : MenuItem.values()) {
			if (isOnMenuRow(menuItemBaseline(screen, item.ordinal()),
					spacing, positionY))
				return item;
		}
		return null;
	}

	/**
	 * Checks whether a height falls on a row of regular text. The row is
	 * centred on the text and as tall as the gap to the next row, so rows
	 * touch and the whole width is hoverable, not just the glyphs. Screens
	 * pass the same baseline they draw at, so drawing and hit-testing
	 * cannot drift apart.
	 *
	 * @param baseline
	 *            Baseline the row's text is drawn at.
	 * @param rowHeight
	 *            Distance between this row and the next one.
	 * @param positionY
	 *            Height to check, in screen coordinates.
	 * @return True if the height is on the row, false otherwise or if
	 *         nothing has been drawn yet.
	 */
	public boolean isOnMenuRow(final int baseline, final int rowHeight,
							   final int positionY) {
		if (fontRegularMetrics == null)
			return false;
		int top = baseline - fontRegularMetrics.getAscent()
				- (rowHeight - fontRegularMetrics.getHeight()) / 2;
		return positionY >= top && positionY < top + rowHeight;
	}

	/**
	 * Height of the baseline of a menu item. Drawing and hit-testing both use
	 * this, so they cannot drift apart.
	 *
	 * @param screen
	 *            Screen the menu is drawn on.
	 * @param index
	 *            Position of the item in the menu, from the top.
	 * @return Baseline of the item's text.
	 */
	private int menuItemBaseline(final Screen screen, final int index) {
		return screen.getHeight() / 2 + menuItemSpacing() * (index + 2);
	}

	/**
	 * Distance between two menu items.
	 *
	 * @return Spacing, in pixels.
	 */
	private int menuItemSpacing() {
		return fontRegularMetrics.getHeight() * 5 / 4;
	}

	/**
	 * Draws the keys available on the current screen, one line along the
	 * bottom.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param hints
	 *            Keys to show, already worded for the current state.
	 */
	public void drawKeyHints(final Screen screen, final String hints) {
		Font hintFont = fontRegular;
		FontMetrics metrics = fontRegularMetrics;
		int maxWidth = screen.getWidth() - 20;
		int textWidth = metrics.stringWidth(hints);
		if (textWidth > maxWidth) {
			hintFont = fontRegular.deriveFont(
					fontRegular.getSize2D() * maxWidth / textWidth);
			metrics = backBufferGraphics.getFontMetrics(hintFont);
		}
		backBufferGraphics.setFont(hintFont);
		backBufferGraphics.setColor(Color.GRAY);
		backBufferGraphics.drawString(hints,
				(screen.getWidth() - metrics.stringWidth(hints)) / 2,
				screen.getHeight() - fontRegularMetrics.getHeight() * 2);
	}

	/**
	 * Draws the exit confirmation over the menu. Filled first so the menu
	 * behind it does not show through.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param yesSelected
	 *            Whether the cursor is on Yes.
	 */
	public void drawExitConfirm(final Screen screen,
								final boolean yesSelected) {
		String question = "Really quit?";
		String yesString = "Yes";
		String noString = "No";

		int spacing = menuItemSpacing();
		int boxWidth = exitConfirmBoxWidth(screen);
		int boxX = exitConfirmBoxX(screen);
		int boxY = exitConfirmBoxY(screen);

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(boxX, boxY, boxWidth, spacing * 4);
		backBufferGraphics.setColor(Color.GRAY);
		backBufferGraphics.drawRect(boxX, boxY, boxWidth, spacing * 4);

		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, question, boxY + spacing * 3 / 2);

		// Yes and No sit either side of the centre, so they need their own
		// x positions rather than the centred helper.
		int answerY = exitConfirmAnswerBaseline(screen);
		int yesX = screen.getWidth() / 2 - boxWidth / 4
				- fontRegularMetrics.stringWidth(yesString) / 2;
		int noX = screen.getWidth() / 2 + boxWidth / 4
				- fontRegularMetrics.stringWidth(noString) / 2;

		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(yesSelected ? Color.GREEN : Color.WHITE);
		backBufferGraphics.drawString(yesString, yesX, answerY);
		backBufferGraphics.setColor(yesSelected ? Color.WHITE : Color.GREEN);
		backBufferGraphics.drawString(noString, noX, answerY);
	}

	/**
	 * Finds the answer the exit confirmation offers at a given point. Each
	 * answer owns half the width of the box on the answer row, so the whole
	 * half is clickable, not just the text.
	 *
	 * @param screen
	 *            Screen the confirmation is drawn on.
	 * @param positionX
	 *            Horizontal position to check, in screen coordinates.
	 * @param positionY
	 *            Height to check, in screen coordinates.
	 * @return True for Yes, false for No, or null when the point is outside
	 *         the answer row or nothing has been drawn yet.
	 */
	public Boolean exitConfirmAnswerAt(final Screen screen,
									   final int positionX, final int positionY) {
		if (fontRegularMetrics == null)
			return null;
		int spacing = menuItemSpacing();
		int top = exitConfirmAnswerBaseline(screen)
				- fontRegularMetrics.getAscent()
				- (spacing - fontRegularMetrics.getHeight()) / 2;
		if (positionY < top || positionY >= top + spacing)
			return null;
		int boxX = exitConfirmBoxX(screen);
		if (positionX < boxX
				|| positionX >= boxX + exitConfirmBoxWidth(screen))
			return null;
		return positionX < screen.getWidth() / 2;
	}

	/**
	 * Width of the exit confirmation box. Drawing and hit-testing both use
	 * this, so they cannot drift apart.
	 *
	 * @param screen
	 *            Screen the confirmation is drawn on.
	 * @return Width of the box, in pixels.
	 */
	private int exitConfirmBoxWidth(final Screen screen) {
		return screen.getWidth() / 2;
	}

	/**
	 * Left edge of the exit confirmation box.
	 *
	 * @param screen
	 *            Screen the confirmation is drawn on.
	 * @return X coordinate of the left edge.
	 */
	private int exitConfirmBoxX(final Screen screen) {
		return (screen.getWidth() - exitConfirmBoxWidth(screen)) / 2;
	}

	/**
	 * Top edge of the exit confirmation box.
	 *
	 * @param screen
	 *            Screen the confirmation is drawn on.
	 * @return Y coordinate of the top edge.
	 */
	private int exitConfirmBoxY(final Screen screen) {
		return (screen.getHeight() - menuItemSpacing() * 4) / 2;
	}

	/**
	 * Baseline of the row the Yes and No answers sit on.
	 *
	 * @param screen
	 *            Screen the confirmation is drawn on.
	 * @return Baseline of the answer row.
	 */
	private int exitConfirmAnswerBaseline(final Screen screen) {
		return exitConfirmBoxY(screen) + menuItemSpacing() * 3;
	}

	/**
	 * Draws an achievement-unlocked popup over the game.
	 *
	 * @param screen Screen where the popup is drawn.
	 * @param achievement Newly unlocked achievement.
	 * @param elapsedMilliseconds Time since the popup started.
	 * @param durationMilliseconds Total popup duration.
	 * @param slideInMilliseconds Slide-in duration.
	 * @param slideOutMilliseconds Slide-out duration.
	 */
	public void drawAchievementUnlocked(final Screen screen,
										final Achievement achievement, final long elapsedMilliseconds,
										final int durationMilliseconds, final int slideInMilliseconds,
										final int slideOutMilliseconds) {
		int boxWidth = 218;
		int boxHeight = 44;
		int visibleX = screen.getWidth() - boxWidth - 6;
		int hiddenX = screen.getWidth() + 2;
		int boxY = 46;
		int boxX = visibleX;

		if (elapsedMilliseconds < slideInMilliseconds)
			boxX = hiddenX - (hiddenX - visibleX) * (int) elapsedMilliseconds
					/ slideInMilliseconds;
		else if (elapsedMilliseconds > durationMilliseconds
				- slideOutMilliseconds)
			boxX = visibleX + (hiddenX - visibleX) * (int) (elapsedMilliseconds
					- (durationMilliseconds - slideOutMilliseconds))
					/ slideOutMilliseconds;

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(boxX, boxY, boxWidth, boxHeight);
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawRect(boxX, boxY, boxWidth, boxHeight);
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawString("ACHIEVEMENT UNLOCKED", boxX + 34,
				boxY + 16);
		if (achievement.getSpriteType() == SpriteType.FleetMaster) {
			drawSmallSprite(
					achievement.getSpriteType(),
					boxX + 8,
					boxY + (boxHeight - 23) / 2,
					Color.WHITE);
		} else {
			drawSprite(
					achievement.getSpriteType(),
					boxX + 8,
					boxY + 20,
					achievement.getIconColor());
		}
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(achievement.getName(), boxX + 34,
				boxY + 35);
	}

	/**
	 * Draws an achievement icon alongside its name, status, and description.
	 *
	 * @param screen Screen where the achievement is drawn.
	 * @param achievement Achievement to display.
	 */
	public void drawAchievement(final Screen screen,
								final Achievement achievement) {
		int iconX = screen.getWidth() / 5;
		int contentX = iconX + 40;
		int nameY = screen.getHeight() / 2;
		String status = achievement.isUnlocked() ? "UNLOCKED" : "LOCKED";
		drawSprite(achievement.getSpriteType(),
				iconX, nameY - 20,
				achievement.isUnlocked() ? achievement.getIconColor() : Color.DARK_GRAY);
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(achievement.isUnlocked() ? Color.WHITE
				: Color.GRAY);
		backBufferGraphics.drawString(achievement.getName() + " - " + status,
				contentX, nameY);
		backBufferGraphics.setColor(Color.GRAY);
		backBufferGraphics.drawString(Core.getAchievementManager()
						.getRequirementText(achievement), contentX,
				nameY + fontRegularMetrics.getHeight() * 2);
	}
	/**Draw smaller sprite in achievement
	 *
	 */
	public void drawSmallSprite(final SpriteType spriteType,
								final int positionX, final int positionY, final Color color) {

		boolean[][] image = spriteMap.get(spriteType);
		backBufferGraphics.setColor(color);

		for (int x = 0; x < image.length; x++) {
			for (int y = 0; y < image[x].length; y++) {
				if (image[x][y]) {
					backBufferGraphics.fillRect(
							positionX + x,
							positionY + y,
							1, 1);
				}
			}
		}
	}


	/**
	 * Draws the scrolling stars behind the main menu. Call it right after
	 * initDrawing so everything else is drawn on top.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param starfield
	 *            Stars to draw.
	 */
	public void drawStarfield(final Screen screen, final Starfield starfield) {
		for (int i = 0; i < starfield.getCount(); i++) {
			int size = starfield.getSize(i);
			backBufferGraphics.setColor(starfield.getColor(i));
			backBufferGraphics.fillRect(starfield.getX(i), starfield.getY(i),
					size, size);
		}
	}

	/**
	 * Draws the title of a screen reached from the main menu, in the same
	 * colour and place on every screen. Sets its own colour, so it does not
	 * depend on what was drawn before it.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param title
	 *            Title to draw.
	 */
	public void drawScreenTitle(final Screen screen, final String title) {
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, title, screen.getHeight() / 8);
	}

	/**
	 * Draws game results.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param score
	 *            Score obtained.
	 * @param livesRemaining
	 *            Lives remaining when finished.
	 * @param shipsDestroyed
	 *            Total ships destroyed.
	 * @param accuracy
	 *            Total accuracy.
	 * @param isNewRecord
	 *            If the score is a new high score.
	 */
	public void drawResults(final Screen screen, final int score,
							final int livesRemaining, final int shipsDestroyed,
							final float accuracy, final boolean isNewRecord) {
		String scoreString = String.format("score %04d", score);
		String livesRemainingString = "lives remaining " + livesRemaining;
		String shipsDestroyedString = "enemies destroyed " + shipsDestroyed;
		String accuracyString = String
				.format("accuracy %.2f%%", accuracy * 100);

		int height = isNewRecord ? 4 : 2;

		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, scoreString, screen.getHeight()
				/ height);
		drawCenteredRegularString(screen, livesRemainingString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 2);
		drawCenteredRegularString(screen, shipsDestroyedString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 4);
		drawCenteredRegularString(screen, accuracyString, screen.getHeight()
				/ height + fontRegularMetrics.getHeight() * 6);
	}

	/**
	 * Draws interactive characters for name input.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param name
	 *            Current name selected.
	 * @param nameCharSelected
	 *            Current character selected for modification.
	 */
	public void drawNameInput(final Screen screen, final char[] name,
							  final int nameCharSelected) {
		String newRecordString = "New Record!";
		String introduceNameString = "Introduce name:";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredRegularString(screen, newRecordString, screen.getHeight()
				/ 4 + fontRegularMetrics.getHeight() * 10);
		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, introduceNameString,
				screen.getHeight() / 4 + fontRegularMetrics.getHeight() * 12);

		// 3 letters name.
		int positionX = screen.getWidth()
				/ 2
				- (fontRegularMetrics.getWidths()[name[0]]
				+ fontRegularMetrics.getWidths()[name[1]]
				+ fontRegularMetrics.getWidths()[name[2]]
				+ fontRegularMetrics.getWidths()[' ']) / 2;

		for (int i = 0; i < 3; i++) {
			if (i == nameCharSelected)
				backBufferGraphics.setColor(Color.GREEN);
			else
				backBufferGraphics.setColor(Color.WHITE);

			positionX += fontRegularMetrics.getWidths()[name[i]] / 2;
			positionX = i == 0 ? positionX
					: positionX
					+ (fontRegularMetrics.getWidths()[name[i - 1]]
					+ fontRegularMetrics.getWidths()[' ']) / 2;

			backBufferGraphics.drawString(Character.toString(name[i]),
					positionX,
					screen.getHeight() / 4 + fontRegularMetrics.getHeight()
							* 14);
		}
	}

	/**
	 * Draws basic content of game over screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param acceptsInput
	 *            If the screen accepts input.
	 * @param isNewRecord
	 *            If the score is a new high score.
	 */
	public void drawGameOver(final Screen screen, final boolean acceptsInput,
							 final boolean isNewRecord) {
		String gameOverString = "Game Over";
		String continueOrExitString =
				"Press Space to play again, Escape to exit";

		int height = isNewRecord ? 4 : 2;

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, gameOverString, screen.getHeight()
				/ height - fontBigMetrics.getHeight() * 2);

		if (acceptsInput)
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, continueOrExitString,
				screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 10);
	}

	/**
	 * Draws high score screen title and instructions.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawHighScoreMenu(final Screen screen) {
		String highScoreString = "High Scores";
		String instructionsString = "Press Space to return";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, highScoreString, screen.getHeight() / 8);

		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, instructionsString,
				screen.getHeight() / 5);
	}

	/**
	 * Draws high scores.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param highScores
	 *            List of high scores.
	 */
	public void drawHighScores(final Screen screen,
							   final List<Score> highScores) {
		backBufferGraphics.setColor(Color.WHITE);
		int i = 0;
		String scoreString = "";

		for (Score score : highScores) {
			scoreString = String.format("%s        %04d", score.getName(),
					score.getScore());
			drawCenteredRegularString(screen, scoreString, screen.getHeight()
					/ 4 + fontRegularMetrics.getHeight() * (i + 1) * 2);
			i++;
		}
	}

	/**
	 * Draws a centered string on regular font.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            String to draw.
	 * @param height
	 *            Height of the drawing.
	 */
	public void drawCenteredRegularString(final Screen screen,
										  final String string, final int height) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontRegularMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * Draws a centered string on big font.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            String to draw.
	 * @param height
	 *            Height of the drawing.
	 */
	public void drawCenteredBigString(final Screen screen, final String string,
									  final int height) {
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontBigMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 * Draws an entity shrunk around its center and faded, used when enemies
	 * disappear on game over.
	 *
	 * @param entity
	 *            Entity to be drawn.
	 * @param scale
	 *            Size of the entity, from 0 (gone) to 1 (normal size).
	 */
	public void drawEntityShrunk(final Entity entity, final double scale) {
		if (scale <= 0)
			return;
		boolean[][] image = spriteMap.get(entity.getSpriteType());
		Color color = entity.getColor();
		int alpha = (int) (255 * Math.min(1, scale));

		double centerX = entity.getPositionX() + entity.getWidth() / 2.0;
		double centerY = entity.getPositionY() + entity.getHeight() / 2.0;
		int pixelSize = Math.max(1, (int) Math.round(2 * scale));

		backBufferGraphics.setColor(new Color(color.getRed(),
				color.getGreen(), color.getBlue(), alpha));
		for (int i = 0; i < image.length; i++)
			for (int j = 0; j < image[i].length; j++)
				if (image[i][j])
					backBufferGraphics.fillRect(
							(int) (centerX + (i * 2 - entity.getWidth()
									/ 2.0) * scale),
							(int) (centerY + (j * 2 - entity.getHeight()
									/ 2.0) * scale),
							pixelSize, pixelSize);
	}

	/**
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 * Draws the game over banner shown on the game screen, typed out up to
	 * the given number of characters. The text stays centered as a whole so
	 * letters do not shift while typing.
	 *
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param text
	 *            Full banner text.
	 * @param visibleChars
	 *            Number of characters typed so far.
	 */
	public void drawGameOverBanner(final Screen screen, final String text,
								   final int visibleChars) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(
				text.substring(0, Math.min(visibleChars, text.length())),
				screen.getWidth() / 2 - fontBigMetrics.stringWidth(text) / 2,
				screen.getHeight() / 2);
	}

	/**
	 * Covers the screen with a translucent black layer, used to fade out.
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param alpha
	 *            Opacity of the layer, from 0 (clear) to 255 (black).
	 */
	public void drawFadeOverlay(final Screen screen, final int alpha) {
		backBufferGraphics.setColor(new Color(0, 0, 0,
				Math.max(0, Math.min(255, alpha))));
		backBufferGraphics.fillRect(0, 0, screen.getWidth(),
				screen.getHeight());
	}

	/**
	 * Countdown to game start.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param level
	 *            Game difficulty level.
	 * @param number
	 *            Countdown number.
	 * @param bonusLife
	 *            Checks if a bonus life is received.
	 */
	public void drawCountDown(final Screen screen, final int level,
							  final int number, final boolean bonusLife) {
		int rectWidth = screen.getWidth();
		int rectHeight = screen.getHeight() / 6;
		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(0, screen.getHeight() / 2 - rectHeight / 2,
				rectWidth, rectHeight);
		backBufferGraphics.setColor(Color.GREEN);
		if (number >= 4)
			if (!bonusLife) {
				drawCenteredBigString(screen, "Level " + level,
						screen.getHeight() / 2
								+ fontBigMetrics.getHeight() / 3);
			} else {
				drawCenteredBigString(screen, "Level " + level
								+ " - Bonus life!",
						screen.getHeight() / 2
								+ fontBigMetrics.getHeight() / 3);
			}
		else if (number != 0)
			drawCenteredBigString(screen, Integer.toString(number),
					screen.getHeight() / 2 + fontBigMetrics.getHeight() / 3);
		else
			drawCenteredBigString(screen, "GO!", screen.getHeight() / 2
					+ fontBigMetrics.getHeight() / 3);
	}

	/**
	 * Draws a centered row of text in the menu style, as enabled. See the
	 * five-argument version.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            Text to draw.
	 * @param height
	 *            Height of the drawing.
	 * @param selected
	 *            Whether the row is currently selected.
	 */
	public void drawMenuRow(final Screen screen, final String string,
			final int height, final boolean selected) {
		drawMenuRow(screen, string, height, selected, true);
	}

	/**
	 * Draws a centered row of text in the menu style shared by every screen,
	 * the main menu included: the selected row is green, in the larger font
	 * and between "> <" markers; a disabled row is dark grey; any other row
	 * is white.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            Text to draw.
	 * @param height
	 *            Height of the drawing.
	 * @param selected
	 *            Whether the row is currently selected.
	 * @param enabled
	 *            Whether the row can be chosen.
	 */
	public void drawMenuRow(final Screen screen, final String string,
			final int height, final boolean selected,
			final boolean enabled) {
		if (selected) {
			String text = "> " + string + " <";
			backBufferGraphics.setColor(Color.GREEN);
			backBufferGraphics.setFont(fontSelected);
			backBufferGraphics.drawString(text,
					(screen.getWidth()
							- fontSelectedMetrics.stringWidth(text)) / 2,
					height);
		} else {
			backBufferGraphics.setColor(
					enabled ? Color.WHITE : Color.DARK_GRAY);
			drawCenteredRegularString(screen, string, height);
		}
	}
	/**
	 * Draws the damage dim overlay when the player is hit.
	 *AUTHORED BY: VFX TEAM (Effection)
	 *Any further inquiries please contact us.
	 * @param screen
	 *            Screen to draw on.
	 * @param effect
	 *            Dim effect to draw.
	 */
	public void drawDamageDim(final Screen screen,
							  final DamageDimEffect effect) {
		if (effect != null)
			effect.draw(backBufferGraphics, screen.getWidth(),
					screen.getHeight());
	}

	/**
	 * Draws the comet trail behind a player bullet.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * @param bullet Player bullet.
	 */
	public void drawBulletTrail(final Entity bullet) {
		if (bullet != null)
			BulletTrail.draw(backBufferGraphics, bullet.getPositionX(),
					bullet.getPositionY(), bullet.getWidth(),
					bullet.getHeight());
	}

	/**
	 * Draws the Lightning Chain kill effect.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * @param effect
	 *            Effect to draw.
	 */
	public void drawLightningChain(final LightningChain effect) {
		if (effect != null)
			effect.draw(backBufferGraphics);
	}

	/**
	 * Draws the low-health glitch effect.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *Any further inquiries please contact us.
	 *
	 * @param screen Screen to draw on.
	 * @param effect Glitch effect to draw.
	 */
	public void drawGlitch(final Screen screen, final GlitchEffect effect) {
		if (effect != null)
			effect.draw(backBuffer, backBufferGraphics);
	}


	/**
	 * Draws the points shown when enemies are destroyed.
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 *
	 * @param screen Screen to draw on.
	 * @param effect Points effect to draw.
	 */
	public void drawPointsEffect(final Screen screen,
								 final PointsEffect effect) {
		if (effect != null)
			effect.draw(backBufferGraphics, screen.getWidth(), fontRegular);
	}

	/**
	 * Draws the Warp In effect while the ship beams in.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * @param warpIn Warp In effect, may be null.
	 * @param ship   Player ship.
	 * @param color  Ship colour this frame.
	 * @param topY   Top of the play area.
	 */
	public void drawWarpIn(final WarpIn warpIn, final Ship ship,
			final Color color, final int topY) {
		if (warpIn != null)
			warpIn.draw((Graphics2D) backBufferGraphics,
					spriteMap.get(ship.getIdleSprite()), ship,
					color, topY);
	}

	/**
	 * Draws the looping Warp In preview for the shop.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * @param shipSprite Sprite of the selected ship.
	 * @param color      Colour of the selected skin.
	 * @param centerX    Centre of the preview.
	 * @param top        Top of the preview.
	 */
	public void drawWarpInPreview(final SpriteType shipSprite,
			final Color color, final int centerX, final int top) {
		WarpIn.drawPreview((Graphics2D) backBufferGraphics,
				spriteMap.get(shipSprite), color, centerX, top, 50);
	}

	/** Point size of the small text used in the shop and Customize panels. */
	private static final float SMALL_FONT_SIZE = 8f;
	/** Small version of the regular game font, made on first use. */
	private static Font fontSmall;

	/**
	 * @return Small version of the regular game font.
	 */
	private static Font smallFont() {
		if (fontSmall == null
				|| !fontSmall.getFamily().equals(fontRegular.getFamily()))
			fontSmall = fontRegular.deriveFont(SMALL_FONT_SIZE);
		return fontSmall;
	}

	/**
	 * Draws a filled rectangle, e.g. a highlighted tab or row.
	 *
	 * @param positionX Horizontal position of the left edge.
	 * @param positionY Vertical position of the top edge.
	 * @param width     Width of the box.
	 * @param height    Height of the box.
	 * @param color     Fill colour.
	 */
	public void fillBox(final int positionX, final int positionY,
						final int width, final int height, final Color color) {
		backBufferGraphics.setColor(color);
		backBufferGraphics.fillRect(positionX, positionY, width, height);
	}

	/**
	 * Measures text in the regular font, for centring and right-aligning.
	 *
	 * @param string Text to measure.
	 * @return Width in pixels; an estimate before the first frame is drawn.
	 */
	public int getRegularStringWidth(final String string) {
		if (fontRegularMetrics == null)
			return string.length() * 10;
		return fontRegularMetrics.stringWidth(string);
	}

	/**
	 * Draws small text at an exact position, left aligned.
	 *
	 * @param string    Text to draw.
	 * @param positionX Horizontal position of the left edge.
	 * @param positionY Vertical position of the baseline.
	 * @param color     Colour of the text.
	 */
	public void drawSmallString(final String string, final int positionX,
								final int positionY, final Color color) {
		backBufferGraphics.setFont(smallFont());
		backBufferGraphics.setColor(color);
		backBufferGraphics.drawString(string, positionX, positionY);
	}

	/**
	 * Measures text in the small font.
	 *
	 * @param string Text to measure.
	 * @return Width in pixels; an estimate before the first frame is drawn.
	 */
	public int getSmallStringWidth(final String string) {
		if (backBufferGraphics == null)
			return string.length() * 7;
		return backBufferGraphics.getFontMetrics(smallFont())
				.stringWidth(string);
	}

	/**
	 * @return Distance between two lines of small text, in pixels.
	 */
	public int getSmallLineHeight() {
		if (backBufferGraphics == null)
			return 12;
		return backBufferGraphics.getFontMetrics(smallFont()).getHeight()
				+ 2;
	}

	/**
	 * Draws a coin or diamond icon in the same colours as the balance HUD.
	 *
	 * @param positionX Horizontal position of the left edge.
	 * @param baseline  Baseline of the text the icon sits next to.
	 * @param diamond   true for a diamond, false for a coin.
	 * @param size      Icon size in pixels.
	 */
	public void drawCurrencyIcon(final int positionX, final int baseline,
								 final boolean diamond, final int size) {
		int top = baseline - size + 1;
		if (diamond) {
			int[] xPoints = { positionX + size / 2, positionX + size,
					positionX + size / 2, positionX };
			int[] yPoints = { top, top + size / 2, top + size,
					top + size / 2 };
			backBufferGraphics.setColor(Color.CYAN);
			backBufferGraphics.fillPolygon(xPoints, yPoints, 4);
		} else {
			backBufferGraphics.setColor(Color.YELLOW);
			backBufferGraphics.fillOval(positionX, top, size, size);
		}
	}

	/**
	 * Draws a sprite with square pixels of any size, for big previews.
	 *
	 * @param spriteType Sprite to draw.
	 * @param positionX  Horizontal position of the left edge.
	 * @param positionY  Vertical position of the top edge.
	 * @param pixelSize  Size of one sprite pixel on screen (2 = in game).
	 * @param color      Colour of the filled pixels.
	 */
	public void drawSpriteScaled(final SpriteType spriteType,
								 final int positionX, final int positionY, final int pixelSize,
								 final Color color) {
		boolean[][] image = spriteMap.get(spriteType);
		if (image == null)
			return;
		backBufferGraphics.setColor(color);
		for (int i = 0; i < image.length; i++)
			for (int j = 0; j < image[i].length; j++)
				if (image[i][j])
					backBufferGraphics.fillRect(positionX + i * pixelSize,
							positionY + j * pixelSize, pixelSize, pixelSize);
	}

	/**
	 * @param spriteType Sprite to measure.
	 * @return Sprite width and height in sprite pixels, {0, 0} if unknown.
	 */
	public int[] getSpriteSize(final SpriteType spriteType) {
		boolean[][] image = spriteMap == null ? null
				: spriteMap.get(spriteType);
		if (image == null || image.length == 0)
			return new int[] { 0, 0 };
		return new int[] { image.length, image[0].length };
	}
}
