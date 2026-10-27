package screen;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import engine.Core;
import engine.DrawManager.SpriteType;
import engine.PurchaseManager;
import entity.Ship.ShipType;
import shop.Currency;
import shop.ShopCatalog;
import shop.ShopCategory;
import shop.ShopItem;
import shop.ShapeSkin;
import shop.Skin;

/**
 * Implements the ship select screen, shown as "Customize" in the menu.
 * The player chooses the ship and skin for the next run from what they
 * own, and switches bought upgrades on or off (any number can be on);
 * locked choices can be looked at but not used, and point to the shop.
 * The ship goes through Core.setSelectedShipType() (Mantis), the skin and
 * upgrades through {@link PurchaseManager}, as soon as they are chosen.
 * Bought shop effects (VFX team) are switched on or off one by one in the
 * Effect row; any number can be on, and the ones that are on play in the
 * preview at the top.
 */
public class ShipSelectScreen extends Screen {

	/** Ship, look, colour, upgrade and effect choices. */
	private enum Row {
		/** Ship type. */
		SHIP("Ship"),
		/** Look (shape) of the ship, from the three of its type (A1). */
		SHAPE("Look"),
		/** Ship colour. */
		SKIN("Skin"),
		/** Item team upgrades, browsed and switched on/off one by one. */
		UPGRADE("Upgrade"),
		/**
		 * Shop effects, browsed and switched on/off one by one; any number
		 * can be on. AUTHORED BY: VFX TEAM (Effection)
		 */
		EFFECT("Effect");

		/** Label on the left of the row. */
		private final String label;

		Row(final String label) {
			this.label = label;
		}
	}

	/** Space between the screen edge and the rows. */
	private static final int MARGIN = 32;
	/** Height of one row box. */
	private static final int ROW_HEIGHT = 30;
	/** Distance between two rows. */
	private static final int ROW_SPACING = 34;
	/** Width of the clickable arrow at each end of a row. */
	private static final int ARROW_WIDTH = 28;
	/** Size of one sprite pixel in the big preview. */
	private static final int PREVIEW_PIXEL = 5;
	/** Colour of locked choices. */
	private static final Color LOCKED = new Color(255, 70, 70);
	/** Colour of unselected row borders. */
	private static final Color BORDER = new Color(70, 70, 70);
	/** Height of the preview band above the rows (ship and effects). */
	private static final int PREVIEW_HEIGHT = 88;
	/** Size of one sprite pixel of the ship while effects play. */
	private static final int EFFECT_PREVIEW_PIXEL = 4;
	/** Highest speed and fire rate among the ships, for the stat bars. */
	private static final double MAX_SPEED = maxSpeed();
	private static final double MAX_FIRE_RATE = maxFireRate();

	/** Keys watched for presses (not holds). */
	private static final int[] KEYS = { KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
			KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_A, KeyEvent.VK_D,
			KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_SPACE,
			KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE };

	/** Array of all available ship types. */
	private final ShipType[] availableShips = ShipType.values();
	/** All skins. */
	private final Skin[] availableSkins = Skin.values();
	/** Every upgrade sold in the shop. */
	private final List<ShopItem> availableUpgrades =
			ShopCatalog.getByCategory(ShopCategory.UPGRADE);
	/** Every effect sold in the shop. AUTHORED BY: VFX TEAM (Effection) */
	private final List<ShopItem> availableEffects =
			ShopCatalog.getByCategory(ShopCategory.EFFECT);

	/** Row the cursor is on. */
	private Row row = Row.SHIP;
	/** Choice shown in each row, which may be locked. */
	private int shipIndex;
	/** 0 is the ship's original look, 1 and up its shape skins. */
	private int shapeIndex;
	private int skinIndex;
	private int upgradeIndex;
	private int effectIndex;
	/** Key states last frame, so a held key acts once. */
	private boolean[] keysWereDown;
	/** Mouse state last frame, so a held button clicks once. */
	private boolean mouseWasDown;

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public ShipSelectScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
		this.returnCode = 1; // Default return to main menu

		PurchaseManager loadout = PurchaseManager.getInstance();
		// Open on the ship chosen last time instead of always the first one.
		this.shipIndex = Core.getSelectedShipType().ordinal();
		this.skinIndex = loadout.getSelectedSkin().ordinal();
		syncShapeIndex();
		// Start on the first upgrade the player owns, if any.
		for (int i = this.availableUpgrades.size() - 1; i >= 0; i--)
			if (loadout.isOwned(this.availableUpgrades.get(i)))
				this.upgradeIndex = i;
		// Start on the first effect the player owns, if any.
		for (int i = this.availableEffects.size() - 1; i >= 0; i--)
			if (loadout.isOwned(this.availableEffects.get(i)))
				this.effectIndex = i;
	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();
		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		if (this.keysWereDown == null) {
			// Keys and clicks still held from the menu must not act here.
			this.keysWereDown = new boolean[KEYS.length];
			for (int i = 0; i < KEYS.length; i++)
				this.keysWereDown[i] = this.inputManager.isKeyDown(KEYS[i]);
			this.mouseWasDown = this.inputManager.isMousePressed();
		}

		if (this.inputDelay.checkFinished()) {
			handleKeys();
			handleMouse();
		}
		draw();
	}

	// --- Input ---

	private boolean pressed(final int key) {
		for (int i = 0; i < KEYS.length; i++)
			if (KEYS[i] == key)
				return this.inputManager.isKeyDown(key)
						&& !this.keysWereDown[i];
		return false;
	}

	private void handleKeys() {
		boolean confirm = pressed(KeyEvent.VK_SPACE)
				|| pressed(KeyEvent.VK_ENTER);
		if (confirm && this.row == Row.UPGRADE) {
			toggleUpgrade();
		} else if (confirm && this.row == Row.EFFECT) {
			toggleEffect();
		} else if (pressed(KeyEvent.VK_ESCAPE) || confirm) {
			this.isRunning = false;
		} else if (pressed(KeyEvent.VK_UP) || pressed(KeyEvent.VK_W)) {
			this.row = Row.values()[Math.floorMod(this.row.ordinal() - 1,
					Row.values().length)];
		} else if (pressed(KeyEvent.VK_DOWN) || pressed(KeyEvent.VK_S)) {
			this.row = Row.values()[(this.row.ordinal() + 1)
					% Row.values().length];
		} else if (pressed(KeyEvent.VK_LEFT) || pressed(KeyEvent.VK_A)) {
			change(this.row, -1);
		} else if (pressed(KeyEvent.VK_RIGHT) || pressed(KeyEvent.VK_D)) {
			change(this.row, 1);
		}
		for (int i = 0; i < KEYS.length; i++)
			this.keysWereDown[i] = this.inputManager.isKeyDown(KEYS[i]);
	}

	/** Clicking a row selects it; clicking its arrows changes it. */
	private void handleMouse() {
		boolean down = this.inputManager.isMousePressed();
		boolean clicked = down && !this.mouseWasDown;
		this.mouseWasDown = down;
		if (!clicked)
			return;
		int x = this.inputManager.getMouseX();
		int y = this.inputManager.getMouseY();
		for (Row each : Row.values()) {
			Rectangle area = rowArea(each);
			if (!area.contains(x, y))
				continue;
			this.row = each;
			if (x < area.x + ARROW_WIDTH)
				change(each, -1);
			else if (x >= area.x + area.width - ARROW_WIDTH)
				change(each, 1);
			else if (each == Row.UPGRADE)
				toggleUpgrade();
			else if (each == Row.EFFECT)
				toggleEffect();
			return;
		}
	}

	/**
	 * Shows the next or previous choice in a row, and uses it right away
	 * when it is unlocked.
	 *
	 * @param which
	 *            Row to change.
	 * @param step
	 *            1 for next, -1 for previous.
	 */
	private void change(final Row which, final int step) {
		PurchaseManager loadout = PurchaseManager.getInstance();
		switch (which) {
			case SHIP:
				this.shipIndex = Math.floorMod(this.shipIndex + step,
						this.availableShips.length);
				ShipType ship = this.availableShips[this.shipIndex];
				if (loadout.isShipUnlocked(ship)) {
					// Remember the choice for the menu and the game (Mantis).
					Core.setSelectedShipType(ship);
					this.logger.info("Ship selected: " + ship);
				}
				// Each ship has its own looks: show the one it wears.
				syncShapeIndex();
				break;
			case SHAPE:
				int looks = browsableShapes().size() + 1;
				this.shapeIndex = Math.floorMod(this.shapeIndex + step, looks);
				if (loadout.selectShape(this.availableShips[this.shipIndex],
						browsedShape()))
					this.logger.info("Ship look selected: " + (browsedShape()
							== null ? "original" : browsedShape().name()));
				break;
			case SKIN:
				this.skinIndex = Math.floorMod(this.skinIndex + step,
						this.availableSkins.length);
				loadout.selectSkin(this.availableSkins[this.skinIndex]);
				break;
			case UPGRADE:
				// Browsing only: upgrades are switched with space or a click.
				if (!this.availableUpgrades.isEmpty())
					this.upgradeIndex = Math.floorMod(this.upgradeIndex + step,
							this.availableUpgrades.size());
				break;
		default:
			// Browsing only: effects are switched with space or a click.
			if (!this.availableEffects.isEmpty())
				this.effectIndex = Math.floorMod(this.effectIndex + step,
						this.availableEffects.size());
			break;
		}
	}

	/** @return Looks of the browsed ship, not counting its original one. */
	private List<ShapeSkin> browsableShapes() {
		return ShapeSkin.forShip(this.availableShips[this.shipIndex]);
	}

	/** @return Look shown in the look row, or null for the original one. */
	private ShapeSkin browsedShape() {
		List<ShapeSkin> shapes = browsableShapes();
		return this.shapeIndex <= 0 || this.shapeIndex > shapes.size()
				? null : shapes.get(this.shapeIndex - 1);
	}

	/** Moves the look row to the look the browsed ship wears. */
	private void syncShapeIndex() {
		ShapeSkin worn = PurchaseManager.getInstance().getSelectedShape(
				this.availableShips[this.shipIndex]);
		this.shapeIndex = worn == null ? 0
				: browsableShapes().indexOf(worn) + 1;
	}

	/** Switches the browsed upgrade on or off, if it is owned. */
	private void toggleUpgrade() {
		ShopItem upgrade = browsedUpgrade();
		if (upgrade != null && PurchaseManager.getInstance().isOwned(upgrade))
			PurchaseManager.getInstance().toggleUpgrade(upgrade.getRef());
	}

	/**
	 * Switches the browsed effect on or off, if it is owned. Other effects
	 * stay as they are. AUTHORED BY: VFX TEAM (Effection)
	 */
	private void toggleEffect() {
		PurchaseManager.getInstance().toggleEffect(browsedEffect());
	}

	/** @return Effect shown in the effect row, or null if none are sold. */
	private ShopItem browsedEffect() {
		return this.availableEffects.isEmpty() ? null
				: this.availableEffects.get(this.effectIndex);
	}

	/** @return Upgrade shown in the upgrade row, or null if none are sold. */
	private ShopItem browsedUpgrade() {
		return this.availableUpgrades.isEmpty() ? null
				: this.availableUpgrades.get(this.upgradeIndex);
	}

	// --- State of each row ---

	private boolean isUnlocked(final Row which) {
		PurchaseManager loadout = PurchaseManager.getInstance();
		switch (which) {
			case SHIP:
				return loadout.isShipUnlocked(this.availableShips[this.shipIndex]);
			case SHAPE:
				return browsedShape() == null
						|| loadout.isShapeUnlocked(browsedShape());
			case SKIN:
				return loadout.isSkinUnlocked(this.availableSkins[this.skinIndex]);
			case UPGRADE:
				return loadout.isOwned(browsedUpgrade());
		default:
			// No effects sold yet: nothing to unlock.
			return browsedEffect() == null
					|| loadout.isOwned(browsedEffect());
		}
	}

	private String choiceName(final Row which) {
		switch (which) {
			case SHIP:
				return ShopCatalog.shipName(this.availableShips[this.shipIndex]);
			case SHAPE:
				return browsedShape() == null ? "Original"
						: browsedShape().getDisplayName();
			case SKIN:
				return this.availableSkins[this.skinIndex].getDisplayName();
			case EFFECT:
				ShopItem effect = browsedEffect();
				return effect == null ? "None" : effect.getName();
			default:
				ShopItem upgrade = browsedUpgrade();
				if (upgrade == null)
					return "None";
				PurchaseManager loadout = PurchaseManager.getInstance();
				if (!loadout.isOwned(upgrade))
					return upgrade.getName();
				return upgrade.getName()
						+ (loadout.isUpgradeOn(upgrade.getRef()) ? " ON" : " OFF");
		}
	}

	/**
	 * @param which
	 *            Row.
	 * @return Shop entry that unlocks the row's current choice, or null.
	 */
	private ShopItem shopEntry(final Row which) {
		switch (which) {
			case SHIP:
				return ShopCatalog.findByRef(ShopCategory.SHIP,
						this.availableShips[this.shipIndex].name());
			case SHAPE:
				return browsedShape() == null ? null : ShopCatalog.findByRef(
						ShopCategory.SHAPE, browsedShape().name());
			case SKIN:
				return ShopCatalog.findByRef(ShopCategory.SKIN,
						this.availableSkins[this.skinIndex].name());
			case UPGRADE:
				return browsedUpgrade();
		default:
			return browsedEffect();
		}
	}

	// --- Layout ---

	private int previewTop() {
		return this.height / 8 + 24;
	}

	private Rectangle rowArea(final Row which) {
		int top = previewTop() + PREVIEW_HEIGHT + which.ordinal() * ROW_SPACING;
		return new Rectangle(MARGIN, top, this.width - MARGIN * 2,
				ROW_HEIGHT);
	}

	// --- Drawing ---

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawScreenTitle(this, MenuItem.SHIP_SELECT.getTitle());
		drawPreview();
		for (Row each : Row.values())
			drawRow(each);
		drawInfo();

		boolean toggleRow = this.row == Row.UPGRADE || this.row == Row.EFFECT;
		drawManager.drawKeyHints(this, toggleRow
				? "up/down row | left/right browse | space on/off | esc back"
				: "up/down row | left/right change | esc back");

		drawManager.completeDrawing(this);
	}

	/**
	 * The browsed ship, in the browsed skin, drawn large. Effects that are
	 * switched on play around it (VFX team).
	 */
	private void drawPreview() {
		ShipType type = this.availableShips[this.shipIndex];
		Skin skin = this.availableSkins[this.skinIndex];
		boolean dim = !isUnlocked(Row.SHIP) || !isUnlocked(Row.SHAPE)
				|| !isUnlocked(Row.SKIN);
		SpriteType sprite = browsedShape() == null ? type.getIdleSprite()
				: browsedShape().getSprite();
		int rowsTop = rowArea(Row.SHIP).y;

		// Effects that are on, shown together. AUTHORED BY: VFX TEAM (Effection)
		PurchaseManager effects = PurchaseManager.getInstance();
		boolean lightning = effects.isEffectOn("lightning_chain");
		boolean comet = effects.isEffectOn("comet_trail");
		boolean ghost = effects.isEffectOn("ghost_trail");
		if (lightning || comet || ghost) {
			int shipHeight = drawManager.getSpriteSize(sprite)[1]
					* EFFECT_PREVIEW_PIXEL;
			drawEffectPreview(sprite, skin, dim,
					this.width / 2, previewTop() - 8,
					rowsTop - 12 - shipHeight, EFFECT_PREVIEW_PIXEL,
					lightning, comet, ghost);
			return;
		}

		Color color = skin.colorAt(System.currentTimeMillis());
		if (dim)
			color = color.darker().darker();
		int[] size = drawManager.getSpriteSize(sprite);
		drawManager.drawSpriteScaled(sprite, this.width / 2 - size[0]
				* PREVIEW_PIXEL / 2, rowsTop - 12 - size[1] * PREVIEW_PIXEL,
				PREVIEW_PIXEL, color);
	}

	// --- Effect preview. AUTHORED BY: VFX TEAM (Effection) ---

	/** Time of one shot: fly, hit, respawn (ms). */
	private static final int FX_SHOT_CYCLE_MS = 1200;
	/** Part of the shot cycle the bullet is flying. */
	private static final float FX_FLY_PART = 0.35f;
	/** Part of the shot cycle after which the target is back. */
	private static final float FX_RESPAWN_PART = 0.75f;
	/** Time of one left-right slide (ms). */
	private static final int FX_SLIDE_CYCLE_MS = 2400;
	/** How far the ship slides each way (px). */
	private static final int FX_SLIDE_SWING = 40;
	/** Ghosts: how far back in time each is drawn (ms), nearest first. */
	private static final int[] FX_GHOST_DELAY_MS = { 64, 128, 192, 256 };
	/** Ghosts: opacity (0-255), nearest first. */
	private static final int[] FX_GHOST_ALPHA = { 150, 115, 80, 45 };
	/** Gap between the three preview enemies (px). */
	private static final int FX_ENEMY_GAP = 70;
	/** Size of one sprite pixel for enemies and bullets. */
	private static final int FX_SMALL_PIXEL = 2;
	/** Comet trail colours, hot to cool. */
	private static final Color[] FX_TRAIL = { new Color(255, 214, 64),
			new Color(255, 170, 40), new Color(255, 140, 26),
			new Color(224, 80, 26), new Color(168, 50, 14) };
	/** Lightning colour. */
	private static final Color FX_BOLT = new Color(79, 184, 255);

	/**
	 * Looping preview of the switched-on shop effects around the ship; all
	 * that are on play together. Ghost Trail: the ship slides with fading
	 * copies. Comet Trail: its shots leave an orange tail. Lightning Chain:
	 * a killed enemy chains lightning to its neighbours.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * @param ship
	 *            Ship sprite.
	 * @param skin
	 *            Skin of the ship.
	 * @param dim
	 *            True to darken the ship (locked ship or skin).
	 * @param centerX
	 *            Horizontal centre.
	 * @param enemyY
	 *            Top of the enemy row.
	 * @param shipY
	 *            Top of the ship.
	 * @param pixel
	 *            Size of one ship sprite pixel.
	 * @param lightning
	 *            True if Lightning Chain is on.
	 * @param comet
	 *            True if Comet Trail is on.
	 * @param ghost
	 *            True if Ghost Trail is on.
	 */
	private void drawEffectPreview(final SpriteType ship, final Skin skin,
			final boolean dim,
			final int centerX, final int enemyY, final int shipY,
			final int pixel, final boolean lightning, final boolean comet,
			final boolean ghost) {
		long now = System.currentTimeMillis();
		int shipWidth = this.drawManager.getSpriteSize(ship)[0] * pixel;

		if (lightning || comet)
			drawShot(now, centerX, enemyY, shipY, shipWidth,
					lightning, comet, ghost);

		if (ghost)
			for (int k = FX_GHOST_DELAY_MS.length - 1; k >= 0; k--) {
				long then = now - FX_GHOST_DELAY_MS[k];
				Color base = shipColor(skin, then, dim);
				this.drawManager.drawSpriteScaled(ship,
						shipLeft(centerX, shipWidth, then, true), shipY,
						pixel, new Color(base.getRed(), base.getGreen(),
								base.getBlue(), FX_GHOST_ALPHA[k]));
			}
		this.drawManager.drawSpriteScaled(ship,
				shipLeft(centerX, shipWidth, now, ghost), shipY, pixel,
				shipColor(skin, now, dim));
	}

	/** Enemies, the flying bullet, the hit and the lightning. */
	private void drawShot(final long now, final int centerX, final int enemyY,
			final int shipY, final int shipWidth, final boolean lightning,
			final boolean comet, final boolean sliding) {
		long fired = now - now % FX_SHOT_CYCLE_MS;
		float k = (now % FX_SHOT_CYCLE_MS) / (float) FX_SHOT_CYCLE_MS;
		int fireX = shipLeft(centerX, shipWidth, fired, sliding)
				+ shipWidth / 2;

		int[] enemyX = { centerX - FX_ENEMY_GAP, centerX,
				centerX + FX_ENEMY_GAP };
		int target = 0;
		for (int i = 1; i < enemyX.length; i++)
			if (Math.abs(enemyX[i] - fireX) < Math.abs(enemyX[target] - fireX))
				target = i;
		int[] size = this.drawManager.getSpriteSize(SpriteType.EnemyShipA1);
		int enemyW = size[0] * FX_SMALL_PIXEL;
		int enemyH = size[1] * FX_SMALL_PIXEL;
		boolean hit = k >= FX_FLY_PART && k < FX_RESPAWN_PART;
		boolean shocked = hit && lightning;

		for (int i = 0; i < enemyX.length; i++) {
			if (i == target && hit)
				continue;
			int jitter = shocked ? (int) (now / 40 % 3) - 1 : 0;
			this.drawManager.drawSpriteScaled(SpriteType.EnemyShipA1,
					enemyX[i] - enemyW / 2 + jitter, enemyY, FX_SMALL_PIXEL,
					shocked ? new Color(200, 230, 255) : Color.WHITE);
		}

		int hitY = enemyY + enemyH / 2;
		if (k < FX_FLY_PART) {
			int startY = shipY - 8;
			int bulletY = startY - (int) (k / FX_FLY_PART
					* (startY - enemyY - enemyH));
			drawBullet(fireX, bulletY, shipY, comet);
		} else if (hit) {
			int tx = enemyX[target];
			if (k < FX_FLY_PART + 0.08f)
				this.drawManager.fillBox(tx - 8, hitY - 6, 16, 12,
						new Color(255, 240, 200, 200));
			if (lightning && (now / 50) % 3 != 2)
				for (int i = 0; i < enemyX.length; i++)
					if (i != target)
						drawBolt(tx, hitY, enemyX[i], hitY);
		}
	}

	/** One player bullet, with a comet tail when that effect is on. */
	private void drawBullet(final int x, final int y, final int shipY,
			final boolean comet) {
		int[] size = this.drawManager.getSpriteSize(SpriteType.Bullet);
		int w = size[0] * FX_SMALL_PIXEL;
		int h = size[1] * FX_SMALL_PIXEL;
		int left = x - w / 2;
		if (comet) {
			for (int i = FX_TRAIL.length; i >= 1; i--) {
				int trailY = y + h + (i - 1) * 4;
				if (trailY + 4 > shipY - 2)
					continue;
				Color c = FX_TRAIL[i - 1];
				int alpha = (int) (200 * (1f - i / (FX_TRAIL.length + 1f)));
				int tw = Math.max(2, 6 - i);
				this.drawManager.fillBox(x - tw / 2, trailY, tw, 4, new Color(
						c.getRed(), c.getGreen(), c.getBlue(), alpha));
			}
			this.drawManager.fillBox(left - 2, y - 2, w + 4, h + 4,
					new Color(255, 176, 64, 80));
			this.drawManager.drawSpriteScaled(SpriteType.Bullet, left, y,
					FX_SMALL_PIXEL, new Color(255, 246, 208));
		} else {
			this.drawManager.drawSpriteScaled(SpriteType.Bullet, left, y,
					FX_SMALL_PIXEL, Color.WHITE);
		}
	}

	/** Jagged pixel bolt between two points: glow plus bright core. */
	private void drawBolt(final int x1, final int y1, final int x2, final int y2) {
		Color glow = new Color(FX_BOLT.getRed(), FX_BOLT.getGreen(),
				FX_BOLT.getBlue(), 90);
		int steps = 8;
		int px = x1;
		int py = y1;
		for (int i = 1; i <= steps; i++) {
			int nx = x1 + (x2 - x1) * i / steps;
			int ny = y1 + (y2 - y1) * i / steps
					+ (i < steps ? (int) (Math.random() * 9) - 4 : 0);
			for (int s = 0; s <= 4; s++) {
				int bx = px + (nx - px) * s / 4;
				int by = py + (ny - py) * s / 4;
				this.drawManager.fillBox(bx - 2, by - 2, 4, 4, glow);
				this.drawManager.fillBox(bx, by, 1, 1, Color.WHITE);
			}
			px = nx;
			py = ny;
		}
	}

	/** @return Left edge of the ship at a time; it slides if asked. */
	private static int shipLeft(final int centerX, final int shipWidth,
			final long time, final boolean sliding) {
		int left = centerX - shipWidth / 2;
		if (!sliding)
			return left;
		double phase = (time % FX_SLIDE_CYCLE_MS) / (double) FX_SLIDE_CYCLE_MS
				* 2 * Math.PI;
		double s = Math.max(-1, Math.min(1, Math.sin(phase) * 1.4));
		return left + (int) Math.round(s * FX_SLIDE_SWING);
	}

	/** @return Ship colour at a time, darkened when locked. */
	private static Color shipColor(final Skin skin, final long time,
			final boolean dim) {
		Color color = skin.colorAt(time);
		return dim ? color.darker().darker() : color;
	}

	private void drawRow(final Row which) {
		Rectangle area = rowArea(which);
		boolean active = which == this.row;
		boolean unlocked = isUnlocked(which);
		Color accent = unlocked ? Color.GREEN : LOCKED;

		if (active)
			drawManager.fillBox(area.x, area.y, area.width, area.height,
					new Color(accent.getRed(), accent.getGreen(),
							accent.getBlue(), 40));
		drawManager.drawBox(area.x, area.y, area.width, area.height,
				active ? accent : BORDER);

		int baseline = area.y + 20;
		Color arrowColor = active ? accent : Color.GRAY;
		drawManager.drawRegularString("<", area.x + 10, baseline, arrowColor);
		drawManager.drawRegularString(">", area.x + area.width - 10
						- drawManager.getRegularStringWidth(">"), baseline,
				arrowColor);
		drawManager.drawSmallString(which.label.toUpperCase(),
				area.x + ARROW_WIDTH + 4, area.y + 18, Color.GRAY);

		String name = choiceName(which);
		Color nameColor = !unlocked ? Color.GRAY
				: active ? Color.GREEN : Color.WHITE;
		if (which == Row.EFFECT && browsedEffect() != null) {
			drawEffectRowName(area, name, nameColor, active);
			return;
		}
		drawManager.drawRegularString(name, area.x + area.width / 2
						- drawManager.getRegularStringWidth(name) / 2 + 16, baseline,
				nameColor);
	}

	/**
	 * Effect row: the name, then an ON / OFF / LOCKED tag at the end.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * @param area
	 *            Row area.
	 * @param name
	 *            Effect name.
	 * @param nameColor
	 *            Colour of the name.
	 * @param active
	 *            True if the cursor is on the row.
	 */
	private void drawEffectRowName(final Rectangle area, final String name,
			final Color nameColor, final boolean active) {
		ShopItem effect = browsedEffect();
		boolean owned = PurchaseManager.getInstance().isOwned(effect);
		boolean on = PurchaseManager.getInstance().isEffectOn(effect);
		String tag = !owned ? "LOCKED" : on ? "ON" : "OFF";
		Color tagColor = !owned ? LOCKED : on ? Color.GREEN : Color.GRAY;

		int tagWidth = Math.max(drawManager.getSmallStringWidth("LOCKED"),
				drawManager.getSmallStringWidth(tag)) + 10;
		int tagHeight = drawManager.getSmallLineHeight() + 4;
		int tagX = area.x + area.width - ARROW_WIDTH - 2 - tagWidth;
		int tagY = area.y + (area.height - tagHeight) / 2;
		if (on)
			drawManager.fillBox(tagX, tagY, tagWidth, tagHeight, Color.GREEN);
		else
			drawManager.drawBox(tagX, tagY, tagWidth, tagHeight,
					active ? tagColor : BORDER);
		drawManager.drawSmallString(tag, tagX + (tagWidth
				- drawManager.getSmallStringWidth(tag)) / 2, tagY + tagHeight
				- 5, on ? Color.BLACK : tagColor);

		// Centre the name between the row label and the tag.
		int left = area.x + ARROW_WIDTH + 4
				+ drawManager.getSmallStringWidth(Row.EFFECT.label
						.toUpperCase()) + 6;
		int right = tagX - 6;
		int baseline = area.y + 20;
		if (drawManager.getRegularStringWidth(name) <= right - left)
			drawManager.drawRegularString(name, (left + right) / 2
					- drawManager.getRegularStringWidth(name) / 2, baseline,
					nameColor);
		else
			drawManager.drawSmallString(name, (left + right) / 2
					- drawManager.getSmallStringWidth(name) / 2,
					area.y + 18, nameColor);
	}

	/** Ship stats, then what the selected row's choice means. */
	private void drawInfo() {
		Rectangle last = rowArea(Row.EFFECT);
		int lineHeight = drawManager.getSmallLineHeight();
		int y = last.y + last.height + 22;

		ShipType type = this.availableShips[this.shipIndex];
		double fireRate = type.getBulletsPerShot() * 1000.0
				/ type.getShootingInterval();
		drawStat("SPEED", type.getSpeed() / MAX_SPEED,
				Integer.toString(type.getSpeed()), y);
		drawStat("SHOTS PER SEC", fireRate / MAX_FIRE_RATE,
				String.format("%.2f", fireRate), y + lineHeight + 4);
		drawStat("BULLETS", type.getBulletsPerShot() / 2.0,
				Integer.toString(type.getBulletsPerShot()),
				y + (lineHeight + 4) * 2);

		y += (lineHeight + 4) * 3 + 14;
		String status;
		Color color;
		if (this.row == Row.EFFECT && browsedEffect() != null
				&& isUnlocked(this.row)) {
			// AUTHORED BY: VFX TEAM (Effection)
			ShopItem effect = browsedEffect();
			boolean on = PurchaseManager.getInstance().isEffectOn(effect);
			status = effect.getName() + (on
					? " is on - space to switch off"
					: " is off - space to switch on");
			color = on ? Color.GREEN : Color.GRAY;
		} else if (this.row == Row.UPGRADE && isUnlocked(this.row)) {
			boolean on = PurchaseManager.getInstance().isUpgradeOn(
					browsedUpgrade().getRef());
			status = browsedUpgrade().getName() + (on
					? " is on - space to switch off"
					: " is off - space to switch on");
			color = on ? Color.GREEN : Color.GRAY;
		} else if (isUnlocked(this.row)) {
			status = this.row == Row.UPGRADE ? "No upgrades in the shop"
					: this.row == Row.EFFECT ? "No effects in the shop yet"
					: choiceName(this.row) + " is equipped";
			color = browsedEffect() == null && this.row == Row.EFFECT
					? Color.GRAY : Color.GREEN;
		} else {
			ShopItem entry = shopEntry(this.row);
			if (entry != null && !ShopCatalog.isOnSale(entry,
					java.time.LocalDate.now()))
				status = "Limited edition - no longer sold";
			else
				status = "Locked - buy it in the Shop"
						+ (entry == null ? "" : " for " + priceText(entry));
			color = LOCKED;
		}
		if (this.row == Row.SKIN
				&& this.availableSkins[this.skinIndex].isLimited()) {
			String tag = "LIMITED EDITION";
			drawManager.drawSmallString(tag, this.width / 2
							- drawManager.getSmallStringWidth(tag) / 2,
					y - lineHeight - 4, new Color(255, 120, 200));
		}
		status = status.toUpperCase();
		drawManager.drawSmallString(status, this.width / 2
				- drawManager.getSmallStringWidth(status) / 2, y, color);

		if (this.row == Row.UPGRADE || this.row == Row.EFFECT) {
			ShopItem upgrade = this.row == Row.UPGRADE ? browsedUpgrade()
					: browsedEffect();
			if (upgrade != null) {
				int lineY = y + lineHeight + 4;
				for (String line : wrap(upgrade.getDescription().toUpperCase(),
						this.width - MARGIN * 2)) {
					drawManager.drawSmallString(line, this.width / 2
									- drawManager.getSmallStringWidth(line) / 2,
							lineY, Color.GRAY);
					lineY += lineHeight;
				}
			}
		}
	}

	/**
	 * @param entry
	 *            Shop entry.
	 * @return Its price in every currency it takes, e.g. "50 coins or 25
	 *         diamonds".
	 */
	private static String priceText(final ShopItem entry) {
		StringBuilder text = new StringBuilder();
		for (Currency currency : Currency.values()) {
			int price = PurchaseManager.getInstance().getPrice(entry,
					currency);
			if (price <= 0)
				continue;
			if (text.length() > 0)
				text.append(" or ");
			text.append(price).append(' ').append(currency.getLabel());
		}
		return text.toString();
	}

	private void drawStat(final String label, final double ratio,
						  final String value, final int baseline) {
		int barLeft = MARGIN + 120;
		int barWidth = this.width - MARGIN * 2 - 120 - 40;
		drawManager.drawSmallString(label, MARGIN + 4, baseline, Color.GRAY);
		drawManager.drawBox(barLeft, baseline - 8, barWidth, 8, BORDER);
		drawManager.fillBox(barLeft + 1, baseline - 7,
				(int) Math.round((barWidth - 1) * Math.min(1, ratio)), 7,
				Color.GREEN);
		drawManager.drawSmallString(value, barLeft + barWidth + 8, baseline,
				Color.WHITE);
	}

	/**
	 * Splits text into lines that fit a width in the small font.
	 *
	 * @param text
	 *            Text to split.
	 * @param maxWidth
	 *            Widest a line may be.
	 * @return Lines, in order.
	 */
	private List<String> wrap(final String text, final int maxWidth) {
		List<String> lines = new ArrayList<String>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split(" ")) {
			String candidate = line.length() == 0 ? word : line + " " + word;
			if (line.length() > 0
					&& drawManager.getSmallStringWidth(candidate) > maxWidth) {
				lines.add(line.toString());
				line = new StringBuilder(word);
			} else {
				line = new StringBuilder(candidate);
			}
		}
		if (line.length() > 0)
			lines.add(line.toString());
		return lines;
	}

	private static double maxSpeed() {
		double max = 1;
		for (ShipType type : ShipType.values())
			max = Math.max(max, type.getSpeed());
		return max;
	}

	private static double maxFireRate() {
		double max = 0.1;
		for (ShipType type : ShipType.values())
			max = Math.max(max, type.getBulletsPerShot() * 1000.0
					/ type.getShootingInterval());
		return max;
	}
}
