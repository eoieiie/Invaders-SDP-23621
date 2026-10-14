package engine.soundEngine;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import engine.Core;
import engine.soundEngine.Sound.Category;

/**
 * Audio backend based on the Java Sound API.
 *
 * <p>Every sound is loaded once into a {@link Clip} and kept in memory, so
 * playing it again costs nothing. Missing or broken files are logged and
 * then ignored, never thrown, because a game without a single sound effect
 * must still be playable.</p>
 *
 * <p>Note that the same effect cannot overlap with itself: replaying it
 * while it is still audible restarts it from the beginning. If overlapping
 * ever becomes necessary, replace the single clip with a small pool.</p>
 *
 * @author Invaders Sound Team
 */
public final class JavaSoundBackend implements AudioBackend {

	/** Application logger. */
	private static Logger logger;
	/** Lowest gain the mixer accepts, used for silence. */
	private static final float MIN_GAIN_DB = -80.0f;
	/** Loaded clips by sound. */
	private final Map<Sound, Clip> clips;
	/** Sounds that failed to load, so they are not retried on every play. */
	private final Set<Sound> failed;
	/** Clip that currently carries the background music. */
	private Clip bgmClip;

	/**
	 * Constructor.
	 */
	public JavaSoundBackend() {
		logger = Core.getLogger();
		clips = new EnumMap<Sound, Clip>(Sound.class);
		failed = EnumSet.noneOf(Sound.class);
	}

	/**
	 * Tells whether the machine offers a usable audio device.
	 *
	 * @return True when at least one mixer is present.
	 */
	public static boolean isAvailable() {
		try {
			return AudioSystem.getMixerInfo().length > 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public synchronized void preload(final Sound sound) {
		if (sound != null && sound.getCategory() == Sound.Category.SFX)
			load(sound);
	}

	@Override
	public synchronized void playSfx(final Sound sound, final float volume) {
		if (sound == null || sound.getCategory() != Sound.Category.SFX)
			return;

		Clip clip = load(sound);
		if (clip == null)
			return;

		if (clip.isRunning())
			clip.stop();
		clip.setFramePosition(0);
		applyVolume(clip, volume);
		clip.start();
	}

	@Override
	public synchronized void playBgm(final Sound sound, final float volume) {
		if (sound == null || sound.getCategory() != Sound.Category.BGM)
			return;

		Clip clip = load(sound);
		if (clip == null)
			return;

		if (bgmClip != null && bgmClip != clip)
			bgmClip.stop();

		bgmClip = clip;
		clip.stop();
		clip.setFramePosition(0);
		applyVolume(clip, volume);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	@Override
	public synchronized void stopBgm() {
		if (bgmClip != null) {
			bgmClip.stop();
			bgmClip.setFramePosition(0);
		}
	}

	@Override
	public synchronized boolean isBgmPlaying() {
		return bgmClip != null && bgmClip.isRunning();
	}

	@Override
	public synchronized void setBgmVolume(final float volume) {
		if (bgmClip != null)
			applyVolume(bgmClip, volume);
	}

	@Override
	public synchronized void dispose() {
		for (Clip clip : clips.values()) {
			clip.stop();
			clip.flush();
			clip.close();
		}
		clips.clear();
		failed.clear();
		bgmClip = null;
	}

	/**
	 * Loads a sound, or returns the already loaded clip.
	 *
	 * @param sound
	 *            Sound to load.
	 * @return The clip, or null if the file is missing or unreadable.
	 */
	private Clip load(final Sound sound) {
		Clip cached = clips.get(sound);
		if (cached != null)
			return cached;
		if (failed.contains(sound))
			return null;

		InputStream stream = null;
		AudioInputStream audio = null;
		Clip clip = null;
		try {
			stream = JavaSoundBackend.class.getClassLoader()
					.getResourceAsStream(sound.getPath());
			if (stream == null) {
				logger.warning("Sound file not found: " + sound.getPath());
				failed.add(sound);
				return null;
			}
			audio = AudioSystem.getAudioInputStream(
					new BufferedInputStream(stream));
			clip = AudioSystem.getClip();
			clip.open(audio);
			clips.put(sound, clip);
		} catch (Exception e) {
			logger.warning("Could not load sound " + sound.getPath() + ": "
					+ e.getMessage());
			failed.add(sound);
			if (clip != null)
				clip.close();
			return null;
		} finally {
			close(audio);
			close(stream);
		}
		return clip;
	}

	/**
	 * Applies a linear volume to a clip through its gain control.
	 *
	 * @param clip
	 *            Clip to adjust.
	 * @param volume
	 *            Volume between 0.0 and 1.0.
	 */
	private static void applyVolume(final Clip clip, final float volume) {
		if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
			return;

		FloatControl gain = (FloatControl) clip
				.getControl(FloatControl.Type.MASTER_GAIN);
		float clamped = Math.max(0.0f, Math.min(1.0f, volume));
		float decibel = clamped <= 0.0f
				? MIN_GAIN_DB
				: (float) (Math.log10(clamped) * 20.0);
		decibel = Math.max(gain.getMinimum(),
				Math.min(gain.getMaximum(), decibel));
		gain.setValue(decibel);
	}

	/**
	 * Closes a stream, ignoring errors.
	 *
	 * @param closeable
	 *            Stream to close, may be null.
	 */
	private static void close(final AutoCloseable closeable) {
		if (closeable == null)
			return;
		try {
			closeable.close();
		} catch (Exception e) {
			logger.fine("Could not close audio stream: " + e.getMessage());
		}
	}
}
