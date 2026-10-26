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
	/** Total levels cleared without the player taking damage. */
	private int flawlessLevelsCleared;
	private int totalItemsUsed;

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
		this(totalEnemiesKilled, unlockedAchievements, new HashSet<String>(), 0,0);
	}

	/** Loads progress, preserving compatibility with older profile callers. */
	public PlayerProfile(final int totalEnemiesKilled,
			final Set<String> unlockedAchievements,
			final Set<String> level10CompletedShips) {
		this(totalEnemiesKilled, unlockedAchievements, level10CompletedShips, 0,0);
	}

	/** Loads progress including cumulative flawless-level clears. */
	public PlayerProfile(final int totalEnemiesKilled,
			final Set<String> unlockedAchievements,
			final Set<String> level10CompletedShips,
			final int flawlessLevelsCleared,
						 final int totalItemsUsed) {
		this.level10CompletedShips = new HashSet<String>(level10CompletedShips);
		this.totalEnemiesKilled = totalEnemiesKilled;
		this.unlockedAchievements = new HashSet<String>(unlockedAchievements);
		this.flawlessLevelsCleared = Math.max(0, flawlessLevelsCleared);
		this.totalItemsUsed = Math.max(0, totalItemsUsed);
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

	/** Records one level clear with no player damage. */
	public final void recordFlawlessLevelCleared() {
		this.flawlessLevelsCleared++;
	}

	/** @return Total levels cleared without player damage. */
	public final int getFlawlessLevelsCleared() {
		return this.flawlessLevelsCleared;
	}

	/** Records one successful item use. */
	public final void recordItemUsed() {this.totalItemsUsed++;
	}

	/** @return Total successful item uses. */
	public final int getTotalItemsUsed() {return this.totalItemsUsed;
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