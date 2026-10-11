package screen;

import java.awt.event.KeyEvent;
import entity.Ship.ShipType;

/**
 * Implements the ship select screen.
 * Allows the player to choose their ship type.
 */
public class ShipSelectScreen extends Screen {

    /** Index of the currently selected ship. */
    private int selectionIndex;
    /** Array of all available ship types. */
    private final ShipType[] availableShips = ShipType.values();

    private boolean leftWasDown;
    private boolean rightWasDown;

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
        this.returnCode = 1; // Default return to main menu
        this.selectionIndex = 0;
        this.leftWasDown = false;
        this.rightWasDown = false;
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

        boolean leftIsDown = inputManager.isKeyDown(KeyEvent.VK_LEFT);
        boolean rightIsDown = inputManager.isKeyDown(KeyEvent.VK_RIGHT);

        if (leftIsDown && !this.leftWasDown) {
            this.selectionIndex--;
            if (this.selectionIndex < 0) {
                this.selectionIndex = this.availableShips.length - 1;
            }
        }

        if (rightIsDown && !this.rightWasDown) {
            this.selectionIndex++;
            if (this.selectionIndex >= this.availableShips.length) {
                this.selectionIndex = 0;
            }
        }

        this.leftWasDown = leftIsDown;
        this.rightWasDown = rightIsDown;

        if (this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                this.isRunning = false;
            }
            if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
                this.isRunning = false;
            }
        }
    }

    /**
     * Draws the elements associated with the screen.
     */
    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

        drawManager.drawScreenTitle(this, MenuItem.SHIP_SELECT.getTitle());

        //Recovers the selected ship type
        ShipType selectedType = availableShips[selectionIndex];
        String currentShipName = selectedType.name().replace("_", " ");

        // Draw the name of the ship in the center
        drawManager.drawMenuRow(this, "< " + currentShipName + " >", this.height / 2, true);

        // Creates a temporary ship just to retrieve its image and display it
        entity.Ship dummyShip = new entity.Ship(0, 0, selectedType);

        // Calculate the position to center the ship just above the text
        int shipWidth = dummyShip.getWidth();
        int positionX = (this.width - shipWidth) / 2;
        int positionY = (this.height / 2) - 40;

        // Draw the image of the ship
        drawManager.drawEntity(dummyShip, positionX, positionY);

        drawManager.drawKeyHints(this, "arrows select | space confirm | esc back");

        drawManager.completeDrawing(this);
        }
    }