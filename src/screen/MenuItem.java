package screen;

/**
 * Items of the main menu.
 *
 * The order of the constants is the order they are drawn in. The code is the
 * value TitleScreen returns to Core, which decides the next screen - the two
 * are independent, so an item can be moved on screen without changing its
 * code, and Exit can stay last while keeping code 0.
 *
 * Each screen already has its line here, disabled. To add yours, flip
 * enabled to true in the same PR as your Core case and your Screen class. A
 * screen that is not listed needs a code assigned first; then add one line.
 */
public enum MenuItem {

	/** Starts the game. */
	PLAY("Play", 2, true),
	/** Shows the high score table. */
	HIGH_SCORES("High scores", 3, true),
	/** Settings screen. */
	SETTINGS("Settings", 4, true),
	/** Shop screen. */
	SHOP("Shop", 5, true),
	/** Achievements screen. */
	ACHIEVEMENTS("Achievements", 6, false),
	/** Ship selection screen. */
	SHIP_SELECT("Ship select", 7, true),
	/** Closes the game. */
	EXIT("Exit", 0, true);

	/** Text drawn on the menu. */
	private final String title;
	/** Value returned to Core to open the associated screen. */
	private final int code;
	/** Whether the associated screen exists yet. */
	private final boolean enabled;

	/**
	 * Constructor, establishes the properties of the item.
	 *
	 * @param title
	 *            Text drawn on the menu.
	 * @param code
	 *            Value returned to Core when the item is chosen.
	 * @param enabled
	 *            False while the associated screen does not exist yet.
	 */
	MenuItem(final String title, final int code, final boolean enabled) {
		this.title = title;
		this.code = code;
		this.enabled = enabled;
	}

	/**
	 * Getter for the text drawn on the menu.
	 *
	 * @return Text of the item.
	 */
	public final String getTitle() {
		return this.title;
	}

	/**
	 * Getter for the code returned to Core.
	 *
	 * @return Code of the item.
	 */
	public final int getCode() {
		return this.code;
	}

	/**
	 * Checks whether the screen behind the item exists.
	 *
	 * @return True if the item can be selected.
	 */
	public final boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Item the cursor starts on.
	 *
	 * @return Topmost item.
	 */
	public static MenuItem first() {
		return values()[0];
	}

	/**
	 * Item below this one. Items without a screen are not skipped - the cursor
	 * stops on them, they just cannot be chosen.
	 *
	 * @return Item below, wrapping around to the top.
	 */
	public final MenuItem next() {
		return step(1);
	}

	/**
	 * Item above this one.
	 *
	 * @return Item above, wrapping around to the bottom.
	 */
	public final MenuItem previous() {
		return step(-1);
	}

	/**
	 * Moves one place along the list, wrapping around at both ends.
	 *
	 * @param direction
	 *            1 to move down, -1 to move up.
	 * @return Item one place away in that direction.
	 */
	private MenuItem step(final int direction) {
		MenuItem[] items = values();
		return items[Math.floorMod(this.ordinal() + direction, items.length)];
	}
}
