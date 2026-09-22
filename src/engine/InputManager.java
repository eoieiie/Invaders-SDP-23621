package engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Manages keyboard and mouse input for the provided screen.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public final class InputManager implements KeyListener, MouseMotionListener, MouseListener {

	/** Number of recognised keys. */
	private static final int NUM_KEYS = 256;
	/** Array with the keys marked as pressed or not. */
	private static boolean[] keys;
	/** Singleton instance of the class. */
	private static InputManager instance;
	/** Current mouse Y position. */
	private int mouseY;
	/** Whether the mouse has moved since the last check. */
	private boolean isMoved = false;
	/** Whether a mouse button is currently held down. */
	private boolean isClick;

	/**
	 * Private constructor.
	 */
	private InputManager() {
		keys = new boolean[NUM_KEYS];
	}

	/**
	 * Returns shared instance of InputManager.
	 * 
	 * @return Shared instance of InputManager.
	 */
	protected static InputManager getInstance() {
		if (instance == null)
			instance = new InputManager();
		return instance;
	}

	/**
	 * Returns true if the provided key is currently pressed.
	 * 
	 * @param keyCode
	 *            Key number to check.
	 * @return Key state.
	 */
	public boolean isKeyDown(final int keyCode) {
		return keys[keyCode];
	}

	/**
	 * Changes the state of the key to pressed.
	 * 
	 * @param key
	 *            Key pressed.
	 */
	@Override
	public void keyPressed(final KeyEvent key) {
		if (key.getKeyCode() >= 0 && key.getKeyCode() < NUM_KEYS)
			keys[key.getKeyCode()] = true;
	}

	/**
	 * Changes the state of the key to not pressed.
	 * 
	 * @param key
	 *            Key released.
	 */
	@Override
	public void keyReleased(final KeyEvent key) {
		if (key.getKeyCode() >= 0 && key.getKeyCode() < NUM_KEYS)
			keys[key.getKeyCode()] = false;
	}

	/**
	 * Does nothing.
	 * 
	 * @param key
	 *            Key typed.
	 */
	@Override
	public void keyTyped(final KeyEvent key) {

	}

	/**
	 * Returns the current mouse Y position.
	 *
	 * @return Current mouse Y position.
	 */
	public int getMouseY() {
		return mouseY;
	}
	
	/**
	 * Returns whether a mouse button is currently held down.
	 *
	 * @return true if a mouse button is held down.
	 */
	public boolean isMousePressed() { 
		return isClick;
	}

	/**
	 * Updates the mouse Y position and records mouse movement.
	 *
	 * @param e
	 * 			Mouse movement event.
	 */
	@Override
	public void mouseMoved(final MouseEvent e) {

		mouseY = e.getY();
		isMoved = true;

	}

	/**
	 * Returns whether the mouse has moved since the last check
	 *
	 *  @return true if movement was recorded
	 */

	public boolean isMouseMoved() {
		boolean moved = isMoved;
		isMoved = false;
		return moved;
	}
	/**
	 * Does nothing.
	 * 
	 * @param e Mouse event.
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
	}
	/**
	 * Does nothing.
	 * 
	 * @param e Mouse event.
	 */
	@Override
	public void mouseClicked(final MouseEvent e) {
	}
	/**
	 * Marks the mouse button as pressed
	 * 
	 * @param e Mouse event containing button and position information.
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		isClick = true;
	}
	/**
	 * Marks the mouse buttons as released
	 *
	 * @param e Mouse event containing button and position information.
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
		isClick = false;
	}
	/**
	 * Does nothing.
	 * 
	 * @param e Mouse event.
	 *            
	 */
	@Override
	public void mouseEntered(final MouseEvent e) {

	}


	/**
	 * Clears the mouse button state when the mouse exits the component.
	 * 
	 * @param e Mouse event.
	 */
	@Override
	public void mouseExited(final MouseEvent e) {
		isClick = false;
	}
}
