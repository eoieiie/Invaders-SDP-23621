package engine.soundEngine;

import java.util.logging.Logger;

import engine.Core;

/**
 * Central entry point for all audio in the game.
 *
 * <p>This is the only class call sites ever talk to. It owns the audio
 * backend, the volumes and the state of the background music, so playing
 * music and effects stays consistent no matter where it is triggered:</p>
 *
 * <pre>
 * SoundManager.getInstance().playSfx(Sound.SHOOT);
 * SoundManager.getInstance().playBgm(Sound.BGM_GAME);
 * </pre>
 *
 * <p>The manager is a singleton because the underlying audio lines are a
 * process wide resource. Two independent audio managers would compete for
 * the same mixer and produce crackling or dropped sounds.</p>
 *
 * @author Invaders Sound Team
 */
public final class SoundManager {

	/** Singleton instance of the class. */
	private static SoundManager instance;
	/** Application logger. */
	private static Logger logger;
	/** Default volume of sound effects. */
	private static final float DEFAULT_SFX_VOLUME = 0.7f;
	/** Default volume of background music. */
	private static final float DEFAULT_BGM_VOLUME = 0.5f;

	/** Backend that does the actual playback. */
	private final AudioBackend backend;
	/** Volume of sound effects. */
	private float sfxVolume;
	/** Volume of background music. */
	private float bgmVolume;
	/** Whether all audio is muted. */
	private boolean muted;
	/** Music that should be playing, even while muted. */
	private Sound currentBgm;

	/**
	 * Private constructor.
	 */
	private SoundManager() {
		logger = Core.getLogger();
		sfxVolume = DEFAULT_SFX_VOLUME;
		bgmVolume = DEFAULT_BGM_VOLUME;

		AudioBackend chosen;
		try {
			chosen = JavaSoundBackend.isAvailable()
					? new JavaSoundBackend()
					: new SilentBackend();
		} catch (RuntimeException e) {
			logger.warning("Audio initialization failed: " + e.getMessage());
			chosen = new SilentBackend();
		}
		backend = chosen;
	}

	/**
	 * Returns the shared instance of SoundManager.
	 *
	 * @return Shared instance of SoundManager.
	 */
	public static SoundManager getInstance() {
		if (instance == null)
			instance = new SoundManager();
		return instance;
	}

	/**
	 * Loads all sound effects into memory. Call once at startup so the first
	 * shot does not stutter. Background music is streamed on demand instead,
	 * because it is much larger.
	 */
	public synchronized void preloadSfx() {
		for (Sound sound : Sound.values())
			backend.preload(sound);
	}

	/**
	 * Plays a short effect. Ignored while muted or when the sound is not an
	 * effect.
	 *
	 * @param sound
	 *            Effect to play.
	 */
	public synchronized void playSfx(final Sound sound) {
		if (muted || sound == null)
			return;
		if (sound.getCategory() != Sound.Category.SFX)
			return;
		backend.playSfx(sound, sfxVolume);
	}

	/**
	 * Switches the background music. Does nothing if the requested track is
	 * already playing, so screens can call this every frame safely.
	 *
	 * @param sound
	 *            Music to play.
	 */
	public synchronized void playBgm(final Sound sound) {
		if (sound == null || sound.getCategory() != Sound.Category.BGM)
			return;
		if (sound == currentBgm && backend.isBgmPlaying())
			return;

		currentBgm = sound;
		if (muted)
			return;
		backend.playBgm(sound, bgmVolume);
	}

	/**
	 * Stops the background music and forgets the current track.
	 */
	public synchronized void stopBgm() {
		currentBgm = null;
		backend.stopBgm();
	}

	/**
	 * Returns the track that should currently be playing.
	 *
	 * @return Current background music, or null if none was requested.
	 */
	public synchronized Sound getCurrentBgm() {
		return currentBgm;
	}

	/**
	 * Mutes or unmutes all audio. Music keeps its position, so unmuting
	 * resumes the current track.
	 *
	 * @param mute
	 *            True to silence everything.
	 */
	public synchronized void setMuted(final boolean mute) {
		if (muted == mute)
			return;

		muted = mute;
		if (muted) {
			backend.stopBgm();
		} else if (currentBgm != null) {
			backend.playBgm(currentBgm, bgmVolume);
		}
	}

	/**
	 * Tells whether audio is muted.
	 *
	 * @return True when all audio is silenced.
	 */
	public synchronized boolean isMuted() {
		return muted;
	}

	/**
	 * Sets the volume of one category.
	 *
	 * @param category
	 *            Category to adjust.
	 * @param volume
	 *            Volume between 0.0 and 1.0, clamped.
	 */
	public synchronized void setVolume(final Sound.Category category,
			final float volume) {
		float clamped = Math.max(0.0f, Math.min(1.0f, volume));
		if (category == Sound.Category.BGM) {
			bgmVolume = clamped;
			backend.setBgmVolume(muted ? 0.0f : clamped);
		} else {
			sfxVolume = clamped;
		}
	}

	/**
	 * Returns the volume of one category.
	 *
	 * @param category
	 *            Category to query.
	 * @return Volume between 0.0 and 1.0.
	 */
	public synchronized float getVolume(final Sound.Category category) {
		return category == Sound.Category.BGM ? bgmVolume : sfxVolume;
	}

	/**
	 * Releases all audio resources. Called when the game exits.
	 */
	public synchronized void dispose() {
		currentBgm = null;
		backend.dispose();
	}
}
