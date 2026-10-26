package screen;

import engine.Cooldown;
import engine.Core;
import java.awt.event.KeyEvent;
import java.awt.Color;
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
	/** Vertical distance between the BGM and SFX sliders. */
	private static final int SLIDER_ROW_SPACING = 80;
	/** Slider currently being dragged, or -1 when none is active. */
	private int draggingSlider = -1;
	/** Whether the left mouse button was pressed on the previous frame. */
	private boolean mouseWasPressed;

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

		this.mouseWasPressed = this.inputManager.isLeftMousePressed();
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
	 * Updates keyboard input, slider dragging and screen drawing.
	 */
	@Override
	protected final void update() {
		super.update();

		boolean mousePressed = this.inputManager.isLeftMousePressed();

		if (this.inputDelay.checkFinished()) {
			if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
				this.draggingSlider = -1;
				this.isRunning = false;
			} else {
				updateMouseVolume(mousePressed);

				// Keyboard changes are paused while a slider is being dragged.
				if (this.draggingSlider == -1
						&& this.selectionCooldown.checkFinished()) {

					if (this.inputManager.isKeyDown(KeyEvent.VK_UP)
							|| this.inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
						this.currentMenuItem = (this.currentMenuItem + 1) % 2;
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
			}
		}

		this.mouseWasPressed = mousePressed;
		draw();
	}

	/**
	 * Adjusts volume while dragging and updates mouse hover selection.
	 *
	 * @param mousePressed
	 *            Whether the left mouse button is currently held down.
	 */
	private void updateMouseVolume(final boolean mousePressed) {
		int mouseX = this.inputManager.getMouseX();
		int mouseY = this.inputManager.getMouseY();
		boolean moved = this.inputManager.isMouseMoved();

		// Start dragging only when a new press occurs on a slider.
		if (mousePressed && !this.mouseWasPressed) {
			for (int index = 0; index < 2; index++) {
				boolean onTrack =
						mouseX >= sliderLeft() - 6
								&& mouseX <= sliderLeft() + sliderWidth() + 6
								&& Math.abs(mouseY - sliderY(index)) <= 14;

				if (onTrack) {
					this.draggingSlider = index;
					this.currentMenuItem = index;
					break;
				}
			}
		}

		// Keep adjusting the original slider until the button is released.
		if (this.draggingSlider != -1) {
			int percent = Math.round(
					(mouseX - sliderLeft()) * 100f / sliderWidth());

			percent = Math.max(0, Math.min(100, percent));

			Sound.Category category = this.draggingSlider == 0
					? Sound.Category.BGM
					: Sound.Category.SFX;

			this.soundManager.setVolume(category, percent / 100f);

			if (!mousePressed)
				this.draggingSlider = -1;
		} else if (moved && !mousePressed) {
			for (int index = 0; index < 2; index++) {
				boolean insideControl =
						mouseX >= sliderLeft() - 6
								&& mouseX <= sliderLeft() + sliderWidth() + 6
								&& mouseY >= sliderY(index) - 36
								&& mouseY <= sliderY(index) + 14;

				if (insideControl) {
					this.currentMenuItem = index;
					break;
				}
			}
		}
	}

	/**
	 * Returns the horizontal starting position of the volume sliders.
	 *
	 * @return Left edge of the slider tracks.
	 */
	private int sliderLeft() {
		return this.getWidth() / 5;
	}

	/**
	 * Returns the distance between the 0% and 100% positions.
	 *
	 * @return Slider track width in pixels.
	 */
	private int sliderWidth() {
		return Math.max(1, this.getWidth() * 3 / 5);
	}

	/**
	 * Returns the vertical centre of a volume slider.
	 *
	 * @param index
	 *            Slider index: 0 for BGM, 1 for SFX.
	 * @return Vertical centre of the slider track.
	 */
	private int sliderY(final int index) {
		return this.getHeight() / 3 + index * SLIDER_ROW_SPACING;
	}

	/**
	 * Returns the baseline of a key-binding row below the sliders.
	 *
	 * @param row
	 *            Key-binding row index, starting from zero.
	 * @return Vertical position of the text baseline.
	 */
	private int bindingY(final int row) {
		return sliderY(1) + 60 + SPACING * row;
	}

	/**
	 * Draws a horizontal volume slider with its label and percentage.
	 *
	 * @param index
	 *            Slider index: 0 for BGM, 1 for SFX.
	 * @param label
	 *            Name displayed above the slider.
	 * @param category
	 *            Sound category whose volume is displayed.
	 */
	private void drawVolumeSlider(final int index, final String label,
								  final Sound.Category category) {

		int left = sliderLeft();
		int width = sliderWidth();
		int centerY = sliderY(index);
		int percent = getVolumePercent(category);
		int thumbX = left + Math.round(width * percent / 100f);

		Color color = this.currentMenuItem == index
				? Color.GREEN
				: Color.WHITE;

		// Draw the label and percentage using the shared selection style.
		this.drawManager.drawMenuRow(
				this, label + "  " + percent + "%",
				centerY - 22, this.currentMenuItem == index);

		// Unfilled track.
		this.drawManager.fillBox(
				left, centerY - 3, width + 1, 6, Color.DARK_GRAY);

		// Filled portion showing the current volume.
		this.drawManager.fillBox(
				left, centerY - 3, thumbX - left, 6, color);

		// Slider handle.
		this.drawManager.fillBox(
				thumbX - 5, centerY - 9, 11, 18, Color.WHITE);
	}

	/**
	 * Draws the volume sliders, key bindings and footer.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawScreenTitle(
				this, MenuItem.SETTINGS.getTitle());

		drawVolumeSlider(0, "BGM", Sound.Category.BGM);
		drawVolumeSlider(1, "SFX", Sound.Category.SFX);

		this.drawManager.drawMenuRow(
				this, "Key Bindings", bindingY(0), false);

		this.drawManager.drawMenuRow(
				this, "- Move Left: A / Left Arrow -",
				bindingY(1), false);

		this.drawManager.drawMenuRow(
				this, "- Move Right: D / Right Arrow -",
				bindingY(2), false);

		this.drawManager.drawMenuRow(
				this, "- Shoot: Space -", bindingY(3), false);

		this.drawManager.drawMenuRow(
				this, "- Back: ESC -", bindingY(4), false);

		this.drawManager.drawKeyHints(
				this, "drag volume, arrows adjust, esc back");

		this.drawManager.completeDrawing(this);
	}
}
