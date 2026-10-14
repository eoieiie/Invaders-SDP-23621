package engine.soundEngine;

/**
 * Logical sound identifiers mapped to their audio files.
 *
 * <p>Call sites reference these constants only, never file names. That keeps
 * the engine branch independent from the branches that add the actual audio
 * files: as long as a constant and its file name match, both teams can work
 * in parallel without touching each other's lines.</p>
 *
 * <p>Files are resolved from the classpath, so they live in
 * {@code res/sounds/} next to the existing {@code res/graphics} resources.
 * All files must be uncompressed {@code .wav} (PCM), because the Java Sound
 * API cannot play {@code .mp3} or {@code .ogg} out of the box.</p>
 *
 * @author Invaders Sound Team
 */
public enum Sound {

	/** Player bullet fired. */
	SHOOT("sounds/shoot.wav", Category.SFX),
	/** Enemy ship destroyed. */
	ENEMY_EXPLOSION("sounds/enemy_explosion.wav", Category.SFX),
	/** Player ship destroyed. */
	PLAYER_EXPLOSION("sounds/player_explosion.wav", Category.SFX),
	/** Coin picked up. */
	COIN_PICKUP("sounds/coin_pickup.wav", Category.SFX),
	/** Diamond picked up. */
	DIAMOND_PICKUP("sounds/diamond_pickup.wav", Category.SFX),
	/** Item bought in the shop. */
	PURCHASE("sounds/purchase.wav", Category.SFX),
	/** Cursor moved to another menu entry. */
	MENU_MOVE("sounds/menu_move.wav", Category.SFX),
	/** Menu entry confirmed. */
	MENU_SELECT("sounds/menu_select.wav", Category.SFX),
	/** Menu entry cancelled or denied. */
	MENU_CANCEL("sounds/menu_cancel.wav", Category.SFX),
	/** New level started. */
	LEVEL_UP("sounds/level_up.wav", Category.SFX),
	/** Achievement unlocked. */
	ACHIEVEMENT("sounds/achievement.wav", Category.SFX),
	/** Run ended without lives left. */
	GAME_OVER("sounds/game_over.wav", Category.SFX),

	/** Background music of the title screen. */
	BGM_TITLE("sounds/bgm_title.wav", Category.BGM),
	/** Background music played during a run. */
	BGM_GAME("sounds/bgm_game.wav", Category.BGM),
	/** Background music of the shop and other menus. */
	BGM_MENU("sounds/bgm_menu.wav", Category.BGM);

	/**
	 * Sound categories, used to apply separate volumes.
	 */
	public enum Category {
		/** Short, fire and forget effect. */
		SFX,
		/** Long, looping background music. */
		BGM
	}

	/** Classpath path of the audio file. */
	private final String path;
	/** Category the sound belongs to. */
	private final Category category;

	/**
	 * Constructor.
	 *
	 * @param path
	 *            Classpath path of the audio file.
	 * @param category
	 *            Category the sound belongs to.
	 */
	Sound(final String path, final Category category) {
		this.path = path;
		this.category = category;
	}

	/**
	 * Returns the classpath path of the audio file.
	 *
	 * @return Classpath path, relative to the resource root.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the category the sound belongs to.
	 *
	 * @return Sound category.
	 */
	public Category getCategory() {
		return category;
	}
}
