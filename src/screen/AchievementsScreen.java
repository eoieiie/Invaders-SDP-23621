package screen;

import java.awt.event.KeyEvent;
import java.util.List;

import engine.Achievement;
import engine.Core;

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
	 * Draws available achievements.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);

		this.drawManager.drawScreenTitle(
				this, MenuItem.ACHIEVEMENTS.getTitle());
		List<Achievement> achievements = Core.getAchievementManager()
				.getAchievements();
		for (Achievement achievement : achievements) {
			String status = achievement.isUnlocked() ? "UNLOCKED" : "LOCKED";
			this.drawManager.drawCenteredRegularString(this,
					achievement.getName() + " - " + status, this.height / 2);
			this.drawManager.drawCenteredRegularString(this,
					achievement.getDescription(),
							this.height / 2 + this.height / 10);
		}

		this.drawManager.completeDrawing(this);
	}
}