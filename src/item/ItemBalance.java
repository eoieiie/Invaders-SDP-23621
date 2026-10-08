package item;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Item balance values. Read from res/item-balance.properties so they can be tuned without recompiling.
 * File lookup order: -Dinvaders.itemBalance=<path> > res/item-balance.properties > item-balance.properties on the classpath.
 * A missing file or key uses the default (initial design value). An invalid value throws instead of being silently corrected.
 * A loaded instance is immutable. Values are read again when a new run starts.
 */
final class ItemBalance {
    static final String PATH_PROPERTY = "invaders.itemBalance";
    static final String FILE_NAME = "item-balance.properties";

    final long shieldDurationMillis;
    final int shieldCharges;
    /** First-pickup bonus (rapid fire: extra shots per second, bullet speed: extra pixels per frame), max stacks, drop weight multiplier per stack. */
    final double rapidFireBonus, bulletSpeedBonus;
    final int rapidFireMaxStacks, bulletSpeedMaxStacks;
    final double rapidFireDropDecay, bulletSpeedDropDecay;
    final long freezeDurationMillis;

    final int maxLives, lifeCapBonusScore;

    final double regularDropProbability, specialDropProbability;
    /** itemId → weight. Keeps registration order. */
    final Map<String, Double> dropWeights;
    final double fallSpeed, dropWidth, dropHeight;
    final long groundLifetimeMillis;

    final int inventoryCapacity;

    /** Default values (initial design values). */
    static ItemBalance defaults() { return new ItemBalance(new Properties()); }

    /** Finds and reads the config file. Throws if the given path is missing or unreadable. */
    static ItemBalance load() {
        Properties settings = new Properties();
        String configured = System.getProperty(PATH_PROPERTY);
        Path path = Paths.get(configured != null ? configured : "res/" + FILE_NAME);
        if (configured != null || Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                settings.load(reader);
            } catch (IOException | IllegalArgumentException error) {
                throw new IllegalStateException("cannot read item balance: " + path.toAbsolutePath(), error);
            }
        } else {
            InputStream stream = ItemBalance.class.getClassLoader().getResourceAsStream(FILE_NAME);
            if (stream != null) {
                try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    settings.load(reader);
                } catch (IOException | IllegalArgumentException error) {
                    throw new IllegalStateException("cannot read item balance resource", error);
                }
            }
        }
        return new ItemBalance(settings);
    }

    ItemBalance(Properties settings) {
        shieldDurationMillis = positiveLong(settings, "shield.durationMillis", 10_000L);
        shieldCharges = positiveInt(settings, "shield.charges", 1);
        rapidFireBonus = positiveDouble(settings, "rapidFire.bonus", 0.5);
        rapidFireMaxStacks = positiveInt(settings, "rapidFire.maxStacks", 10);
        rapidFireDropDecay = probability(settings, "rapidFire.dropDecay", 0.7);
        bulletSpeedBonus = positiveDouble(settings, "bulletSpeed.bonus", 1.0);
        bulletSpeedMaxStacks = positiveInt(settings, "bulletSpeed.maxStacks", 10);
        bulletSpeedDropDecay = probability(settings, "bulletSpeed.dropDecay", 0.7);
        freezeDurationMillis = positiveLong(settings, "freeze.durationMillis", 5_000L);

        maxLives = positiveInt(settings, "life.maxLives", 5);
        lifeCapBonusScore = nonNegativeInt(settings, "life.capBonusScore", 500);

        regularDropProbability = probability(settings, "drop.regular.probability", 0.15);
        specialDropProbability = probability(settings, "drop.special.probability", 1.0);
        Map<String, Double> weights = new LinkedHashMap<String, Double>();
        weights.put("life", weight(settings, "drop.weight.life"));
        weights.put("shield", weight(settings, "drop.weight.shield"));
        weights.put("rapid_fire", weight(settings, "drop.weight.rapidFire"));
        weights.put("bullet_speed", weight(settings, "drop.weight.bulletSpeed"));
        weights.put("freeze", weight(settings, "drop.weight.freeze"));
        dropWeights = java.util.Collections.unmodifiableMap(weights);
        fallSpeed = positiveDouble(settings, "drop.fallSpeed", 80);
        dropWidth = positiveDouble(settings, "drop.width", 24);
        dropHeight = positiveDouble(settings, "drop.height", 24);
        groundLifetimeMillis = positiveLong(settings, "drop.groundLifetimeMillis", 8_000L);

        inventoryCapacity = positiveInt(settings, "inventory.capacity", 3);
    }

    private static String raw(Properties settings, String key) {
        String value = settings.getProperty(key);
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static double number(Properties settings, String key, double fallback) {
        String value = raw(settings, key);
        if (value == null) return fallback;
        try {
            double parsed = Double.parseDouble(value);
            ItemAPI.finite(parsed, key);
            return parsed;
        } catch (NumberFormatException error) {
            throw invalid(key, value, "a number");
        }
    }

    private static long integer(Properties settings, String key, long fallback) {
        String value = raw(settings, key);
        if (value == null) return fallback;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException error) {
            throw invalid(key, value, "an integer");
        }
    }

    private static double positiveDouble(Properties settings, String key, double fallback) {
        double value = number(settings, key, fallback);
        if (value <= 0) throw invalid(key, value, "> 0");
        return value;
    }

    private static double probability(Properties settings, String key, double fallback) {
        double value = number(settings, key, fallback);
        if (value < 0 || value > 1) throw invalid(key, value, "between 0 and 1");
        return value;
    }

    private static double weight(Properties settings, String key) {
        double value = number(settings, key, 1.0);
        if (value < 0) throw invalid(key, value, ">= 0");
        return value;
    }

    private static long positiveLong(Properties settings, String key, long fallback) {
        long value = integer(settings, key, fallback);
        if (value <= 0) throw invalid(key, value, "> 0");
        return value;
    }

    private static int positiveInt(Properties settings, String key, int fallback) {
        long value = positiveLong(settings, key, fallback);
        if (value > Integer.MAX_VALUE) throw invalid(key, value, "<= " + Integer.MAX_VALUE);
        return (int) value;
    }

    private static int nonNegativeInt(Properties settings, String key, int fallback) {
        long value = integer(settings, key, fallback);
        if (value < 0 || value > Integer.MAX_VALUE) throw invalid(key, value, ">= 0");
        return (int) value;
    }

    private static IllegalArgumentException invalid(String key, Object value, String expected) {
        return new IllegalArgumentException("item balance " + key + "=" + value + " must be " + expected);
    }
}
