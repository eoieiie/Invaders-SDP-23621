package entity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Random;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager;
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

	/** Special attacks chosen when the shot counter reaches its target. */
	private enum SpecialAttack { BURST, AIMED, BULLET_RAIN }
	/* Last special attack used, so the same one is never used twice in a row */
	private SpecialAttack lastSpecialAttack;

	/* Aimed shot: total bullet speed and the minimum downward speed */
	private static final int AIMED_BULLET_SPEED = 6;
	private static final int MIN_AIMED_SPEED_Y = 2;
	/* Frames of travel between bullets of the aimed train */
	private static final int AIMED_TRAIN_STEPS = 3;
	/* Player position used by the aimed shot */
	private int targetX;
	private int targetY;
	private boolean hasTarget = false;

	/* Bullet rain: one drop per lane so there is always a gap to dodge into */
	private static final int RAIN_LANES = 5;
	private static final int RAIN_LANE_MARGIN = 10;
	/* Just below the HUD separation line, where bullets are not cleaned up */
	private static final int RAIN_START_Y = 45;
	private static final int RAIN_BULLET_SPEED = 4;
	private static final int RAIN_WARNING_TIME = 800;
	private static final int RAIN_MARKER_SIZE = 8;
	/* X positions of the pending rain drops, empty when no rain is coming */
	private final List<Integer> rainTargets = new ArrayList<Integer>();
	private final Cooldown rainWarningCooldown = Core.getCooldown(RAIN_WARNING_TIME);
	/* Screen width, remembered from moveBoss for the rain lanes */
	private int screenWidth = 0;

	/* Teleport: average time between teleports and its random variance */
	private static final int TELEPORT_INTERVAL = 5000;
	private static final int TELEPORT_VARIANCE = 1500;
	/* How long the boss flickers and shows its destination before jumping */
	private static final int TELEPORT_WARNING_TIME = 700;
	/* Grace period after a teleport before the boss fires again */
	private static final int TELEPORT_HOLD_FIRE_TIME = 500;
	private static final int MIN_TELEPORT_DISTANCE = 100;
	private static final int FLICKER_INTERVAL = 80;
	private Cooldown teleportCooldown;
	private final Cooldown teleportWarningCooldown = Core.getCooldown(TELEPORT_WARNING_TIME);
	private final Cooldown holdFireCooldown = Core.getCooldown(TELEPORT_HOLD_FIRE_TIME);
	private boolean teleporting = false;
	private int teleportX;
	private int teleportY;

	/* Warnings blink so they stand out from bullets and ships */
	private static final int WARNING_BLINK_INTERVAL = 100;
	private static final Color WARNING_COLOR = Color.RED;

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

	/** Keeps warning, shooting and movement timers still during a pause. */
	public final void resumeAfterPause(final long pausedMilliseconds) {
		this.animationCooldown.delayBy(pausedMilliseconds);
		this.rainWarningCooldown.delayBy(pausedMilliseconds);
		this.teleportWarningCooldown.delayBy(pausedMilliseconds);
		this.holdFireCooldown.delayBy(pausedMilliseconds);
		if (this.shootCooldown != null)
			this.shootCooldown.delayBy(pausedMilliseconds);
		if (this.teleportCooldown != null)
			this.teleportCooldown.delayBy(pausedMilliseconds);
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
	 * Getter for the boss's current remaining health points.
	  *
	   * @return Current health points.
	    */
	public final int getHp() {
		return this.hp;
	}

	/**
	 * Getter for the health points this boss starts an encounter with.
	  *
	   * @return Maximum health points.
	    */
	public final int getMaxHp() {
		return FINAL_BOSS_HP;
	}

	/**
	 * Destroys the ship, causing an explosion.
	 */
	public final void destroy() {
		this.isDestroyed = true;
		this.spriteType = SpriteType.Explosion;
		this.teleporting = false;
		this.rainTargets.clear();
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
		this.screenWidth = screenWidth;

		// Teleport: flicker in place while the destination is shown, then jump.
		if (this.teleporting) {
			if (this.teleportWarningCooldown.checkFinished())
				finishTeleport();
			return;
		}
		if (this.teleportCooldown == null) {
			restartTeleportTimer();
		}
		if (this.teleportCooldown.checkFinished()) {
			startTeleport(screenWidth, screenHeight);
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
		if (this.isDestroyed()) {
			return false;
		}
		// Pending rain falls on its own timer, even while the boss holds fire.
		boolean rainFell = dropPendingRain(bullets);
		if (this.teleporting || !this.holdFireCooldown.checkFinished()) {
			return rainFell;
		}
        if (this.shootCooldown.checkFinished()) {

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
				this.fireSpecialAttack(bullets);
				resetBurstCounter();
			} else {
				bullets.add(BulletPool.getBullet(this.positionX + this.width / 2,
					this.positionY + this.height, BULLET_SPEED));
			}
			return true;
        }
        return rainFell;
    }

	/**
	 * Fires a random special attack, never the same one twice in a row.
	 * Bullet rain is skipped while a previous rain is still pending.
	 *
	 * @param bullets General bullet set to add the new bullets to.
	 */
	private void fireSpecialAttack(final Set<Bullet> bullets) {
		SpecialAttack[] attacks = SpecialAttack.values();
		SpecialAttack attack;
		do {
			attack = attacks[this.random.nextInt(attacks.length)];
		} while (attack == this.lastSpecialAttack
				|| (attack == SpecialAttack.BULLET_RAIN && !canStartRain()));
		this.lastSpecialAttack = attack;

		switch (attack) {
		case AIMED:
			shootAimed(bullets);
			break;
		case BULLET_RAIN:
			startBulletRain();
			break;
		default:
			shoot3Burst(bullets);
			break;
		}
	}

	/**
	 * Fires a three-bullet train toward the player's last known position.
	 *
	 * @param bullets General bullet set to add the new bullets to.
	 */
	private void shootAimed(final Set<Bullet> bullets) {
		int startX = this.positionX + this.width / 2;
		int startY = this.positionY + this.height;
		double dx = this.hasTarget ? this.targetX - startX : 0;
		double dy = this.hasTarget ? Math.max(this.targetY - startY, 1) : 1;
		double length = Math.hypot(dx, dy);

		int bulletSpeedX = (int) Math.round(dx / length * AIMED_BULLET_SPEED);
		int bulletSpeedY = Math.max(MIN_AIMED_SPEED_Y,
				(int) Math.round(dy / length * AIMED_BULLET_SPEED));

		// Space the bullets along their path so the train stays in line.
		for (int i = 0; i < 3; i++) {
			Bullet bullet = BulletPool.getBullet(
					startX - i * bulletSpeedX * AIMED_TRAIN_STEPS,
					startY - i * bulletSpeedY * AIMED_TRAIN_STEPS, bulletSpeedY);
			bullet.setSpeedX(bulletSpeedX);
			bullets.add(bullet);
		}
	}

	/**
	 * Checks if a new bullet rain can start.
	 *
	 * @return True if the screen width is known and no rain is pending.
	 */
	private boolean canStartRain() {
		return this.screenWidth / RAIN_LANES > 2 * RAIN_LANE_MARGIN
				&& this.rainTargets.isEmpty();
	}

	/**
	 * Picks one drop position in each lane across the screen and starts the
	 * warning. The bullets fall when the warning ends.
	 */
	private void startBulletRain() {
		int laneWidth = this.screenWidth / RAIN_LANES;
		for (int lane = 0; lane < RAIN_LANES; lane++) {
			this.rainTargets.add(lane * laneWidth + RAIN_LANE_MARGIN
					+ this.random.nextInt(laneWidth - 2 * RAIN_LANE_MARGIN));
		}
		this.rainWarningCooldown.reset();
	}

	/**
	 * Drops the pending bullet rain once its warning has finished.
	 *
	 * @param bullets General bullet set to add the new bullets to.
	 * @return True if the rain fell.
	 */
	private boolean dropPendingRain(final Set<Bullet> bullets) {
		if (this.rainTargets.isEmpty()
				|| !this.rainWarningCooldown.checkFinished()) {
			return false;
		}
		for (int x : this.rainTargets) {
			bullets.add(BulletPool.getBullet(x, RAIN_START_Y, RAIN_BULLET_SPEED));
		}
		this.rainTargets.clear();
		return true;
	}

	/**
	 * Picks a random destination away from the current position and starts
	 * the teleport warning.
	 *
	 * @param screenWidth  The width of the screen.
	 * @param screenHeight The height of the screen.
	 */
	private void startTeleport(final int screenWidth, final int screenHeight) {
		int maxX = Math.max(0, screenWidth - this.width);
		int minY = TOP_BOUNDARY;
		int maxY = Math.max(minY, screenHeight - PLAYER_CLEARANCE - this.height);

		for (int attempt = 0; attempt < 10; attempt++) {
			this.teleportX = this.random.nextInt(maxX + 1);
			this.teleportY = minY + this.random.nextInt(maxY - minY + 1);
			if (Math.hypot(this.teleportX - this.positionX,
					this.teleportY - this.positionY) >= MIN_TELEPORT_DISTANCE) {
				break;
			}
		}
		this.teleporting = true;
		this.teleportWarningCooldown.reset();
	}

	/**
	 * Moves the boss to its teleport destination and briefly holds its fire.
	 */
	private void finishTeleport() {
		this.positionX = this.teleportX;
		this.positionY = this.teleportY;
		this.teleporting = false;
		restartTeleportTimer();
		this.holdFireCooldown.reset();
	}

	/**
	 * Starts the wait until the next teleport. The timer is created on first
	 * use so the wait begins when the boss starts moving.
	 */
	private void restartTeleportTimer() {
		if (this.teleportCooldown == null) {
			this.teleportCooldown = Core.getVariableCooldown(
					TELEPORT_INTERVAL, TELEPORT_VARIANCE);
		}
		this.teleportCooldown.reset();
	}

	/**
	 * Updates the player position used by the aimed shot.
	 *
	 * @param x Horizontal center of the player.
	 * @param y Vertical center of the player.
	 */
	@Override
	public final void setTarget(final int x, final int y) {
		this.targetX = x;
		this.targetY = y;
		this.hasTarget = true;
	}

	/**
	 * The boss flickers while it is about to teleport.
	 *
	 * @return False on the hidden frames of the flicker.
	 */
	@Override
	public final boolean isVisible() {
		return !this.teleporting
				|| (System.currentTimeMillis() / FLICKER_INTERVAL) % 2 == 0;
	}

	/**
	 * Draws the blinking teleport destination and incoming rain markers.
	 *
	 * @param drawManager Manager used to draw the warnings.
	 */
	@Override
	public final void drawWarnings(final DrawManager drawManager) {
		if (this.isDestroyed()
				|| (System.currentTimeMillis() / WARNING_BLINK_INTERVAL) % 2 != 0) {
			return;
		}
		if (this.teleporting) {
			drawManager.drawBox(this.teleportX, this.teleportY,
					this.width, this.height, WARNING_COLOR);
		}
		for (int x : this.rainTargets) {
			drawManager.drawBox(x - RAIN_MARKER_SIZE / 2, RAIN_START_Y,
					RAIN_MARKER_SIZE, RAIN_MARKER_SIZE, WARNING_COLOR);
		}
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
