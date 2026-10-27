package shop;

import java.awt.Color;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Colours the player ship can be painted in. Classic is the original green
 * and is always owned; the others are sold in the shop and chosen in
 * Customize.
 *
 * Limited edition skins are animated and only sold until their last day.
 * Players who bought one keep it after that.
 *
 * @author GoG - Currency System
 */
public enum Skin {
	/** Original green ship, free. */
	CLASSIC("Classic", new Color(0, 255, 0)),
	/** Red hull. */
	CRIMSON_HULL("Crimson Hull", new Color(230, 40, 70)),
	/** Blue hull. */
	AZURE_WING("Azure Wing", new Color(70, 150, 255)),
	/** Purple hull. */
	VIOLET_NOVA("Violet Nova", new Color(190, 100, 255)),
	/** Gold hull. */
	GILDED("Gilded", new Color(255, 205, 40)),

	// --- Limited edition: animated, sold until the date given. ---

	/** Wooden hull that flashes three times, then rests: tung tung tung... sahur. */
	TRIPLE_T_SAHUR("Triple T Sahur", new Color(185, 125, 65),
			Effect.TRIPLE_BEAT, LocalDate.of(2026, 11, 30)),
	/** Hull that cycles through every colour. */
	RAINBOW_RUSH("Rainbow Rush", new Color(255, 80, 80),
			Effect.RAINBOW, LocalDate.of(2026, 12, 31)),
	/** Pale hull that fades in and out. Halloween edition. */
	PHANTOM("Phantom", new Color(225, 230, 255),
			Effect.FADE, LocalDate.of(2026, 10, 31));

	/** How a skin's colour changes over time. */
	public enum Effect {
		/** Always the same colour. */
		NONE,
		/** Three quick flashes, then a pause. */
		TRIPLE_BEAT,
		/** Cycles through the colour wheel. */
		RAINBOW,
		/** Slowly fades darker and back. */
		FADE
	}

	/** Length of one tung-tung-tung-pause cycle, in milliseconds. */
	private static final long BEAT_CYCLE = 1500;
	/** Time between two flashes, in milliseconds. */
	private static final long BEAT_GAP = 200;
	/** How long one flash lasts, in milliseconds. */
	private static final long BEAT_LENGTH = 110;
	/** Time for one full trip round the colour wheel, in milliseconds. */
	private static final long RAINBOW_CYCLE = 3000;
	/** Time for one fade out and back, in milliseconds. */
	private static final double FADE_CYCLE = 2400;

	/** Name shown in the shop and in Customize. */
	private final String displayName;
	/** Base colour the ship is drawn in. */
	private final Color color;
	/** How the colour changes over time. */
	private final Effect effect;
	/** Last day the skin is sold, or null if it is always sold. */
	private final LocalDate availableUntil;

	/**
	 * A plain, always-available skin.
	 *
	 * @param displayName
	 *            Name shown in the shop and in Customize.
	 * @param color
	 *            Colour the ship is drawn in.
	 */
	Skin(final String displayName, final Color color) {
		this(displayName, color, Effect.NONE, null);
	}

	/**
	 * @param displayName
	 *            Name shown in the shop and in Customize.
	 * @param color
	 *            Base colour the ship is drawn in.
	 * @param effect
	 *            How the colour changes over time.
	 * @param availableUntil
	 *            Last day the skin is sold, or null for always.
	 */
	Skin(final String displayName, final Color color, final Effect effect,
			final LocalDate availableUntil) {
		this.displayName = displayName;
		this.color = color;
		this.effect = effect;
		this.availableUntil = availableUntil;
	}

	/** @return Name shown in the shop and in Customize. */
	public String getDisplayName() {
		return this.displayName;
	}

	/** @return Base colour, for anything that does not animate. */
	public Color getColor() {
		return this.color;
	}

	/**
	 * Colour at a moment in time, so animated skins can be redrawn each
	 * frame. Plain skins always return their base colour.
	 *
	 * @param millis
	 *            Current time, e.g. System.currentTimeMillis().
	 * @return Colour to draw the ship in right now.
	 */
	public Color colorAt(final long millis) {
		switch (this.effect) {
		case TRIPLE_BEAT: {
			long t = millis % BEAT_CYCLE;
			boolean flash = t < BEAT_GAP * 3 && t % BEAT_GAP < BEAT_LENGTH;
			return flash ? new Color(255, 225, 160) : this.color;
		}
		case RAINBOW:
			return Color.getHSBColor((millis % RAINBOW_CYCLE)
					/ (float) RAINBOW_CYCLE, 0.75f, 1f);
		case FADE: {
			double phase = (Math.cos(millis * 2 * Math.PI / FADE_CYCLE) + 1)
					/ 2;
			double amount = 0.25 + 0.75 * phase;
			return new Color((int) (this.color.getRed() * amount),
					(int) (this.color.getGreen() * amount),
					(int) (this.color.getBlue() * amount));
		}
		default:
			return this.color;
		}
	}

	/** @return true if this skin is a limited edition. */
	public boolean isLimited() {
		return this.availableUntil != null;
	}

	/** @return Last day the skin is sold, or null if it is always sold. */
	public LocalDate getAvailableUntil() {
		return this.availableUntil;
	}

	/**
	 * @param today
	 *            Current date.
	 * @return true if the skin can still be bought on that day.
	 */
	public boolean isOnSale(final LocalDate today) {
		return this.availableUntil == null
				|| !today.isAfter(this.availableUntil);
	}

	/**
	 * @param today
	 *            Current date.
	 * @return Days left to buy it, counting today; 0 once it has ended.
	 *         Only meaningful for limited skins.
	 */
	public long daysLeft(final LocalDate today) {
		if (this.availableUntil == null)
			return Long.MAX_VALUE;
		return Math.max(0, ChronoUnit.DAYS.between(today,
				this.availableUntil) + 1);
	}
}