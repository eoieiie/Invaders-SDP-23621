package engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Economy balance values, read from res/economy.properties so the team can
 * tune coin drops and Night Market deals without recompiling.
 *
 * File lookup order: -Dinvaders.economy=&lt;path&gt; &gt;
 * res/economy.properties &gt; economy.properties on the classpath. Unlike
 * the item balance file, a missing file, missing key or invalid value never
 * stops the game: it falls back to the default and logs a warning, because
 * the economy should keep working even with a broken config.
 *
 * Loaded once, on first use. A loaded instance is immutable.
 *
 * @author GoG - Currency System
 */
public final class EconomyConfig {

	/** System property that overrides the config file location. */
	static final String PATH_PROPERTY = "invaders.economy";
	/** Name of the config file. */
	static final String FILE_NAME = "economy.properties";

	/** Default chance of a coin dropping after a kill. */
	static final double DEFAULT_COIN_DROP_CHANCE = 0.10d;
	/** Default Night Market discount, in percent. */
	static final int DEFAULT_DISCOUNT_PERCENT = 30;
	/** Default number of Night Market deals per day. */
	static final int DEFAULT_LISTING_COUNT = 3;

	/** Singleton instance. */
	private static EconomyConfig instance;

	/** Chance that a regular enemy drops a coin, 0.0 to 1.0. */
	private final double coinDropChance;
	/** Night Market discount, 1 to 99 percent. */
	private final int nightMarketDiscountPercent;
	/** Night Market deals per day, at least 1. */
	private final int nightMarketListingCount;

	/**
	 * Builds a config from already loaded settings.
	 *
	 * @param settings
	 *            Loaded key/value pairs; may be empty.
	 */
	EconomyConfig(final Properties settings) {
		this.coinDropChance = readDouble(settings, "coin.dropChance",
				DEFAULT_COIN_DROP_CHANCE, 0.0d, 1.0d);
		this.nightMarketDiscountPercent = readInt(settings,
				"nightMarket.discountPercent", DEFAULT_DISCOUNT_PERCENT, 1, 99);
		this.nightMarketListingCount = readInt(settings,
				"nightMarket.listingCount", DEFAULT_LISTING_COUNT, 1,
				Integer.MAX_VALUE);
	}

	/**
	 * Controls access to the economy config, loading it on first use.
	 *
	 * @return shared instance of EconomyConfig.
	 */
	public static EconomyConfig getInstance() {
		if (instance == null)
			instance = new EconomyConfig(load());
		return instance;
	}

	/**
	 * @return Chance that a destroyed regular enemy drops a coin.
	 */
	public double getCoinDropChance() {
		return this.coinDropChance;
	}

	/**
	 * @return Night Market discount, in percent.
	 */
	public int getNightMarketDiscountPercent() {
		return this.nightMarketDiscountPercent;
	}

	/**
	 * @return How many shop entries are on sale each day.
	 */
	public int getNightMarketListingCount() {
		return this.nightMarketListingCount;
	}

	/**
	 * Finds and reads the config file. Never throws.
	 *
	 * @return Loaded settings, empty if no file could be read.
	 */
	private static Properties load() {
		Properties settings = new Properties();
		String configured = System.getProperty(PATH_PROPERTY);
		Path path = Paths.get(configured != null ? configured
				: "res/" + FILE_NAME);
		try {
			if (Files.exists(path)) {
				try (Reader reader = Files.newBufferedReader(path,
						StandardCharsets.UTF_8)) {
					settings.load(reader);
				}
				logger().info("Loaded economy config from "
						+ path.toAbsolutePath());
				return settings;
			}
			InputStream stream = EconomyConfig.class.getClassLoader()
					.getResourceAsStream(FILE_NAME);
			if (stream != null) {
				try (Reader reader = new InputStreamReader(stream,
						StandardCharsets.UTF_8)) {
					settings.load(reader);
				}
				logger().info("Loaded economy config from the classpath.");
				return settings;
			}
			logger().info("No economy config found, using defaults.");
		} catch (IOException | IllegalArgumentException e) {
			logger().warning("Couldn't read economy config, using defaults: "
					+ e.getMessage());
			settings.clear();
		}
		return settings;
	}

	/**
	 * Reads a decimal value within [min, max], or the default.
	 */
	private static double readDouble(final Properties settings,
			final String key, final double fallback, final double min,
			final double max) {
		String raw = settings.getProperty(key);
		if (raw == null)
			return fallback;
		try {
			double value = Double.parseDouble(raw.trim());
			if (value >= min && value <= max)
				return value;
		} catch (NumberFormatException e) {
			// Falls through to the warning below.
		}
		logger().warning("Invalid economy value " + key + "=" + raw
				+ ", using " + fallback + ".");
		return fallback;
	}

	/**
	 * Reads a whole-number value within [min, max], or the default.
	 */
	private static int readInt(final Properties settings, final String key,
			final int fallback, final int min, final int max) {
		String raw = settings.getProperty(key);
		if (raw == null)
			return fallback;
		try {
			int value = Integer.parseInt(raw.trim());
			if (value >= min && value <= max)
				return value;
		} catch (NumberFormatException e) {
			// Falls through to the warning below.
		}
		logger().warning("Invalid economy value " + key + "=" + raw
				+ ", using " + fallback + ".");
		return fallback;
	}

	/**
	 * @return Game logger.
	 */
	private static Logger logger() {
		return Core.getLogger();
	}
}
