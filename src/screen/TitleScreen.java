package screen;

import java.awt.event.KeyEvent;

import engine.Cooldown;
import engine.Core;

/**
 * Implements the title screen.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class TitleScreen extends Screen {

	/** Milliseconds between changes in user selection. */
	private static final int SELECTION_TIME = 200;
	
	/** Time between changes in user selection. */
	private Cooldown selectionCooldown;
	/** Menu item the cursor is on. */
	private MenuItem selected;
	/** Whether the exit confirmation is open. */
	private boolean showingExitConfirm;
	/** Whether the cursor in the confirmation is on Yes. */
	private boolean exitConfirmYes;

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
	public TitleScreen(final int width, final int height, final int fps) {
		super(width, height, fps);

		// Starts on the topmost item.
		this.selected = MenuItem.first();
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
		if (this.selectionCooldown.checkFinished()
				&& this.inputDelay.checkFinished()) {
			if (this.showingExitConfirm)
				updateExitConfirm();
			else
				updateMenu();
		}
	}

	/**
	 * Handles input while the menu has focus.
	 */
	private void updateMenu() {
		if (inputManager.isKeyDown(KeyEvent.VK_UP)
				|| inputManager.isKeyDown(KeyEvent.VK_W)) {
			this.selected = this.selected.previous();
			this.selectionCooldown.reset();
		}
		if (inputManager.isKeyDown(KeyEvent.VK_DOWN)
				|| inputManager.isKeyDown(KeyEvent.VK_S)) {
			this.selected = this.selected.next();
			this.selectionCooldown.reset();
		}
		if (inputManager.isKeyDown(KeyEvent.VK_SPACE))
			confirm();
	}

	/**
	 * Handles input while the exit confirmation is open. The cursor starts on
	 * No, so a second space press cannot close the game by accident.
	 */
	private void updateExitConfirm() {
		if (inputManager.isKeyDown(KeyEvent.VK_LEFT)
				|| inputManager.isKeyDown(KeyEvent.VK_RIGHT)
				|| inputManager.isKeyDown(KeyEvent.VK_A)
				|| inputManager.isKeyDown(KeyEvent.VK_D)) {
			this.exitConfirmYes = !this.exitConfirmYes;
			this.selectionCooldown.reset();
		}
		if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
			closeExitConfirm();
			return;
		}
		if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
			if (this.exitConfirmYes) {
				this.returnCode = MenuItem.EXIT.getCode();
				this.isRunning = false;
			} else
				closeExitConfirm();
		}
	}

	/**
	 * Closes the confirmation and hands focus back to the menu.
	 */
	private void closeExitConfirm() {
		this.showingExitConfirm = false;
		this.exitConfirmYes = false;
		this.selectionCooldown.reset();
	}

	/**
	 * Keys the player can use right now.
	 *
	 * @return Line drawn at the bottom of the screen.
	 */
	private String keyHints() {
		if (this.showingExitConfirm)
			return "arrows to choose, space ok, esc cancel";
		return "w+s / arrows to move, space to select";
	}

	/**
	 * Chooses the item the cursor is on and closes the screen, so Core opens
	 * the screen behind it. Every way of choosing an item - space today, a
	 * mouse click later - goes through here. Items without a screen are
	 * ignored, and Exit opens the confirmation instead of closing the game,
	 * so every way of choosing Exit is confirmed.
	 */
	private void confirm() {
		if (!this.selected.isEnabled())
			return;
		if (this.selected == MenuItem.EXIT) {
			this.showingExitConfirm = true;
			this.selectionCooldown.reset();
			return;
		}
		this.returnCode = this.selected.getCode();
		this.isRunning = false;
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawTitle(this);
		drawManager.drawMenu(this, this.selected);
		drawManager.drawKeyHints(this, keyHints());

		// Drawn last so it sits over the menu.
		if (this.showingExitConfirm)
			drawManager.drawExitConfirm(this, this.exitConfirmYes);

		drawManager.completeDrawing(this);
	}
}
