package screen;

import java.awt.event.KeyEvent;
import engine.Core;
import engine.DrawManager;
import engine.InputManager;


public class SettingScreen extends Screen{
    public SettingScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.returnCode = 1;
    }
    public final int run(){
        super.run();
        return this.returnCode;
    }

    protected final void update(){
        super.update();
        draw();

        if(this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
                && this.inputDelay.checkFinished()){
            this.isRunning = false;
        }
    }

    private void draw(){
        this.drawManager.initDrawing(this);
        this.drawManager.drawCenteredRegularString(this, "SETTINGS", this.getHeight()/3);
        this.drawManager.completeDrawing(this);
    }
}
