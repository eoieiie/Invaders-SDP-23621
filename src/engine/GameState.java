package engine;

/**
 * Implements an object that stores the state of the game between levels.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameState {

	/** Current game level. */
	private int level;
	/** Current score. */
	private int score;
	/** Lives currently remaining. */
	private int livesRemaining;
	/** Bullets shot until now. */
	private int bulletsShot;
	/** Ships destroyed until now. */
	private int shipsDestroyed;
	/** Diamonds earned so far this run but not yet cashed out (GoG -
	 * Currency System). Lost if the run ends in death. */
	private int pendingDiamonds;
	/** Levels cleared alive during the current run. */
	private int levelsCompletedRun;
    /**coins collected until now.*/
    private int coinsCollected;

	/**
	 * Constructor.
	 *
	 * @param level
	 *            Current game level.
	 * @param score
	 *            Current score.
	 * @param livesRemaining
	 *            Lives currently remaining.
	 * @param bulletsShot
	 *            Bullets shot until now.
	 * @param shipsDestroyed
	 *            Ships destroyed until now.
     * @param coinsCollected
     *             Coins collected until now.
	 */
	public GameState(final int level, final int score,
			final int livesRemaining, final int bulletsShot,
			final int shipsDestroyed, final int coinsCollected) {
		this(level, score, livesRemaining, bulletsShot, shipsDestroyed, 0, coinsCollected);
	}

	/**
	 * Constructor that also carries diamonds earned but not yet cashed out
	 * (GoG - Currency System). The 5-argument constructor above still
	 * works and simply means no pending diamonds.
	 * 
	 * @param level
	 *            Current game level.
	 * @param score
	 *            Current score.
	 * @param livesRemaining
	 *            Lives currently remaining.
	 * @param bulletsShot
	 *            Bullets shot until now.
	 * @param shipsDestroyed
	 *            Ships destroyed until now.
     * @param coinsCollected
     *             Coins collected until now.
	 * @param pendingDiamonds
	 *            Diamonds earned this run, not yet cashed out.
	 */
	public GameState(final int level, final int score,
			final int livesRemaining, final int bulletsShot,
			final int shipsDestroyed, final int pendingDiamonds,final int coinsCollected) {
		this(level, score, livesRemaining, bulletsShot, shipsDestroyed,
				pendingDiamonds, 0, coinsCollected);
	}

	/**
	 * Constructor that also carries run progress needed by achievements.
	 *
	 * @param level Current level.
	 * @param score Current score.
	 * @param livesRemaining Lives remaining.
	 * @param bulletsShot Shots fired during the run.
	 * @param shipsDestroyed Ships destroyed during the run.
	 * @param pendingDiamonds Diamonds earned but not yet cashed out.
     * @param coinsCollected Coins collected until now.
	 * @param levelsCompletedRun Levels cleared alive during this run.
	 */
	public GameState(final int level, final int score,
			final int livesRemaining, final int bulletsShot,
			final int shipsDestroyed, final int pendingDiamonds,
			final int levelsCompletedRun, final int coinsCollected) {
		this.level = level;
		this.score = score;
		this.livesRemaining = livesRemaining;
		this.bulletsShot = bulletsShot;
		this.shipsDestroyed = shipsDestroyed;
		this.pendingDiamonds = Math.max(0, pendingDiamonds);
		this.levelsCompletedRun = Math.max(0, levelsCompletedRun);
        this.coinsCollected = coinsCollected;
	}

	/**
	 * @return the level
	 */
	public final int getLevel() {
		return level;
	}

	/**
	 * @return the score
	 */
	public final int getScore() {
		return score;
	}

	/**
	 * @return the livesRemaining
	 */
	public final int getLivesRemaining() {
		return livesRemaining;
	}

	/**
	 * @return the bulletsShot
	 */
	public final int getBulletsShot() {
		return bulletsShot;
	}

	/**
	 * @return the shipsDestroyed
	 */
	public final int getShipsDestroyed() {
		return shipsDestroyed;
	}

	/**
	 * @return diamonds earned so far this run but not yet cashed out.
	 */
	public final int getPendingDiamonds() {
		return pendingDiamonds;
	}

	/**
	 * @return Levels cleared alive during this run.
	 */
	public final int getLevelsCompletedRun() {
		return levelsCompletedRun;
	}

    /**
     * @return Coins collected during this run.
     */
    public final int getCoinsCollected() {return coinsCollected;}

}
