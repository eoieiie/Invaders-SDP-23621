package screen;

import java.awt.event.KeyEvent;

/**
 * Implements the shop screen.
 * Placeholder only: currency, items and purchases are added by the teams that own them.
 */
public class ShopScreen extends Screen {

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
	public ShopScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
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

		if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
				&& this.inputDelay.checkFinished()) {
			this.isRunning = false;
		}
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawScreenTitle(this, MenuItem.SHOP.getTitle());
		this.drawManager.completeDrawing(this);
	}
}
