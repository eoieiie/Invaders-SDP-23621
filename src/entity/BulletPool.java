package entity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Implements a pool of recyclable bullets.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public final class BulletPool {

	/** Set of already created bullets. */
	private static Set<Bullet> pool = new HashSet<Bullet>();

	/**
	 * Constructor, not called.
	 */
	private BulletPool() {

	}

	/**
	 * Returns a bullet from the pool if one is available, a new one if there
	 * isn't.
	 * 
	 * @param positionX
	 *            Requested position of the bullet in the X axis.
	 * @param positionY
	 *            Requested position of the bullet in the Y axis.
	 * @param speed
	 *            Requested speed of the bullet, positive or negative depending
	 *            on direction - positive is down.
	 * @return Requested bullet.
	 */
	public static Bullet getBullet(final int positionX,
			final int positionY, final int speed) {
		Bullet bullet = takeRecycledBullet(Bullet.DEFAULT_WIDTH,
				Bullet.DEFAULT_HEIGHT);
		if (bullet == null)
			bullet = new Bullet(positionX, positionY, speed);
		prepare(bullet, positionX, positionY, speed);
		return bullet;
	}

	/** Returns a bullet with the requested size, creating one if necessary. */
	public static Bullet getCustomBullet(final int positionX,
			final int positionY, final int speed, final int width,
			final int height) {
		Bullet bullet = takeRecycledBullet(width, height);
		if (bullet == null)
			bullet = new Bullet(positionX, positionY, speed, width, height);
		prepare(bullet, positionX, positionY, speed);
		return bullet;
	}

	/** Removes only a bullet whose collision dimensions match the request. */
	private static Bullet takeRecycledBullet(final int width, final int height) {
		Iterator<Bullet> iterator = pool.iterator();
		while (iterator.hasNext()) {
			Bullet bullet = iterator.next();
			if (bullet.getWidth() == width && bullet.getHeight() == height) {
				iterator.remove();
				return bullet;
			}
		}
		return null;
	}

	/** Resets properties that may differ from the bullet's previous use. */
	private static void prepare(final Bullet bullet, final int positionX,
			final int positionY, final int speed) {
		bullet.setPositionX(positionX - bullet.getWidth() / 2);
		bullet.setPositionY(positionY);
		bullet.setSpeed(speed);
		bullet.setSpeedX(0);
		bullet.setSprite();
	}

	/**
	 * Adds one or more bullets to the list of available ones.
	 * 
	 * @param bullet
	 *            Bullets to recycle.
	 */
	public static void recycle(final Set<Bullet> bullet) {
		pool.addAll(bullet);
	}
}
