package screen;

import java.awt.event.KeyEvent;

public class SettingsScreen extends Screen {
    public SettingsScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.returnCode = 1;
    }

    public final int run() {
        super.run();
        return this.returnCode;
    }

    protected final void update() {
        super.update();
        draw();

        if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
                && this.inputDelay.checkFinished()) {
            this.isRunning = false;
        }
    }

    private void draw() {
        this.drawManager.initDrawing(this);
        this.drawManager.drawHorizontalLine(this, this.getHeight() / 3 - 20);
        this.drawManager.drawCenteredRegularString(this, "SETTINGS", this.getHeight() / 3);
        this.drawManager.completeDrawing(this);
    }
}
