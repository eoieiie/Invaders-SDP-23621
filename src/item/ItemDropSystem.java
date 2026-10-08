package item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import item.ItemAPI.*;

/**
 * Drop management: owns the source state for spawning, falling, landing, contact and removal of item drops.
 * Stores id/ItemInfo/double position/landed/TTL in the inner private static class DroppedItem.
 * No separate files, no Entity subclass, no inventory/effect changes, no game object references.
 * No event queue here. The manager receives spawn/expire/contact results and records them as events.
 */
class ItemDropSystem {
    private final ItemDefinitions definitions;
    private final Random random;
    private final Map<Long, DroppedItem> items = new LinkedHashMap<Long, DroppedItem>();
    private LevelRules rules;
    private long nextDropId = 1;

    private static class DroppedItem {
        final long id;
        final ItemInfo info;
        final double x;
        double y, remainingMillis;
        boolean grounded;

        DroppedItem(long id, ItemInfo info, double x, double y, LevelRules rules) {
            this.id = id;
            this.info = info;
            this.x = x;
            this.y = y;
            grounded = y >= rules.floorY - rules.pickupHeight;
            remainingMillis = rules.groundLifetimeMillis;
        }

        DropView view(LevelRules rules) {
            return new DropView(id, info,
                new Bounds(x, y, rules.pickupWidth, rules.pickupHeight), grounded);
        }
    }

    ItemDropSystem(ItemDefinitions definitions, Random random) {
        this.definitions = ItemAPI.required(definitions, "definitions");
        this.random = ItemAPI.required(random, "random");
    }

    /** Installs new validated rules and clears the drop list. The dropId counter is never reused. */
    void beginLevel(LevelRules rules) {
        this.rules = ItemAPI.required(rules, "rules");
        items.clear();
    }

    /**
     * Rolls one kind using the source's probability/weights, registers it, then returns an immutable View.
     * null only for a normal no-drop. p==0/1 consumes no probability roll. A single candidate skips the pick roll too.
     * Weight order is LinkedHashMap insertion order. Centers on cx/cy and clamps to the play area.
     * Regular 0.15 / special 1.0 are demo values; the actual values come from LevelRules.
     * Preventing duplicate defeat notices for the same enemy is the game's job.
     * stacks (itemId → current stack count) lowers stacking items' weights and removes them at max stacks.
     * No drop (null) when every candidate is removed.
     */
    DropView spawn(DropSource source, double cx, double cy, Map<String, Integer> stacks) {
        requireActive();
        ItemAPI.required(source, "source");
        ItemAPI.finite(cx, "cx");
        ItemAPI.finite(cy, "cy");
        ItemAPI.required(stacks, "stacks");
        DropRule rule = rules.dropRules.get(source);
        if (rule == null) throw new IllegalStateException("missing drop rule: " + source);
        if (rule.probability == 0) return null;
        if (rule.probability < 1 && random.nextDouble() >= rule.probability) return null;

        Map<String, Double> weights = new LinkedHashMap<String, Double>();
        double total = 0;
        String selected = null;
        for (Map.Entry<String, Double> entry : rule.weights.entrySet()) {
            double weight = effectiveWeight(rule, entry.getKey(), entry.getValue(), stacks);
            if (weight > 0) {
                weights.put(entry.getKey(), weight);
                total += weight;
                selected = entry.getKey();
            }
        }
        if (!Double.isFinite(total)) throw new IllegalStateException("invalid drop weights");
        if (weights.isEmpty()) return null; // every remaining candidate is at max stacks
        if (weights.size() > 1) {
            double target = random.nextDouble() * total;
            double cumulative = 0;
            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                cumulative += entry.getValue();
                if (target < cumulative) {
                    selected = entry.getKey();
                    break;
                }
            }
        }
        ItemInfo info = definitions.find(selected);
        if (info == null) throw new IllegalStateException("unknown item: " + selected);
        if (nextDropId == Long.MAX_VALUE) throw new IllegalStateException("drop IDs exhausted");
        double x = Math.max(rules.left, Math.min(rules.right - rules.pickupWidth,
            cx - rules.pickupWidth / 2));
        double y = Math.max(rules.top, Math.min(rules.floorY - rules.pickupHeight,
            cy - rules.pickupHeight / 2));
        DroppedItem item = new DroppedItem(nextDropId++, info, x, y, rules);
        items.put(item.id, item);
        return item.view(rules);
    }

    /**
     * Moves drops by delta>=0 milliseconds. y += fallSpeed * delta / 1000.0.
     * Stops when the bottom reaches floorY; when landing mid-frame, only the time after landing counts against TTL.
     * 1) Expired drops are removed from the list first and put in expired.
     * 2) The rest are checked for contact between their previous/current vertical span and the player rectangle (edges included).
     *    contacts is empty when canPickup=false. Even with delta=0, allowed current contacts are checked.
     * 3) contacts is in ascending dropId order without duplicates; these drops are not yet removed from the list.
     * During the same serial update, contacts are not changed/removed except by completePickup.
     * The manager decides pickup and effect application. No storing, consuming or event publishing here.
     */
    Frame advance(long delta, PlayerSnapshot player) {
        requireActive();
        if (delta < 0) throw new IllegalArgumentException("negative delta");
        ItemAPI.required(player, "player");
        List<DropView> expired = new ArrayList<DropView>();
        List<DropView> contacts = new ArrayList<DropView>();
        Iterator<DroppedItem> iterator = items.values().iterator();
        while (iterator.hasNext()) {
            DroppedItem item = iterator.next();
            double previousY = item.y;
            double groundedMillis = delta;
            if (!item.grounded) {
                double floorTop = rules.floorY - rules.pickupHeight;
                double landingMillis = (floorTop - item.y) / rules.fallSpeed * 1000.0;
                if (delta >= landingMillis) {
                    item.y = floorTop;
                    item.grounded = true;
                    groundedMillis = delta - landingMillis;
                } else {
                    item.y = Math.min(floorTop, item.y + rules.fallSpeed * (delta / 1000.0));
                    groundedMillis = 0;
                }
            }
            if (item.grounded) item.remainingMillis -= groundedMillis;
            DropView view = item.view(rules);
            // Expiry wins over contact. Contact alone does not remove the drop.
            if (item.remainingMillis <= 0) {
                iterator.remove();
                expired.add(view);
                continue;
            }
            Bounds playerBounds = player.bounds;
            if (player.canPickup
                    && item.x <= playerBounds.x + playerBounds.width
                    && item.x + rules.pickupWidth >= playerBounds.x
                    && previousY <= playerBounds.y + playerBounds.height
                    && item.y + rules.pickupHeight >= playerBounds.y) {
                contacts.add(view);
            }
        }
        return new Frame(expired, contacts);
    }

    /**
     * Removes one contact the manager picked up successfully. Always completes within a single valid update.
     * An unknown ID is an integration bug, so it throws. Never silently fail to remove after the item was granted.
     */
    void completePickup(long dropId) {
        if (items.remove(dropId) == null)
            throw new IllegalStateException("unknown drop: " + dropId);
    }

    /** Immutable View copies in dropId order. The source drops/list are never exposed. */
    List<DropView> snapshot() {
        List<DropView> result = new ArrayList<DropView>();
        for (DroppedItem item : items.values()) result.add(item.view(rules));
        return ItemAPI.frozen(result, false);
    }

    /** Clears drops and current rules when a stage ends. Creates no expiry events and keeps the IDs. */
    void clear() {
        items.clear();
        rules = null;
    }

    private double effectiveWeight(DropRule rule, String itemId, double weight, Map<String, Integer> stacks) {
        if (weight <= 0) return 0;
        Integer count = stacks.get(itemId);
        if (count == null || count <= 0) return weight;
        ItemInfo info = definitions.find(itemId);
        if (info != null && info.maxStacks != null && count >= info.maxStacks) return 0;
        return rule.weightFor(itemId, weight, count);
    }

    /** Internal result. A plain value object with no movement/contact logic. */
    static final class Frame {
        final List<DropView> expired, contacts;
        Frame(List<DropView> expired, List<DropView> contacts) {
            this.expired = ItemAPI.frozen(expired, false);
            this.contacts = ItemAPI.frozen(contacts, false);
        }
    }
    private void requireActive() {
        if (rules == null) throw new IllegalStateException("level is not active");
    }
}
