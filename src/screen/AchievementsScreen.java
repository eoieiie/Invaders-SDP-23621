package screen;

import java.awt.event.KeyEvent;

/**
 * Implements the achievements screen shell.
 * Achievement content will be added later.
 */
public class AchievementsScreen extends Screen {

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

		if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
				&& this.inputDelay.checkFinished()) {
			this.isRunning = false;
		}
	}

	/**
	 * Draws the title and placeholder text.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);

		this.drawManager.drawScreenTitle(
				this, MenuItem.ACHIEVEMENTS.getTitle());
		this.drawManager.drawCenteredRegularString(this,
				"Coming soon - press ESC to return", this.height / 2);

		this.drawManager.completeDrawing(this);
	}
}