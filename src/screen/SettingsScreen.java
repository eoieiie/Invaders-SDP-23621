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
				currentMenuItem = Math.max(0, currentMenuItem - 1);
				this.selectionCooldown.reset();
			} else if (this.inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
				currentMenuItem = Math.min(2, currentMenuItem + 1);
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

			if (this.inputManager.isKeyDown(KeyEvent.VK_SPACE) || this.inputManager.isKeyDown(KeyEvent.VK_ENTER)) {
				if (currentMenuItem == 2) {
					this.isRunning = false;
				}
				this.selectionCooldown.reset();
			}
		}
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawScreenTitle(
				this, MenuItem.SETTINGS.getTitle());
		int baseY = this.getHeight() / 3;
		String bgmStr = "BGM Volume: < " + bgmVolume + "% >";
		String sfxStr = "SFX Volume: < " + sfxVolume + "% >";

		String m0 = (currentMenuItem == 0 ? "-> " : "") + bgmStr;
		String m1 = (currentMenuItem == 1 ? "-> " : "") + sfxStr;
		String m2 = "Key Bindings";
		String m3 = (currentMenuItem == 2 ? "-> " : "") + "Back";

		this.drawManager.drawMenuRow(this, m0, baseY,
				currentMenuItem == 0);
		this.drawManager.drawMenuRow(this, m1, baseY + SPACING,
				currentMenuItem == 1);

		this.drawManager.drawMenuRow(this, m2, baseY + SPACING * 3, false);

		this.drawManager.drawMenuRow(this, "- Move Left: A / Left Arrow -",
				baseY + SPACING * 4, false);
		this.drawManager.drawMenuRow(this, "- Move Right: D / Right Arrow -",
				baseY + SPACING * 5, false);
		this.drawManager.drawMenuRow(this, "- Shoot: Space -",
				baseY + SPACING * 6, false);
		this.drawManager.drawMenuRow(this, "- Back: ESC -",
				baseY + SPACING * 7, false);

		this.drawManager.drawMenuRow(this, m3,
				baseY + SPACING * 9, currentMenuItem == 2);

		this.drawManager.completeDrawing(this);
	}
}
