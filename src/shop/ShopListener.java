package shop;

/**
 * Lets another team react when the player buys or changes something,
 * instead of checking every frame. Register it with
 * engine.PurchaseManager.getInstance().addListener(...).
 *
 * Every method has an empty default, so implement only what you need.
 * Called on the game thread, after the change has been saved.
 *
 * @author GoG - Currency System
 */
public interface ShopListener {

	/**
	 * Something was bought.
	 *
	 * @param item
	 *            Entry that was bought.
	 * @param level
	 *            How many the player has now: 1 for ships and skins, the new
	 *            level for upgrades.
	 */
	default void onPurchased(final ShopItem item, final int level) {
	}

	/**
	 * An upgrade was bought, levelled up, or switched on/off (Item team).
	 *
	 * @param itemId
	 *            Item System item id, e.g. "magnet".
	 * @param activeLevel
	 *            Level to apply in game: 0 when off or not owned.
	 */
	default void onUpgradeChanged(final String itemId, final int activeLevel) {
	}

	/**
	 * The player chose another skin in Customize (Skin team).
	 *
	 * @param skin
	 *            Skin used from now on; CLASSIC for the default look.
	 */
	default void onSkinChanged(final Skin skin) {
	}
}
