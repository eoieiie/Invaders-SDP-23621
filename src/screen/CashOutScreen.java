package screen;

import java.awt.event.KeyEvent;

import engine.Core;
import engine.DiamondManager;
import engine.GameState;

/**
 * Shown right after a level is cleared, when there is a next level to play
 * and diamonds are riding on the decision. This is the "push your luck"
 * choice that keeps diamonds from being handed out for free: the player
 * either banks the diamonds earned so far and ends the run, or risks them
 * all by continuing for a bigger payout on the next level.
 *
 * Opened directly by Core between levels, not from the main menu, so it
 * needs no MenuItem entry.
 *
 * @author GoG - Currency System
 */
public class CashOutScreen extends Screen {

	/** Milliseconds before the screen accepts a choice. */
	private static final int CHOICE_DELAY = 500;

	/** Level that was just cleared. */
	private final int clearedLevel;
	/** Level the player would move to if they choose to continue. */
	private final int nextLevel;
	/** Diamonds earned so far this run, still unbanked. */
	private final int pendingDiamonds;
	/** True once the player has chosen to cash out. */
	private boolean cashedOut;
	/** True once SPACE and ESC have both been released since the screen
	 * opened, so a key still held from the level (e.g. SPACE for shooting)
	 * can't make the decision by accident. */
	private boolean keysReleased;

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 * @param gameState
	 *            Game state going into the next level: its level is the
	 *            level about to be played if the player continues, and its
	 *            pending diamonds are what is at stake.
	 */
	public CashOutScreen(final int width, final int height, final int fps,
			final GameState gameState) {
		super(width, height, fps);

		this.nextLevel = gameState.getLevel();
		this.clearedLevel = this.nextLevel - 1;
		this.pendingDiamonds = gameState.getPendingDiamonds();
		this.cashedOut = false;
		this.keysReleased = false;
		this.inputDelay = Core.getCooldown(CHOICE_DELAY);
		this.inputDelay.reset();
	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code (unused: Core opens this screen directly).
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

		boolean spaceDown = inputManager.isKeyDown(KeyEvent.VK_SPACE);
		boolean escapeDown = inputManager.isKeyDown(KeyEvent.VK_ESCAPE);

		if (!this.keysReleased) {
			this.keysReleased = !spaceDown && !escapeDown;
			return;
		}
		if (!this.inputDelay.checkFinished())
			return;

		if (escapeDown) {
			// Bank what's been earned so far, then end the run.
			DiamondManager.getInstance().addDiamonds(this.pendingDiamonds);
			this.cashedOut = true;
			this.isRunning = false;
			this.logger.info("Cashed out " + this.pendingDiamonds
					+ " diamonds, balance: "
					+ DiamondManager.getInstance().getDiamonds());
		} else if (spaceDown) {
			// Push on: diamonds stay pending, at risk.
			this.isRunning = false;
			this.logger.info("Continuing with " + this.pendingDiamonds
					+ " diamonds at risk.");
		}
	}

	/**
	 * @return true if the player chose to cash out on this screen; false
	 *         if they chose to continue playing.
	 */
	public final boolean didCashOut() {
		return this.cashedOut;
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);

		this.drawManager.drawScreenTitle(this, "Level " + this.clearedLevel
				+ " clear!");

		this.drawManager.drawDiamondBalance(this, this.pendingDiamonds,
				this.height / 3);
		this.drawManager.drawCenteredRegularString(this,
				"Diamonds at risk", this.height / 3 + 25);

		this.drawManager.drawDiamondBalance(this, DiamondManager
				.getInstance().getDiamonds(), this.height / 2);
		this.drawManager.drawCenteredRegularString(this,
				"Diamonds banked", this.height / 2 + 25);

		this.drawManager.drawKeyHints(this, "esc cash out and end run");
		this.drawManager.drawCenteredRegularString(this, "space risk it on level "
				+ this.nextLevel, this.height - 45);

		this.drawManager.completeDrawing(this);
	}
}
