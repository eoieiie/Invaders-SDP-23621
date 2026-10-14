package shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import entity.Ship.ShipType;

/**
 * Fixed list of everything sold in the shop, in display order, and every
 * price. Prices are the Currency team's call; the other teams only say what
 * exists.
 *
 * <ul>
 * <li>Ships (A1): stats stay in Ship.ShipType. A ship type not listed here
 * (Standard, or one added later) is free.</li>
 * <li>Skins (Skin team): every {@link Skin} except CLASSIC is sold
 * automatically, so the Skin team only adds a constant to Skin.java. Its
 * price comes from {@link #SKIN_PRICES}, or the default until we add one.</li>
 * <li>Upgrades (Item team): their equipped items, by Item System item id.
 * Regular items (shield, freeze, ...) are not sold: they only drop in
 * game.</li>
 * </ul>
 *
 * How prices were set (default settings, 7 levels): a full run earns
 * roughly 30-40 coins and up to 28 diamonds, but unbanked diamonds are lost
 * on death, so one diamond is priced like two coins. Skins and upgrades take
 * either currency; ships take coins.
 *
 * @author GoG - Currency System
 */
public final class ShopCatalog {

	/** Price of a skin with no line in SKIN_PRICES: {coins, diamonds}. */
	private static final int[] DEFAULT_SKIN_PRICE = { 40, 20 };
	/** Skin prices: {coins, diamonds}. */
	private static final Map<Skin, int[]> SKIN_PRICES =
			new EnumMap<Skin, int[]>(Skin.class);
	/** Skin descriptions; skins without one get a generic line. */
	private static final Map<Skin, String> SKIN_DESCRIPTIONS =
			new EnumMap<Skin, String>(Skin.class);

	/** Every entry, in the order shown in the shop. */
	private static final List<ShopItem> ITEMS;

	static {
		// --- Skin prices {coins, diamonds} -----------------------------
		SKIN_PRICES.put(Skin.CRIMSON_HULL, new int[] { 30, 15 });
		SKIN_PRICES.put(Skin.AZURE_WING, new int[] { 30, 15 });
		SKIN_PRICES.put(Skin.VIOLET_NOVA, new int[] { 40, 20 });
		SKIN_PRICES.put(Skin.GILDED, new int[] { 60, 30 });
		SKIN_PRICES.put(Skin.TRIPLE_T_SAHUR, new int[] { 70, 35 });
		SKIN_PRICES.put(Skin.RAINBOW_RUSH, new int[] { 70, 35 });
		SKIN_PRICES.put(Skin.PHANTOM, new int[] { 50, 25 });

		SKIN_DESCRIPTIONS.put(Skin.CRIMSON_HULL,
				"A red-and-black repaint of your ship.");
		SKIN_DESCRIPTIONS.put(Skin.AZURE_WING,
				"A cool blue repaint of your ship.");
		SKIN_DESCRIPTIONS.put(Skin.VIOLET_NOVA,
				"A deep purple repaint of your ship.");
		SKIN_DESCRIPTIONS.put(Skin.GILDED, "Show off a golden hull.");
		SKIN_DESCRIPTIONS.put(Skin.TRIPLE_T_SAHUR, "Wooden hull that flashes"
				+ " to the beat: tung, tung, tung... sahur!");
		SKIN_DESCRIPTIONS.put(Skin.RAINBOW_RUSH,
				"Cycles through every colour of the rainbow.");
		SKIN_DESCRIPTIONS.put(Skin.PHANTOM, "A ghostly hull that fades in"
				+ " and out. Halloween only.");

		List<ShopItem> items = new ArrayList<ShopItem>();

		// Ships (A1): bought once with coins, chosen in Customize.
		items.add(ship(ShipType.FAST_MOVE, "Moves twice as fast as the"
				+ " standard ship.", 120));
		items.add(ship(ShipType.FAST_ATTACK, "Fires twice as often as the"
				+ " standard ship.", 180));
		items.add(ship(ShipType.TWO_WAY, "Fires from two barrels at once,"
				+ " but moves slowly.", 240));
		items.add(ship(ShipType.BIG_BULLET, "Heavy cannon with a slower"
				+ " rate of fire.", 300));

		// Skins (Skin team): every skin but Classic, in Skin.java order.
		for (Skin skin : Skin.values())
			if (skin != Skin.CLASSIC)
				items.add(skin(skin));

		// Upgrades (Item team): up to level 3, either currency.
		// upgrade(id, name, description, coins Lv1, +coins per level,
		// diamonds Lv1, +diamonds per level, max level)
		items.add(upgrade("extra_slots", "Extra Slots", "More item slots"
				+ " in every run.", 60, 30, 30, 15, 3));
		items.add(upgrade("magnet", "Magnet", "Picks up items as soon as"
				+ " they land.", 50, 25, 25, 12, 3));
		items.add(upgrade("max_life", "Max Life", "Life items can take you"
				+ " higher than the normal cap.", 80, 40, 40, 20, 3));
		items.add(upgrade("long_lasting", "Long Lasting", "Shield, freeze"
				+ " and boosts last longer.", 50, 25, 25, 12, 3));

		ITEMS = Collections.unmodifiableList(items);
	}

	/**
	 * Not instantiable.
	 */
	private ShopCatalog() {
	}

	/** @return Every entry, read-only, in shop order. */
	public static List<ShopItem> getAll() {
		return ITEMS;
	}

	/**
	 * @param category
	 *            Category to filter by.
	 * @return Entries of that category, in shop order.
	 */
	public static List<ShopItem> getByCategory(final ShopCategory category) {
		List<ShopItem> result = new ArrayList<ShopItem>();
		for (ShopItem item : ITEMS)
			if (item.getCategory() == category)
				result.add(item);
		return result;
	}

	/**
	 * @param id
	 *            Shop id.
	 * @return The entry with that id, or null if there is none.
	 */
	public static ShopItem findById(final String id) {
		for (ShopItem item : ITEMS)
			if (item.getId().equals(id))
				return item;
		return null;
	}

	/**
	 * @param category
	 *            Category of the entry.
	 * @param ref
	 *            Ship type name, skin name or item id.
	 * @return The entry selling that thing, or null if it is not sold
	 *         (which means it is free).
	 */
	public static ShopItem findByRef(final ShopCategory category,
			final String ref) {
		for (ShopItem item : ITEMS)
			if (item.getCategory() == category && item.getRef().equals(ref))
				return item;
		return null;
	}

	/**
	 * @param item
	 *            Shop entry.
	 * @return The limited skin it sells, or null if it is not one.
	 */
	public static Skin limitedSkin(final ShopItem item) {
		if (item == null || item.getCategory() != ShopCategory.SKIN)
			return null;
		Skin skin = Skin.valueOf(item.getRef());
		return skin.isLimited() ? skin : null;
	}

	/**
	 * Everything is always for sale except limited edition skins after
	 * their last day.
	 *
	 * @param item
	 *            Shop entry.
	 * @param today
	 *            Current date.
	 * @return true if the entry can be bought on that day.
	 */
	public static boolean isOnSale(final ShopItem item,
			final java.time.LocalDate today) {
		Skin skin = limitedSkin(item);
		return skin == null || skin.isOnSale(today);
	}

	/**
	 * Display name of a ship type, e.g. FAST_MOVE becomes "Fast Move".
	 *
	 * @param type
	 *            Ship type.
	 * @return Name shown in the shop and Customize.
	 */
	public static String shipName(final ShipType type) {
		switch (type) {
		case TWO_WAY:
			return "Two-Way Shot";
		default:
			String[] words = type.name().toLowerCase().split("_");
			StringBuilder name = new StringBuilder();
			for (String word : words) {
				if (name.length() > 0)
					name.append(' ');
				name.append(Character.toUpperCase(word.charAt(0)))
						.append(word.substring(1));
			}
			return name.toString();
		}
	}

	private static ShopItem ship(final ShipType type,
			final String description, final int coins) {
		return new ShopItem("ship_" + type.name().toLowerCase(),
				shipName(type), description, ShopCategory.SHIP, coins, 0, 0,
				0, 1, type.name());
	}

	private static ShopItem skin(final Skin skin) {
		int[] price = SKIN_PRICES.containsKey(skin) ? SKIN_PRICES.get(skin)
				: DEFAULT_SKIN_PRICE;
		String description = SKIN_DESCRIPTIONS.containsKey(skin)
				? SKIN_DESCRIPTIONS.get(skin) : "A new look for your ship.";
		return new ShopItem("skin_" + skin.name().toLowerCase(),
				skin.getDisplayName(), description, ShopCategory.SKIN,
				price[0], price[1], 0, 0, 1, skin.name());
	}

	private static ShopItem upgrade(final String itemId, final String name,
			final String description, final int coins, final int coinStep,
			final int diamonds, final int diamondStep, final int maxLevel) {
		return new ShopItem("upgrade_" + itemId, name, description,
				ShopCategory.UPGRADE, coins, diamonds, coinStep, diamondStep,
				maxLevel, itemId);
	}
}
