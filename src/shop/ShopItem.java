package shop;

/**
 * One thing that can be bought in the shop. Immutable: names and prices are
 * fixed in {@link ShopCatalog}.
 *
 * An entry can accept coins, diamonds or both; the player picks which to
 * pay with. Entries with a max level above 1 (upgrades) can be bought again
 * to level them up, and each level costs a step more than the one before:
 * level n costs base + step * (n - 1).
 *
 * @author GoG - Currency System
 */
public final class ShopItem {

	/** Unique id, also used as the save key for ownership. */
	private final String id;
	/** Name shown in the shop. */
	private final String name;
	/** Short description shown in the detail panel. */
	private final String description;
	/** What kind of entry this is. */
	private final ShopCategory category;
	/** Coin price of level 1, or 0 if coins are not accepted. */
	private final int coinPrice;
	/** Diamond price of level 1, or 0 if diamonds are not accepted. */
	private final int diamondPrice;
	/** Extra coins for each level already bought. */
	private final int coinStep;
	/** Extra diamonds for each level already bought. */
	private final int diamondStep;
	/** How many times it can be bought: 1 for ships and skins. */
	private final int maxLevel;
	/**
	 * What the entry unlocks in the other team's code: a Ship.ShipType name
	 * for ships, a {@link Skin} name for skins, an Item System item id for
	 * upgrades.
	 */
	private final String ref;

	/**
	 * Creates a shop entry.
	 *
	 * @param id
	 *            Unique id, no spaces, ':' or '=' signs.
	 * @param name
	 *            Name shown in the shop.
	 * @param description
	 *            Short description.
	 * @param category
	 *            What kind of entry this is.
	 * @param coinPrice
	 *            Coins for level 1, 0 if coins are not accepted.
	 * @param diamondPrice
	 *            Diamonds for level 1, 0 if diamonds are not accepted.
	 * @param coinStep
	 *            Extra coins per level already bought.
	 * @param diamondStep
	 *            Extra diamonds per level already bought.
	 * @param maxLevel
	 *            How many times it can be bought, at least 1.
	 * @param ref
	 *            What the entry unlocks in the other team's code.
	 */
	public ShopItem(final String id, final String name,
			final String description, final ShopCategory category,
			final int coinPrice, final int diamondPrice, final int coinStep,
			final int diamondStep, final int maxLevel, final String ref) {
		if (id == null || id.isEmpty() || id.contains("=")
				|| id.contains(" ") || id.contains(":") || id.contains(","))
			throw new IllegalArgumentException("invalid shop id: " + id);
		if (coinPrice < 0 || diamondPrice < 0 || coinStep < 0
				|| diamondStep < 0)
			throw new IllegalArgumentException("negative price: " + id);
		if (coinPrice == 0 && diamondPrice == 0)
			throw new IllegalArgumentException("no price: " + id);
		if (maxLevel < 1)
			throw new IllegalArgumentException("maxLevel must be >= 1: " + id);
		if (category == null || ref == null)
			throw new IllegalArgumentException("missing field: " + id);
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.coinPrice = coinPrice;
		this.diamondPrice = diamondPrice;
		this.coinStep = coinStep;
		this.diamondStep = diamondStep;
		this.maxLevel = maxLevel;
		this.ref = ref;
	}

	/** @return Unique id. */
	public String getId() {
		return this.id;
	}

	/** @return Name shown in the shop. */
	public String getName() {
		return this.name;
	}

	/** @return Short description. */
	public String getDescription() {
		return this.description;
	}

	/** @return What kind of entry this is. */
	public ShopCategory getCategory() {
		return this.category;
	}

	/** @return What the entry unlocks in the other team's code. */
	public String getRef() {
		return this.ref;
	}

	/**
	 * @param currency
	 *            Currency to check.
	 * @return true if the entry can be paid with it.
	 */
	public boolean accepts(final Currency currency) {
		return (currency == Currency.DIAMONDS ? this.diamondPrice
				: this.coinPrice) > 0;
	}

	/**
	 * Normal price of the next purchase.
	 *
	 * @param currency
	 *            Currency to pay with.
	 * @param alreadyBought
	 *            Levels already bought (0 for a first purchase).
	 * @return Price, or 0 if the currency is not accepted.
	 */
	public int getPrice(final Currency currency, final int alreadyBought) {
		int base = currency == Currency.DIAMONDS ? this.diamondPrice
				: this.coinPrice;
		int step = currency == Currency.DIAMONDS ? this.diamondStep
				: this.coinStep;
		if (base == 0)
			return 0;
		long price = base + (long) step * Math.max(0, alreadyBought);
		return (int) Math.min(Integer.MAX_VALUE, price);
	}

	/** @return How many times it can be bought: its max level. */
	public int getMaxLevel() {
		return this.maxLevel;
	}

	/** @return true if the entry can be bought more than once (has levels). */
	public boolean isRepeatable() {
		return this.maxLevel > 1;
	}
}
