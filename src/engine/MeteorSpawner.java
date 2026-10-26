package engine;

import java.util.Random;

/**
 * AUTHORED BY: VFX TEAM (Effection)
 * Decides when the bonus meteor appears, for any number of levels:none before METEOR_START_LEVEL or in the final level, up to
 * METEOR_MAX_LATE_LEVEL in the METEOR_LATE_LEVELS levels before the final,otherwise up to METEOR_MAX_PER_LEVEL. Each meteor drops one diamond.The meteor count is rolled once when the level starts.
 */
public class MeteorSpawner {

	public static final int METEOR_START_LEVEL = 3;
	public static final double METEOR_BASE_CHANCE = 0.40; // chance at the start level
	public static final double METEOR_CHANCE_STEP = 0.10; // extra chance per level
	public static final double METEOR_MAX_CHANCE = 0.80;
	public static final int METEOR_MAX_PER_LEVEL = 1;
	public static final int METEOR_MAX_LATE_LEVEL = 2;
	public static final int METEOR_LATE_LEVELS = 2; // levels before the final
	public static final int METEOR_SAFE_START = 10000; // ms with no meteor
	public static final int METEOR_FIRST_VARIANCE = 15000; // ms
	public static final int METEOR_MIN_GAP = 20000; // ms between two meteors
	public static final int METEOR_GAP_VARIANCE = 10000; // ms
	public static final int METEOR_MIN_ENEMIES = 5; // no meteor below this
	public static final int METEOR_BASE_CROSS_TIME = 5000; // ms at the start level
	public static final int METEOR_CROSS_TIME_STEP = 200; // ms faster per level
	public static final int METEOR_MIN_CROSS_TIME = 4000; // ms, fastest
	public static final double METEOR_MIN_ANGLE = 10; // degrees
	public static final double METEOR_MAX_ANGLE =30; // degrees
	public static final double METEOR_MAX_CURVE = 10; // extra bend, degrees
	public static final double METEOR_SPEED_VARIANCE = 0.15; // +-15% speed
	public static final int METEOR_DIAMOND_VALUE = 1;

	private final int level;
	private final Random random;
	private final int plannedMeteors;
	private int spawnedMeteors;
	private long playStart;
	private long nextSpawnAt;

	/** Creates the spawner for one level and rolls its meteors. */
	public MeteorSpawner(final int level, final int finalLevel,
			final Random random) {
		this.level = level;
		this.random = random == null ? new Random() : random;
		int planned = 0;
		double chance = chanceForLevel(level);
		for (int i = 0; i < maxForLevel(level, finalLevel); i++)
			if (this.random.nextDouble() < chance)
				planned++;
		this.plannedMeteors = planned;
	}

	/** Creates the spawner for a level, using the game's number of levels. */
	public static MeteorSpawner forLevel(final int level) {
		return new MeteorSpawner(level, Core.getNumLevels(), new Random());
	}

	/** @return Chance of each meteor in a level, 0 before the start level. */
	public static double chanceForLevel(final int level) {
		if (level < METEOR_START_LEVEL)
			return 0;
		return Math.min(METEOR_MAX_CHANCE, METEOR_BASE_CHANCE
				+ METEOR_CHANCE_STEP * (level - METEOR_START_LEVEL));
	}

	/** @return Most meteors a level can have. */
	public static int maxForLevel(final int level, final int finalLevel) {
		if (level < METEOR_START_LEVEL || level >= finalLevel)
			return 0;
		if (level >= finalLevel - METEOR_LATE_LEVELS)
			return METEOR_MAX_LATE_LEVEL;
		return METEOR_MAX_PER_LEVEL;
	}

	/** @return Time to cross the screen in a level, in ms. */
	public static int crossTimeForLevel(final int level) {
		int levelsIn = Math.max(0, level - METEOR_START_LEVEL);
		return Math.max(METEOR_MIN_CROSS_TIME, METEOR_BASE_CROSS_TIME - METEOR_CROSS_TIME_STEP * levelsIn);
	}

	/** @return True if a meteor should appear now. Call every frame. */
	public final boolean shouldSpawn(final long now, final int enemiesLeft,
			final boolean meteorOnScreen) {
		if (this.playStart == 0) {
			this.playStart = now;
			this.nextSpawnAt = now + METEOR_SAFE_START + this.random.nextInt(METEOR_FIRST_VARIANCE + 1);
		}
		return this.spawnedMeteors < this.plannedMeteors && !meteorOnScreen && now >= this.nextSpawnAt && enemiesLeft >= METEOR_MIN_ENEMIES;
	}

	/** Records a spawned meteor and schedules the earliest next one. */
	public final void markSpawned(final long now) {
		this.spawnedMeteors++;
		this.nextSpawnAt = now + METEOR_MIN_GAP + this.random.nextInt(METEOR_GAP_VARIANCE + 1);
	}

	/** @return True to make the next meteor enter from the left. */
	public final boolean rollFromLeft() {
		return this.random.nextBoolean();
	}

	/** @return Random height between minY and maxY. */
	public final int rollHeight(final int minY, final int maxY) {
		return minY + this.random.nextInt(Math.max(0, maxY - minY) + 1);
	}

	/** @return This level's crossing time, a little faster or slower. */
	public final int getCrossTime() {
		double change = (this.random.nextDouble() * 2 - 1) * METEOR_SPEED_VARIANCE;
		return (int) Math.round(crossTimeForLevel(this.level) * (1 + change));
	}

	/** @return Random entry angle, in degrees below the horizontal. */
	public final double rollStartAngle() {
		return METEOR_MIN_ANGLE + this.random.nextDouble() * (METEOR_MAX_ANGLE - METEOR_MIN_ANGLE);
	}

	/** @return Exit angle: entry angle plus a random bend, max 80. */
	public final double rollEndAngle(final double startAngle) {
		return Math.min(80, startAngle + this.random.nextDouble() * METEOR_MAX_CURVE);
	}

	/** @return Diamonds each meteor drops. */
	public final int getDiamondValue() {
		return METEOR_DIAMOND_VALUE;
	}

	/** @return Meteors rolled for this level. */
	public final int getPlannedMeteors() {
		return this.plannedMeteors;
	}
}
