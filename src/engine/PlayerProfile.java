package engine;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores player state that remains after the current game ends.
 */
public class PlayerProfile {

	/** Total number of enemies defeated across all games. */
	private int totalEnemiesKilled;
	/** Identifiers for unlocked achievements. */
	private Set<String> unlockedAchievements;

	/** Stable ship model IDs with a confirmed level 10 clear. */
	private Set<String> level10CompletedShips;

	/** Creates an empty player profile. */
	public PlayerProfile() {
		this(0, new HashSet<String>());
	}

	/**
	 * Creates a player profile from saved values.
	 *
	 * @param totalEnemiesKilled Total enemies defeated.
	 * @param unlockedAchievements Unlocked achievement identifiers.
	 */
	public PlayerProfile(final int totalEnemiesKilled,
			final Set<String> unlockedAchievements) {
		this(totalEnemiesKilled, unlockedAchievements, new HashSet<String>());
	}

	/** Loads progress, preserving compatibility with older profile callers. */
	public PlayerProfile(final int totalEnemiesKilled,
			final Set<String> unlockedAchievements,
			final Set<String> level10CompletedShips) {
		this.level10CompletedShips = new HashSet<String>(level10CompletedShips);
		this.totalEnemiesKilled = totalEnemiesKilled;
		this.unlockedAchievements = new HashSet<String>(unlockedAchievements);
	}

	/** Records a distinct ship; repeating a clear does not add progress. */
	public final boolean recordLevel10Completed(final String shipId) {
		return this.level10CompletedShips.add(shipId);
	}

	/** @return A copy of the saved level 10 ship IDs. */
	public final Set<String> getLevel10CompletedShips() {
		return new HashSet<String>(this.level10CompletedShips);
	}

	/** Increments the persistent enemy defeat count. */
	public final void recordEnemyDefeated() {
		this.totalEnemiesKilled++;
	}

	/** @return Total enemies defeated across all games. */
	public final int getTotalEnemiesKilled() {
		return this.totalEnemiesKilled;
	}

	/**
	 * Checks whether an achievement is unlocked.
	 *
	 * @param achievementId Achievement identifier.
	 * @return True when the achievement is unlocked.
	 */
	public final boolean isAchievementUnlocked(final String achievementId) {
		return this.unlockedAchievements.contains(achievementId);
	}

	/**
	 * Marks an achievement as unlocked.
	 *
	 * @param achievementId Achievement identifier.
	 */
	public final void unlockAchievement(final String achievementId) {
		this.unlockedAchievements.add(achievementId);
	}

	/** @return A copy of the unlocked achievement identifiers. */
	public final Set<String> getUnlockedAchievements() {
		return new HashSet<String>(this.unlockedAchievements);
	}
}