package engine;

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
import entity.Coin;
import entity.Entity;
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
	/** Size of one item slot box in the item panel. */
	private static final int ITEM_SLOT_WIDTH = 52;
	private static final int ITEM_SLOT_HEIGHT = 16;
	/** Item notice baseline, measured up from the bottom of the screen. */
	private static final int ITEM_NOTICE_BOTTOM_OFFSET = 60;
	/** The shield bubble blinks during this many last milliseconds. */
	private static final long SHIELD_BLINK_MILLIS = 2000;
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
		/** First Flight achievement icon. */
		FirstFlight,
		/** Fleet Master achievement icon. */
		FleetMaster,
		/** Weakestship sprite. */
		Weakestship,
		/** Infinity Void achievement icon. */
		InfinityVoid,
        /** First Boss Kill achievement icon. */
        BossKill
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
			spriteMap.put(SpriteType.FirstFlight, new boolean[11][8]);
			spriteMap.put(SpriteType.FleetMaster, new boolean[23][23]);
			spriteMap.put(SpriteType.Weakestship, new boolean[11][11]);
			spriteMap.put(SpriteType.InfinityVoid, new boolean[11][11]);
            spriteMap.put(SpriteType.BossKill, new boolean[11][11]);

			fileManager.loadSprite(spriteMap);
			logger.info("Finished loading the sprites.");

			// Font loading.
			fontRegular = fileManager.loadFont(14f);
			fontBig = fileManager.loadFont(24f);
			fontSelected = fontRegular.deriveFont(17f);
			logger.info("Finished loading the fonts.");

		} catch (IOException e) {
			logger.warning("Loading failed.");
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
		graphics.drawImage(backBuffer, frame.getInsets().left,
				frame.getInsets().top, frame);
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
		drawSprite(entity.getSpriteType(), positionX, positionY,
				entity.getColor());
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
		Font font = fontRegular.deriveFont(12f);
		FontMetrics metrics = backBufferGraphics.getFontMetrics(font);
		backBufferGraphics.setFont(font);
		int baseline = ITEM_PANEL_TOP + ITEM_SLOT_HEIGHT - 4;

		// Slots: "1 [Shield]" ... an empty slot is a dark box.
		int x = 6;
		for (int i = 0; i < view.slots.size(); i++) {
			ItemAPI.ItemInfo item = view.slots.get(i);
			backBufferGraphics.setColor(Color.GRAY);
			backBufferGraphics.drawString(Integer.toString(i + 1), x, baseline);
			x += metrics.stringWidth("0") + 3;
			backBufferGraphics.setColor(item == null ? Color.DARK_GRAY
					: ItemSystem.colorOf(item.effectKind));
			backBufferGraphics.drawRect(x, ITEM_PANEL_TOP, ITEM_SLOT_WIDTH,
					ITEM_SLOT_HEIGHT);
			if (item != null)
				backBufferGraphics.drawString(item.displayName, x + 4, baseline);
			x += ITEM_SLOT_WIDTH + 8;
		}

		// Effects, right-aligned: "RF x2  SH 8s".
		int right = screen.getWidth() - 6;
		for (int i = view.effects.size() - 1; i >= 0; i--) {
			ItemAPI.EffectView effect = view.effects.get(i);
			String label = shortItemName(effect.item.effectKind);
			if (effect.stacks != null)
				label += " x" + effect.stacks;
			else if (effect.remainingMillis != null)
				label += " " + (effect.remainingMillis + 999) / 1000 + "s";
			right -= metrics.stringWidth(label);
			backBufferGraphics.setColor(ItemSystem
					.colorOf(effect.item.effectKind));
			backBufferGraphics.drawString(label, right, baseline);
			right -= 10;
		}

		String notice = items.getNotice();
		if (notice != null) {
			backBufferGraphics.setColor(Color.YELLOW);
			drawCenteredRegularString(screen, notice, screen.getHeight()
					- ITEM_NOTICE_BOTTOM_OFFSET);
		}
	}

	/**
	 * Explains the items while the level countdown runs (Team CS - Item
	 * System).
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

	/**
	 * Draws current score on screen.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param score
	 *            Current score.
	 */
	public void drawScore(final Screen screen, final int score) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		String scoreString = String.format("%04d", score);
		backBufferGraphics.drawString(scoreString, screen.getWidth() - 60, 25);
	}

	/**
	 * Draws number of remaining lives on screen.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param lives
	 *            Current lives.
	 */
	public void drawLives(final Screen screen, final int lives) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(Integer.toString(lives), 20, 25);
		Ship dummyShip = new Ship(0, 0);
		for (int i = 0; i < lives; i++)
			drawEntity(dummyShip, 40 + 35 * i, 10);
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
	 * Draws main menu.
	 * 
	 * @param screen
	 *            Screen to draw on.
	 * @param selected
	 *            Item the cursor is on.
	 */
	public void drawMenu(final Screen screen, final MenuItem selected) {
		for (MenuItem item : MenuItem.values()) {
			int baseline = menuItemBaseline(screen, item.ordinal());

			if (item == selected) {
				String text = "> " + item.getTitle() + " <";
				backBufferGraphics.setColor(Color.GREEN);
				backBufferGraphics.setFont(fontSelected);
				backBufferGraphics.drawString(text, screen.getWidth() / 2
						- fontSelectedMetrics.stringWidth(text) / 2, baseline);
			} else {
				if (!item.isEnabled())
					backBufferGraphics.setColor(Color.DARK_GRAY);
				else
					backBufferGraphics.setColor(Color.WHITE);
				drawCenteredRegularString(screen, item.getTitle(), baseline);
			}
		}
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
			int top = menuItemBaseline(screen, item.ordinal())
					- fontRegularMetrics.getAscent()
					- (spacing - fontRegularMetrics.getHeight()) / 2;
			if (positionY >= top && positionY < top + spacing)
				return item;
		}
		return null;
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
		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, hints,
				screen.getHeight() - fontRegularMetrics.getHeight());
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
		drawSprite(achievement.getSpriteType(),
				boxX + 8, boxY + 20, achievement.getIconColor());
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
	 * Draws a centered row of text in the menu colours: green when
	 * selected, white otherwise.
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
	 * Draws a centered row of text in the menu colours: green when
	 * selected, dark grey when disabled, white otherwise.
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
		if (selected)
			backBufferGraphics.setColor(Color.GREEN);
		else if (!enabled)
			backBufferGraphics.setColor(Color.DARK_GRAY);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, string, height);
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
}