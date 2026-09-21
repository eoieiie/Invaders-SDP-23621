package engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

//Mouset import
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Manages keyboard and mouse input for the provided screen.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public final class InputManager implements KeyListener, MouseMotionListener,MouseListener {

	/** Number of recognised keys. */
	private static final int NUM_KEYS = 256;
	/** Array with the jeys marked as pressed or not. */
	private static boolean[] keys;
	/** Singleton instance of the class. */
	private static InputManager instance;

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

	// modify mouse

	/*Current mouse Y position */
	private int mouseY;
	public int getMouseY() {
    	return mouseY;
	}
	/* Whether the mouse has moved since the last check.*/
	private boolean isMoved = false;
	/* Check Mouse button clicked*/
	private boolean isClick;

	/**
	* return mouse button is currently held down.

	 @return true if a mouse button is held down
	*/
	public boolean getIsClick()
	{ return isClick;}


	/**
	 * Updates the mouse Y and record mouse movement.
	 * 
	 * @param e
	 * 			Mouse movement event.
	 */
	@Override 
	public void mouseMoved(MouseEvent e){
		
		mouseY = e.getY();
		isMoved = true;

	}

	/**
	 * Returns whether the mouse has moved since the last check
	 * 
	 *  @return true if movement was recorded
	 */
	
	public boolean isMouseMoved(){
		boolean moved = isMoved;
		isMoved = false;
		return moved;
	}

	@Override 
	public void mouseDragged(MouseEvent e){
	}

	@Override 
	public void mouseClicked(MouseEvent e){
	}

	/**
	* Mouse button as pressed.
	*
	* @param e Mouse event containing button and position information.
	*/
	@Override 
	public void mousePressed(MouseEvent e){
		isClick = true;
	}
	/**
	* Mouse button as released.
	* 
	*
	*/
	@Override 
	public void mouseReleased(MouseEvent e){
		isClick = false;
	}
	
	@Override 
	public void mouseEntered(MouseEvent e){}


	//Clears the mouse button state when the mouse exits the component.
	@Override 
	public void mouseExited(MouseEvent e){
		isClick = false;
	}
}