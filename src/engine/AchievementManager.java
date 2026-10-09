package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import engine.DrawManager.SpriteType;

/** Manages achievement progress and persistence. */
public class AchievementManager {

	/** Most achievements a single page of the achievements screen shows. */
	public static final int ACHIEVEMENTS_PER_PAGE = 5;

	/** Number of player kills required for First Flight. */
	private static final int THREE_KILLS_TARGET = 3;

	/** Persistent player profile. */
	private PlayerProfile playerProfile;
	/** Normal achievements, shown on page 1 of the achievements screen. */
	private List<Achievement> normalAchievements;
	/** Tier achievements, shown on page 2 of the achievements screen. */
	private List<Achievement> tierAchievements;

	/** Creates the manager and loads the saved player profile. */
	public AchievementManager() {
		try {
			this.playerProfile = Core.getFileManager().loadPlayerProfile();
		} catch (IOException | NumberFormatException e) {
			Core.getLogger().warning("Couldn't load player profile.");
			this.playerProfile = new PlayerProfile();
		}

		this.normalAchievements = new ArrayList<Achievement>();
		this.tierAchievements = new ArrayList<Achievement>();

		// Page 1: normal achievements. Add new ones below.
		addFirstKillAchievement();

		// Page 2: tier achievements. The tier team adds theirs below,
		// using addTierAchievement(...).
	}

	/** Adds the First Flight achievement. */
	private void addFirstKillAchievement() {
		addNormalAchievement(new Achievement("first_kill", "First Flight",
				"Welcome to Invaders.", THREE_KILLS_TARGET,
				SpriteType.FirstFlight, this.playerProfile
						.isAchievementUnlocked("first_kill")));
	}

	/**
	 * Adds an achievement to page 1 (normal achievements).
	 *
	 * @param achievement Achievement to add.
	 */
	private void addNormalAchievement(final Achievement achievement) {
		addToPage(this.normalAchievements, achievement, "normal");
	}

	/**
	 * Adds an achievement to page 2 (tier achievements).
	 *
	 * @param achievement Achievement to add.
	 */
	private void addTierAchievement(final Achievement achievement) {
		addToPage(this.tierAchievements, achievement, "tier");
	}

	/**
	 * Adds an achievement to a page, refusing it when the page is full.
	 *
	 * @param page        Page list to add to.
	 * @param achievement Achievement to add.
	 * @param pageName    Page name, used in the log message.
	 */
	private void addToPage(final List<Achievement> page,
			final Achievement achievement, final String pageName) {
		if (page.size() >= ACHIEVEMENTS_PER_PAGE) {
			Core.getLogger().warning("The " + pageName + " achievement page "
					+ "is full, skipping " + achievement.getId() + ".");
			return;
		}
		page.add(achievement);
	}

	/**
	 * Records one confirmed enemy defeat and saves the resulting progress.
	 *
	 * @return Newly unlocked achievement, or null when nothing unlocks.
	 */
	public final Achievement recordEnemyDefeated() {
		this.playerProfile.recordEnemyDefeated();
		Achievement unlockedAchievement = null;

		for (Achievement achievement : getAchievements())
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

	/** @return Read-only list of every achievement, normal then tier. */
	public final List<Achievement> getAchievements() {
		List<Achievement> all = new ArrayList<Achievement>(
				this.normalAchievements);
		all.addAll(this.tierAchievements);
		return Collections.unmodifiableList(all);
	}

	/** @return Read-only list of normal achievements (page 1). */
	public final List<Achievement> getNormalAchievements() {
		return Collections.unmodifiableList(this.normalAchievements);
	}

	/** @return Read-only list of tier achievements (page 2). */
	public final List<Achievement> getTierAchievements() {
		return Collections.unmodifiableList(this.tierAchievements);
	}

	/** @return Total enemies defeated across all games. */
	public final int getTotalEnemiesKilled() {
		return this.playerProfile.getTotalEnemiesKilled();
	}

}