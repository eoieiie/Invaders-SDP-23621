package item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import item.ItemAPI.*;

/**
 * Item system coordinator, created once per run by game setup code.
 * The game loop calls beginLevel/update/endLevel; general features are offered through ItemAPI.
 * The same instance is kept across stages; a new run creates a new instance.
 * Call only from the thread that owns game state. Re-entrant callbacks are not allowed.
 * Drop/inventory/effect source state is owned by their own files.
 * This file owns only the phase, current connection, external grant results, reservations and events.
 */
public final class ItemManager {
    private final ItemDefinitions definitions;
    private final ItemDropSystem drops;
    private final ItemInventory inventory;
    private final ItemEffectSystem effects;
    private final Map<String, GrantResult> receipts = new LinkedHashMap<String, GrantResult>();
    private final Map<EffectKind, PendingGrantView> pending = new LinkedHashMap<EffectKind, PendingGrantView>();
    private final List<ItemEvent> events = new ArrayList<ItemEvent>();
    private long nextPendingId = 1, nextEventId = 1;
    private boolean active;
    private LevelRules rules;
    private LifePort lifePort;
    private PlayerSnapshot player;
    /** Told about every event as it happens (logging/UI). The event queue is unaffected. */
    private Consumer<ItemEvent> listener;

    /** Sets the run's inventory capacity and drop random source. Does not start a stage yet. */
    public ItemManager(int capacity, Random random) {
        this(capacity, ItemBalance.load(), random);
    }

    /** Assembles from already loaded balance values. ItemSystem passes the values it read once at run start. */
    ItemManager(int capacity, ItemBalance balance, Random random) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity");
        definitions = new ItemDefinitions(ItemAPI.required(balance, "balance"));
        drops = new ItemDropSystem(definitions, ItemAPI.required(random, "random"));
        inventory = new ItemInventory(capacity);
        effects = new ItemEffectSystem();
    }

    /** For in-package test assembly. Not an API for other teams. The four objects must belong to this manager only. */
    ItemManager(ItemDefinitions definitions, ItemDropSystem drops,
                ItemInventory inventory, ItemEffectSystem effects) {
        this.definitions = ItemAPI.required(definitions, "definitions");
        this.drops = ItemAPI.required(drops, "drops");
        this.inventory = ItemAPI.required(inventory, "inventory");
        this.effects = ItemAPI.required(effects, "effects");
    }

    ItemInfo getItemInfo(String id) { return definitions.find(ItemAPI.text(id, "itemId")); }
    List<ItemInfo> getItemInfos() { return ItemAPI.frozen(definitions.all(), false); }

    GrantCheck checkGrant(GrantRequest request) {
        ItemAPI.required(request, "request");
        GrantResult previous = receipts.get(request.requestId);
        if (previous != null) {
            return previous.request.sameContent(request)
                ? GrantCheck.processed(previous) : GrantCheck.rejected(GrantFailure.REQUEST_ID_CONFLICT);
        }
        GrantFailure failure = checkNewGrant(request);
        return failure == null ? GrantCheck.eligible() : GrantCheck.rejected(failure);
    }

    GrantResult tryGrant(GrantRequest request) {
        ItemAPI.required(request, "request");
        GrantResult previous = receipts.get(request.requestId);
        if (previous != null) {
            // Returns the original result even after use, effect expiry or stage change.
            return previous.request.sameContent(request) ? previous
                : GrantResult.rejected(request, GrantFailure.REQUEST_ID_CONFLICT, levelId());
        }
        GrantFailure failure = checkNewGrant(request);
        if (failure != null) return remember(GrantResult.rejected(request, failure, levelId()));
        ItemInfo item = definitions.find(request.itemId);
        if (request.timing == GrantTiming.NEXT_LEVEL) {
            PendingGrantView reservation = new PendingGrantView(nextPendingId++, request, item);
            pending.put(item.effectKind, reservation);
            GrantResult result = remember(GrantResult.queued(request, reservation.pendingGrantId, levelId()));
            emit(EventType.ITEM_GRANTED, item.itemId, null, null, null,
                reservation.pendingGrantId, request, result.status, null, null);
            return result;
        }
        Acquisition acquired = acquire(item);
        if (acquired.failure != null)
            return remember(GrantResult.rejected(request, acquired.failure, levelId()));
        GrantResult result = acquired.slot != null
            ? GrantResult.stored(request, acquired.slot, levelId())
            : GrantResult.applied(request, effectId(acquired.effect), levelId());
        remember(result);
        emit(EventType.ITEM_GRANTED, item.itemId, null, effectId(acquired.effect), acquired.slot,
            null, request, result.status, null, null);
        emitStarted(acquired.effect, request);
        return result;
    }

    private GrantFailure checkNewGrant(GrantRequest request) {
        ItemInfo item = definitions.find(request.itemId);
        if (item == null) return GrantFailure.UNKNOWN_ITEM;
        if (!item.supportedGrantTimings.contains(request.timing)) return GrantFailure.TIMING_NOT_SUPPORTED;
        if (request.timing == GrantTiming.NEXT_LEVEL) {
            if (!supportsNextLevel(item)) return GrantFailure.TIMING_NOT_SUPPORTED;
            if (active) return GrantFailure.INVALID_PHASE;
            if (pending.containsKey(item.effectKind)) return GrantFailure.EFFECT_ALREADY_QUEUED;
            // Run-wide stacks survive between levels; a full stack would fail when the next level starts.
            return effects.check(item, null);
        }
        if (item.activationMode == ActivationMode.MANUAL)
            return inventory.firstEmptySlot() < 0 ? GrantFailure.INVENTORY_FULL : null;
        if (!active) return GrantFailure.LEVEL_NOT_ACTIVE;
        return effects.check(item, lifePort); // For LIFE, only the read-only canAddLife may be used.
    }

    /** Called by the game loop when a stage starts. Validates rules, then applies reserved effects. */
    public void beginLevel(LevelRules newRules, LifePort newPort) {
        if (active) throw new IllegalStateException("level is already active");
        ItemAPI.required(newRules, "rules"); ItemAPI.required(newPort, "lifePort");
        definitions.validate(newRules); // Validates all rules/catalog before changing state.
        // The effect list must be empty while INACTIVE. Reservations are limited to port-free stage effects.
        for (PendingGrantView reservation : pending.values()) {
            if (!supportsNextLevel(reservation.item)) throw new IllegalStateException("invalid pending definition");
            if (effects.check(reservation.item, newPort) != null)
                throw new IllegalStateException("pending preflight failed");
        }
        drops.beginLevel(newRules);
        rules = newRules; lifePort = newPort; player = null; active = true;
        for (PendingGrantView reservation : new ArrayList<PendingGrantView>(pending.values())) {
            ItemEffectSystem.Applied applied = effects.apply(reservation.item, lifePort);
            if (applied.failure != null || applied.effect == null)
                throw new IllegalStateException("prevalidated pending effect failed; investigate, do not retry blindly");
            pending.remove(reservation.item.effectKind);
            emit(EventType.PENDING_GRANT_APPLIED, reservation.item.itemId, null,
                applied.effect.effectId, null, reservation.pendingGrantId,
                reservation.request, null, null, null);
            emitStarted(applied.effect, reservation.request);
        }
    }

    void onEnemyDefeated(DropSource source, double x, double y) {
        requireActive(); ItemAPI.required(source, "source");
        ItemAPI.finite(x, "x"); ItemAPI.finite(y, "y");
        DropView spawned = drops.spawn(source, x, y, effects.stacksByItemId());
        if (spawned != null)
            emit(EventType.ITEM_SPAWNED, spawned.item.itemId, spawned.dropId,
                null, null, null, null, null, null, spawned.bounds);
    }

    /** The game loop passes the elapsed game time (ms) and the current player state. */
    public void update(long delta, PlayerSnapshot currentPlayer) {
        requireActive(); ItemAPI.required(currentPlayer, "player");
        if (delta < 0) throw new IllegalArgumentException("negative delta");
        // Handles expiry for the elapsed time first, then applies effects gained in this update.
        for (ItemEffectSystem.Ended ended : effects.advance(delta)) emitEnded(ended);
        player = currentPlayer;
        ItemDropSystem.Frame frame = drops.advance(delta, currentPlayer);
        for (DropView expired : frame.expired)
            emit(EventType.ITEM_EXPIRED, expired.item.itemId, expired.dropId,
                null, null, null, null, null, null, expired.bounds);
        if (!currentPlayer.canPickup) return;
        for (DropView contact : frame.contacts) {
            Acquisition acquired = acquire(contact.item);
            if (acquired.failure != null) continue;
            // The DropSystem contract guarantees contacts stay valid during the same serial call.
            drops.completePickup(contact.dropId);
            emit(EventType.ITEM_COLLECTED, contact.item.itemId, contact.dropId,
                effectId(acquired.effect), acquired.slot, null, null, null, null, contact.bounds);
            emitStarted(acquired.effect, null);
        }
    }

    UseResult useSlot(int slot) {
        if (!active) return UseResult.LEVEL_NOT_ACTIVE;
        if (slot < 0 || slot >= inventory.capacity()) return UseResult.INVALID_SLOT;
        if (player == null || !player.canUseItems) return UseResult.PLAYER_UNAVAILABLE;
        ItemInfo item = inventory.at(slot);
        if (item == null) return UseResult.EMPTY_SLOT;
        ItemEffectSystem.Applied applied = effects.apply(item, lifePort);
        if (applied.failure != null)
            return applied.failure == GrantFailure.EFFECT_ALREADY_ACTIVE
                ? UseResult.EFFECT_ALREADY_ACTIVE : UseResult.EFFECT_REJECTED;
        inventory.consume(slot); // Never empty the slot before the effect is applied.
        emit(EventType.ITEM_USED, item.itemId, null, effectId(applied.effect), slot,
            null, null, null, null, null);
        emitStarted(applied.effect, null);
        return UseResult.USED;
    }

    boolean tryBlockHit() {
        if (!active) return false;
        ItemEffectSystem.Hit hit = effects.tryBlockHit();
        if (hit == null) return false;
        emit(EventType.SHIELD_BLOCKED, hit.before.item.itemId, null, hit.before.effectId,
            null, null, null, null, null, player == null ? null : player.bounds);
        if (hit.exhausted)
            emit(EventType.EFFECT_ENDED, hit.before.item.itemId, null, hit.before.effectId,
                null, null, null, null, EffectEndReason.CHARGES_EXHAUSTED, null);
        return true;
    }

    Modifiers getModifiers() { return active ? effects.modifiers() : Modifiers.neutral(); }

    View getView() {
        return new View(active ? drops.snapshot() : Collections.<DropView>emptyList(),
            inventory.snapshot(), active ? effects.snapshot() : Collections.<EffectView>emptyList(),
            new ArrayList<PendingGrantView>(pending.values()));
    }

    List<ItemEvent> drainEvents() {
        List<ItemEvent> result = ItemAPI.frozen(events, false);
        events.clear();
        return result;
    }

    /** Clears the stage connection, drops and stage effects. Inventory, grant records and run-wide stacking effects stay for the run. */
    public void endLevel() {
        if (!active) return;
        List<ItemEffectSystem.Ended> ended = effects.clear();
        drops.clear();
        for (ItemEffectSystem.Ended value : ended) emitEnded(value); // Keeps the levelId being ended.
        active = false; rules = null; lifePort = null; player = null;
        // Inventory, pending reservations, grant results, IDs and queued events are kept for the run.
    }

    /** Shared by floor pickup and direct NOW grants. The caller handles drop removal, receipts and cause events. */
    private Acquisition acquire(ItemInfo item) {
        if (item.activationMode == ActivationMode.MANUAL) {
            int slot = inventory.firstEmptySlot();
            if (slot < 0) return new Acquisition(GrantFailure.INVENTORY_FULL, null, null);
            inventory.store(slot, item);
            return new Acquisition(null, slot, null);
        }
        ItemEffectSystem.Applied applied = effects.apply(item, lifePort);
        return new Acquisition(applied.failure, null, applied.effect);
    }

    private static final class Acquisition {
        final GrantFailure failure;
        final Integer slot;
        final EffectView effect;
        Acquisition(GrantFailure failure, Integer slot, EffectView effect) {
            this.failure = failure; this.slot = slot; this.effect = effect;
        }
    }

    private GrantResult remember(GrantResult result) {
        receipts.put(result.request.requestId, result);
        return result;
    }
    private static boolean supportsNextLevel(ItemInfo item) {
        return item.supportedGrantTimings.contains(GrantTiming.NEXT_LEVEL)
            && item.activationMode == ActivationMode.ON_PICKUP
            && item.durationKind == DurationKind.UNTIL_RUN_END
            && (item.effectKind == EffectKind.RAPID_FIRE || item.effectKind == EffectKind.BULLET_SPEED);
    }
    /** Whether a level is active. Used by the game bridge (ItemSystem) to avoid double begin/end. */
    boolean isLevelActive() { return active; }
    private void requireActive() { if (!active) throw new IllegalStateException("level is inactive"); }
    private String levelId() { return active ? rules.levelId : null; }
    private static Long effectId(EffectView effect) { return effect == null ? null : effect.effectId; }

    private void emitStarted(EffectView effect, GrantRequest request) {
        if (effect != null)
            emit(EventType.EFFECT_STARTED, effect.item.itemId, null, effect.effectId,
                null, null, request, null, null, null);
    }
    private void emitEnded(ItemEffectSystem.Ended ended) {
        emit(EventType.EFFECT_ENDED, ended.itemId, null, ended.effectId,
            null, null, null, null, ended.reason, null);
    }
    private void emit(EventType type, String itemId, Long dropId, Long effectId, Integer slot,
                      Long pendingId, GrantRequest request, GrantStatus status,
                      EffectEndReason reason, Bounds bounds) {
        ItemEvent event = new ItemEvent(nextEventId++, type, itemId, levelId(), dropId, effectId,
            slot, pendingId, request, status, reason, bounds);
        events.add(event);
        if (listener != null) listener.accept(event);
    }

    /** Sets the listener told about every new event. The listener must not call back into the manager. */
    void setEventListener(Consumer<ItemEvent> eventListener) { listener = eventListener; }
}
