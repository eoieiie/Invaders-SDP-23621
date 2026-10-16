package shop;

/**
 * What kind of thing a shop entry is. Decides how it is owned and where it
 * is used after buying.
 *
 * Regular items (shield, freeze, ...) are not sold: the Item team decided
 * they only drop during gameplay. Only their equipped items are sold here,
 * as upgrades.
 *
 * @author GoG - Currency System
 */
public enum ShopCategory {
	/** Player ship from the Ship Variety team (A1). Bought once, chosen in Customize. */
	SHIP("Ship"),
	/** Cosmetic recolour of the ship (Skin team). Bought once, one used at a time. */
	SKIN("Cosmetic skin"),
	/**
	 * Item System (Team CS) equipped item. Bought once (no levels), on as
	 * soon as it is bought, switched on/off in Customize. Only one upgrade
	 * can be on at once.
	 */
	UPGRADE("Upgrade");

	/** Label shown in the shop's detail panel. */
	private final String label;

	/**
	 * @param label
	 *            Label shown in the shop's detail panel.
	 */
	ShopCategory(final String label) {
		this.label = label;
	}

	/** @return Label shown in the shop's detail panel. */
	public String getLabel() {
		return this.label;
	}
}
