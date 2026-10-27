package entity;

import java.awt.Color;
import java.util.Set;
import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;
import engine.GameSettings;

/** Two-hit bouncing mid-boss used at Stage 5. */
public final class MidBoss extends Entity implements Boss {
    private static final int DEFEAT_POINTS = 1000;
    private static final int HIT_POINTS = 100;
    private static final int TOP_BOUNDARY = 40;
    private static final int PLAYER_CLEARANCE = 200;
    private static final int MIN_SHOOTING_INTERVAL = 500;
    private static final int INTERVAL_REDUCTION = 30;
    private static final int BULLET_SPEED = 5;
    /** Health points this boss starts an encounter with. */
    private static final int MAX_HP = 2;
    private final Cooldown animationCooldown = Core.getCooldown(500);
    private Cooldown shootCooldown;
    private int shootingInterval;
    private int hp = MAX_HP;
    private boolean destroyed;
    private int speedX;
    private int speedY;

    public MidBoss(final int x, final int y, final SpriteType sprite) {
        super(x, y, 32, 20, Color.ORANGE);
        if (sprite != SpriteType.MidBoss1 && sprite != SpriteType.MidBoss2)
            throw new IllegalArgumentException("Unsupported boss sprite: " + sprite);
        this.spriteType = sprite;
    }

    public int getPointValue() { return DEFEAT_POINTS; }
    public boolean isDestroyed() { return destroyed; }
    public int getHp() { return hp; }
    public int getMaxHp() { return MAX_HP; }

    public void update() {
        if (!destroyed && animationCooldown.checkFinished()) {
            animationCooldown.reset();
            spriteType = spriteType == SpriteType.MidBoss1
                    ? SpriteType.MidBoss2 : SpriteType.MidBoss1;
        }
    }

    public void destroy() {
        destroyed = true;
        spriteType = SpriteType.Explosion;
    }

    public void takeDamage() {
        if (!destroyed && --hp <= 0)
            destroy();
    }

    /** Award the hit before destruction changes the sprite; defeat counts once. */
    public int manageCollisionsBoss(final Bullet bullet) {
        if (destroyed)
            return 0;
        takeDamage();
        return HIT_POINTS + (destroyed ? DEFEAT_POINTS : 0);
    }

    /** Boss baseSpeed is pixels per update, unlike formation movement intervals. */
    public void moveBoss(final int width, final int height, final GameSettings settings) {
        if (destroyed)
            return;
        if (speedX == 0 && speedY == 0) {
            speedX = settings.getBaseSpeed();
            speedY = speedX;
        }
        positionX += speedX;
        positionY += speedY;
        if (positionX <= 0) {
            positionX = 0;
            speedX = -speedX;
        } else if (positionX >= width - this.width) {
            positionX = width - this.width;
            speedX = -speedX;
        }
        int bottom = height - PLAYER_CLEARANCE - this.height;
        if (positionY <= TOP_BOUNDARY) {
            positionY = TOP_BOUNDARY;
            speedY = -speedY;
        } else if (positionY >= bottom) {
            positionY = bottom;
            speedY = -speedY;
        }
    }

    public boolean shoot(final Set<Bullet> bullets, final GameSettings settings) {
        if (destroyed)
            return false;
        if (shootCooldown == null) {
            shootingInterval = Math.max(MIN_SHOOTING_INTERVAL,
                    settings.getShootingFrecuency());
            shootCooldown = Core.getCooldown(shootingInterval);
            shootCooldown.reset();
        }
        if (!shootCooldown.checkFinished())
            return false;
        shootingInterval = Math.max(MIN_SHOOTING_INTERVAL,
                shootingInterval - INTERVAL_REDUCTION);
        shootCooldown = Core.getCooldown(shootingInterval);
        shootCooldown.reset();
        bullets.add(BulletPool.getBullet(positionX + this.width / 2,
                positionY + this.height, BULLET_SPEED));
        return true;
    }
}
