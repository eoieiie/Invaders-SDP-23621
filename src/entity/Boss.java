package entity;

import java.util.Set;
import engine.GameSettings;

/** Behavior shared by the Stage 5 and Stage 10 encounters. */
public interface Boss {
    int getPositionX();
    int getPositionY();
    boolean isDestroyed();
    void destroy();
    void update();
    void moveBoss(int screenWidth, int screenHeight, GameSettings settings);
    boolean shoot(Set<Bullet> bullets, GameSettings settings);
    int manageCollisionsBoss(Bullet bullet);
}
