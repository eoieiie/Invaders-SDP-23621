package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Manages achievement progress and persistence. */
public class AchievementManager {

	/** Number of player kills required for Three Kills. */
	private static final int THREE_KILLS_TARGET = 3;

	/** Persistent player profile. */
	private PlayerProfile playerProfile;
	/** Achievements currently supported by the game. */
	private List<Achievement> achievements;

	/** Creates the manager and loads the saved player profile. */
	public AchievementManager() {
		try {
			this.playerProfile = Core.getFileManager().loadPlayerProfile();
		} catch (IOException | NumberFormatException e) {
			Core.getLogger().warning("Couldn't load player profile.");
			this.playerProfile = new PlayerProfile();
		}

		this.achievements = new ArrayList<Achievement>();
		addFirstKillAchievement();
	}

	/** Adds the Three Kills achievement. */
	private void addFirstKillAchievement() {
		this.achievements.add(new Achievement("first_kill", "Three Kills",
				"Defeat three enemies.", THREE_KILLS_TARGET, this.playerProfile
						.isAchievementUnlocked("first_kill")));
	}

	/**
	 * Records one confirmed enemy defeat and saves the resulting progress.
	 *
	 * @return Newly unlocked achievement, or null when nothing unlocks.
	 */
	public final Achievement recordEnemyDefeated() {
		this.playerProfile.recordEnemyDefeated();
		Achievement unlockedAchievement = null;

		for (Achievement achievement : this.achievements)
			if (!achievement.isUnlocked()
					&& this.playerProfile.getTotalEnemiesKilled()
							>= achievement.getRequiredEnemyKills()) {
				achievement.unlock();
				this.playerProfile.unlockAchievement(achievement.getId());
				// TODO Connect the shared CurrencyManager reward here when its API is available.
				unlockedAchievement = achievement;
			}

		saveProfile();
		return unlockedAchievement;
	}

	/** Saves the player profile, retaining progress after restarting. */
	private void saveProfile() {
		try {
			Core.getFileManager().savePlayerProfile(this.playerProfile);
		} catch (IOException e) {
			Core.getLogger().warning("Couldn't save player profile.");
		}
	}

	/** @return Read-only list of available achievements. */
	public final List<Achievement> getAchievements() {
		return Collections.unmodifiableList(this.achievements);
	}

	/** @return Total enemies defeated across all games. */
	public final int getTotalEnemiesKilled() {
		return this.playerProfile.getTotalEnemiesKilled();
	}

}