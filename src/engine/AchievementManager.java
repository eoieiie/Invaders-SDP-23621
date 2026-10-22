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
	public static final int ACHIEVEMENTS_PER_PAGE = 6;

	/** Number of player kills required for First Flight. */
	private static final int THREE_KILLS_TARGET = 3;
	/** Number of lifetime kills required for Bronze Hunter. */
	private static final int BRONZE_HUNTER_KILLS_TARGET = 100;
	/** Number of lifetime kills required for Silver Hunter. */
	private static final int SILVER_HUNTER_KILLS_TARGET = 500;
	/** Number of lifetime kills required for Gold Hunter. */
	private static final int GOLD_HUNTER_KILLS_TARGET = 1000;
	/** Flawless levels required for Bronze Survivor. */
	private static final int BRONZE_SURVIVOR_TARGET = 1;
	/** Flawless levels required for Silver Survivor. */
	private static final int SILVER_SURVIVOR_TARGET = 4;
	/** Flawless levels required for Gold Survivor. */
	private static final int GOLD_SURVIVOR_TARGET = 10;
	/** Accuracy thresholds for the tiered accuracy achievements. */
	private static final int ACCURACY_60_TARGET = 60;
	private static final int ACCURACY_90_TARGET = 90;
	private static final int ACCURACY_100_TARGET = 100;
	/** Levels that must be cleared in the run for accuracy achievements. */
	private static final int ACCURACY_LEVELS_TARGET = 3;
    /** thresholds for the tiered currency achievements. */
    private static final int RUNCOIN_10_TARGET = 10;
    private static final int RUNCOIN_25_TARGET = 25;
    private static final int RUNCOIN_50_TARGET = 50;
	/** The id we use for the weakest ship. */
	public static final String STARTER_SHIP_ID = "starter";
	/** Level that must be cleared to unlock Endless Mode. */
	private static final int ENDLESS_UNLOCK_LEVEL = 10;
	/** Identifier of the Infinity Void achievement. */
	private static final String INFINITY_VOID_ID = "infinity_void";
	/** Keep track of item used by player*/
	private static final int[] ITEM_USES_TARGETS = {100, 250, 500};

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
		addBronzeHunterAchievement();
		addSilverHunterAchievement();
		addGoldHunterAchievement();
		addBronzeSurvivorAchievement();
		addSilverSurvivorAchievement();
		addGoldSurvivorAchievement();
		addAccuracyAchievements();
        addRunCoinAchievements();
		addItemUseAchievements();
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
				"Fleet Master", "Beat the game with every ship.", 0,
				SpriteType.FleetMaster,
				this.playerProfile.isAchievementUnlocked(LEVEL10_ALL_SHIPS_ID),
				Requirement.LEVEL10_ALL_SHIPS);
		addNormalAchievement(this.level10AllShips);
	}

	/** Adds the Bronze Hunter lifetime-kill tier achievement. */
	private void addBronzeHunterAchievement() {
		addTierAchievement(new Achievement("bronze_hunter", "Bronze Hunter",
				"Defeat " + BRONZE_HUNTER_KILLS_TARGET + " enemies.",
				BRONZE_HUNTER_KILLS_TARGET, SpriteType.Hunter,
				this.playerProfile.isAchievementUnlocked("bronze_hunter"),
				new Color(205, 127, 50)));
	}

	/** Adds the Silver Hunter lifetime-kill tier achievement. */
	private void addSilverHunterAchievement() {
		addTierAchievement(new Achievement("silver_hunter", "Silver Hunter",
				"Defeat " + SILVER_HUNTER_KILLS_TARGET + " enemies.",
				SILVER_HUNTER_KILLS_TARGET, SpriteType.Hunter,
				this.playerProfile.isAchievementUnlocked("silver_hunter"),
				new Color(192, 192, 192)));
	}

	/** Adds the Gold Hunter lifetime-kill tier achievement. */
	private void addGoldHunterAchievement() {
		addTierAchievement(new Achievement("gold_hunter", "Gold Hunter",
				"Defeat " + GOLD_HUNTER_KILLS_TARGET + " enemies.",
				GOLD_HUNTER_KILLS_TARGET, SpriteType.Hunter,
				this.playerProfile.isAchievementUnlocked("gold_hunter"),
				new Color(255, 215, 0)));
	}

	/** Adds the Bronze survival tier for the first flawless level. */
	private void addBronzeSurvivorAchievement() {
		addTierAchievement(new Achievement("bronze_survivor",
				"Bronze Survivor", "Clear 1 level without taking damage.",
				BRONZE_SURVIVOR_TARGET,
				SpriteType.Trophy,
				this.playerProfile.isAchievementUnlocked("bronze_survivor"),
				Requirement.FLAWLESS_LEVELS, new Color(205, 127, 50)));
	}

	/** Adds the Silver survival tier for four flawless levels. */
	private void addSilverSurvivorAchievement() {
		addTierAchievement(new Achievement("silver_survivor",
				"Silver Survivor", "Clear 4 levels without taking damage.",
				SILVER_SURVIVOR_TARGET,
				SpriteType.Trophy,
				this.playerProfile.isAchievementUnlocked("silver_survivor"),
				Requirement.FLAWLESS_LEVELS, new Color(192, 192, 192)));
	}

	/** Adds the Gold survival tier for ten flawless levels. */
	private void addGoldSurvivorAchievement() {
		addTierAchievement(new Achievement("gold_survivor", "Gold Survivor",
				"Clear 10 levels without taking damage.", GOLD_SURVIVOR_TARGET,
				SpriteType.Trophy,
				this.playerProfile.isAchievementUnlocked("gold_survivor"),
				Requirement.FLAWLESS_LEVELS, new Color(255, 215, 0)));
	}

	/** Adds the 60%, 90%, and 100% accuracy achievements. */
	private void addAccuracyAchievements() {
		addAccuracyAchievement("accuracy_60", "Bronze Sharpshooter",
				ACCURACY_60_TARGET, Color.GREEN);
		addAccuracyAchievement("accuracy_90", "Silver Sharpshooter",
				ACCURACY_90_TARGET, Color.YELLOW);
		addAccuracyAchievement("accuracy_100", "Gold Sharpshooter",
				ACCURACY_100_TARGET, Color.RED);
	}

    /** Adds the 10, 25, and 50 coins per run achievements. */
    private void addRunCoinAchievements() {
        addRunCoinAchievement("runcoin_10", "Bronze Looter",
                RUNCOIN_10_TARGET, new Color(205, 127, 50));
        addRunCoinAchievement("runcoin_25", "Silver Looter",
                RUNCOIN_25_TARGET, new Color(192, 192, 192));
        addRunCoinAchievement("runcoin_50", "Gold Looter",
                RUNCOIN_50_TARGET, new Color(255, 215, 0));
    }

	/** Adds one accuracy achievement to the tier page. */
	private void addAccuracyAchievement(final String id, final String name,
			final int target, final Color color) {
		addTierAchievement(new Achievement(id, name,
				"Finish with " + target + "% accuracy on "
						+ ACCURACY_LEVELS_TARGET + " levels.", target,
				SpriteType.Accuracytarget,
				this.playerProfile.isAchievementUnlocked(id),
				Requirement.ACCURACY, color));
	}

    /** Adds one coins in one run achievement to the tier page. */
    private void addRunCoinAchievement(final String id, final String name,
                                       final int target, final Color color) {
        addTierAchievement(new Achievement(id, name,
                "Obtain " + target + " coins in one run. ", target,
                SpriteType.Looter,
                this.playerProfile.isAchievementUnlocked(id),
                Requirement.RUNCOINS, color));
    }

	/** Adds the three item-use tier achievements. */
	private void addItemUseAchievements() {
		addTierAchievement(new Achievement(
				"item_use_bronze",
				"Bronze Item User",
				"Successfully use 100 items.",
				ITEM_USES_TARGETS[0],
				SpriteType.ItemCol,
				this.playerProfile.isAchievementUnlocked("item_use_bronze"),
				Requirement.ITEM_USES,
				new Color(205, 127, 50)));

		addTierAchievement(new Achievement(
				"item_use_silver",
				"Silver Item User",
				"Successfully use 250 items.",
				ITEM_USES_TARGETS[1],
				SpriteType.ItemCol,
				this.playerProfile.isAchievementUnlocked("item_use_silver"),
				Requirement.ITEM_USES,
				new Color(192, 192, 192)));

		addTierAchievement(new Achievement(
				"item_use_gold",
				"Gold Item User",
				"Successfully use 500 items.",
				ITEM_USES_TARGETS[2],
				SpriteType.ItemCol,
				this.playerProfile.isAchievementUnlocked("item_use_gold"),
				Requirement.ITEM_USES,
				new Color(255, 215, 0)));
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


	/** Records one successfully used item and checks item-use tiers. */

	/** Records one successfully used item and checks item-use tiers. */
	public final Achievement recordItemUsed() {
		this.playerProfile.recordItemUsed();
		Achievement unlockedAchievement = null;

		for (Achievement achievement : this.tierAchievements) {
			if (!achievement.getId().startsWith("item_use_")
					|| achievement.isUnlocked()
					|| this.playerProfile.getTotalItemsUsed()
					< achievement.getRequiredEnemyKills()) {
				continue;
			}

			achievement.unlock();
			this.playerProfile.unlockAchievement(achievement.getId());
			unlockedAchievement = achievement;
		}

		saveProfile();
		return unlockedAchievement;
	}



	/**
	 * Records final run accuracy and unlocks every achieved accuracy tier.
	 *
	 * @param bulletsShot Number of shots fired in the run.
	 * @param shipsDestroyed Number of enemy ships destroyed in the run.
	 * @param levelsCompleted Number of levels cleared alive in the run.
	 * @return The highest newly unlocked accuracy achievement, or null.
	 */
	public final Achievement recordAccuracy(final int bulletsShot,
			final int shipsDestroyed, final int levelsCompleted) {
		if (bulletsShot <= 0 || shipsDestroyed < 0
				|| levelsCompleted < ACCURACY_LEVELS_TARGET)
			return null;

		float accuracy = 100.0f * shipsDestroyed / bulletsShot;
		Achievement highestUnlocked = null;
		for (Achievement achievement : this.tierAchievements) {
			if (achievement.getRequirement() != Requirement.ACCURACY
					|| achievement.isUnlocked()
					|| accuracy < achievement.getRequiredEnemyKills())
				continue;
			achievement.unlock();
			this.playerProfile.unlockAchievement(achievement.getId());
			highestUnlocked = achievement;
		}
		if (highestUnlocked != null)
			saveProfile();
		return highestUnlocked;
	}

    /**
     * Records coins obtained in this run and unlocks every achieved accuracy tier.
     *
     * @param coinsCollected Number of coins collected in the run.
     * @return The highest newly unlocked accuracy achievement, or null.
     */
    public final Achievement recordRunCoins(final int coinsCollected) {
        Achievement highestUnlocked = null;
        for (Achievement achievement : this.tierAchievements) {
            if (achievement.getRequirement() != Requirement.RUNCOINS
                    || achievement.isUnlocked()
                    || coinsCollected < achievement.getRequiredEnemyKills())
                continue;
            achievement.unlock();
            this.playerProfile.unlockAchievement(achievement.getId());
            highestUnlocked = achievement;
        }
        if (highestUnlocked != null)
            saveProfile();
        return highestUnlocked;
    }

	/**
	 * Records one level clear for the survival achievement tiers.
	 *
	 * @param tookDamage Whether the player was hit during the level.
	 * @return Newly unlocked survival achievements, in tier order.
	 */
	public final List<Achievement> recordFlawlessLevelCompleted(
			final boolean tookDamage) {
		List<Achievement> unlockedAchievements = new ArrayList<Achievement>();
		if (tookDamage)
			return unlockedAchievements;
		this.playerProfile.recordFlawlessLevelCleared();
		addUnlockedSurvivalAchievement(unlockedAchievements, "bronze_survivor",
				this.playerProfile.getFlawlessLevelsCleared()
						>= BRONZE_SURVIVOR_TARGET);
		addUnlockedSurvivalAchievement(unlockedAchievements, "silver_survivor",
				this.playerProfile.getFlawlessLevelsCleared()
						>= SILVER_SURVIVOR_TARGET);
		addUnlockedSurvivalAchievement(unlockedAchievements, "gold_survivor",
				this.playerProfile.getFlawlessLevelsCleared()
						>= GOLD_SURVIVOR_TARGET);
		saveProfile();
		return unlockedAchievements;
	}

	/** Adds a newly unlocked survival achievement to the result popup queue. */
	private void addUnlockedSurvivalAchievement(
			final List<Achievement> unlockedAchievements, final String id,
			final boolean conditionMet) {
		if (!conditionMet)
			return;
		Achievement achievement = unlockById(id);
		if (achievement != null)
			unlockedAchievements.add(achievement);
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
		if (!victory || level != Core.getNumLevels()
				|| !this.level10RosterConfigured
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
		if (achievement.getId().startsWith("item_use_"))
			return achievement.getDescription();
		if (achievement.getRequirement() == Requirement.ACCURACY)
			return achievement.getDescription();
		if (achievement.getRequirement() == Requirement.LEVEL10_ALL_SHIPS
				|| achievement.getRequirement() == Requirement.FLAWLESS_LEVELS
                || achievement.getRequirement() == Requirement.RUNCOINS
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
		if (achievement.getRequirement() == Requirement.FLAWLESS_LEVELS)
			return this.playerProfile.getFlawlessLevelsCleared() + "/"
					+ achievement.getRequiredEnemyKills();
		if (achievement.getRequirement() == Requirement.ACCURACY)
			return achievement.getRequiredEnemyKills() + "%";
		if (achievement.getRequiredEnemyKills() <= 0 || achievement.getRequirement() == Requirement.RUNCOINS)
			return "LOCKED";
		if (achievement.getId().startsWith("item_use_"))
			return this.playerProfile.getTotalItemsUsed() + "/"
					+ achievement.getRequiredEnemyKills();
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