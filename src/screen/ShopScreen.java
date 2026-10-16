package screen;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import engine.CurrencyManager;
import engine.DiamondManager;
import engine.DrawManager.SpriteType;
import engine.PurchaseManager;
import engine.PurchaseManager.PurchaseResult;
import entity.Ship.ShipType;
import item.ItemAPI;
import item.ItemSystem;
import shop.Currency;
import shop.NightMarket;
import shop.ShopCatalog;
import shop.ShopCategory;
import shop.ShopItem;
import shop.Skin;

/**
 * The shop (GoG - Currency System). Four tabs: Ships, Skins, Upgrades and
 * the Night Market. The left side lists what is for sale, the right side
 * shows the selected entry with its price and BUY buttons.
 *
 * Skins and upgrades can be paid with coins or diamonds, so they get one
 * BUY button per currency. Upgrades can be bought again to level them up.
 *
 * Buying happens here; choosing what to use happens in Customize. Both go
 * through {@link PurchaseManager}, so they always agree.
 *
 * Keyboard: left/right switch tabs, up/down select, C (or space/enter) buy
 * with coins, V buy with diamonds, esc back. Mouse: click a tab, an entry,
 * the scroll arrows or a BUY button.
 *
 * @author GoG - Currency System
 */
public class ShopScreen extends Screen {

	/** The shop's tabs, each with its own accent colour. */
	private enum Tab {
		/** Player ships. */
		SHIPS("Ships", Color.GREEN),
		/** Ship skins. */
		SKINS("Skins", Color.CYAN),
		/** Item team upgrades. */
		UPGRADES("Upgrades", new Color(255, 200, 0)),
		/** Today's discounts. */
		NIGHT_MARKET("Night Mkt", new Color(255, 80, 255));

		/** Text on the tab. */
		private final String title;
		/** Accent colour of the tab and its selection. */
		private final Color color;

		Tab(final String title, final Color color) {
			this.title = title;
			this.color = color;
		}
	}

	/** One row in the list: an entry and whether it is discounted here. */
	private static final class Entry {
		/** Entry for sale. */
		private final ShopItem item;
		/** Whether this row sells at the Night Market discount. */
		private final boolean discounted;

		Entry(final ShopItem item, final boolean discounted) {
			this.item = item;
			this.discounted = discounted;
		}

		/**
		 * @param currency
		 *            Currency to pay with.
		 * @return Price of the next purchase on this row, 0 if not sold for
		 *         that currency or already maxed.
		 */
		int price(final Currency currency) {
			return PurchaseManager.getInstance().getPrice(this.item,
					currency, this.discounted);
		}
	}

	/** Space between the screen edge and the panels. */
	private static final int MARGIN = 16;
	/** Height of the tab bar. */
	private static final int TAB_HEIGHT = 20;
	/** Height of one row in the list. */
	private static final int ROW_HEIGHT = 30;
	/** Width of the list on the left. */
	private static final int LIST_WIDTH = 188;
	/** Gap between the list and the detail panel. */
	private static final int PANEL_GAP = 8;
	/** Width of the scroll arrows column. */
	private static final int SCROLL_WIDTH = 12;
	/** How long a purchase message stays on screen. */
	private static final long MESSAGE_MILLIS = 3000;
	/** Colour of prices the player cannot afford. */
	private static final Color TOO_EXPENSIVE = new Color(255, 70, 70);
	/** Colour of unselected panel borders. */
	private static final Color BORDER = new Color(70, 70, 70);
	/** Colour of the LIMITED tag. */
	private static final Color LIMITED = new Color(255, 120, 200);

	/** Keys watched for presses (not holds). */
	private static final int[] KEYS = { KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
			KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_A, KeyEvent.VK_D,
			KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_SPACE,
			KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE, KeyEvent.VK_C,
			KeyEvent.VK_V };

	/** Tab shown. */
	private Tab tab = Tab.SHIPS;
	/** Rows of the tab shown. */
	private List<Entry> entries = new ArrayList<Entry>();
	/** Selected row. */
	private int selected;
	/** First row shown, for scrolling. */
	private int scroll;
	/** Last purchase message, and when and in what colour it was set. */
	private String message;
	private long messageTime;
	private Color messageColor = Color.WHITE;
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
	public ShopScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
		this.returnCode = 1;
		showTab(Tab.SHIPS);
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
			// Keys and clicks still held from the menu must not buy anything.
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

	/**
	 * @param key
	 *            Key code from {@link #KEYS}.
	 * @return true if the key went down this frame.
	 */
	private boolean pressed(final int key) {
		for (int i = 0; i < KEYS.length; i++)
			if (KEYS[i] == key)
				return this.inputManager.isKeyDown(key)
						&& !this.keysWereDown[i];
		return false;
	}

	/** Reacts to key presses. */
	private void handleKeys() {
		if (pressed(KeyEvent.VK_ESCAPE)) {
			this.isRunning = false;
		} else if (pressed(KeyEvent.VK_LEFT) || pressed(KeyEvent.VK_A)) {
			showTab(Tab.values()[Math.floorMod(this.tab.ordinal() - 1,
					Tab.values().length)]);
		} else if (pressed(KeyEvent.VK_RIGHT) || pressed(KeyEvent.VK_D)) {
			showTab(Tab.values()[(this.tab.ordinal() + 1)
					% Tab.values().length]);
		} else if (pressed(KeyEvent.VK_UP) || pressed(KeyEvent.VK_W)) {
			select(this.selected - 1);
		} else if (pressed(KeyEvent.VK_DOWN) || pressed(KeyEvent.VK_S)) {
			select(this.selected + 1);
		} else if (pressed(KeyEvent.VK_SPACE) || pressed(KeyEvent.VK_ENTER)) {
			buySelected(null);
		} else if (pressed(KeyEvent.VK_C)) {
			buySelected(Currency.COINS);
		} else if (pressed(KeyEvent.VK_V)) {
			buySelected(Currency.DIAMONDS);
		}
		for (int i = 0; i < KEYS.length; i++)
			this.keysWereDown[i] = this.inputManager.isKeyDown(KEYS[i]);
	}

	/** Reacts to a mouse click on a tab, a row, a scroll arrow or BUY. */
	private void handleMouse() {
		boolean down = this.inputManager.isMousePressed();
		boolean clicked = down && !this.mouseWasDown;
		this.mouseWasDown = down;
		if (!clicked)
			return;

		int x = this.inputManager.getMouseX();
		int y = this.inputManager.getMouseY();
		for (Tab each : Tab.values())
			if (tabArea(each).contains(x, y)) {
				showTab(each);
				return;
			}
		for (int row = 0; row < visibleRows(); row++)
			if (this.scroll + row < this.entries.size()
					&& rowArea(row).contains(x, y)) {
				select(this.scroll + row);
				return;
			}
		if (scrollUpArea().contains(x, y)) {
			select(this.scroll - 1);
			return;
		}
		if (scrollDownArea().contains(x, y)) {
			select(this.scroll + visibleRows());
			return;
		}
		if (this.entries.isEmpty())
			return;
		ShopItem item = this.entries.get(this.selected).item;
		for (Currency currency : Currency.values())
			if (buyArea(item, currency).contains(x, y)) {
				buySelected(currency);
				return;
			}
	}

	/**
	 * Switches tab and selects its first row.
	 *
	 * @param newTab
	 *            Tab to show.
	 */
	private void showTab(final Tab newTab) {
		this.tab = newTab;
		this.entries = buildEntries(newTab);
		this.selected = 0;
		this.scroll = 0;
	}

	/**
	 * Selects a row, keeping it inside the list and on screen.
	 *
	 * @param row
	 *            Row to select.
	 */
	private void select(final int row) {
		if (this.entries.isEmpty())
			return;
		this.selected = Math.max(0, Math.min(this.entries.size() - 1, row));
		if (this.selected < this.scroll)
			this.scroll = this.selected;
		if (this.selected >= this.scroll + visibleRows())
			this.scroll = this.selected - visibleRows() + 1;
	}

	/**
	 * @param shown
	 *            Tab to list.
	 * @return Its rows, in shop order.
	 */
	private static List<Entry> buildEntries(final Tab shown) {
		List<Entry> result = new ArrayList<Entry>();
		switch (shown) {
		case SHIPS:
			addAll(result, ShopCatalog.getByCategory(ShopCategory.SHIP));
			break;
		case SKINS:
			addAll(result, ShopCatalog.getByCategory(ShopCategory.SKIN));
			break;
		case UPGRADES:
			addAll(result, ShopCatalog.getByCategory(ShopCategory.UPGRADE));
			break;
		default:
			for (ShopItem item : NightMarket.getListings())
				result.add(new Entry(item, true));
			break;
		}
		return result;
	}

	private static void addAll(final List<Entry> result,
			final List<ShopItem> items) {
		for (ShopItem item : items)
			result.add(new Entry(item, false));
	}

	/**
	 * Buys the selected row and shows what happened.
	 *
	 * @param chosen
	 *            Currency to pay with, or null for coins when accepted,
	 *            otherwise diamonds.
	 */
	private void buySelected(final Currency chosen) {
		if (this.entries.isEmpty())
			return;
		Entry entry = this.entries.get(this.selected);
		ShopItem item = entry.item;
		Currency currency = chosen;
		if (currency == null)
			currency = item.accepts(Currency.COINS) ? Currency.COINS
					: Currency.DIAMONDS;
		PurchaseManager purchases = PurchaseManager.getInstance();
		int price = entry.price(currency);
		PurchaseResult result = purchases.purchase(item, currency,
				entry.discounted);
		switch (result) {
		case SUCCESS:
			showMessage((item.isRepeatable() ? item.getName() + " is now"
					+ " level " + purchases.getQuantity(item) + "! "
					: "Bought " + item.getName() + "! ") + afterBuying(item),
					Color.GREEN);
			break;
		case INSUFFICIENT_FUNDS:
			showMessage("Not enough " + currency.getLabel() + " (need "
					+ price + ").", TOO_EXPENSIVE);
			break;
		case ALREADY_OWNED:
			showMessage("You already own this.", TOO_EXPENSIVE);
			break;
		case MAX_LEVEL:
			showMessage("Already at max level.", TOO_EXPENSIVE);
			break;
		case CURRENCY_NOT_ACCEPTED:
			showMessage("Can't be paid with " + currency.getLabel() + ".",
					TOO_EXPENSIVE);
			break;
		case NOT_ON_SALE:
			// A Night Market deal can end at midnight while the shop is open.
			showMessage(entry.discounted ? "Today's deal has ended."
					: "This limited edition is no longer sold.", TOO_EXPENSIVE);
			break;
		default:
			showMessage("Purchase failed, nothing was charged.",
					TOO_EXPENSIVE);
			break;
		}
	}

	/**
	 * @param item
	 *            Entry just bought.
	 * @return Where to find it now.
	 */
	private static String afterBuying(final ShopItem item) {
		if (item.getCategory() == ShopCategory.UPGRADE)
			return "It is on.";
		return "Choose it in Customize.";
	}

	private void showMessage(final String text, final Color color) {
		this.message = text;
		this.messageColor = color;
		this.messageTime = System.currentTimeMillis();
	}

	// --- Layout. Drawing and clicking both use these, so they line up. ---

	private int balanceBaseline() {
		return this.height / 8 + 28;
	}

	private int tabTop() {
		return balanceBaseline() + 10;
	}

	private int bodyTop() {
		return tabTop() + TAB_HEIGHT + 8;
	}

	private int bodyBottom() {
		return this.height - 70;
	}

	private int visibleRows() {
		return Math.max(1, (bodyBottom() - bodyTop()) / ROW_HEIGHT);
	}

	private Rectangle tabArea(final Tab each) {
		int count = Tab.values().length;
		int tabWidth = (this.width - MARGIN * 2) / count;
		return new Rectangle(MARGIN + each.ordinal() * tabWidth, tabTop(),
				tabWidth, TAB_HEIGHT);
	}

	private Rectangle rowArea(final int row) {
		return new Rectangle(MARGIN, bodyTop() + row * ROW_HEIGHT,
				LIST_WIDTH - SCROLL_WIDTH - 2, ROW_HEIGHT - 3);
	}

	private Rectangle scrollUpArea() {
		return new Rectangle(MARGIN + LIST_WIDTH - SCROLL_WIDTH, bodyTop(),
				SCROLL_WIDTH, ROW_HEIGHT);
	}

	private Rectangle scrollDownArea() {
		return new Rectangle(MARGIN + LIST_WIDTH - SCROLL_WIDTH,
				bodyTop() + (visibleRows() - 1) * ROW_HEIGHT, SCROLL_WIDTH,
				ROW_HEIGHT);
	}

	private Rectangle detailArea() {
		int left = MARGIN + LIST_WIDTH + PANEL_GAP;
		return new Rectangle(left, bodyTop(), this.width - MARGIN - left,
				visibleRows() * ROW_HEIGHT - 3);
	}

	/** @return Area of the BUY button(s) row. */
	private Rectangle buyRow() {
		Rectangle detail = detailArea();
		return new Rectangle(detail.x + 12, detail.y + detail.height - 32,
				detail.width - 24, 22);
	}

	/**
	 * BUY button for one currency: the whole row when the entry takes one
	 * currency, half of it when it takes both (coins left, diamonds right).
	 *
	 * @param item
	 *            Entry shown.
	 * @param currency
	 *            Currency of the button.
	 * @return Its area, or an empty one if there is no such button.
	 */
	private Rectangle buyArea(final ShopItem item, final Currency currency) {
		Rectangle row = buyRow();
		if (!item.accepts(currency))
			return new Rectangle();
		if (!item.accepts(Currency.COINS) || !item.accepts(Currency.DIAMONDS))
			return row;
		int half = (row.width - 6) / 2;
		return currency == Currency.COINS
				? new Rectangle(row.x, row.y, half, row.height)
				: new Rectangle(row.x + row.width - half, row.y, half,
						row.height);
	}

	// --- Drawing ---

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawScreenTitle(this, MenuItem.SHOP.getTitle());
		drawBalances();
		drawTabs();
		drawList();
		drawDetail();
		drawMessage();
		this.drawManager.drawKeyHints(this,
				"arrows/click select | C coins | V diamonds | esc back");
		this.drawManager.completeDrawing(this);
	}

	/** Coins on the left, diamonds on the right, under the title. */
	private void drawBalances() {
		int baseline = balanceBaseline();
		String coins = Integer.toString(CurrencyManager.getInstance()
				.getCoins());
		String diamonds = Integer.toString(DiamondManager.getInstance()
				.getDiamonds());
		this.drawManager.drawCurrencyIcon(MARGIN, baseline, false, 12);
		this.drawManager.drawRegularString(coins, MARGIN + 18, baseline,
				Color.WHITE);
		int diamondsWidth = this.drawManager.getRegularStringWidth(diamonds);
		int right = this.width - MARGIN;
		this.drawManager.drawCurrencyIcon(right - diamondsWidth - 18,
				baseline, true, 12);
		this.drawManager.drawRegularString(diamonds, right - diamondsWidth,
				baseline, Color.WHITE);
	}

	private void drawTabs() {
		for (Tab each : Tab.values()) {
			Rectangle area = tabArea(each);
			boolean active = each == this.tab;
			if (active)
				this.drawManager.fillBox(area.x, area.y, area.width,
						area.height, tint(each.color));
			this.drawManager.drawBox(area.x, area.y, area.width - 1,
					area.height - 1, active ? each.color : BORDER);
			String title = each.title.toUpperCase();
			this.drawManager.drawSmallString(title, area.x + (area.width
					- this.drawManager.getSmallStringWidth(title)) / 2,
					area.y + 14, active ? each.color : Color.GRAY);
		}
	}

	private void drawList() {
		if (this.entries.isEmpty()) {
			this.drawManager.drawSmallString("Nothing for sale here.",
					MARGIN + 6, bodyTop() + 18, Color.GRAY);
			return;
		}
		PurchaseManager purchases = PurchaseManager.getInstance();
		for (int row = 0; row < visibleRows()
				&& this.scroll + row < this.entries.size(); row++) {
			int index = this.scroll + row;
			Entry entry = this.entries.get(index);
			Rectangle area = rowArea(row);
			boolean isSelected = index == this.selected;
			boolean soldOut = purchases.isMaxed(entry.item);

			if (isSelected)
				this.drawManager.fillBox(area.x, area.y, area.width,
						area.height, tint(this.tab.color));
			this.drawManager.drawBox(area.x, area.y, area.width - 1,
					area.height - 1, isSelected ? this.tab.color : BORDER);

			Color nameColor = isSelected ? this.tab.color
					: soldOut ? Color.GRAY : Color.WHITE;
			this.drawManager.drawSmallString(entry.item.getName()
					.toUpperCase(), area.x + 6, area.y + 12, nameColor);
			String tag = null;
			Color tagColor = Color.YELLOW;
			if (entry.discounted) {
				tag = "-" + NightMarket.DISCOUNT_PERCENT + "%";
			} else if (ShopCatalog.limitedSkin(entry.item) != null) {
				tag = "LIMITED";
				tagColor = LIMITED;
			}
			if (tag != null)
				this.drawManager.drawSmallString(tag, area.x + area.width - 6
						- this.drawManager.getSmallStringWidth(tag),
						area.y + 12, tagColor);

			int baseline = area.y + 24;
			if (soldOut) {
				this.drawManager.drawSmallString(entry.item.isRepeatable()
						? "MAX LEVEL" : "OWNED", area.x + 6, baseline,
						Color.GRAY);
			} else if (!ShopCatalog.isOnSale(entry.item, LocalDate.now())) {
				this.drawManager.drawSmallString("ENDED", area.x + 6,
						baseline, Color.GRAY);
			} else {
				int x = drawPrice(entry, Currency.COINS, area.x + 6,
						baseline);
				drawPrice(entry, Currency.DIAMONDS, x, baseline);
			}
			int quantity = purchases.getQuantity(entry.item);
			if (entry.item.isRepeatable() && quantity > 0) {
				String level = "LV " + quantity + "/"
						+ entry.item.getMaxLevel();
				this.drawManager.drawSmallString(level, area.x + area.width
						- 6 - this.drawManager.getSmallStringWidth(level),
						baseline, Color.GRAY);
			}
		}

		// Scroll arrows, only when the list does not fit.
		if (this.scroll > 0)
			drawArrow(scrollUpArea(), "^");
		if (this.scroll + visibleRows() < this.entries.size())
			drawArrow(scrollDownArea(), "v");
	}

	private void drawArrow(final Rectangle area, final String arrow) {
		this.drawManager.drawSmallString(arrow, area.x + 3,
				area.y + area.height / 2 + 4, this.tab.color);
	}

	/**
	 * Draws a currency icon and price, red when the player cannot afford it.
	 * Draws nothing if the row is not sold for that currency.
	 *
	 * @param entry
	 *            Row to draw the price of.
	 * @param currency
	 *            Currency of the price.
	 * @param left
	 *            Left edge.
	 * @param baseline
	 *            Text baseline.
	 * @return Left edge for whatever is drawn next.
	 */
	private int drawPrice(final Entry entry, final Currency currency,
			final int left, final int baseline) {
		int price = entry.price(currency);
		if (price <= 0)
			return left;
		boolean affordable = PurchaseManager.getInstance().canAfford(
				currency, price);
		String text = Integer.toString(price);
		this.drawManager.drawCurrencyIcon(left, baseline,
				currency == Currency.DIAMONDS, 8);
		this.drawManager.drawSmallString(text, left + 12, baseline,
				affordable ? Color.WHITE : TOO_EXPENSIVE);
		return left + 12 + this.drawManager.getSmallStringWidth(text) + 12;
	}

	/** Right-hand panel: preview, description, price and BUY. */
	private void drawDetail() {
		Rectangle area = detailArea();
		this.drawManager.drawBox(area.x, area.y, area.width, area.height,
				BORDER);
		if (this.entries.isEmpty())
			return;

		Entry entry = this.entries.get(this.selected);
		ShopItem item = entry.item;
		PurchaseManager purchases = PurchaseManager.getInstance();
		int centerX = area.x + area.width / 2;

		String name = item.getName();
		this.drawManager.drawRegularString(name, centerX
				- this.drawManager.getRegularStringWidth(name) / 2,
				area.y + 22, this.tab.color);

		drawPreview(item, centerX, area.y + 36);

		int lineHeight = this.drawManager.getSmallLineHeight();
		int y = area.y + 94;
		drawCentered(item.getCategory().getLabel().toUpperCase(), centerX, y,
				Color.GRAY);
		y += lineHeight + 4;
		for (String line : wrap(item.getDescription().toUpperCase(),
				area.width - 20)) {
			drawCentered(line, centerX, y, Color.WHITE);
			y += lineHeight;
		}
		y += 4;
		for (String line : extraLines(item)) {
			drawCentered(line, centerX, y, new Color(170, 170, 170));
			y += lineHeight;
		}

		// Price label above the buttons.
		int priceBaseline = buyRow().y - 10;
		boolean maxed = purchases.isMaxed(item);
		boolean ended = !maxed
				&& !ShopCatalog.isOnSale(item, LocalDate.now());
		String priceLabel = item.isRepeatable() && !maxed
				? "PRICE OF LEVEL " + (purchases.getQuantity(item) + 1)
				: "PRICE";
		this.drawManager.drawSmallString(priceLabel, area.x + 12,
				priceBaseline, Color.GRAY);
		if (entry.discounted && !maxed) {
			StringBuilder was = new StringBuilder("WAS");
			for (Currency currency : Currency.values())
				if (item.accepts(currency))
					was.append(was.length() > 3 ? " / " : " ").append(item
							.getPrice(currency, purchases.getQuantity(item)));
			String text = was.toString();
			this.drawManager.drawSmallString(text, area.x + area.width - 12
					- this.drawManager.getSmallStringWidth(text),
					priceBaseline, Color.GRAY);
		}

		// One finished button, or one BUY button per currency.
		if (maxed || ended) {
			Rectangle button = buyRow();
			String label = ended ? "SOLD OUT"
					: item.isRepeatable() ? "MAX LEVEL" : "OWNED";
			this.drawManager.drawBox(button.x, button.y, button.width,
					button.height, Color.DARK_GRAY);
			drawCentered(label, centerX, button.y + 15, Color.DARK_GRAY);
			return;
		}
		for (Currency currency : Currency.values()) {
			if (!item.accepts(currency))
				continue;
			Rectangle button = buyArea(item, currency);
			int price = entry.price(currency);
			boolean affordable = purchases.canAfford(currency, price);
			Color color = affordable ? this.tab.color : Color.GRAY;
			if (affordable)
				this.drawManager.fillBox(button.x, button.y, button.width,
						button.height, tint(this.tab.color));
			this.drawManager.drawBox(button.x, button.y, button.width,
					button.height, color);
			String label = "BUY " + price;
			int labelWidth = 12 + this.drawManager.getSmallStringWidth(label);
			int left = button.x + (button.width - labelWidth) / 2;
			this.drawManager.drawCurrencyIcon(left, button.y + 15,
					currency == Currency.DIAMONDS, 8);
			this.drawManager.drawSmallString(label, left + 12, button.y + 15,
					affordable ? color : TOO_EXPENSIVE);
		}
	}

	/**
	 * Big picture of the entry: the ship, the ship in the skin's colour, or
	 * an item badge in the Item System's colour for it.
	 *
	 * @param item
	 *            Entry to show.
	 * @param centerX
	 *            Horizontal centre.
	 * @param top
	 *            Top of the preview.
	 */
	private void drawPreview(final ShopItem item, final int centerX,
			final int top) {
		PurchaseManager purchases = PurchaseManager.getInstance();
		final int pixel = 3;
		switch (item.getCategory()) {
		case SHIP:
		case SKIN: {
			ShipType type = item.getCategory() == ShopCategory.SHIP
					? ShipType.valueOf(item.getRef())
					: purchases.getSelectedShip();
			// Animated skins move in the preview too.
			long now = System.currentTimeMillis();
			Color color = item.getCategory() == ShopCategory.SKIN
					? Skin.valueOf(item.getRef()).colorAt(now)
					: purchases.getSelectedSkin().colorAt(now);
			SpriteType sprite = type.getIdleSprite();
			int[] size = this.drawManager.getSpriteSize(sprite);
			this.drawManager.drawSpriteScaled(sprite, centerX - size[0]
					* pixel / 2, top + 6, pixel, color);
			break;
		}
		default: {
			Color color = itemColor(item);
			int badge = 30;
			int left = centerX - badge / 2;
			this.drawManager.fillBox(left, top, badge, badge, tint(color));
			this.drawManager.drawBox(left, top, badge, badge, color);
			String letter = item.getName().substring(0, 1);
			this.drawManager.drawRegularString(letter, centerX
					- this.drawManager.getRegularStringWidth(letter) / 2,
					top + 21, color);
			break;
		}
		}
	}

	/**
	 * Extra facts under the description: ship stats, how many items the
	 * player has, or how gear is used.
	 *
	 * @param item
	 *            Entry shown.
	 * @return Lines to draw.
	 */
	private static List<String> extraLines(final ShopItem item) {
		List<String> lines = new ArrayList<String>();
		PurchaseManager purchases = PurchaseManager.getInstance();
		switch (item.getCategory()) {
		case SHIP: {
			ShipType type = ShipType.valueOf(item.getRef());
			lines.add("SPEED " + type.getSpeed() + "  |  BULLETS "
					+ type.getBulletsPerShot());
			lines.add("SHOTS PER SEC " + String.format("%.2f",
					type.getBulletsPerShot() * 1000.0
							/ type.getShootingInterval()));
			break;
		}
		case SKIN: {
			Skin skin = ShopCatalog.limitedSkin(item);
			if (skin == null)
				break;
			long days = skin.daysLeft(LocalDate.now());
			lines.add(days == 0 ? "LIMITED EDITION - NO LONGER SOLD"
					: "LIMITED EDITION - " + days
							+ (days == 1 ? " DAY LEFT" : " DAYS LEFT"));
			break;
		}
		case UPGRADE: {
			if (!purchases.isOwned(item)) {
				lines.add("ONE EQUIPPED AT A TIME");
			} else {
				lines.add("OWNED - "
						+ (purchases.isUpgradeOn(item.getRef()) ? "ON"
								: "OFF"));
				lines.add("SWITCH ON/OFF IN CUSTOMIZE");
			}
			break;
		}
		default:
			break;
		}
		return lines;
	}

	/**
	 * @param item
	 *            Upgrade entry.
	 * @return The Item System's colour for it; light grey for upgrades,
	 *         which have no colour there.
	 */
	private static Color itemColor(final ShopItem item) {
		try {
			Color color = ItemSystem.colorOf(ItemAPI.EffectKind.valueOf(
					item.getRef().toUpperCase()));
			if (!Color.WHITE.equals(color))
				return color;
		} catch (IllegalArgumentException e) {
			// Not an Item System kind; use the default below.
		}
		return new Color(200, 200, 200);
	}

	private void drawMessage() {
		if (this.message == null || System.currentTimeMillis()
				- this.messageTime > MESSAGE_MILLIS)
			return;
		drawCentered(this.message.toUpperCase(), this.width / 2,
				bodyBottom() + 20, this.messageColor);
	}

	private void drawCentered(final String text, final int centerX,
			final int baseline, final Color color) {
		this.drawManager.drawSmallString(text, centerX
				- this.drawManager.getSmallStringWidth(text) / 2, baseline,
				color);
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
					&& this.drawManager.getSmallStringWidth(candidate)
							> maxWidth) {
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

	/**
	 * @param color
	 *            Accent colour.
	 * @return A dark, see-through version for filled highlights.
	 */
	private static Color tint(final Color color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(),
				45);
	}
}
