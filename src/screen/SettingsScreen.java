package screen;

import engine.Cooldown;
import engine.Core;
import java.awt.event.KeyEvent;

/**
 * Implements the settings screen.
 */
public class SettingsScreen extends Screen {
	/** Milliseconds between selection steps. */
	private static final int SELECTION_TIME = 200;
	/** Spacing between menu items. */
	private static final int SPACING = 30;
	/** Cooldown for menu selection and volume changes. */
	private final Cooldown selectionCooldown;
	/** Current background music volume. */
	private int bgmVolume = 50;
	/** Current sound effects volume. */
	private int sfxVolume = 50;
	/** Current selected menu item. */
	private int currentMenuItem = 0;

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public SettingsScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
		this.returnCode = 1;
		this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
		this.selectionCooldown.reset();
	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();
		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();
		draw();

		if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE) && this.inputDelay.checkFinished()) {
			this.isRunning = false;
		}

		if (this.selectionCooldown.checkFinished()) {
			if (this.inputManager.isKeyDown(KeyEvent.VK_UP)) {
				currentMenuItem = (currentMenuItem + 1) % 2;
				this.selectionCooldown.reset();
			} else if (this.inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
				currentMenuItem = (currentMenuItem + 1) % 2;
				this.selectionCooldown.reset();
			}

			if (this.inputManager.isKeyDown(KeyEvent.VK_LEFT)) {
				if (currentMenuItem == 0) bgmVolume = Math.max(0, bgmVolume - 10);
				else if (currentMenuItem == 1) sfxVolume = Math.max(0, sfxVolume - 10);
				this.selectionCooldown.reset();
			} else if (this.inputManager.isKeyDown(KeyEvent.VK_RIGHT)) {
				if (currentMenuItem == 0) bgmVolume = Math.min(100, bgmVolume + 10);
				else if (currentMenuItem == 1) sfxVolume = Math.min(100, sfxVolume + 10);
				this.selectionCooldown.reset();
			}
		}

		if (this.inputManager.isMouseMoved()) {
			int mouseY = this.inputManager.getMouseY();
			for (int row = 0; row < 2; row++)
				if (this.drawManager.isOnMenuRow(rowBaseline(row), SPACING,
						mouseY))
					currentMenuItem = row;
		}
	}

	/**
	 * Baseline of a row, counted from the first volume row. Drawing and
	 * mouse hover both use this, so they cannot drift apart.
	 *
	 * @param row
	 *            Row index, from the top.
	 * @return Baseline of the row's text.
	 */
	private int rowBaseline(final int row) {
		return this.getHeight() / 3 + SPACING * row;
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawScreenTitle(
				this, MenuItem.SETTINGS.getTitle());
		String bgmStr = "BGM Volume: < " + bgmVolume + "% >";
		String sfxStr = "SFX Volume: < " + sfxVolume + "% >";

        String m0 = (currentMenuItem == 0 ? "> " + bgmStr + " <" : bgmStr);
        String m1 = (currentMenuItem == 1 ? "> " + sfxStr + " <" : sfxStr);
		String m2 = "Key Bindings";

		this.drawManager.drawMenuRow(this, m0, rowBaseline(0),
				currentMenuItem == 0);
		this.drawManager.drawMenuRow(this, m1, rowBaseline(1),
				currentMenuItem == 1);

		this.drawManager.drawMenuRow(this, m2, rowBaseline(3), false);

		this.drawManager.drawMenuRow(this, "- Move Left: A / Left Arrow -",
				rowBaseline(4), false);
		this.drawManager.drawMenuRow(this, "- Move Right: D / Right Arrow -",
				rowBaseline(5), false);
		this.drawManager.drawMenuRow(this, "- Shoot: Space -",
				rowBaseline(6), false);
		this.drawManager.drawMenuRow(this, "- Back: ESC -",
				rowBaseline(7), false);
		this.drawManager.drawKeyHints(this, "arrows adjust, esc back");

		this.drawManager.completeDrawing(this);
	}
}
