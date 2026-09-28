package engine;

/** Defines an achievement shown to the player. */
public class Achievement {

	/** Persistent achievement identifier. */
	private String id;
	/** Player-facing achievement name. */
	private String name;
	/** Player-facing achievement description. */
	private String description;
	/** Player kills required to unlock this achievement. */
	private int requiredEnemyKills;
	/** Whether the achievement is unlocked. */
	private boolean unlocked;

	/**
	 * Creates an achievement definition.
	 *
	 * @param id Persistent identifier.
	 * @param name Player-facing name.
	 * @param description Player-facing description.
	 * @param requiredEnemyKills Player kills required to unlock.
	 * @param unlocked Whether the achievement is unlocked.
	 */
	public Achievement(final String id, final String name,
			final String description, final int requiredEnemyKills,
			final boolean unlocked) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.requiredEnemyKills = requiredEnemyKills;
		this.unlocked = unlocked;
	}

	/** @return Persistent identifier. */
	public final String getId() {
		return this.id;
	}

	/** @return Player-facing name. */
	public final String getName() {
		return this.name;
	}

	/** @return Player-facing description. */
	public final String getDescription() {
		return this.description;
	}

	/** @return Player kills required to unlock. */
	public final int getRequiredEnemyKills() {
		return this.requiredEnemyKills;
	}

	/** @return Whether this achievement is unlocked. */
	public final boolean isUnlocked() {
		return this.unlocked;
	}

	/** Marks this achievement as unlocked. */
	public final void unlock() {
		this.unlocked = true;
	}
}