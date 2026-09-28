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
		this.totalEnemiesKilled = totalEnemiesKilled;
		this.unlockedAchievements = new HashSet<String>(unlockedAchievements);
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