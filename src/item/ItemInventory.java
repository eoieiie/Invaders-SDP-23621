package item;

import java.util.Arrays;
import java.util.List;
import item.ItemAPI.*;

/**
 * The only owner of the run's active slots.
 * One ItemInfo or null per slot. Indexes start at 0; no auto-compacting, moving or quantity stacking.
 * Holds no game/shop objects. The manager coordinates effect application.
 * Invalid calls (out of range, storing into an occupied slot, consuming an empty slot, storing a non-MANUAL item)
 * throw before any state changes. They never fail silently or turn into a no-op.
 */
class ItemInventory {
    private final int capacity;
    /** Source slot array. Never exposed outside this file (snapshot is a copy). */
    private final ItemInfo[] slots;

    ItemInventory(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity");
        this.capacity = capacity;
        this.slots = new ItemInfo[capacity];
    }
    int capacity() { return capacity; }

    /** Read-only. Lowest empty slot index, or -1 if none. Does not reserve or store. */
    int firstEmptySlot() {
        for (int i = 0; i < capacity; i++) {
            if (slots[i] == null) return i;
        }
        return -1;
    }

    /** Returns the slot's item, or null if empty. Out of range throws. */
    ItemInfo at(int slot) {
        checkRange(slot);
        return slots[slot];
    }

    /**
     * Stores a MANUAL item in an empty slot checked by the manager.
     * Throws before any change if the slot is occupied or the index/category is invalid.
     */
    void store(int slot, ItemInfo item) {
        checkRange(slot);
        ItemAPI.required(item, "item");
        if (item.activationMode != ActivationMode.MANUAL)
            throw new IllegalArgumentException("only MANUAL items can be stored: " + item.itemId);
        if (slots[slot] != null)
            throw new IllegalStateException("slot " + slot + " is occupied by " + slots[slot].itemId);
        slots[slot] = item;
    }

    /** Empties an occupied slot after the manager applied its effect. Throws before any change if empty. */
    void consume(int slot) {
        checkRange(slot);
        if (slots[slot] == null)
            throw new IllegalStateException("slot " + slot + " is already empty");
        slots[slot] = null;
    }

    /** Unmodifiable copy of length capacity. null is an empty slot and is allowed only in this list. */
    List<ItemInfo> snapshot() {
        return ItemAPI.frozen(Arrays.asList(slots), true);
    }

    private void checkRange(int slot) {
        if (slot < 0 || slot >= capacity)
            throw new IndexOutOfBoundsException("slot " + slot + " out of range [0, " + capacity + ")");
    }
}
