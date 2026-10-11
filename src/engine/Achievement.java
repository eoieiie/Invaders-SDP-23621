package engine;

import java.awt.Color;

import engine.DrawManager.SpriteType;

/** Defines an achievement shown to the player. */
public class Achievement {

	/** Events that can unlock an achievement. */
	public enum Requirement { ENEMY_KILLS, LEVEL10_ALL_SHIPS }
	private final Requirement requirement;

	/** Persistent achievement identifier. */
	private String id;
	/** Player-facing achievement name. */
	private String name;
	/** Player-facing achievement description. */
	private String description;
	/** Player kills required to unlock this achievement. */
	private int requiredEnemyKills;
	/** Icon displayed for this achievement. */
	private SpriteType spriteType;
	/** Colour of the icon once unlocked. */
	private Color iconColor;
	/** Whether the achievement is unlocked. */
	private boolean unlocked;

	/**
	 * Creates an achievement definition.
	 *
	 * @param id Persistent identifier.
	 * @param name Player-facing name.
	 * @param description Player-facing description.
	 * @param requiredEnemyKills Player kills required to unlock.
	 * @param spriteType Icon displayed for this achievement.
	 * @param unlocked Whether the achievement is unlocked.
	 */
	public Achievement(final String id, final String name,
			final String description, final int requiredEnemyKills,
			final SpriteType spriteType, final boolean unlocked) {
		this(id, name, description, requiredEnemyKills, spriteType, unlocked,
				Requirement.ENEMY_KILLS);
	}

	/** Creates an achievement with an explicit unlocking condition. */
	public Achievement(final String id, final String name,
			final String description, final int requiredEnemyKills,
			final SpriteType spriteType, final boolean unlocked,
			final Requirement requirement) {
		this.requirement = requirement;
		this.id = id;
		this.name = name;
		this.description = description;
		this.requiredEnemyKills = requiredEnemyKills;
		this.spriteType = spriteType;
		this.unlocked = unlocked;
		this.iconColor = Color.YELLOW;
	}

	/**
	 * Creates an achievement definition with a custom icon colour.
	 *
	 * @param id Persistent identifier.
	 * @param name Player-facing name.
	 * @param description Player-facing description.
	 * @param requiredEnemyKills Player kills required to unlock.
	 * @param spriteType Icon displayed for this achievement.
	 * @param unlocked Whether the achievement is unlocked.
	 * @param iconColor Colour of the icon once unlocked.
	 */
	public Achievement(final String id, final String name,
			final String description, final int requiredEnemyKills,
			final SpriteType spriteType, final boolean unlocked,
			final Color iconColor) {
		this(id, name, description, requiredEnemyKills, spriteType, unlocked);
		this.iconColor = iconColor;
	}

	/** @return Event used to evaluate this achievement. */
	public final Requirement getRequirement() {
		return this.requirement;
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

	/** @return Icon displayed for this achievement. */
	public final SpriteType getSpriteType() {
		return this.spriteType;
	}

	/** @return Colour of the icon once unlocked. */
	public final Color getIconColor() {
		return this.iconColor;
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