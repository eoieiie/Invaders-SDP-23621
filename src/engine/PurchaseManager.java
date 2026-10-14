package engine;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import entity.Ship.ShipType;
import item.ItemSystem;
import shop.Currency;
import shop.NightMarket;
import shop.ShopCatalog;
import shop.ShopCategory;
import shop.ShopItem;
import shop.ShopListener;
import shop.Skin;

/**
 * The one place that knows what the player has bought and what they are
 * using (GoG - Currency System). The Shop screen buys through here, the
 * Customize screen and the game read from here, and so do the other teams:
 *
 * <pre>
 * PurchaseManager shop = PurchaseManager.getInstance();
 * // Item team
 * shop.getActiveUpgradeLevel("magnet"); // level while on, 0 when off/not bought
 * shop.getActiveUpgrades();             // every upgrade that is on, with its level
 * // Skin team
 * shop.getSelectedSkin();               // CLASSIC = default look
 * // Anyone
 * shop.addListener(new ShopListener() { ... });
 * </pre>
 *
 * <ul>
 * <li>Ships and skins: bought once. The chosen ship and skin (one at a
 * time) are the player's loadout, used by GameScreen.</li>
 * <li>Upgrades: bought up to their max level. On as soon as they are
 * bought; the player can switch each one off and on in Customize. Any
 * number can be on at once.</li>
 * <li>Skins and upgrades can be paid with coins or diamonds.</li>
 * </ul>
 *
 * Everything is saved to its own "shop-owned" file next to the other save
 * files, so it cannot overwrite balances or the player profile. A change
 * that cannot be saved is undone (and a purchase refunded).
 *
 * @author GoG - Currency System
 */
public final class PurchaseManager {

	/** Outcome of a purchase attempt. */
	public enum PurchaseResult {
		/** Price deducted and the entry added. */
		SUCCESS,
		/** Not enough coins or diamonds; nothing changed. */
		INSUFFICIENT_FUNDS,
		/** Ship or skin that is already owned; nothing changed. */
		ALREADY_OWNED,
		/** Upgrade already at its max level; nothing changed. */
		MAX_LEVEL,
		/** The entry cannot be paid with that currency; nothing changed. */
		CURRENCY_NOT_ACCEPTED,
		/** Entry is null or not in the catalog; nothing changed. */
		UNKNOWN_ITEM,
		/** Limited edition or discount that is no longer on; nothing changed. */
		NOT_ON_SALE,
		/** The purchase could not be saved; the price was refunded. */
		SAVE_FAILED
	}

	/** Name of the file purchases and the loadout are saved to. */
	private static final String OWNED_FILE = "shop-owned";
	/** Save key prefix for owned entries and upgrade levels. */
	private static final String OWNED_PREFIX = "owned.";
	/** Save key of the chosen ship. */
	private static final String SHIP_KEY = "selected.ship";
	/** Save key of the chosen skin. */
	private static final String SKIN_KEY = "selected.skin";
	/** Save key of the upgrades the player switched off. */
	private static final String OFF_KEY = "upgrades.off";
	/** Save key set once old gear from the Item System save was copied. */
	private static final String MIGRATED_KEY = "upgrades.migrated";

	/** Singleton instance. */
	private static PurchaseManager instance;

	/** Shop id mapped to how many the player has (upgrade level). */
	private final Map<String, Integer> owned;
	/** Shop ids of bought upgrades the player switched off. */
	private final Set<String> upgradesOff;
	/** Ship used in the next run. */
	private ShipType selectedShip;
	/** Skin used in the next run. */
	private Skin selectedSkin;
	/** Teams told about changes. */
	private final List<ShopListener> listeners;
	/** Upgrade most recently bought or switched on, for the bridge. */
	private String lastSwitchedOn;
	/** Application logger. */
	private final Logger logger;

	/**
	 * Private constructor, loads whatever was last saved.
	 */
	private PurchaseManager() {
		this.logger = Core.getLogger();
		this.owned = new LinkedHashMap<String, Integer>();
		this.upgradesOff = new LinkedHashSet<String>();
		this.listeners = new ArrayList<ShopListener>();
		this.selectedShip = ShipType.STANDARD;
		this.selectedSkin = Skin.CLASSIC;
		load();
		syncItemSystem();
	}

	/**
	 * @return Shared instance of PurchaseManager.
	 */
	public static PurchaseManager getInstance() {
		if (instance == null)
			instance = new PurchaseManager();
		return instance;
	}

	// --- Buying ---

	/**
	 * Buys an entry (or the next level of an upgrade) at its normal price.
	 *
	 * @param item
	 *            Entry to buy.
	 * @param currency
	 *            Currency to pay with.
	 * @return What happened.
	 */
	public PurchaseResult purchase(final ShopItem item,
			final Currency currency) {
		return purchase(item, currency, false);
	}

	/**
	 * Buys an entry (or the next level of an upgrade). Checks and pays in
	 * one call; nothing changes unless it returns SUCCESS. Buying an
	 * upgrade switches it on.
	 *
	 * @param item
	 *            Entry to buy.
	 * @param currency
	 *            Currency to pay with.
	 * @param nightMarket
	 *            true to buy at today's Night Market discount.
	 * @return What happened.
	 */
	public PurchaseResult purchase(final ShopItem item,
			final Currency currency, final boolean nightMarket) {
		if (item == null || ShopCatalog.findById(item.getId()) == null
				|| currency == null)
			return PurchaseResult.UNKNOWN_ITEM;
		if (isMaxed(item))
			return item.isRepeatable() ? PurchaseResult.MAX_LEVEL
					: PurchaseResult.ALREADY_OWNED;
		if (!item.accepts(currency))
			return PurchaseResult.CURRENCY_NOT_ACCEPTED;
		if (!ShopCatalog.isOnSale(item, java.time.LocalDate.now())
				|| (nightMarket && !NightMarket.isListedToday(item)))
			return PurchaseResult.NOT_ON_SALE;

		int price = getPrice(item, currency, nightMarket);
		if (!spend(currency, price))
			return PurchaseResult.INSUFFICIENT_FUNDS;

		int before = getQuantity(item);
		boolean wasOff = this.upgradesOff.remove(item.getId());
		this.owned.put(item.getId(), before + 1);
		if (!save()) {
			if (before == 0)
				this.owned.remove(item.getId());
			else
				this.owned.put(item.getId(), before);
			if (wasOff)
				this.upgradesOff.add(item.getId());
			refund(currency, price);
			this.logger.warning("Purchase of " + item.getId()
					+ " could not be saved, refunded.");
			return PurchaseResult.SAVE_FAILED;
		}

		this.logger.info("Bought " + item.getId() + " (" + (before + 1) + "/"
				+ item.getMaxLevel() + ") for " + price + " "
				+ currency.getLabel() + ".");
		for (ShopListener listener : listenersCopy())
			listener.onPurchased(item, before + 1);
		if (item.getCategory() == ShopCategory.UPGRADE) {
			this.lastSwitchedOn = item.getId();
			upgradeChanged(item);
		}
		return PurchaseResult.SUCCESS;
	}

	/**
	 * Normal price of the next purchase of an entry.
	 *
	 * @param item
	 *            Entry.
	 * @param currency
	 *            Currency to pay with.
	 * @return Price, or 0 if it is maxed, owned, or the currency is not
	 *         accepted.
	 */
	public int getPrice(final ShopItem item, final Currency currency) {
		return getPrice(item, currency, false);
	}

	/**
	 * Price of the next purchase of an entry.
	 *
	 * @param item
	 *            Entry.
	 * @param currency
	 *            Currency to pay with.
	 * @param nightMarket
	 *            true for today's Night Market price.
	 * @return Price, or 0 if it is maxed, owned, or the currency is not
	 *         accepted.
	 */
	public int getPrice(final ShopItem item, final Currency currency,
			final boolean nightMarket) {
		if (item == null || isMaxed(item) || !item.accepts(currency))
			return 0;
		int price = item.getPrice(currency, getQuantity(item));
		return nightMarket ? NightMarket.discounted(price) : price;
	}

	/**
	 * Checks a price against the right balance without changing anything.
	 *
	 * @param currency
	 *            Currency of the price.
	 * @param price
	 *            Price to check.
	 * @return true if the player can currently afford it.
	 */
	public boolean canAfford(final Currency currency, final int price) {
		if (currency == Currency.DIAMONDS)
			return DiamondManager.getInstance().canAfford(price);
		return CurrencyManager.getInstance().canAfford(price);
	}

	/**
	 * @param item
	 *            Shop entry.
	 * @return true if the player has at least one.
	 */
	public boolean isOwned(final ShopItem item) {
		return getQuantity(item) > 0;
	}

	/**
	 * @param item
	 *            Shop entry.
	 * @return How many the player has: 0 or 1 for ships and skins, the
	 *         level for upgrades.
	 */
	public int getQuantity(final ShopItem item) {
		if (item == null)
			return 0;
		Integer quantity = this.owned.get(item.getId());
		return quantity == null ? 0 : quantity;
	}

	/**
	 * @param item
	 *            Shop entry.
	 * @return true if it cannot be bought again (owned, or max level).
	 */
	public boolean isMaxed(final ShopItem item) {
		return item != null && getQuantity(item) >= item.getMaxLevel();
	}

	// --- Loadout, read by Customize and GameScreen ---

	/**
	 * A ship is unlocked when it is not sold (Standard, or a ship added
	 * later without a price) or when it has been bought.
	 *
	 * @param type
	 *            Ship type.
	 * @return true if the player may fly it.
	 */
	public boolean isShipUnlocked(final ShipType type) {
		ShopItem item = ShopCatalog.findByRef(ShopCategory.SHIP, type.name());
		return item == null || isOwned(item);
	}

	/**
	 * @param skin
	 *            Skin.
	 * @return true if the player may use it.
	 */
	public boolean isSkinUnlocked(final Skin skin) {
		ShopItem item = ShopCatalog.findByRef(ShopCategory.SKIN, skin.name());
		return item == null || isOwned(item);
	}

	/** @return Ship used in the next run. */
	public ShipType getSelectedShip() {
		return isShipUnlocked(this.selectedShip) ? this.selectedShip
				: ShipType.STANDARD;
	}

	/** @return Skin used in the next run; CLASSIC is the default look. */
	public Skin getSelectedSkin() {
		return isSkinUnlocked(this.selectedSkin) ? this.selectedSkin
				: Skin.CLASSIC;
	}

	/**
	 * Chooses the ship for the next run.
	 *
	 * @param type
	 *            Ship type.
	 * @return false if the ship is locked; nothing changes then.
	 */
	public boolean selectShip(final ShipType type) {
		if (type == null || !isShipUnlocked(type))
			return false;
		if (type != this.selectedShip) {
			this.selectedShip = type;
			save();
		}
		return true;
	}

	/**
	 * Chooses the skin for the next run. Only one skin is used at a time;
	 * CLASSIC takes the skin off.
	 *
	 * @param skin
	 *            Skin.
	 * @return false if the skin is locked; nothing changes then.
	 */
	public boolean selectSkin(final Skin skin) {
		if (skin == null || !isSkinUnlocked(skin))
			return false;
		if (skin != this.selectedSkin) {
			this.selectedSkin = skin;
			save();
			for (ShopListener listener : listenersCopy())
				listener.onSkinChanged(skin);
		}
		return true;
	}

	/**
	 * Stable ship id used by the achievements (Mantis): Standard is the
	 * starter ship, every other ship uses its lower-case type name.
	 *
	 * @param type
	 *            Ship type.
	 * @return Ship id.
	 */
	public static String getShipId(final ShipType type) {
		if (type == null)
			return AchievementManager.STARTER_SHIP_ID;
		// One definition of the ids, shared with Core (Mantis).
		return Core.getShipId(type);
	}

	// --- Upgrades, read by the Item System (Team CS) ---

	/**
	 * @param itemId
	 *            Item System item id, e.g. "magnet".
	 * @return Level bought, 0 if never bought.
	 */
	public int getUpgradeLevel(final String itemId) {
		return getQuantity(upgrade(itemId));
	}

	/**
	 * @param itemId
	 *            Item System item id.
	 * @return true if bought and switched on.
	 */
	public boolean isUpgradeOn(final String itemId) {
		ShopItem item = upgrade(itemId);
		return isOwned(item) && !this.upgradesOff.contains(item.getId());
	}

	/**
	 * Level the Item System should apply in a run.
	 *
	 * @param itemId
	 *            Item System item id.
	 * @return Level while switched on; 0 when off or not bought.
	 */
	public int getActiveUpgradeLevel(final String itemId) {
		return isUpgradeOn(itemId) ? getUpgradeLevel(itemId) : 0;
	}

	/**
	 * @return Every upgrade that is on, as Item System item id to level, in
	 *         shop order.
	 */
	public Map<String, Integer> getActiveUpgrades() {
		Map<String, Integer> active = new LinkedHashMap<String, Integer>();
		for (ShopItem item : ShopCatalog.getByCategory(ShopCategory.UPGRADE))
			if (isUpgradeOn(item.getRef()))
				active.put(item.getRef(), getQuantity(item));
		return Collections.unmodifiableMap(active);
	}

	/**
	 * Switches a bought upgrade on or off. Takes effect from the next run.
	 *
	 * @param itemId
	 *            Item System item id.
	 * @param on
	 *            true to switch on.
	 * @return false if it is not bought or could not be saved.
	 */
	public boolean setUpgradeOn(final String itemId, final boolean on) {
		ShopItem item = upgrade(itemId);
		if (!isOwned(item))
			return false;
		if (isUpgradeOn(itemId) == on)
			return true;
		if (on)
			this.upgradesOff.remove(item.getId());
		else
			this.upgradesOff.add(item.getId());
		if (!save()) {
			if (on)
				this.upgradesOff.add(item.getId());
			else
				this.upgradesOff.remove(item.getId());
			return false;
		}
		if (on)
			this.lastSwitchedOn = item.getId();
		this.logger.info("Upgrade " + itemId + (on ? " on." : " off."));
		upgradeChanged(item);
		return true;
	}

	/**
	 * @param itemId
	 *            Item System item id.
	 * @return false if it is not bought or could not be saved.
	 */
	public boolean toggleUpgrade(final String itemId) {
		return setUpgradeOn(itemId, !isUpgradeOn(itemId));
	}

	// --- Listeners ---

	/**
	 * Tells a team about purchases, upgrade changes and skin changes.
	 *
	 * @param listener
	 *            Listener to add.
	 */
	public void addListener(final ShopListener listener) {
		if (listener != null && !this.listeners.contains(listener))
			this.listeners.add(listener);
	}

	/**
	 * @param listener
	 *            Listener to remove.
	 */
	public void removeListener(final ShopListener listener) {
		this.listeners.remove(listener);
	}

	// --- Internals ---

	/**
	 * @param itemId
	 *            Item System item id.
	 * @return Its upgrade entry, or null.
	 */
	private static ShopItem upgrade(final String itemId) {
		return itemId == null ? null
				: ShopCatalog.findByRef(ShopCategory.UPGRADE, itemId);
	}

	private List<ShopListener> listenersCopy() {
		return new ArrayList<ShopListener>(this.listeners);
	}

	/**
	 * Tells listeners and the Item System an upgrade changed.
	 *
	 * @param item
	 *            Upgrade entry.
	 */
	private void upgradeChanged(final ShopItem item) {
		syncItemSystem();
		int level = getActiveUpgradeLevel(item.getRef());
		for (ShopListener listener : listenersCopy())
			listener.onUpgradeChanged(item.getRef(), level);
	}

	/**
	 * TEMPORARY bridge, delete once the Item team reads
	 * {@link #getActiveUpgrades()}. Their run still reads one equipped item
	 * at level 1 from their own "equipment" save, so this copies our state
	 * there: every bought upgrade is owned, and the one most recently
	 * switched on (or the first one on) is equipped.
	 */
	private void syncItemSystem() {
		try {
			String equip = null;
			if (this.lastSwitchedOn != null) {
				ShopItem last = ShopCatalog.findById(this.lastSwitchedOn);
				if (last != null && isUpgradeOn(last.getRef()))
					equip = last.getRef();
			}
			for (ShopItem item : ShopCatalog.getByCategory(
					ShopCategory.UPGRADE)) {
				if (!isOwned(item))
					continue;
				ItemSystem.grantEquipment(item.getRef());
				if (equip == null && isUpgradeOn(item.getRef()))
					equip = item.getRef();
			}
			if (equip == null)
				ItemSystem.unequip();
			else
				ItemSystem.equip(equip);
		} catch (RuntimeException e) {
			this.logger.warning("Couldn't update the Item System: "
					+ e.getMessage());
		}
	}

	/**
	 * Gear bought before upgrades existed was saved by the Item System.
	 * Copies it over once, as level 1 and switched on.
	 *
	 * @return true if anything was copied.
	 */
	private boolean migrateOldGear() {
		boolean copied = false;
		try {
			for (ShopItem item : ShopCatalog.getByCategory(
					ShopCategory.UPGRADE))
				if (!isOwned(item) && ItemSystem.isOwned(item.getRef())) {
					this.owned.put(item.getId(), 1);
					copied = true;
				}
		} catch (RuntimeException e) {
			this.logger.warning("Couldn't read old gear: " + e.getMessage());
		}
		return copied;
	}

	private void removeAll() {
		this.owned.clear();
		this.upgradesOff.clear();
	}

	/**
	 * Deducts a price from the right balance.
	 *
	 * @param currency
	 *            Currency to pay in.
	 * @param price
	 *            Amount to deduct.
	 * @return true if the balance covered it and it was deducted.
	 */
	private boolean spend(final Currency currency, final int price) {
		if (price <= 0)
			return false;
		if (currency == Currency.DIAMONDS)
			return DiamondManager.getInstance().trySpend(price);
		return CurrencyManager.getInstance().trySpend(price);
	}

	/**
	 * Gives a price back after a purchase that could not be saved.
	 *
	 * @param currency
	 *            Currency that was paid.
	 * @param price
	 *            Amount to give back.
	 */
	private void refund(final Currency currency, final int price) {
		if (currency == Currency.DIAMONDS)
			DiamondManager.getInstance().addDiamonds(price);
		else
			CurrencyManager.getInstance().addCoins(price);
	}

	/**
	 * Loads purchases and the loadout. Never throws: a missing or broken
	 * file just means owning nothing and using the Standard ship.
	 */
	private void load() {
		Properties saved = new Properties();
		try {
			File file = getOwnedFile();
			if (file.exists()) {
				try (Reader reader = Files.newBufferedReader(file.toPath(),
						StandardCharsets.UTF_8)) {
					saved.load(reader);
				}
			}
		} catch (IOException | IllegalArgumentException e) {
			this.logger.warning("Couldn't load shop purchases.");
			removeAll();
			return;
		}

		for (String key : saved.stringPropertyNames()) {
			if (!key.startsWith(OWNED_PREFIX))
				continue;
			String id = key.substring(OWNED_PREFIX.length());
			try {
				int quantity = Integer.parseInt(saved.getProperty(key).trim());
				ShopItem item = ShopCatalog.findById(id);
				// Entries no longer sold (e.g. old power-up items) are dropped.
				if (quantity > 0 && item != null)
					this.owned.put(id, Math.min(quantity, item.getMaxLevel()));
			} catch (NumberFormatException e) {
				this.logger.warning("Skipping broken shop entry " + id);
			}
		}
		for (String id : saved.getProperty(OFF_KEY, "").split(","))
			if (this.owned.containsKey(id.trim()))
				this.upgradesOff.add(id.trim());
		try {
			this.selectedShip = ShipType.valueOf(saved.getProperty(SHIP_KEY,
					ShipType.STANDARD.name()).trim());
		} catch (IllegalArgumentException e) {
			this.selectedShip = ShipType.STANDARD;
		}
		try {
			this.selectedSkin = Skin.valueOf(saved.getProperty(SKIN_KEY,
					Skin.CLASSIC.name()).trim());
		} catch (IllegalArgumentException e) {
			this.selectedSkin = Skin.CLASSIC;
		}
		if (!Boolean.parseBoolean(saved.getProperty(MIGRATED_KEY, "false"))) {
			if (migrateOldGear())
				this.logger.info("Copied gear from the old save as upgrades.");
			save();
		}
		this.logger.info("Loaded shop purchases.");
	}

	/**
	 * Writes purchases and the loadout. Writes a temporary file first and
	 * then replaces the save, so a crash while saving never leaves a
	 * half-written file.
	 *
	 * @return false (and logs) when the file could not be written.
	 */
	private boolean save() {
		Properties saved = new Properties();
		for (Map.Entry<String, Integer> entry : this.owned.entrySet())
			saved.setProperty(OWNED_PREFIX + entry.getKey(),
					Integer.toString(entry.getValue()));
		saved.setProperty(OFF_KEY, String.join(",", this.upgradesOff));
		saved.setProperty(SHIP_KEY, this.selectedShip.name());
		saved.setProperty(SKIN_KEY, this.selectedSkin.name());
		saved.setProperty(MIGRATED_KEY, "true");
		try {
			Path file = getOwnedFile().toPath();
			Path temp = file.resolveSibling(OWNED_FILE + ".tmp");
			try (Writer writer = Files.newBufferedWriter(temp,
					StandardCharsets.UTF_8)) {
				saved.store(writer, "Space Invaders shop purchases and loadout");
			}
			try {
				Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		} catch (IOException e) {
			this.logger.warning("Couldn't save shop purchases: "
					+ e.getMessage());
			return false;
		}
	}

	/**
	 * @return The save file, next to the game like the other saves.
	 * @throws IOException
	 *             In case the application directory cannot be decoded.
	 */
	private File getOwnedFile() throws IOException {
		String path = PurchaseManager.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		path = URLDecoder.decode(path, "UTF-8");
		return new File(new File(path).getParent(), OWNED_FILE);
	}
}
