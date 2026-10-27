package entity;

import java.awt.Color;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;

/**
 * Implements a ship, to be controlled by the player.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class Ship extends Entity {

	/** Speed of the bullets shot by the ship. */
	private static final int BULLET_SPEED = -6;
	/** Distance in px from the ship's center to each barrel of a two-way ship. */
	private static final int BARREL_OFFSET = 8;

	/**
	 * Types of player ships. Each type has its own speed, shooting interval
	 * and sprite.
	 */
	/**
	 * Types of player ships. Each type has its own speed, shooting interval
	 * and sprite.
	 */
	public enum ShipType {
		/** Standard ship. */
		STANDARD(2, 750, SpriteType.Ship),
		/** Ship with a high movement speed. */
		FAST_MOVE(4, 750, SpriteType.ShipFastMove),
		/** Ship that shoots a bullet from each of its two barrels. */
		TWO_WAY(1, 1000, SpriteType.ShipTwoWay),
		/** Ship with a high rate of fire. */
		FAST_ATTACK(2, 375, SpriteType.ShipFastAttack),
		/** Ship with a large projectile. */
		BIG_BULLET(2, 1000, SpriteType.ShipBigBullet);

		/** Movement of the ship for each unit of time. */
		private final int speed;
		/** Time between shots, in milliseconds. */
		private final int shootingInterval;
		/** Sprite of the ship while it is not destroyed. */
		private final SpriteType idleSprite;

		/**
		 * Constructor, establishes the properties of the ship type.
		 *
		 * @param speed
		 *            Movement of the ship for each unit of time.
		 * @param shootingInterval
		 *            Time between shots, in milliseconds.
		 * @param idleSprite
		 *            Sprite of the ship while it is not destroyed.
		 */
		ShipType(final int speed, final int shootingInterval,
				 final SpriteType idleSprite) {
			this.speed = speed;
			this.shootingInterval = shootingInterval;
			this.idleSprite = idleSprite;
		}

		/**
		 * Read-only stats, so the Shop and Customize screens can show them
		 * without keeping their own copy (GoG - Currency System).
		 *
		 * @return Movement of the ship for each unit of time.
		 */
		public int getSpeed() {
			return this.speed;
		}

		/** @return Time between shots, in milliseconds. */
		public int getShootingInterval() {
			return this.shootingInterval;
		}

		/** @return Bullets fired per shot. */
		public int getBulletsPerShot() {
			return this == TWO_WAY ? 2 : 1;
		}

		/** @return Sprite of the ship while it is not destroyed. */
		public SpriteType getIdleSprite() {
			return this.idleSprite;
		}
	}

	/** Type of this ship. */
	private final ShipType type;
	/** Sprite drawn while the ship is not destroyed: the type's own, or a shape skin's. */
	private SpriteType idleSprite;

	/**
	 * AUTHORED BY: VFX TEAM (effection)
	 *
	 * Blink settings for low health.
	 */
	private static final int BLINK_INTERVAL = 200;
	private static final Color BASE_COLOR = Color.GREEN;
	private static final Color BLINK_COLOR = new Color(255, 60, 60);

	/** Colour of the ship when not blinking; changed by skins (GoG). */
	private Color skinColor = BASE_COLOR;

	/** Minimum time between shots. */
	private Cooldown shootingCooldown;
	/** Time spent inactive between hits. */
	private Cooldown destructionCooldown;
	/** Extra shots per second from items, added to the base rate (Team CS). */
	private double itemFireRateBonus;
	/** Extra bullet speed from items, added to the base speed (Team CS). */
	private double itemBulletSpeedBonus;
	/** Extra movement speed from items, added to the base speed (Team CS). */
	private int itemMovementSpeedBonus;

	/**
	 * AUTHORED BY: VFX TEAM (effection)
	 *
	 * Blink state for low health.
	 */
	private Cooldown blinkCooldown;
	private boolean blinking;
	private boolean blinkOn;

	/**
	 * Constructor, establishes the ship's properties.
	 *
	 * @param positionX
	 *            Initial position of the ship in the X axis.
	 * @param positionY
	 *            Initial position of the ship in the Y axis.
	 */
	public Ship(final int positionX, final int positionY) {
		this(positionX, positionY, ShipType.STANDARD);
	}

	/**
	 * Constructor, establishes the ship's properties.
	 *
	 * @param positionX
	 *            Initial position of the ship in the X axis.
	 * @param positionY
	 *            Initial position of the ship in the Y axis.
	 * @param type
	 *            Type of the ship, which defines its speed and sprite.
	 */
	public Ship(final int positionX, final int positionY, final ShipType type) {
		super(positionX, positionY, 13 * 2, 8 * 2, Color.GREEN);

		this.type = type;
		this.idleSprite = type.idleSprite;
		this.spriteType = this.idleSprite;
		this.shootingCooldown = Core.getCooldown(type.shootingInterval);
		this.destructionCooldown = Core.getCooldown(1000);

		/**
		 * AUTHORED BY: VFX TEAM (effection)
		 */
		this.blinkCooldown = Core.getCooldown(BLINK_INTERVAL);
	}

	/**
	 * Moves the ship speed uni ts right, or until the right screen border is
	 * reached.
	 */
	public final void moveRight() {
		this.positionX += getSpeed();
	}

	/**
	 * Moves the ship speed units left, or until the left screen border is
	 * reached.
	 */
	public final void moveLeft() {
		this.positionX -= getSpeed();
	}

	/**
	 * Shoots a bullet upwards. A two-way ship shoots one bullet from each of
	 * its two barrels instead.
	 *
	 * @param bullets
	 *            List of bullets on screen, to add the new bullet.
	 * @return Checks if the bullet was shot correctly.
	 */
	public final boolean shoot(final Set<Bullet> bullets) {
		if (this.shootingCooldown.checkFinished()) {
			this.shootingCooldown.reset();
			final int centerX = positionX + this.width / 2;
			final int bulletSpeed = BULLET_SPEED
					- (int) Math.round(this.itemBulletSpeedBonus);
			if (this.type == ShipType.TWO_WAY) {
				bullets.add(BulletPool.getBullet(centerX - BARREL_OFFSET,
						positionY, bulletSpeed));
				bullets.add(BulletPool.getBullet(centerX + BARREL_OFFSET,
						positionY, bulletSpeed));
			} else if (this.type == ShipType.BIG_BULLET) {
				// Spawns a larger projectile (e.g., 9x15 scaled by 2)
				bullets.add(BulletPool.getCustomBullet(centerX, positionY,
						bulletSpeed, 9 * 2, 15 * 2));
			} else {
				bullets.add(BulletPool.getBullet(centerX, positionY,
						bulletSpeed));
			}
			return true;
		}
		return false;
	}

	/**
	 * Sets the bonuses items add on top of the base fire rate and bullet
	 * speed (Team CS - Item System). The base values stay unchanged; with
	 * both bonuses at 0 the ship shoots exactly as before.
	 *
	 * @param fireRateBonus
	 *            Extra shots per second.
	 * @param bulletSpeedBonus
	 *            Extra bullet speed in pixels per frame.
	 */
	public final void setItemBonuses(final double fireRateBonus,
									 final double bulletSpeedBonus) {
		setItemBonuses(fireRateBonus, bulletSpeedBonus, 0);
	}

	/**
	 * Same as {@link #setItemBonuses(double, double)}, plus extra movement
	 * speed (e.g. the boost item), added on top of the base speed and rounded
	 * to whole pixels per frame.
	 *
	 * @param fireRateBonus
	 *            Extra shots per second.
	 * @param bulletSpeedBonus
	 *            Extra bullet speed in pixels per frame.
	 * @param movementSpeedBonus
	 *            Extra movement speed in pixels per frame.
	 */
	public final void setItemBonuses(final double fireRateBonus,
									 final double bulletSpeedBonus, final double movementSpeedBonus) {
		this.itemMovementSpeedBonus = (int) Math.round(Math.max(0,
				movementSpeedBonus));
		this.itemBulletSpeedBonus = Math.max(0, bulletSpeedBonus);
		double bonus = Math.max(0, fireRateBonus);
		if (bonus != this.itemFireRateBonus) {
			this.itemFireRateBonus = bonus;
			// (base shots per second) + (item bonus) -> new interval.
			this.shootingCooldown = Core.getCooldown((int) Math.round(
					1000.0 / (1000.0 / this.type.shootingInterval + bonus)));
		}
	}

	/**
	 * Paints the ship in a skin colour (GoG - Currency System). The
	 * low-health blink still flashes red and returns to this colour.
	 *
	 * @param color
	 *            Skin colour; null goes back to the standard green.
	 */
	public final void setSkinColor(final Color color) {
		this.skinColor = color == null ? BASE_COLOR : color;
		if (!this.blinkOn)
			setColor(this.skinColor);
	}

	/**
	 * Draws the ship with another shape (A1 - ship skins). Only the sprite
	 * changes: hitbox, speed and shooting stay those of the ship type.
	 *
	 * @param sprite
	 *            Sprite to use while the ship is not destroyed; null goes
	 *            back to the ship type's own sprite.
	 */
	public final void setShapeSprite(final SpriteType sprite) {
		this.idleSprite = sprite == null ? this.type.idleSprite : sprite;
		if (!isDestroyed())
			this.spriteType = this.idleSprite;
	}

	/** Equipped shape even while the ship is temporarily destroyed. */
	public final SpriteType getIdleSprite() {
		return this.idleSprite;
	}

	/**
	 * Updates status of the ship.
	 */
	public final void update() {
		if (!this.destructionCooldown.checkFinished())
			this.spriteType = SpriteType.ShipDestroyed;
		else
			this.spriteType = this.idleSprite;

		/**
		 * AUTHORED BY: VFX TEAM (effection)
		 *
		 * Toggle color each BLINK_INTERVAL ms.
		 */
		if (this.blinking && this.blinkCooldown.checkFinished()) {
			this.blinkOn = !this.blinkOn;
			setColor(this.blinkOn ? BLINK_COLOR : this.skinColor);
			this.blinkCooldown.reset();
		}
	}

	/**
	 * AUTHORED BY: VFX TEAM (effection)
	 *
	 * Turns low-health blinking on or off.
	 *
	 * @param blinking
	 *            True to start blinking.
	 */
	public final void setBlinking(final boolean blinking) {
		if (this.blinking == blinking)
			return;
		this.blinking = blinking;
		this.blinkOn = false;
		setColor(this.skinColor);
		this.blinkCooldown.reset();
	}

	/**
	 * Switches the ship to its destroyed state.
	 */
	public final void destroy() {
		this.destructionCooldown.reset();
	}

	/**
	 * Checks if the ship is destroyed.
	 *
	 * @return True if the ship is currently destroyed.
	 */
	public final boolean isDestroyed() {
		return !this.destructionCooldown.checkFinished();
	}

	/**
	 * Getter for the ship's speed.
	 *
	 * @return Speed of the ship, including any item bonus.
	 */
	public final int getSpeed() {
		return this.type.speed + this.itemMovementSpeedBonus;
	}

	/**
	 * Base movement speed without item bonuses (Team CS - Item System).
	 *
	 * @return Base speed in pixels per frame.
	 */
	public final int getBaseSpeed() {
		return this.type.speed;
	}

	/**
	 * Base fire rate without item bonuses (Team CS - Item System).
	 *
	 * @return Base shots per second.
	 */
	public final double getBaseFireRate() {
		return 1000.0 / this.type.shootingInterval;
	}

	/**
	 * Getter for the ship's type.
	 *
	 * @return Type of the ship.
	 */
	public final ShipType getType() {
		return this.type;
	}
}
