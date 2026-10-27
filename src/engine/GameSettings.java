package engine;

/**
 * Implements an object that stores a single game's difficulty settings.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class GameSettings {

	/** Width of the level's enemy formation. */
	private int formationWidth;
	/** Height of the level's enemy formation. */
	private int formationHeight;
	/** Speed of the enemies, function of the remaining number. */
	private int baseSpeed;
	/** Frequency of enemy shootings, +/- 30%. */
	private int shootingFrecuency;
	/** Number of two-hit enemies in this stage's formation. */
	private int twoHitEnemyCount;
	/** Whether the whole formation moves diagonally (Team A1). */
	private boolean diagonalMovement;

	/**
	 * Constructor.
	 *
	 * @param formationWidth
	 *            Width of the level's enemy formation.
	 * @param formationHeight
	 *            Height of the level's enemy formation.
	 * @param baseSpeed
	 *            Speed of the enemies.
	 * @param shootingFrecuency
	 *            Frecuency of enemy shootings, +/- 30%.
	 */
	public GameSettings(final int formationWidth, final int formationHeight,
			final int baseSpeed, final int shootingFrecuency) {
		this(formationWidth, formationHeight, baseSpeed, shootingFrecuency, false, 0);
	}

	/** Creates stage settings with a small, configurable two-hit enemy count. */
	public GameSettings(final int formationWidth, final int formationHeight,
			final int baseSpeed, final int shootingFrecuency,
			final int twoHitEnemyCount) {
		this(formationWidth, formationHeight, baseSpeed, shootingFrecuency,
				false, twoHitEnemyCount);
	}

	/** Configures movement and health variations independently, or together. */
	public GameSettings(final int formationWidth, final int formationHeight,
			final int baseSpeed, final int shootingFrecuency,
			final boolean diagonalMovement, final int twoHitEnemyCount) {
		if (twoHitEnemyCount < 0
				|| (twoHitEnemyCount > 0
						&& twoHitEnemyCount >= formationWidth * formationHeight))
			throw new IllegalArgumentException(
					"Two-hit enemies must leave at least one ordinary enemy");
		this.twoHitEnemyCount = twoHitEnemyCount;
		this.formationWidth = formationWidth;
		this.formationHeight = formationHeight;
		this.baseSpeed = baseSpeed;
		this.shootingFrecuency = shootingFrecuency;
		this.diagonalMovement = diagonalMovement;
	}

	/**
	 * Constructor that also sets how the formation moves.
	 *
	 * @param formationWidth
	 *            Width of the level's enemy formation.
	 * @param formationHeight
	 *            Height of the level's enemy formation.
	 * @param baseSpeed
	 *            Speed of the enemies.
	 * @param shootingFrecuency
	 *            Frecuency of enemy shootings, +/- 30%.
	 * @param diagonalMovement
	 *            True if the whole formation moves diagonally.
	 */
	public GameSettings(final int formationWidth, final int formationHeight,
			final int baseSpeed, final int shootingFrecuency,
			final boolean diagonalMovement) {
		this(formationWidth, formationHeight, baseSpeed, shootingFrecuency,
				diagonalMovement, 0);
	}

	/**
	 * @return the formationWidth
	 */
	public final int getFormationWidth() {
		return formationWidth;
	}

	/** @return Number of two-hit enemies to spawn. */
	public final int getTwoHitEnemyCount() {
		return this.twoHitEnemyCount;
	}

	/**
	 * @return the formationHeight
	 */
	public final int getFormationHeight() {
		return formationHeight;
	}

	/**
	 * @return the baseSpeed
	 */
	public final int getBaseSpeed() {
		return baseSpeed;
	}

	/**
	 * @return the shootingFrecuency
	 */
	public final int getShootingFrecuency() {
		return shootingFrecuency;
	}

	/**
	 * @return true if the formation moves diagonally
	 */
	public final boolean isDiagonalMovement() {
		return diagonalMovement;
	}

}
