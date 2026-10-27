package engine.soundEngine;

/**
 * Sounds of the special items. Each item from the item catalog gets its own
 * entry, so a new item only needs one line here and no new method.
 */
public final class ItemSounds {

    private ItemSounds() {
    }

    /**
     * Plays the sound of an item, if it has one. Items without a sound are
     * silently ignored, so callers do not need to check the id first.
     *
     * @param itemId
     *            Id of the item from the item catalog.
     */
    public static void play(final String itemId) {
        final Sound sound = forItem(itemId);
        if (sound != null)
            SoundManager.getInstance().playSfx(sound);
    }

    /**
     * Returns the sound that belongs to an item.
     *
     * @param itemId
     *            Id of the item from the item catalog.
     * @return The item's sound, or null for items without one.
     */
    private static Sound forItem(final String itemId) {
        if (itemId == null)
            return null;
        switch (itemId) {
            case "shield":
                return Sound.ITEM_SHIELD;
            case "freeze":
                return Sound.ITEM_FREEZE;
            case "boost":
                return Sound.ITEM_BOOST;
            case "score_boost":
                return Sound.ITEM_SCORE_BOOST;
            case "life":
                return Sound.ITEM_LIFE;
            case "rapid_fire":
                return Sound.ITEM_RAPID_FIRE;
            case "bullet_speed":
                return Sound.ITEM_BULLET_SPEED;
            default:
                return null;
        }
    }
}
