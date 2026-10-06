package item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import item.ItemAPI.*;

/**
 * 종류의 데이터 사전. 게임 상태/가격/재고/드랍 확률은 저장하지 않는다.
 * 이 클래스는 package-private이다. 다른 팀은 ItemAPI의 조회 함수만 사용한다.
 * ItemInfo 자체를 불변 정의로 재사용한다. 별도 Definition/Repository 파일을 만들지 않는다.
 */
class ItemDefinitions {
    private final Map<String, ItemInfo> itemsById;
    private final List<ItemInfo> items;

    ItemDefinitions() {
        Map<String, ItemInfo> definitions = new LinkedHashMap<String, ItemInfo>();

        ItemInfo life = new ItemInfo(
            "life",
            "Life",
            "Adds one life, or awards 500 points at the life cap.",
            "life",
            ActivationMode.ON_PICKUP,
            EffectKind.LIFE,
            DurationKind.INSTANT,
            null,
            null,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(life.itemId, life);

        ItemInfo shield = new ItemInfo(
            "shield",
            "Shield",
            "Blocks one incoming hit for up to 10 seconds.",
            "shield",
            ActivationMode.MANUAL,
            EffectKind.SHIELD,
            DurationKind.TIMED,
            10_000L,
            1,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(shield.itemId, shield);

        ItemInfo rapidFire = new ItemInfo(
            "rapid_fire",
            "Rapid Fire",
            "Increases firing rate by 50% until the level ends.",
            "rapid_fire",
            ActivationMode.ON_PICKUP,
            EffectKind.RAPID_FIRE,
            DurationKind.UNTIL_LEVEL_END,
            null,
            null,
            1.5,
            EnumSet.of(GrantTiming.NOW, GrantTiming.NEXT_LEVEL)
        );
        definitions.put(rapidFire.itemId, rapidFire);

        ItemInfo bulletSpeed = new ItemInfo(
            "bullet_speed",
            "Bullet Speed",
            "Increases projectile speed by 10% until the level ends.",
            "bullet_speed",
            ActivationMode.ON_PICKUP,
            EffectKind.BULLET_SPEED,
            DurationKind.UNTIL_LEVEL_END,
            null,
            null,
            1.10,
            EnumSet.of(GrantTiming.NOW, GrantTiming.NEXT_LEVEL)
        );
        definitions.put(bulletSpeed.itemId, bulletSpeed);

        ItemInfo freeze = new ItemInfo(
            "freeze",
            "Freeze",
            "Stops all enemy movement for 5 seconds.",
            "freeze",
            ActivationMode.MANUAL,
            EffectKind.FREEZE,
            DurationKind.TIMED,
            5_000L,
            null,
            null,
            EnumSet.of(GrantTiming.NOW)
        );
        definitions.put(freeze.itemId, freeze);

        itemsById = Collections.unmodifiableMap(definitions);
        items = Collections.unmodifiableList(new ArrayList<ItemInfo>(definitions.values()));
        validateDefinitions();
    }

    /**
     * 등록된 ID를 조회한다. 없는 유효 ID만 null이다. 목록은 한 판 동안 불변이다.
     * 기본 등록: life(즉시 목숨1), shield(수동 10초/1회), rapid_fire(즉시 1.5배/스테이지),
     * bullet_speed(즉시 1.10배/스테이지), freeze(수동 5초 이동차단).
     * 모두 NOW 지원. rapid_fire/bullet_speed만 NEXT_LEVEL 지원. 각 수치는 기획 합의와 대조한다.
     */
    ItemInfo find(String itemId) { return itemsById.get(itemId); }

    /** 등록 순서의 불변 목록. 상점의 판매 목록이 아니다. find와 같은 정의를 사용한다. */
    List<ItemInfo> all() { return items; }

    /**
     * 상태 변경 없이 모든 정의/규칙을 검증한다.
     * 두 DropSource 규칙, 존재하는 ID, 양의 유한 가중치 합(p>0일 때), 설정 영역을 검증한다.
     * 정의의 kind/duration/수치 조합도 확인한다. 동일 kind 중복 효과 정책은 REJECT다.
     * NEXT_LEVEL은 ON_PICKUP + UNTIL_LEVEL_END인 RAPID_FIRE/BULLET_SPEED만 가능하다.
     * LIFE는 INSTANT, SHIELD/FREEZE는 TIMED, 두 배율 효과는 UNTIL_LEVEL_END로 고정한다.
     * SHIELD는 durationMillis/charges, FREEZE는 durationMillis, 두 배율은 magnitude가 필수다.
     * 지원하지 않는 값 조합은 예외. 잘못된 설정을 자동 보정하거나 게임을 시작하지 않는다.
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
                        && item.supportedGrantTimings.equals(EnumSet.of(GrantTiming.NOW)));
                    break;
                case SHIELD:
                    requireDefinition(item, item.activationMode == ActivationMode.MANUAL
                        && item.durationKind == DurationKind.TIMED
                        && Long.valueOf(10_000L).equals(item.durationMillis)
                        && Integer.valueOf(1).equals(item.charges) && item.magnitude == null
                        && item.supportedGrantTimings.equals(EnumSet.of(GrantTiming.NOW)));
                    break;
                case RAPID_FIRE:
                    requireDefinition(item, isLevelMultiplier(item, 1.5));
                    break;
                case BULLET_SPEED:
                    requireDefinition(item, isLevelMultiplier(item, 1.10));
                    break;
                case FREEZE:
                    requireDefinition(item, item.activationMode == ActivationMode.MANUAL
                        && item.durationKind == DurationKind.TIMED
                        && Long.valueOf(5_000L).equals(item.durationMillis)
                        && item.charges == null && item.magnitude == null
                        && item.supportedGrantTimings.equals(EnumSet.of(GrantTiming.NOW)));
                    break;
                default:
                    throw new IllegalArgumentException("unsupported effect kind: " + item.effectKind);
            }
        }

        requireValid(kinds.size() == EffectKind.values().length, "missing effect definition");
    }

    private boolean isLevelMultiplier(ItemInfo item, double magnitude) {
        return item.activationMode == ActivationMode.ON_PICKUP
            && item.durationKind == DurationKind.UNTIL_LEVEL_END
            && item.durationMillis == null && item.charges == null
            && Double.valueOf(magnitude).equals(item.magnitude)
            && item.supportedGrantTimings.equals(
                EnumSet.of(GrantTiming.NOW, GrantTiming.NEXT_LEVEL));
    }

    private void requireDefinition(ItemInfo item, boolean condition) {
        requireValid(condition, "invalid definition: " + item.itemId);
    }

    private void requireValid(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
