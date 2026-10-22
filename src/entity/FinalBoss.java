package entity;

import java.awt.Color;
import java.util.Set;
import java.util.Random;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;
import engine.GameSettings;

public class FinalBoss extends Entity implements Boss {

    /** Point value of a Final Boss. */
    private static final int FINAL_BOSS_POINTS = 5000;


    /** Cooldown between sprite changes. */
    private Cooldown animationCooldown;
	/** Checks if the ship has been hit by a bullet. */
    private boolean isDestroyed;
    /** Values of the ship, in points, when destroyed. */
	private int pointValue;



	/** To calculate the health points. */
	private int hp;
	/** The health points of the Final Boss. */
	private static final int FINAL_BOSS_HP = 5;


	/** Point value awarded for hitting the Final Boss. */
	private static final int FINAL_BOSS_POINTS_HIT = 300;

	/* Ship movement speeds */
	private int speedX = 0;
	private int speedY = 0;

	/* Boss shooting cooldown */
    private Cooldown shootCooldown;
	private int currentShootingFrequency;
	/* Minimum shooting rate */
	private static final int MIN_SHOOTING_RATE = 250;
	/* Shooting rate decline per shot */
	private static final int DECLINE_RATE = 50;
	/* Bullet speed */
	private static final int BULLET_SPEED = 5;
	private static final int TOP_BOUNDARY = 40;
	private static final int PLAYER_CLEARANCE = 200;


	/* Tracks the current number of shots fired */
	private int shotCount = 0;
	/* Target number of shots before a burst attack is triggered */
	private int targetShotsForBurst;

	private Random random = new Random();
	/* Maximum and minimum number of shots before a burst attack */
	private static final int MAX_BURST_SHOTS = 5;
	private static final int MIN_BURST_SHOTS = 3;

	/* Vertical gap between bullets in a burst */
	private static final int BULLET_GAP = 15;

    /**
	 * Constructor for FinalBoss Ship.
	 * 
	 * @param positionX
	 *            Initial position of the ship in the X axis.
	 * @param positionY
	 *            Initial position of the ship in the Y axis.
	 * @param spriteType
	 *            Sprite type, image corresponding to the ship.
	 */
    public FinalBoss(final int positionX, final int positionY,
			final SpriteType spriteType) {
		super(positionX, positionY, 24 * 2, 16 * 2, Color.GRAY);

		this.spriteType = spriteType;
		this.animationCooldown = Core.getCooldown(500);
		this.isDestroyed = false;

		switch (this.spriteType) {
		case FinalBoss1:
		case FinalBoss2:
			this.pointValue = FINAL_BOSS_POINTS;
			this.hp = FINAL_BOSS_HP;
			break;
		default:
			this.pointValue = 0;
			break;
		}
		resetBurstCounter();
	}

    /**
	 * Getter for the score bonus if this ship is destroyed.
	 * 
	 * @return Value of the ship.
	 */
	public final int getPointValue() {
		return this.pointValue;
	}

	/**
	 * Updates attributes, mainly used for animation purposes.
	 */
	public final void update() {
		// Toggle boss sprite when animation cooldown expires to create an animation effect.
		if (!this.isDestroyed && this.animationCooldown.checkFinished()) {
			this.animationCooldown.reset();

			switch (this.spriteType) {
			case FinalBoss1:
				this.spriteType = SpriteType.FinalBoss2;
				break;
			case FinalBoss2:
				this.spriteType = SpriteType.FinalBoss1;
				break;
			default:
				break;
            }
        }
    }

	/**
	 * Checks if the ship has been destroyed.
	 * 
	 * @return True if the ship has been destroyed.
	 */
	public final boolean isDestroyed() {
		return this.isDestroyed;
	}

	/**
	 * Destroys the ship, causing an explosion.
	 */
	public final void destroy() {
		this.isDestroyed = true;
		this.spriteType = SpriteType.Explosion;
	}

	/**
	 *  Applies damage to the ship, reducing its health points. 
	 *  If health points reach zero, the ship is destroyed.
	 */
	public final void takeDamage() {
		this.hp--;
		if (this.hp <= 0) {
			this.destroy();
		}
	}

	/**
	 * Manages the collision between a bullet and the boss ship.
	 * 
	 * @param bullet
	 *            The bullet that collided with the boss ship.
	 * @return The score earned from the collision.
	 */
	public final int manageCollisionsBoss(final Bullet bullet) {
        if (this.isDestroyed()) {
            return 0;
        }

        int scoreEarned = 0;
        if (this.spriteType == SpriteType.FinalBoss1 || this.spriteType == SpriteType.FinalBoss2) {
            scoreEarned = FINAL_BOSS_POINTS_HIT;
        } 

	    this.takeDamage();


        if (this.isDestroyed()) {
            scoreEarned += this.getPointValue();
        }

        return scoreEarned;
    }

	/**
	 * Moves the ship the specified distance.
	 * 
	 * @param distanceX
	 *            Distance to move in the X axis.
	 * @param distanceY
	 *            Distance to move in the Y axis.
	 */
	public final void move(final int distanceX, final int distanceY) {
		this.positionX += distanceX;
		this.positionY += distanceY;
	}

	/**
 	* Moves the boss back and forth within the screen boundaries (top, bottom, left, right)
 	* by applying the baseSpeed from Core, making it smoothly bounce off the edges.
 	* 
 	* @param screenWidth  The width of the screen (this.width in GameScreen)
 	* @param screenHeight The height of the screen (this.height in GameScreen)
 	* @param gameSettings The core settings for difficulty and speed
 	*/
    public final void moveBoss(final int screenWidth, final int screenHeight, 
            final GameSettings gameSettings) {
        if (this.isDestroyed()) {
            return;
        }

		if (this.speedX == 0 && this.speedY == 0) {
			this.speedX = gameSettings.getBaseSpeed();
			this.speedY = gameSettings.getBaseSpeed();
		}
       
        this.move(this.speedX, this.speedY);

        if (this.positionX <= 0) {
            this.positionX = 0;
            this.speedX = -this.speedX;
        } else if (this.positionX >= screenWidth - this.width) {
            this.positionX = screenWidth - this.width;
            this.speedX = -this.speedX;
        }

        int topBoundary = TOP_BOUNDARY;
        int bottomBoundary = screenHeight - PLAYER_CLEARANCE - this.height;

        if (this.positionY <= topBoundary) {
            this.positionY = topBoundary;
            this.speedY = -this.speedY;
        } else if (this.positionY >= bottomBoundary) {
            this.positionY = bottomBoundary;
            this.speedY = -this.speedY;
        }
	}


    /**
     * Manages the shooting of bullets by the boss ship, ensuring 
	 * that it adheres to a cooldown period between shots.
     * 
     * @param bullets general bullet set to add the new bullet to
     * @param gameSettings Core settings for difficulty and shooting frequency
     * @return bullet firing success status (true: fired, false: on cooldown or destroyed)
     */
    public final boolean shoot(final Set<Bullet> bullets, final GameSettings gameSettings) {
        if (this.shootCooldown == null) {
			this.currentShootingFrequency = gameSettings.getShootingFrecuency();
            this.shootCooldown = Core.getCooldown(gameSettings.getShootingFrecuency());
            this.shootCooldown.reset();
        }
        if (!this.isDestroyed() && this.shootCooldown.checkFinished()) {

			if (this.currentShootingFrequency > MIN_SHOOTING_RATE) {
				this.currentShootingFrequency -= DECLINE_RATE;
				if (this.currentShootingFrequency < MIN_SHOOTING_RATE) {
                	this.currentShootingFrequency = MIN_SHOOTING_RATE;
            	}
			}

			this.shootCooldown = Core.getCooldown(this.currentShootingFrequency);
			this.shootCooldown.reset();

			this.shotCount++;
			if (this.shotCount >= this.targetShotsForBurst) {
				this.shoot3Burst(bullets);
				resetBurstCounter();
			} else {
				bullets.add(BulletPool.getBullet(this.positionX + this.width / 2, 
					this.positionY + this.height, BULLET_SPEED));
			}
			return true;
        }
        return false;
    }

	/**
     * Resets the shot counter and randomly assigns the next target count 
	 * (between MIN_BURST_SHOTS and MAX_BURST_SHOTS) needed to trigger the burst attack.
     */
	private void resetBurstCounter() {
		this.shotCount = 0;
		int range = MAX_BURST_SHOTS - MIN_BURST_SHOTS + 1;
		this.targetShotsForBurst = this.random.nextInt(range) + MIN_BURST_SHOTS;
	}

	/**
     * Fires a three-projectile train in one attack, with vertical spacing.
     * 
     * @param bullets General bullet set to add the new bullets to.
     */
	private void shoot3Burst(final Set<Bullet> bullets) {
		int centerX = this.positionX + this.width / 2;
		int startY = this.positionY + this.height;

		bullets.add(BulletPool.getBullet(centerX, startY, BULLET_SPEED));
		bullets.add(BulletPool.getBullet(centerX, startY - BULLET_GAP, BULLET_SPEED));
		bullets.add(BulletPool.getBullet(centerX, startY - 2 * BULLET_GAP, BULLET_SPEED));
	}
}