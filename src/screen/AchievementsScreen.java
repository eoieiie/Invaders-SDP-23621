package screen;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.List;
import engine.Achievement;
import engine.Cooldown;
import engine.Core;
import engine.DrawManager;

/**
 * Implements the achievements screen.
 */
public class AchievementsScreen extends Screen {
	/** Colour of the row the player has selected. */
	private static final Color SELECTED = Color.GREEN;
	/** Colour of rows that are not selected. */
	private static final Color UNSELECTED = Color.WHITE;
	/** Colour of secondary text and locked items. */
	private static final Color MUTED = Color.GRAY;
	/** Colour of an unlocked achievement's icon. */
	private static final Color UNLOCKED = Color.YELLOW;
	/** Colour of a locked achievement's icon. */
	private static final Color LOCKED = Color.DARK_GRAY;
	/** Vertical position of the first row. */
	private static final int FIRST_ROW_Y = 110;
	/** Vertical distance between rows. */
	private static final int ROW_SPACING = 52;
	/** Horizontal position of the trophy icon. */
	private static final int TROPHY_X = 30;
	/** Horizontal position of the name and description. */
	private static final int TEXT_X = 70;
	/** Horizontal position of the status area. */
	private static final int STATUS_X = 320;
	/** Time between selection moves, in milliseconds. */
	private static final int SELECTION_INTERVAL = 200;
	/** Achievements shown on this screen. */
	private List<Achievement> achievements;
	/** Enemies defeated across all games. */
	private int totalKills;
	/** Index of the currently selected row. */
	private int selected;
	/** Stops the selection moving on every frame. */
	private Cooldown selectionCooldown;

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param width  Screen width.
	 * @param height Screen height.
	 * @param fps    Frames per second.
	 */
	public AchievementsScreen(final int width, final int height,
			final int fps) {
		super(width, height, fps);
		// Return to the main menu when this screen closes.
		this.returnCode = 1;
		this.achievements = Core.getAchievementManager().getAchievements();
		this.totalKills = Core.getAchievementManager().getTotalEnemiesKilled();
		this.selected = 0;
		this.selectionCooldown = Core.getCooldown(SELECTION_INTERVAL);
		this.selectionCooldown.reset();
	}

	/**
	 * Starts the screen.
	 *
	 * @return Next screen code.
	 */
	@Override
	public final int run() {
		super.run();
		return this.returnCode;
	}

	/**
	 * PR 2 — Achievements screen layout: step by step
	 * Page 4 of 12
	 * Draws the screen and checks for keyboard input.
	 */
	@Override
	protected final void update() {
		super.update();
		draw();
		if (this.inputDelay.checkFinished()
				&& this.selectionCooldown.checkFinished()) {
			if (this.inputManager.isKeyDown(KeyEvent.VK_UP)
					||
					this.inputManager.isKeyDown(KeyEvent.VK_W)) {
				if (this.selected > 0) {
					this.selected--;
					this.selectionCooldown.reset();
				}
			}
			if (this.inputManager.isKeyDown(KeyEvent.VK_DOWN)
					||
					this.inputManager.isKeyDown(KeyEvent.VK_S)) {
				if (this.selected < this.achievements.size()
						- 1) {
					this.selected++;
					this.selectionCooldown.reset();
				}
			}
		}
		if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
				&& this.inputDelay.checkFinished()) {
			this.isRunning = false;
		}
	}

	/**
	 * Draws the title, one row per achievement, and the key hints.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawScreenTitle(
				this, MenuItem.ACHIEVEMENTS.getTitle());
		for (int i = 0; i < this.achievements.size(); i++)
			drawAchievement(this.achievements.get(i),
					FIRST_ROW_Y + i * ROW_SPACING, i == this.selected);
		this.drawManager.drawKeyHints(this, "up down move, esc back");
		this.drawManager.completeDrawing(this);
	}

	/**
	 * Draws a single achievement row.
	 *
	 * @param achievement Achievement to draw.
	 * @param positionY   Vertical position of the row's top edge.
	 * @param isSelected  Whether this row is currently highlighted.
	 */
	private void drawAchievement(final Achievement achievement,
			final int positionY, final boolean isSelected) {
		Color trophyColor;
		if (achievement.isUnlocked())
			trophyColor = UNLOCKED;
		else
			trophyColor = LOCKED;
		Color nameColor;
		if (isSelected)
			nameColor = SELECTED;
		else
			nameColor = UNSELECTED;
		this.drawManager.drawSprite(DrawManager.SpriteType.FirstFlight,
				TROPHY_X, positionY, trophyColor);
		this.drawManager.drawRegularString(achievement.getName(),
				TEXT_X, positionY + 8, nameColor);
		this.drawManager.drawRegularString("Unlock: defeat "
				+ achievement.getRequiredEnemyKills() + " enemies.", TEXT_X,
				positionY + 24, MUTED);
		if (achievement.isUnlocked())
			this.drawManager.drawRegularString("UNLOCKED",
					STATUS_X, positionY + 8, SELECTED);
		else
			this.drawManager.drawRegularString(
					this.totalKills + "/" +
							achievement.getRequiredEnemyKills(),
					STATUS_X, positionY + 8, MUTED);
	}
}
