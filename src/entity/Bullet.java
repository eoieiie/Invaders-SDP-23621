package entity;

import java.awt.Color;

import engine.DrawManager.SpriteType;

/**
 * Implements a bullet that moves vertically up or down, optionally drifting
 * sideways (used by aimed boss attacks).
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class Bullet extends Entity {
	/** Dimensions of an ordinary player or enemy bullet. */
	public static final int DEFAULT_WIDTH = 6;
	public static final int DEFAULT_HEIGHT = 10;

	/**
	 * Speed of the bullet, positive or negative depending on direction -
	 * positive is down.
	 */
	private int speed;
	/** Horizontal speed of the bullet, positive is right. Usually zero. */
	private int speedX;

	/**
	 * Constructor, establishes the bullet's properties.
	 * 
	 * @param positionX
	 *            Initial position of the bullet in the X axis.
	 * @param positionY
	 *            Initial position of the bullet in the Y axis.
	 * @param speed
	 *            Speed of the bullet, positive or negative depending on
	 *            direction - positive is down.
	 */
    public Bullet(final int positionX, final int positionY, final int speed) {
        this(positionX, positionY, speed, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Constructor, establishes the bullet's properties with custom size.
     *
     * @param positionX
     *            Initial position of the bullet in the X axis.
     * @param positionY
     *            Initial position of the bullet in the Y axis.
     * @param speed
     *            Speed of the bullet.
     * @param width
     *            Width of the bullet.
     * @param height
     *            Height of the bullet.
     */
    public Bullet(final int positionX, final int positionY, final int speed, final int width, final int height) {
        super(positionX, positionY, width, height, Color.WHITE);

        this.speed = speed;
        setSprite();
    }

	/**
	 * Sets correct sprite for the bullet, based on speed.
	 */
	public final void setSprite() {
		if (speed < 0)
			this.spriteType = SpriteType.Bullet;
		else
			this.spriteType = SpriteType.EnemyBullet;
	}

	/**
	 * Updates the bullet's position.
	 */
	public final void update() {
		this.positionX += this.speedX;
		this.positionY += this.speed;
	}

	/**
	 * Setter of the speed of the bullet.
	 * 
	 * @param speed
	 *            New speed of the bullet.
	 */
	public final void setSpeed(final int speed) {
		this.speed = speed;
	}

	/**
	 * Getter for the speed of the bullet.
	 * 
	 * @return Speed of the bullet.
	 */
	public final int getSpeed() {
		return this.speed;
	}

	/**
	 * Setter of the horizontal speed of the bullet.
	 *
	 * @param speedX
	 *            New horizontal speed of the bullet, positive is right.
	 */
	public final void setSpeedX(final int speedX) {
		this.speedX = speedX;
	}

	/**
	 * Getter for the horizontal speed of the bullet.
	 *
	 * @return Horizontal speed of the bullet.
	 */
	public final int getSpeedX() {
		return this.speedX;
	}
}
