package engine.soundEngine;

/**
 * Audio backend that does nothing.
 *
 * <p>Used when no audio device is available, for example in a headless build
 * server. The game keeps running, just without sound, instead of failing at
 * startup.</p>
 *
 * @author Invaders Sound Team
 */
public final class SilentBackend implements AudioBackend {

	@Override
	public void preload(final Sound sound) {
	}

	@Override
	public void playSfx(final Sound sound, final float volume) {
	}

	@Override
	public void playBgm(final Sound sound, final float volume) {
	}

	@Override
	public void stopBgm() {
	}

	@Override
	public boolean isBgmPlaying() {
		return false;
	}

	@Override
	public void setBgmVolume(final float volume) {
	}

	@Override
	public void dispose() {
	}
}
