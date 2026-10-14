package engine.soundEngine;

/**
 * Low level audio output used by {@link SoundManager}.
 *
 * <p>The interface exists so the actual audio technology can be swapped
 * without touching the rest of the game: {@link JavaSoundBackend} plays real
 * sound, {@link SilentBackend} is a no-op used when no audio device is
 * available, and tests can plug in their own implementation.</p>
 *
 * @author Invaders Sound Team
 */
public interface AudioBackend {

	/**
	 * Loads a sound into memory so playing it later does not stall.
	 *
	 * @param sound
	 *            Sound to load.
	 */
	void preload(Sound sound);

	/**
	 * Plays a short effect once. Calling this while the same effect is still
	 * playing restarts it.
	 *
	 * @param sound
	 *            Sound to play.
	 * @param volume
	 *            Volume between 0.0 and 1.0.
	 */
	void playSfx(Sound sound, float volume);

	/**
	 * Starts background music, replacing whatever was playing before. The
	 * music loops until it is stopped.
	 *
	 * @param sound
	 *            Music to play.
	 * @param volume
	 *            Volume between 0.0 and 1.0.
	 */
	void playBgm(Sound sound, float volume);

	/**
	 * Stops the background music.
	 */
	void stopBgm();

	/**
	 * Tells whether background music is currently playing.
	 *
	 * @return True while music is running.
	 */
	boolean isBgmPlaying();

	/**
	 * Changes the volume of the running background music.
	 *
	 * @param volume
	 *            Volume between 0.0 and 1.0.
	 */
	void setBgmVolume(float volume);

	/**
	 * Releases all audio resources. The backend is unusable afterwards.
	 */
	void dispose();
}
