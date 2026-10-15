package screen;

import engine.Cooldown;
import engine.Core;
import java.awt.event.KeyEvent;
import engine.soundEngine.Sound;
import engine.soundEngine.SoundManager;

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
	/** get volume from soundManager */
	private final SoundManager soundManager = SoundManager.getInstance();
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

	/** Returns the current volume as a percentage. */
	private int getVolumePercent(final Sound.Category category) {
		return Math.round(this.soundManager.getVolume(category) * 100f);
	}

	/** Adjusts the selected category by the given percentage points. */
	private void adjustSelectedVolume(final int delta) {
		Sound.Category category = this.currentMenuItem == 0
				? Sound.Category.BGM
				: Sound.Category.SFX;

		int percent = Math.max(0,
				Math.min(100, getVolumePercent(category) + delta));

		this.soundManager.setVolume(category, percent / 100f);
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
				adjustSelectedVolume(-10);
				this.selectionCooldown.reset();
			} else if (this.inputManager.isKeyDown(KeyEvent.VK_RIGHT)) {
				adjustSelectedVolume(10);
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

		int bgmVolume = getVolumePercent(Sound.Category.BGM);
		int sfxVolume = getVolumePercent(Sound.Category.SFX);

		String bgmStr = "BGM Volume: " + bgmVolume + "%";
		String sfxStr = "SFX Volume: " + sfxVolume + "%";
		String m2 = "Key Bindings";

		this.drawManager.drawMenuRow(this, bgmStr, rowBaseline(0),
				currentMenuItem == 0);
		this.drawManager.drawMenuRow(this, sfxStr, rowBaseline(1),
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
