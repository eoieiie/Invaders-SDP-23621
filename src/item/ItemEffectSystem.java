package item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import item.ItemAPI.*;

/**
 * Manages every effect in one file. No per-effect source files or inheritance framework.
 * The inner RunningEffect holds effectId/ItemInfo/remaining time/charges, etc. At most one running effect per kind.
 * Starts empty. clear removes only stage effects and keeps run-wide stacking effects (UNTIL_RUN_END).
 * effectIds are never reused within a run. Picking up a stacking effect again only raises the stack count of the same effectId.
 * Event IDs/queue belong to the manager. This class only returns apply/end/block results.
 */
class ItemEffectSystem {
    /** Source of running effects, iterated in ascending effectId. At most one per kind. */
    private final TreeMap<Long, RunningEffect> running = new TreeMap<Long, RunningEffect>();
    /** Never reused within a run. Keeps increasing after clear. */
    private long nextEffectId = 1;

    ItemEffectSystem() { }

    /**
     * Read-only check of whether the effect can be applied. null if possible, otherwise the failure reason.
     * LIFE only calls port.canAddLife. Others check whether the same kind is running.
     * Normal failures return only EFFECT_ALREADY_ACTIVE or EFFECT_REJECTED.
     * The port is used only for LIFE; slots, effects, IDs, randomness and time are not changed.
     */
    GrantFailure check(ItemInfo item, LifePort port) {
        ItemAPI.required(item, "item");
        if (item.effectKind == EffectKind.LIFE)
            return ItemAPI.required(port, "port").canAddLife() ? null : GrantFailure.EFFECT_REJECTED;
        RunningEffect existing = findByKind(item.effectKind);
        if (existing == null) return null;
        if (existing.stackable()) return existing.stacks < item.maxStacks ? null : GrantFailure.EFFECT_REJECTED;
        if (existing.refreshable()) return null;
        return GrantFailure.EFFECT_ALREADY_ACTIVE;
    }

    /**
     * Applies synchronously and returns Applied. All foreseeable checks run before success.
     * LIFE: calls port.tryAddLife() once; true → ok(null), false → failed(EFFECT_REJECTED).
     * SHIELD: keeps durationMillis/charges; using one while active restarts both. RAPID_FIRE/BULLET_SPEED: run-wide stacks (up to maxStacks).
     * FREEZE: blocks movement for durationMillis. BOOST/SCORE_BOOST add temporary bonuses derived from base stats.
     * Using another boost or score boost while the same kind is active restarts its time, like the shield.
     * None of the effects change the ship's base stats directly.
     * A successful lasting effect returns an EffectView with a new effectId, registered before returning.
     * A rejection changes nothing, refreshes no time and consumes no ID. MANUAL items can also be triggered here via useSlot.
     * Input/catalog/coding errors are not hidden as normal rejections. No re-entering the manager via the port or other callbacks.
     */
    Applied apply(ItemInfo item, LifePort port) {
        ItemAPI.required(item, "item");
        if (item.effectKind == EffectKind.LIFE) {
            ItemAPI.required(port, "port");
            return port.tryAddLife() ? Applied.ok(null) : Applied.failed(GrantFailure.EFFECT_REJECTED);
        }
        validateDefinition(item); // Catalog errors surface as exceptions, not rejections.
        RunningEffect existing = findByKind(item.effectKind);
        if (existing != null && existing.stackable()) {
            if (existing.stacks >= item.maxStacks) return Applied.failed(GrantFailure.EFFECT_REJECTED);
            existing.stacks++;
            return Applied.ok(existing.view());
        }
        if (existing != null && existing.refreshable()) {
            existing.refresh(activeExtraMillis(item)); // Same effectId, duration and charges back to full.
            return Applied.ok(existing.view());
        }
        if (existing != null) return Applied.failed(GrantFailure.EFFECT_ALREADY_ACTIVE);

        RunningEffect effect = new RunningEffect(nextEffectId++, item, activeExtraMillis(item));
        running.put(effect.effectId, effect); // Returns the View only after registering the source.
        return Applied.ok(effect.view());
    }

    /**
     * Reduces only existing effects' time by delta. Expired ones are removed and returned as Ended(EXPIRED).
     * Stage-long effects never expire by time. Endings are reported in id order.
     * Effects applied later in this update do not get the past delta applied retroactively.
     */
    List<Ended> advance(long delta) {
        if (delta < 0) throw new IllegalArgumentException("negative delta");
        List<Ended> ended = new ArrayList<Ended>();
        Iterator<RunningEffect> iterator = running.values().iterator();
        while (iterator.hasNext()) {
            RunningEffect effect = iterator.next();
            if (!effect.timed()) continue;
            if (delta >= effect.remainingMillis) {
                iterator.remove();
                ended.add(new Ended(effect.item.itemId, effect.effectId, EffectEndReason.EXPIRED));
            } else {
                effect.remainingMillis -= delta;
            }
        }
        return Collections.unmodifiableList(ended);
    }

    /**
     * null if there is no valid shield. Otherwise keeps an immutable View from before the hit and lowers the charges.
     * On the last charge the effect is removed and Hit(before,true) is returned, otherwise Hit(before,false).
     * Check and consume happen in one call. The manager records SHIELD_BLOCKED and any end event.
     * advance and clear never report an already removed shield again.
     */
    Hit tryBlockHit() {
        RunningEffect shield = findByKind(EffectKind.SHIELD);
        if (shield == null) return null;
        EffectView before = shield.view();
        boolean exhausted = --shield.remainingCharges == 0;
        if (exhausted) running.remove(shield.effectId);
        return new Hit(before, exhausted);
    }

    /**
     * Always computes current effects from zero bonuses, unit multipliers and no movement block.
     * At most one effect per kind exists. Temporary BOOST does not multiply run-wide stacking bonuses.
     * Stacking bonus = magnitude * log2(1 + stacks): 1 stack 1x, 2 about 1.58x, 3 2x, 7 3x.
     */
    Modifiers modifiers() {
        double fireRate = 0.0;
        double bulletSpeed = 0.0;
        double movementSpeedMultiplier = 1.0, fireRateMultiplier = 1.0, scoreMultiplier = 1.0;
        boolean movementBlocked = false;
        for (RunningEffect effect : running.values()) {
            switch (effect.item.effectKind) {
                case RAPID_FIRE:
                    fireRate = stackedBonus(effect);
                    break;
                case BULLET_SPEED:
                    bulletSpeed = stackedBonus(effect);
                    break;
                case FREEZE:
                    movementBlocked = true;
                    break;
                case BOOST:
                    movementSpeedMultiplier = effect.item.magnitude;
                    fireRateMultiplier = effect.item.magnitude;
                    break;
                case SCORE_BOOST:
                    scoreMultiplier = effect.item.magnitude;
                    break;
                default:
                    break; // The shield works by charges and does not affect stats.
            }
        }
        return new Modifiers(fireRate, bulletSpeed, movementBlocked,
            movementSpeedMultiplier, fireRateMultiplier, scoreMultiplier);
    }

    /** itemId → current stack count. Used to lower drop chances. */
    Map<String, Integer> stacksByItemId() {
        Map<String, Integer> stacks = new HashMap<String, Integer>();
        for (RunningEffect effect : running.values())
            if (effect.stackable()) stacks.put(effect.item.itemId, effect.stacks);
        return stacks;
    }

    /** Immutable list of running lasting effects in id order. Excludes LIFE and ended effects. */
    List<EffectView> snapshot() {
        List<EffectView> views = new ArrayList<EffectView>(running.size());
        for (RunningEffect effect : running.values()) views.add(effect.view());
        return Collections.unmodifiableList(views);
    }

    /** Removes stage effects and returns Ended(LEVEL_ENDED) for each. Run-wide stacking and equipped effects and IDs are kept. */
    List<Ended> clear() {
        List<Ended> ended = new ArrayList<Ended>(running.size());
        Iterator<RunningEffect> iterator = running.values().iterator();
        while (iterator.hasNext()) {
            RunningEffect effect = iterator.next();
            if (effect.stackable() || effect.equipped()) continue; // run-wide effects stay
            ended.add(new Ended(effect.item.itemId, effect.effectId, EffectEndReason.LEVEL_ENDED));
            iterator.remove();
        }
        return Collections.unmodifiableList(ended);
    }

    private static double stackedBonus(RunningEffect effect) {
        return effect.item.magnitude * Math.log(1 + effect.stacks) / Math.log(2);
    }

    /** Extra time for a timed active (MANUAL) item while Long Lasting is equipped, otherwise 0. */
    private long activeExtraMillis(ItemInfo item) {
        if (item.activationMode != ActivationMode.MANUAL || item.durationKind != DurationKind.TIMED) return 0;
        RunningEffect longLasting = findByKind(EffectKind.LONG_LASTING);
        return longLasting == null ? 0 : longLasting.item.magnitude.longValue();
    }

    private RunningEffect findByKind(EffectKind kind) {
        for (RunningEffect effect : running.values()) if (effect.item.effectKind == kind) return effect;
        return null;
    }

    /** Same kind/duration/value rules as ItemDefinitions.validate. Not called for LIFE. */
    private static void validateDefinition(ItemInfo item) {
        switch (item.effectKind) {
            case SHIELD:
                expect(item, DurationKind.TIMED, item.durationMillis != null && item.charges != null);
                break;
            case FREEZE:
                expect(item, DurationKind.TIMED, item.durationMillis != null);
                break;
            case BOOST:
            case SCORE_BOOST:
                expect(item, DurationKind.TIMED, item.durationMillis != null
                    && item.magnitude != null && item.magnitude > 1.0);
                break;
            case RAPID_FIRE:
            case BULLET_SPEED:
                expect(item, DurationKind.UNTIL_RUN_END, item.magnitude != null && item.maxStacks != null);
                break;
            case EXTRA_SLOTS:
            case MAGNET:
            case MAX_LIFE:
            case LONG_LASTING:
                expect(item, DurationKind.WHILE_EQUIPPED, item.magnitude != null);
                break;
            default:
                throw new IllegalStateException("unsupported effect kind: " + item.effectKind);
        }
    }
    private static void expect(ItemInfo item, DurationKind duration, boolean valuesPresent) {
        if (item.durationKind != duration || !valuesPresent)
            throw new IllegalStateException("invalid effect definition: " + item.itemId);
    }

    /** Internal mutable source. Only the immutable EffectView from view() is exposed. */
    private static final class RunningEffect {
        final long effectId;
        final ItemInfo item;
        /** Remaining time for TIMED. Others store 0 and never expire by time. */
        long remainingMillis;
        /** Remaining SHIELD charges. null otherwise. Lowered by tryBlockHit. */
        Integer remainingCharges;
        /** Stack count of a stacking (UNTIL_RUN_END) effect. 0 otherwise. */
        int stacks;

        RunningEffect(long effectId, ItemInfo item, long extraMillis) {
            this.effectId = effectId; this.item = item;
            refresh(extraMillis);
            stacks = stackable() ? 1 : 0;
        }
        /** Sets the remaining time (plus extraMillis) and charges to the item's full values. */
        void refresh(long extraMillis) {
            remainingMillis = item.durationKind == DurationKind.TIMED ? item.durationMillis + extraMillis : 0;
            remainingCharges = item.effectKind == EffectKind.SHIELD ? item.charges : null;
        }
        boolean timed() { return item.durationKind == DurationKind.TIMED; }
        boolean stackable() { return item.durationKind == DurationKind.UNTIL_RUN_END; }
        boolean equipped() { return item.durationKind == DurationKind.WHILE_EQUIPPED; }
        /** Using another one while active restarts it instead of being rejected (shield, boost, score boost). */
        boolean refreshable() {
            return item.effectKind == EffectKind.SHIELD || item.effectKind == EffectKind.BOOST
                || item.effectKind == EffectKind.SCORE_BOOST;
        }
        EffectView view() {
            return new EffectView(effectId, item, timed() ? Long.valueOf(remainingMillis) : null,
                remainingCharges, stackable() ? Integer.valueOf(stacks) : null);
        }
    }

    static final class Applied {
        final GrantFailure failure;
        final EffectView effect;
        private Applied(GrantFailure failure, EffectView effect) { this.failure = failure; this.effect = effect; }
        static Applied ok(EffectView effect) { return new Applied(null, effect); }
        static Applied failed(GrantFailure failure) {
            if (failure != GrantFailure.EFFECT_ALREADY_ACTIVE && failure != GrantFailure.EFFECT_REJECTED)
                throw new IllegalArgumentException("invalid effect failure");
            return new Applied(failure, null);
        }
    }
    static final class Ended {
        final String itemId;
        final long effectId;
        final EffectEndReason reason;
        Ended(String itemId, long effectId, EffectEndReason reason) {
            this.itemId = ItemAPI.text(itemId, "itemId");
            if (effectId <= 0) throw new IllegalArgumentException("effectId");
            this.effectId = effectId; this.reason = ItemAPI.required(reason, "reason");
        }
    }
    static final class Hit {
        final EffectView before;
        final boolean exhausted;
        Hit(EffectView before, boolean exhausted) {
            this.before = ItemAPI.required(before, "before"); this.exhausted = exhausted;
        }
    }
}
