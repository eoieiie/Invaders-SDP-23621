package engine;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Simple hub for game moments that many teams need.
 * Example: PLAYER_HIT (value = lives left after the hit).
 *
 * Usage:
 *   GameEvents.subscribe(GameEvents.Type.PLAYER_HIT, listener);
 *   GameEvents.emit(GameEvents.Type.PLAYER_HIT, livesLeft);
 */
public final class GameEvents {

	/** Event types. */
	public enum Type {
		/** Enemy bullet hit the player ship. */
		PLAYER_HIT
	}

	/** Receives events. */
	public interface Listener {
		/**
		 * Called when an event is sent.
		 *
		 * @param type
		 *            Event type.
		 * @param value
		 *            Extra data (PLAYER_HIT: lives left).
		 */
		void onEvent(Type type, int value);
	}

	/** Listeners per event type. */
	private static final Map<Type, List<Listener>> LISTENERS =
			new EnumMap<Type, List<Listener>>(Type.class);

	/** Not used. Static class. */
	private GameEvents() {
	}

	/**
	 * Starts listening to an event type.
	 *
	 * @param type
	 *            Event type.
	 * @param listener
	 *            Listener to add.
	 */
	public static void subscribe(final Type type, final Listener listener) {
		if (!LISTENERS.containsKey(type))
			LISTENERS.put(type, new ArrayList<Listener>());
		LISTENERS.get(type).add(listener);
	}

	/**
	 * Stops listening to an event type.
	 *
	 * @param type
	 *            Event type.
	 * @param listener
	 *            Listener to remove.
	 */
	public static void unsubscribe(final Type type, final Listener listener) {
		if (LISTENERS.containsKey(type))
			LISTENERS.get(type).remove(listener);
	}

	/**
	 * Sends an event to all listeners of its type.
	 *
	 * @param type
	 *            Event type.
	 * @param value
	 *            Extra data.
	 */
	public static void emit(final Type type, final int value) {
		List<Listener> list = LISTENERS.get(type);
		if (list == null)
			return;
		for (Listener l : new ArrayList<Listener>(list))
			l.onEvent(type, value);
	}
}
