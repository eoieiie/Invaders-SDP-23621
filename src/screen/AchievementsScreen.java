package screen;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import engine.Achievement;
import engine.AchievementManager;
import engine.Cooldown;
import engine.Core;
import engine.DrawManager;

/**
 * Implements the achievements screen, split into pages.
 * Page 1 shows normal achievements, page 2 shows tier achievements.
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
	/** Vertical position of the page label. */
	private static final int PAGE_LABEL_Y = 95;
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
	/** Name of each page, in page order. */
	private static final String[] PAGE_NAMES = { "Normal", "Tier" };

	/** Achievements on each page, in page order. */
	private List<List<Achievement>> pages;
	/** Index of the page currently shown. */
	private int currentPage;
	/** Enemies defeated across all games. */
	private int totalKills;
	/** Index of the selected row on the current page. */
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
		AchievementManager manager = Core.getAchievementManager();
		this.pages = new ArrayList<List<Achievement>>();
		this.pages.add(manager.getNormalAchievements());
		this.pages.add(manager.getTierAchievements());
		this.totalKills = manager.getTotalEnemiesKilled();
		this.currentPage = 0;
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
	 * Draws the screen and checks for keyboard input.
	 */
	@Override
	protected final void update() {
		super.update();
		draw();
		if (this.inputDelay.checkFinished()
				&& this.selectionCooldown.checkFinished()) {
			if (this.inputManager.isKeyDown(KeyEvent.VK_UP)
					|| this.inputManager.isKeyDown(KeyEvent.VK_W)) {
				if (this.selected > 0) {
					this.selected--;
					this.selectionCooldown.reset();
				}
			}
			if (this.inputManager.isKeyDown(KeyEvent.VK_DOWN)
					|| this.inputManager.isKeyDown(KeyEvent.VK_S)) {
				if (this.selected < getRowCount() - 1) {
					this.selected++;
					this.selectionCooldown.reset();
				}
			}
			if (this.inputManager.isKeyDown(KeyEvent.VK_LEFT)
					|| this.inputManager.isKeyDown(KeyEvent.VK_A)) {
				if (this.currentPage > 0) {
					this.currentPage--;
					this.selected = 0;
					this.selectionCooldown.reset();
				}
			}
			if (this.inputManager.isKeyDown(KeyEvent.VK_RIGHT)
					|| this.inputManager.isKeyDown(KeyEvent.VK_D)) {
				if (this.currentPage < this.pages.size() - 1) {
					this.currentPage++;
					this.selected = 0;
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
	 * @return Number of rows shown on the current page, at most
	 *         {@link AchievementManager#ACHIEVEMENTS_PER_PAGE}.
	 */
	private int getRowCount() {
		return Math.min(this.pages.get(this.currentPage).size(),
				AchievementManager.ACHIEVEMENTS_PER_PAGE);
	}

	/**
	 * Draws the title, the page label, the current page's rows, and the
	 * key hints.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawScreenTitle(
				this, MenuItem.ACHIEVEMENTS.getTitle());

		String pageLabel = "< " + PAGE_NAMES[this.currentPage] + "  "
				+ (this.currentPage + 1) + "/" + this.pages.size() + " >";
		this.drawManager.drawMenuRow(this, pageLabel, PAGE_LABEL_Y, false);

		List<Achievement> page = this.pages.get(this.currentPage);
		if (page.isEmpty()) {
			this.drawManager.drawRegularString("No achievements yet.",
					TEXT_X, FIRST_ROW_Y + 24, MUTED);
		} else {
			for (int i = 0; i < getRowCount(); i++)
				drawAchievement(page.get(i),
						FIRST_ROW_Y + i * ROW_SPACING, i == this.selected);
		}

		this.drawManager.drawKeyHints(this,
				"left right page, up down move, esc back");
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
		DrawManager.SpriteType icon = achievement.getSpriteType();
		if (icon == null)
			icon = DrawManager.SpriteType.FirstFlight;
		this.drawManager.drawSprite(icon, TROPHY_X, positionY, trophyColor);
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