package shop;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import engine.EconomyConfig;

/**
 * The Night Market: a few shop entries sold at a discount, changing once a
 * day. The picks come from a random generator seeded with the date, so the
 * deals stay the same all day and nothing needs saving. The discount
 * applies to both coin and diamond prices.
 *
 * @author GoG - Currency System
 */
public final class NightMarket {

	/** How many entries are on sale each day, from res/economy.properties. */
	public static final int LISTING_COUNT = EconomyConfig.getInstance()
			.getNightMarketListingCount();
	/** Discount applied to every listing, in percent, from
	 * res/economy.properties. */
	public static final int DISCOUNT_PERCENT = EconomyConfig.getInstance()
			.getNightMarketDiscountPercent();

	/**
	 * Not instantiable.
	 */
	private NightMarket() {
	}

	/** @return Today's discounted entries. */
	public static List<ShopItem> getListings() {
		return getListings(LocalDate.now());
	}

	/**
	 * Picks the listings for a given day. Same day, same picks.
	 *
	 * @param date
	 *            Day to pick for.
	 * @return That day's entries, in display order.
	 */
	public static List<ShopItem> getListings(final LocalDate date) {
		// Limited skins are never discounted, so they stay special.
		List<ShopItem> pool = new ArrayList<ShopItem>();
		for (ShopItem item : ShopCatalog.getAll())
			if (ShopCatalog.limitedSkin(item) == null)
				pool.add(item);
		Collections.shuffle(pool, new Random(date.toEpochDay()));
		return new ArrayList<ShopItem>(pool.subList(0,
				Math.min(LISTING_COUNT, pool.size())));
	}

	/**
	 * @param item
	 *            Shop entry.
	 * @return true if it is on today's market.
	 */
	public static boolean isListedToday(final ShopItem item) {
		return item != null && getListings().contains(item);
	}

	/**
	 * @param price
	 *            Normal price.
	 * @return Price after the discount, never below 1.
	 */
	public static int discounted(final int price) {
		return Math.max(1, price * (100 - DISCOUNT_PERCENT) / 100);
	}
}
