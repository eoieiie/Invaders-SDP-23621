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
import shop.Skin;

/**
 * Implements the ship select screen, shown as "Customize" in the menu.
 * The player chooses the ship and skin for the next run from what they
 * own, and switches bought upgrades on or off (any number can be on);
 * locked choices can be looked at but not used, and point to the shop.
 * The ship goes through Core.setSelectedShipType() (Mantis), the skin and
 * upgrades through {@link PurchaseManager}, as soon as they are chosen.
 */
public class ShipSelectScreen extends Screen {

	/** The three things that can be chosen. */
	private enum Row {
		/** Ship type. */
		SHIP("Ship"),
		/** Ship colour. */
		SKIN("Skin"),
		/** Item team upgrades, browsed and switched on/off one by one. */
		UPGRADE("Upgrade");

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
	private static final int ROW_SPACING = 38;
	/** Width of the clickable arrow at each end of a row. */
	private static final int ARROW_WIDTH = 28;
	/** Size of one sprite pixel in the big preview. */
	private static final int PREVIEW_PIXEL = 5;
	/** Colour of locked choices. */
	private static final Color LOCKED = new Color(255, 70, 70);
	/** Colour of unselected row borders. */
	private static final Color BORDER = new Color(70, 70, 70);
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

	/** Row the cursor is on. */
	private Row row = Row.SHIP;
	/** Choice shown in each row, which may be locked. */
	private int shipIndex;
	private int skinIndex;
	private int upgradeIndex;
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
		// Start on the first upgrade the player owns, if any.
		for (int i = this.availableUpgrades.size() - 1; i >= 0; i--)
			if (loadout.isOwned(this.availableUpgrades.get(i)))
				this.upgradeIndex = i;
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
			break;
		case SKIN:
			this.skinIndex = Math.floorMod(this.skinIndex + step,
					this.availableSkins.length);
			loadout.selectSkin(this.availableSkins[this.skinIndex]);
			break;
		default:
			// Browsing only: upgrades are switched with space or a click.
			if (!this.availableUpgrades.isEmpty())
				this.upgradeIndex = Math.floorMod(this.upgradeIndex + step,
						this.availableUpgrades.size());
			break;
		}
	}

	/** Switches the browsed upgrade on or off, if it is owned. */
	private void toggleUpgrade() {
		ShopItem upgrade = browsedUpgrade();
		if (upgrade != null && PurchaseManager.getInstance().isOwned(upgrade))
			PurchaseManager.getInstance().toggleUpgrade(upgrade.getRef());
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
		case SKIN:
			return loadout.isSkinUnlocked(this.availableSkins[this.skinIndex]);
		default:
			return loadout.isOwned(browsedUpgrade());
		}
	}

	private String choiceName(final Row which) {
		switch (which) {
		case SHIP:
			return ShopCatalog.shipName(this.availableShips[this.shipIndex]);
		case SKIN:
			return this.availableSkins[this.skinIndex].getDisplayName();
		default:
			ShopItem upgrade = browsedUpgrade();
			if (upgrade == null)
				return "None";
			PurchaseManager loadout = PurchaseManager.getInstance();
			if (!loadout.isOwned(upgrade))
				return upgrade.getName();
			return upgrade.getName() + " Lv" + loadout.getQuantity(upgrade)
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
		case SKIN:
			return ShopCatalog.findByRef(ShopCategory.SKIN,
					this.availableSkins[this.skinIndex].name());
		default:
			return browsedUpgrade();
		}
	}

	// --- Layout ---

	private int previewTop() {
		return this.height / 8 + 24;
	}

	private Rectangle rowArea(final Row which) {
		int top = previewTop() + 84 + which.ordinal() * ROW_SPACING;
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

		drawManager.drawKeyHints(this, this.row == Row.UPGRADE
				? "up/down row | left/right browse | space on/off | esc back"
				: "up/down row | left/right change | esc back");

		drawManager.completeDrawing(this);
	}

	/** The browsed ship, in the browsed skin, drawn large. */
	private void drawPreview() {
		ShipType type = this.availableShips[this.shipIndex];
		Color color = this.availableSkins[this.skinIndex]
				.colorAt(System.currentTimeMillis());
		if (!isUnlocked(Row.SHIP) || !isUnlocked(Row.SKIN))
			color = color.darker().darker();
		SpriteType sprite = type.getIdleSprite();
		int[] size = drawManager.getSpriteSize(sprite);
		drawManager.drawSpriteScaled(sprite, this.width / 2 - size[0]
				* PREVIEW_PIXEL / 2, previewTop() + 10, PREVIEW_PIXEL, color);
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
		drawManager.drawRegularString(name, area.x + area.width / 2
				- drawManager.getRegularStringWidth(name) / 2 + 16, baseline,
				nameColor);
	}

	/** Ship stats, then what the selected row's choice means. */
	private void drawInfo() {
		Rectangle last = rowArea(Row.UPGRADE);
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
		if (this.row == Row.UPGRADE && isUnlocked(this.row)) {
			boolean on = PurchaseManager.getInstance().isUpgradeOn(
					browsedUpgrade().getRef());
			status = browsedUpgrade().getName() + (on
					? " is on - space to switch off"
					: " is off - space to switch on");
			color = on ? Color.GREEN : Color.GRAY;
		} else if (isUnlocked(this.row)) {
			status = this.row == Row.UPGRADE ? "No upgrades in the shop"
					: choiceName(this.row) + " is equipped";
			color = Color.GREEN;
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

		if (this.row == Row.UPGRADE) {
			ShopItem upgrade = browsedUpgrade();
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
