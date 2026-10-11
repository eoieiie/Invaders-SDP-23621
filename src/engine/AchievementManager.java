package engine;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import engine.Achievement.Requirement;

import engine.DrawManager.SpriteType;

/** Manages achievement progress and persistence. */
public class AchievementManager {

	/** Most achievements a single page of the achievements screen shows. */
	public static final int ACHIEVEMENTS_PER_PAGE = 5;

	/** Number of player kills required for First Flight. */
	private static final int THREE_KILLS_TARGET = 3;
	/** The id we use for the weakest ship. */
	public static final String STARTER_SHIP_ID = "starter";
	/** Level that must be cleared to unlock Endless Mode. */
	private static final int ENDLESS_UNLOCK_LEVEL = 10;
	/** Identifier of the Infinity Void achievement. */
	private static final String INFINITY_VOID_ID = "infinity_void";

	public static final String LEVEL10_ALL_SHIPS_ID = "level10_all_ships";
	/** Empty until the ship team supplies the complete required roster. */
	private Set<String> requiredLevel10Ships = Collections.emptySet();
	private boolean level10RosterConfigured;
	private Achievement level10AllShips;

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
		addStarterShipWinAchievement();
		addInfinityVoidAchievement();
		addFirstBossKill();
		addFleetMasterAchievement();


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

	/** Adds the Humble Beginnings achievement. */
	private void addStarterShipWinAchievement() {
		addNormalAchievement(new Achievement("starter_ship_win",
				"Humble Beginnings", "Beat the game with the starter ship.", 0,
				SpriteType.Weakestship, this.playerProfile
				.isAchievementUnlocked("starter_ship_win"), Color.RED));
	}

	/**
	 * Adds the Infinity Void achievement. It has no kill requirement (0);
	 * recordLevelCompleted() unlocks it.
	 */
	private void addInfinityVoidAchievement() {
		addNormalAchievement(new Achievement(INFINITY_VOID_ID,
				"Infinity Void", "Clear level " + ENDLESS_UNLOCK_LEVEL
						+ " to unlock Endless Mode.", 0,
				SpriteType.InfinityVoid, this.playerProfile
						.isAchievementUnlocked(INFINITY_VOID_ID),
				new Color(160, 32, 240)));
	}
    /**
     * Adds the Buggin' the Boss achievement.
     */
    private void addFirstBossKill() {
        addNormalAchievement(new Achievement(
                "first_boss_kill",
                "Buggin' the Boss",
                "Defeat a boss for the first time.",
                0, SpriteType.BossKill,
                this.playerProfile.isAchievementUnlocked("first_boss_kill"),
                Color.ORANGE));
    }

	/** Adds the Fleet Master achievement.It has no kill requirement(0)
	 * */
	private void addFleetMasterAchievement(){
		this.level10AllShips = new Achievement(LEVEL10_ALL_SHIPS_ID,
				"Fleet Master", "Clear level 10 with every ship.", 0,
				SpriteType.FleetMaster,
				this.playerProfile.isAchievementUnlocked(LEVEL10_ALL_SHIPS_ID),
				Requirement.LEVEL10_ALL_SHIPS);
		addNormalAchievement(this.level10AllShips);
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
	@SuppressWarnings("unused")
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
			if (achievement.getRequirement() == Requirement.ENEMY_KILLS
					&& !achievement.isUnlocked()
					&& achievement.getRequiredEnemyKills() > 0
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

	/**
	 * Configures the complete roster once per manager, including locked ships.
	 * Call during startup when the ship model catalogue is available.
	 * IDs must be stable save keys, not translated display names or sprites.
	 */
	public final void configureLevel10Ships(final Set<String> shipIds) {
		if (shipIds == null || shipIds.isEmpty())
			throw new IllegalArgumentException("Required ship roster is empty.");
		for (String id : shipIds)
			if (id == null || !id.matches("[A-Za-z0-9_.-]+"))
				throw new IllegalArgumentException("Invalid stable ship ID: " + id);
		if (this.level10RosterConfigured
				&& !this.requiredLevel10Ships.equals(shipIds))
			throw new IllegalStateException("Ship roster already configured.");
		this.requiredLevel10Ships = new HashSet<String>(shipIds);
		this.level10RosterConfigured = true;
	}

	/**
	 * Records a level result. Only a confirmed level 10 victory qualifies.
	 * Call once at level completion, using the model used in that level.
	 * @return Newly unlocked achievement, or null.
	 */
	public final Achievement recordLevelCompleted(final int level,
			final String shipId, final boolean victory) {
		if (!victory || level != 10 || !this.level10RosterConfigured
				|| !this.requiredLevel10Ships.contains(shipId))
			return null;
		boolean changed = this.playerProfile.recordLevel10Completed(shipId);
		Achievement newlyUnlocked = null;
		if (!this.level10AllShips.isUnlocked()
				&& this.playerProfile.getLevel10CompletedShips()
						.containsAll(this.requiredLevel10Ships)) {
			this.level10AllShips.unlock();
			this.playerProfile.unlockAchievement(LEVEL10_ALL_SHIPS_ID);
			newlyUnlocked = this.level10AllShips;
			changed = true;
		}
		if (changed)
			saveProfile();
		return newlyUnlocked;
	}

	/** @return Distinct required ships that have cleared level 10. */
	public final int getLevel10CompletedShipCount() {
		Set<String> completed = this.playerProfile.getLevel10CompletedShips();
		completed.retainAll(this.requiredLevel10Ships);
		return completed.size();
	}

	/** @return Requirement text suitable for the achievement screen. */
	public final String getRequirementText(final Achievement achievement) {
		if (achievement.getRequirement() == Requirement.LEVEL10_ALL_SHIPS
				|| achievement.getRequiredEnemyKills() <= 0)
			return achievement.getDescription();
		return "Unlock: defeat " + achievement.getRequiredEnemyKills()
				+ " enemies.";
	}

	/** @return Unlocked status or progress for the appropriate condition. */
	public final String getProgressText(final Achievement achievement) {
		if (achievement.isUnlocked())
			return "UNLOCKED";
		if (achievement.getRequirement() == Requirement.LEVEL10_ALL_SHIPS) {
			if (!this.level10RosterConfigured)
				return "PENDING";
			return getLevel10CompletedShipCount() + "/"
					+ this.requiredLevel10Ships.size();
		}
		if (achievement.getRequiredEnemyKills() <= 0)
			return "LOCKED";
		return getTotalEnemiesKilled() + "/" + achievement.getRequiredEnemyKills();
	}

	/**
	 * Called when the player beats the game.
	 *
	 * @param shipId Ship used for this run.
	 * @return Newly unlocked achievement, or null.
	 */
	public final Achievement recordGameWon(final String shipId) {
		Achievement unlockedAchievement = null;
		for (Achievement achievement : getAchievements())
			if (!achievement.isUnlocked()
					&& achievement.getId().equals("starter_ship_win")
					&& STARTER_SHIP_ID.equals(shipId)) {
				achievement.unlock();
				this.playerProfile.unlockAchievement(achievement.getId());
				unlockedAchievement = achievement;
			}
		if (unlockedAchievement != null)
			saveProfile();
		return unlockedAchievement;
	}

	/**
	 * Records that the player cleared a level.
	 *
	 * @param level Number of the level just cleared.
	 * @return Newly unlocked achievement, or null when nothing unlocks.
	 */
	public final Achievement recordLevelCompleted(final int level) {
		if (level >= ENDLESS_UNLOCK_LEVEL)
			return unlockById(INFINITY_VOID_ID);
		return null;
	}
    /**
     * Records the first boss defeat.
     *
     * @return Newly unlocked achievement, or null if already unlocked.
     */
    public final Achievement recordBossDefeated() {
        return unlockById("first_boss_kill");
    }

	/**
	 * Unlocks one achievement by identifier and saves the progress.
	 *
	 * @param id Identifier of the achievement to unlock.
	 * @return The achievement if it was just unlocked, otherwise null.
	 */
	private Achievement unlockById(final String id) {
		for (Achievement achievement : getAchievements())
			if (achievement.getId().equals(id) && !achievement.isUnlocked()) {
				achievement.unlock();
				this.playerProfile.unlockAchievement(id);
				saveProfile();
				return achievement;
			}
		return null;
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