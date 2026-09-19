package screen;

import java.awt.event.KeyEvent;

/**
 * Implements the ship select screen shell. Contents are provided by
 * the Player & Enemy Ship Variety team.
 */
public class ShipSelectScreen extends Screen {

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
	public ShipSelectScreen(final int width, final int height, final int fps) {
		super(width, height, fps);

		// Back to the main menu when this screen closes.
		this.returnCode = 1;
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
		if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
				&& this.inputDelay.checkFinished())
			this.isRunning = false;
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawScreenTitle(this, MenuItem.SHIP_SELECT.getTitle());
		drawManager.drawCenteredRegularString(this,
				"Coming soon - press ESC to return", this.height / 2);

		drawManager.completeDrawing(this);
	}
}
