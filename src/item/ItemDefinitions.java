package item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import item.ItemAPI.*;

/**
 * Data dictionary of item kinds. Does not store game state, prices, stock or drop rates.
 * This class is package-private. Other teams use only the lookup functions in ItemAPI.
 * ItemInfo itself is reused as the immutable definition. No separate Definition/Repository files.
 */
class ItemDefinitions {
    private final Map<String, ItemInfo> itemsById;
    private final List<ItemInfo> items;

    /** Builds the catalog from the values in the config file (res/item-balance.properties). */
    ItemDefinitions() { this(ItemBalance.load()); }

    ItemDefinitions(ItemBalance balance) {
        ItemAPI.required(balance, "balance");
        Map<String, ItemInfo> definitions = new LinkedHashMap<String, ItemInfo>();

        ItemInfo life = new ItemInfo(
            "life",
            "Life",
            "Adds one life, or awards " + balance.lifeCapBonusScore + " points at the life cap.",
            "life",
            ActivationMode.ON_PICKUP,
            EffectKind.LIFE,
            DurationKind.INSTANT,
            null,
            null,
            null,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(life.itemId, life);

        ItemInfo shield = new ItemInfo(
            "shield",
            "Shield",
            "Blocks " + balance.shieldCharges + " incoming hit(s) for up to "
                + seconds(balance.shieldDurationMillis) + " seconds.",
            "shield",
            ActivationMode.MANUAL,
            EffectKind.SHIELD,
            DurationKind.TIMED,
            balance.shieldDurationMillis,
            balance.shieldCharges,
            null,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(shield.itemId, shield);

        ItemInfo rapidFire = new ItemInfo(
            "rapid_fire",
            "Rapid Fire",
            "Fires faster for the rest of the run. Stacks up to " + balance.rapidFireMaxStacks
                + " times with smaller gains each time.",
            "rapid_fire",
            ActivationMode.ON_PICKUP,
            EffectKind.RAPID_FIRE,
            DurationKind.UNTIL_RUN_END,
            null,
            null,
            balance.rapidFireBonus,
            balance.rapidFireMaxStacks,
            EnumSet.of(GrantTiming.NOW, GrantTiming.NEXT_LEVEL)
        );
        definitions.put(rapidFire.itemId, rapidFire);

        ItemInfo bulletSpeed = new ItemInfo(
            "bullet_speed",
            "Bullet Speed",
            "Bullets fly faster for the rest of the run. Stacks up to " + balance.bulletSpeedMaxStacks
                + " times with smaller gains each time.",
            "bullet_speed",
            ActivationMode.ON_PICKUP,
            EffectKind.BULLET_SPEED,
            DurationKind.UNTIL_RUN_END,
            null,
            null,
            balance.bulletSpeedBonus,
            balance.bulletSpeedMaxStacks,
            EnumSet.of(GrantTiming.NOW, GrantTiming.NEXT_LEVEL)
        );
        definitions.put(bulletSpeed.itemId, bulletSpeed);

        ItemInfo freeze = new ItemInfo(
            "freeze",
            "Freeze",
            "Stops all enemy movement for " + seconds(balance.freezeDurationMillis) + " seconds.",
            "freeze",
            ActivationMode.MANUAL,
            EffectKind.FREEZE,
            DurationKind.TIMED,
            balance.freezeDurationMillis,
            null,
            null,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(freeze.itemId, freeze);

        ItemInfo boost = new ItemInfo(
            "boost", "Boost",
            "Adds " + percent(balance.boostMultiplier)
                + "% of base ship speed and fire rate for " + seconds(balance.boostDurationMillis) + " seconds.",
            "boost", ActivationMode.MANUAL, EffectKind.BOOST, DurationKind.TIMED,
            balance.boostDurationMillis, null, balance.boostMultiplier, null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(boost.itemId, boost);

        ItemInfo scoreBoost = new ItemInfo(
            "score_boost", "Score Boost",
            "Adds " + percent(balance.scoreBoostMultiplier)
                + "% of base earned score for " + seconds(balance.scoreBoostDurationMillis) + " seconds.",
            "score_boost", ActivationMode.MANUAL, EffectKind.SCORE_BOOST, DurationKind.TIMED,
            balance.scoreBoostDurationMillis, null, balance.scoreBoostMultiplier, null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(scoreBoost.itemId, scoreBoost);

        ItemInfo extraSlots = new ItemInfo(
            "extra_slots",
            "Extra Slots",
            "Equip: +" + balance.extraSlotsAmount + " inventory slot(s) in every run.",
            "extra_slots",
            ActivationMode.EQUIPPED,
            EffectKind.EXTRA_SLOTS,
            DurationKind.WHILE_EQUIPPED,
            null,
            null,
            (double) balance.extraSlotsAmount,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(extraSlots.itemId, extraSlots);

        ItemInfo magnet = new ItemInfo(
            "magnet",
            "Magnet",
            "Equip: picks up items as soon as they land. A drop it cannot keep gives "
                + balance.magnetConvertScore + " points instead.",
            "magnet",
            ActivationMode.EQUIPPED,
            EffectKind.MAGNET,
            DurationKind.WHILE_EQUIPPED,
            null,
            null,
            (double) balance.magnetConvertScore,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(magnet.itemId, magnet);

        ItemInfo maxLife = new ItemInfo(
            "max_life",
            "Max Life",
            "Equip: the life item can raise lives up to " + (balance.maxLives + balance.maxLifeAmount)
                + " instead of " + balance.maxLives + ".",
            "max_life",
            ActivationMode.EQUIPPED,
            EffectKind.MAX_LIFE,
            DurationKind.WHILE_EQUIPPED,
            null,
            null,
            (double) balance.maxLifeAmount,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(maxLife.itemId, maxLife);

        ItemInfo longLasting = new ItemInfo(
            "long_lasting",
            "Long Lasting",
            "Equip: active items (shield, freeze, boost, score boost) last "
                + seconds(balance.longLastingExtraMillis) + " seconds longer.",
            "long_lasting",
            ActivationMode.EQUIPPED,
            EffectKind.LONG_LASTING,
            DurationKind.WHILE_EQUIPPED,
            null,
            null,
            (double) balance.longLastingExtraMillis,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(longLasting.itemId, longLasting);

        itemsById = Collections.unmodifiableMap(definitions);
        items = Collections.unmodifiableList(new ArrayList<ItemInfo>(definitions.values()));
        validateDefinitions();
    }

    /**
     * Looks up a registered ID. Returns null only for a valid but unknown ID. The list is immutable for a run.
     * Registered by default: life (instant, +1 life), shield (manual, time/charges), rapid_fire (on pickup, stacks for the run),
     * bullet_speed (on pickup, stacks for the run), freeze (manual, blocks enemy movement for a time),
     * boost (manual, temporary extra ship speed/fire rate), score_boost (manual, temporary extra earned score),
     * extra_slots / magnet / max_life / long_lasting (owned and equipped outside a run, work in every run while equipped; never dropped).
     * boost / score_boost drop from enemies like the other items (drop.weight.boost / drop.weight.scoreBoost).
     * All support NOW. Only rapid_fire/bullet_speed support NEXT_LEVEL. Values come from ItemBalance (config file).
     */
    ItemInfo find(String itemId) { return itemsById.get(itemId); }

    /** Immutable list in registration order. Not the shop's sale list. Uses the same definitions as find. */
    List<ItemInfo> all() { return items; }

    /**
     * Validates all definitions/rules without changing state.
     * Checks both DropSource rules, existing IDs, a positive finite weight sum (when p>0), and the configured area.
     * Also checks each definition's kind/duration/value combination. Duplicate effects of the same kind are REJECTed, except SHIELD/BOOST/SCORE_BOOST, which restart.
     * NEXT_LEVEL is allowed only for RAPID_FIRE/BULLET_SPEED with ON_PICKUP + UNTIL_RUN_END.
     * LIFE is fixed to INSTANT, active items to TIMED, and the two stacking effects to UNTIL_RUN_END.
     * SHIELD requires durationMillis/charges, FREEZE requires durationMillis, BOOST/SCORE_BOOST require durationMillis/magnitude>1,
     * and the two stacking effects require magnitude/maxStacks.
     * Unsupported combinations throw. Invalid settings are never auto-corrected and the game does not start with them.
     */
    void validate(LevelRules rules) {
        ItemAPI.required(rules, "rules");
        validateDefinitions();

        for (DropSource source : DropSource.values()) {
            DropRule rule = rules.dropRules.get(source);
            requireValid(rule != null, "missing drop rule: " + source);

            double totalWeight = 0.0;
            for (Map.Entry<String, Double> entry : rule.weights.entrySet()) {
                requireValid(itemsById.containsKey(entry.getKey()),
                    "unknown item ID: " + entry.getKey());
                requireValid(itemsById.get(entry.getKey()).activationMode != ActivationMode.EQUIPPED,
                    "equipped items are not dropped: " + entry.getKey());
                double weight = entry.getValue();
                ItemAPI.finite(weight, "weight");
                requireValid(weight >= 0.0, "weight must not be negative: " + entry.getKey());
                totalWeight += weight;
                ItemAPI.finite(totalWeight, "total weight");
            }
            if (rule.probability > 0.0)
                requireValid(totalWeight > 0.0, "positive total weight required: " + source);
        }
    }

    private void validateDefinitions() {
        requireValid(items.size() == itemsById.size(), "definition index mismatch");
        EnumSet<EffectKind> kinds = EnumSet.noneOf(EffectKind.class);

        for (ItemInfo item : items) {
            requireValid(itemsById.get(item.itemId) == item, "definition index mismatch: " + item.itemId);
            requireValid(kinds.add(item.effectKind), "duplicate effect kind: " + item.effectKind);
            requireValid(item.supportedGrantTimings.contains(GrantTiming.NOW),
                "NOW timing required: " + item.itemId);

            switch (item.effectKind) {
                case LIFE:
                    requireDefinition(item, item.activationMode == ActivationMode.ON_PICKUP
                        && item.durationKind == DurationKind.INSTANT
                        && item.durationMillis == null && item.charges == null && item.magnitude == null
                        && item.maxStacks == null
                        && item.supportedGrantTimings.equals(EnumSet.of(GrantTiming.NOW)));
                    break;
                case SHIELD:
                    requireDefinition(item, item.activationMode == ActivationMode.MANUAL
                        && item.durationKind == DurationKind.TIMED
                        && item.durationMillis != null
                        && item.charges != null && item.magnitude == null && item.maxStacks == null
                        && item.supportedGrantTimings.equals(EnumSet.of(GrantTiming.NOW)));
                    break;
                case RAPID_FIRE:
                    requireDefinition(item, isRunStack(item));
                    break;
                case BULLET_SPEED:
                    requireDefinition(item, isRunStack(item));
                    break;
                case FREEZE:
                    requireDefinition(item, item.activationMode == ActivationMode.MANUAL
                        && item.durationKind == DurationKind.TIMED
                        && item.durationMillis != null
                        && item.charges == null && item.magnitude == null && item.maxStacks == null
                        && item.supportedGrantTimings.equals(EnumSet.of(GrantTiming.NOW)));
                    break;
                case BOOST:
                case SCORE_BOOST:
                    requireDefinition(item, item.activationMode == ActivationMode.MANUAL
                        && item.durationKind == DurationKind.TIMED
                        && item.durationMillis != null
                        && item.charges == null && item.magnitude != null && item.magnitude > 1.0
                        && item.maxStacks == null
                        && item.supportedGrantTimings.equals(EnumSet.of(GrantTiming.NOW)));
                    break;
                case EXTRA_SLOTS:
                case MAGNET:
                case MAX_LIFE:
                case LONG_LASTING:
                    requireDefinition(item, isEquipped(item));
                    break;
                default:
                    throw new IllegalArgumentException("unsupported effect kind: " + item.effectKind);
            }
        }

        requireValid(kinds.size() == EffectKind.values().length, "missing effect definition");
    }

    private boolean isRunStack(ItemInfo item) {
        return item.activationMode == ActivationMode.ON_PICKUP
            && item.durationKind == DurationKind.UNTIL_RUN_END
            && item.durationMillis == null && item.charges == null
            && item.magnitude != null && item.maxStacks != null
            && item.supportedGrantTimings.equals(
                EnumSet.of(GrantTiming.NOW, GrantTiming.NEXT_LEVEL));
    }

    /** Equipped items: EQUIPPED + WHILE_EQUIPPED, one value in magnitude, never dropped or stacked. */
    private boolean isEquipped(ItemInfo item) {
        return item.activationMode == ActivationMode.EQUIPPED
            && item.durationKind == DurationKind.WHILE_EQUIPPED
            && item.durationMillis == null && item.charges == null
            && item.magnitude != null && item.maxStacks == null
            && item.supportedGrantTimings.equals(EnumSet.of(GrantTiming.NOW));
    }

    /** Extra share of a multiplier as a percent without trailing zeros: 1.5 -> "50". */
    private static String percent(double multiplier) {
        return new java.math.BigDecimal(Double.toString((multiplier - 1.0) * 100))
            .setScale(1, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private static String seconds(long millis) {
        return millis % 1000 == 0 ? Long.toString(millis / 1000) : Double.toString(millis / 1000.0);
    }

    private void requireDefinition(ItemInfo item, boolean condition) {
        requireValid(condition, "invalid definition: " + item.itemId);
    }

    private void requireValid(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
