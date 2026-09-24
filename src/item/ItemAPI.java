package item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 외부에서 사용하는 유일한 진입점. 공개 자료형도 이 파일의 static 중첩 타입이다.
 * 요청 전달과 값 객체는 작성되어 있다. 내부 4개 파일이 STUB이므로 아직 게임에 연결하지 않는다.
 * 한 판당 한 인스턴스, 같은 게임 상태 소유 스레드에서만 호출한다. 콜백의 재진입은 금지한다.
 */
public final class ItemAPI {
    private final ItemManager manager;

    public ItemAPI(int inventoryCapacity, Random random) {
        if (inventoryCapacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        required(random, "random");
        manager = new ItemManager(inventoryCapacity, random);
    }

    public ItemInfo getItemInfo(String itemId) { return manager.getItemInfo(itemId); }
    public List<ItemInfo> getItemInfos() { return manager.getItemInfos(); }
    public GrantCheck checkGrant(GrantRequest request) { return manager.checkGrant(request); }
    public GrantResult tryGrant(GrantRequest request) { return manager.tryGrant(request); }
    public void beginLevel(LevelRules rules, LifePort port) { manager.beginLevel(rules, port); }
    public void onEnemyDefeated(DropSource source, double x, double y) {
        manager.onEnemyDefeated(source, x, y);
    }
    public void update(long deltaGameMillis, PlayerSnapshot player) { manager.update(deltaGameMillis, player); }
    public UseResult useSlot(int slotIndex) { return manager.useSlot(slotIndex); }
    public boolean tryBlockHit() { return manager.tryBlockHit(); }
    public Modifiers getModifiers() { return manager.getModifiers(); }
    public View getView() { return manager.getView(); }
    public List<ItemEvent> drainEvents() { return manager.drainEvents(); }
    public void endLevel() { manager.endLevel(); }

    public interface LifePort {
        /** 읽기 전용. 가능 여부만 확인하며 상태/이벤트를 변경하지 않는다. */
        boolean canAddLife();
        /** true: 목숨 정확히 1개 증가 완료. false: 아무 상태도 변경하지 않음. */
        boolean tryAddLife();
    }

    public enum ActivationMode { ON_PICKUP, MANUAL }
    public enum EffectKind { LIFE, SHIELD, RAPID_FIRE, BULLET_SPEED, FREEZE }
    public enum DurationKind { INSTANT, TIMED, UNTIL_LEVEL_END }
    public enum DropSource { REGULAR_ENEMY, SPECIAL_ENEMY }
    public enum GrantSource { SHOP, REWARD }
    public enum GrantTiming { NOW, NEXT_LEVEL }
    public enum GrantStatus { STORED, APPLIED, QUEUED_FOR_NEXT_LEVEL, REJECTED }
    public enum GrantCheckStatus { ELIGIBLE, REJECTED, ALREADY_PROCESSED }
    public enum GrantFailure {
        UNKNOWN_ITEM, TIMING_NOT_SUPPORTED, INVALID_PHASE, LEVEL_NOT_ACTIVE,
        INVENTORY_FULL, EFFECT_ALREADY_ACTIVE, EFFECT_ALREADY_QUEUED,
        EFFECT_REJECTED, REQUEST_ID_CONFLICT
    }
    public enum UseResult {
        USED, LEVEL_NOT_ACTIVE, INVALID_SLOT, PLAYER_UNAVAILABLE,
        EMPTY_SLOT, EFFECT_ALREADY_ACTIVE, EFFECT_REJECTED
    }
    public enum EffectEndReason { EXPIRED, CHARGES_EXHAUSTED, LEVEL_ENDED }
    public enum EventType {
        ITEM_SPAWNED, ITEM_COLLECTED, ITEM_USED, EFFECT_STARTED,
        EFFECT_ENDED, SHIELD_BLOCKED, ITEM_EXPIRED,
        ITEM_GRANTED, PENDING_GRANT_APPLIED
    }

    /** 작은 불변 값은 public final 필드로 읽는다. setter/상태 변경 메서드는 없다. */
    public static final class Bounds {
        public final double x, y, width, height;
        public Bounds(double x, double y, double width, double height) {
            finite(x, "x"); finite(y, "y"); positive(width, "width"); positive(height, "height");
            finite(x + width, "right"); finite(y + height, "bottom");
            this.x = x; this.y = y; this.width = width; this.height = height;
        }
    }

    public static final class PlayerSnapshot {
        public final Bounds bounds;
        public final boolean canPickup, canUseItems;
        public PlayerSnapshot(Bounds bounds, boolean canPickup, boolean canUseItems) {
            this.bounds = required(bounds, "bounds");
            this.canPickup = canPickup; this.canUseItems = canUseItems;
        }
    }

    public static final class Modifiers {
        public final double fireRateMultiplier, bulletSpeedMultiplier;
        public final boolean enemyMovementBlocked;
        public Modifiers(double fireRate, double bulletSpeed, boolean blocked) {
            positive(fireRate, "fireRate"); positive(bulletSpeed, "bulletSpeed");
            fireRateMultiplier = fireRate; bulletSpeedMultiplier = bulletSpeed;
            enemyMovementBlocked = blocked;
        }
        public static Modifiers neutral() { return new Modifiers(1.0, 1.0, false); }
    }

    /** 종류의 불변 정의 정보. 현재 소유/판매 여부와 무관하다. */
    public static final class ItemInfo {
        public final String itemId, displayName, description, iconKey;
        public final ActivationMode activationMode;
        public final EffectKind effectKind;
        public final DurationKind durationKind;
        public final Long durationMillis;
        public final Integer charges;
        public final Double magnitude;
        public final Set<GrantTiming> supportedGrantTimings;
        public ItemInfo(String itemId, String name, String description, String iconKey,
                        ActivationMode mode, EffectKind kind, DurationKind duration,
                        Long milliseconds, Integer charges, Double magnitude,
                        Set<GrantTiming> timings) {
            this.itemId = text(itemId, "itemId"); displayName = text(name, "name");
            this.description = required(description, "description"); this.iconKey = text(iconKey, "iconKey");
            activationMode = required(mode, "mode"); effectKind = required(kind, "kind");
            durationKind = required(duration, "duration");
            if (milliseconds != null && milliseconds <= 0) throw new IllegalArgumentException("durationMillis");
            if (charges != null && charges <= 0) throw new IllegalArgumentException("charges");
            if (magnitude != null) positive(magnitude, "magnitude");
            durationMillis = milliseconds; this.charges = charges; this.magnitude = magnitude;
            required(timings, "timings");
            if (timings.isEmpty()) throw new IllegalArgumentException("timings");
            for (GrantTiming timing : timings) required(timing, "timing");
            supportedGrantTimings = Collections.unmodifiableSet(EnumSet.copyOf(timings));
            // 항목 간 의미 검증(NEXT_LEVEL 지원 조합 등)은 ItemDefinitions 담당이다.
        }
    }

    public static final class DropRule {
        public final double probability;
        public final Map<String, Double> weights;
        public DropRule(double probability, Map<String, Double> weights) {
            finite(probability, "probability");
            if (probability < 0 || probability > 1) throw new IllegalArgumentException("probability");
            this.probability = probability;
            required(weights, "weights");
            Map<String, Double> copy = new LinkedHashMap<String, Double>();
            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                text(entry.getKey(), "itemId"); double weight = required(entry.getValue(), "weight");
                finite(weight, "weight");
                if (weight < 0) throw new IllegalArgumentException("weight");
                copy.put(entry.getKey(), weight);
            }
            this.weights = Collections.unmodifiableMap(copy);
        }
    }

    public static final class LevelRules {
        public final String levelId;
        public final double left, right, top, floorY, fallSpeed, pickupWidth, pickupHeight;
        public final long groundLifetimeMillis;
        public final Map<DropSource, DropRule> dropRules;
        public LevelRules(String id, double left, double right, double top, double floorY,
                          double fallSpeed, double width, double height, long lifetime,
                          Map<DropSource, DropRule> dropRules) {
            levelId = text(id, "levelId");
            finite(left, "left"); finite(right, "right"); finite(top, "top"); finite(floorY, "floorY");
            positive(fallSpeed, "fallSpeed"); positive(width, "pickupWidth"); positive(height, "pickupHeight");
            if (right <= left || floorY <= top || lifetime <= 0) throw new IllegalArgumentException("level bounds/lifetime");
            finite(right - left, "area width"); finite(floorY - top, "area height");
            if (width > right - left || height > floorY - top) throw new IllegalArgumentException("item exceeds area");
            this.left = left; this.right = right; this.top = top; this.floorY = floorY;
            this.fallSpeed = fallSpeed; pickupWidth = width; pickupHeight = height;
            groundLifetimeMillis = lifetime;
            required(dropRules, "dropRules");
            Map<DropSource, DropRule> copy = new EnumMap<DropSource, DropRule>(DropSource.class);
            for (Map.Entry<DropSource, DropRule> entry : dropRules.entrySet()) {
                copy.put(required(entry.getKey(), "source"), required(entry.getValue(), "rule"));
            }
            this.dropRules = Collections.unmodifiableMap(copy);
        }
    }

    public static final class GrantRequest {
        public final String requestId, itemId;
        public final GrantSource source;
        public final GrantTiming timing;
        public GrantRequest(String requestId, String itemId, GrantSource source, GrantTiming timing) {
            this.requestId = text(requestId, "requestId"); this.itemId = text(itemId, "itemId");
            this.source = required(source, "source"); this.timing = required(timing, "timing");
        }
        boolean sameContent(GrantRequest other) {
            return requestId.equals(other.requestId) && itemId.equals(other.itemId)
                && source == other.source && timing == other.timing;
        }
    }

    public static final class GrantResult {
        public final GrantRequest request;
        public final GrantStatus status;
        public final GrantFailure reason;
        public final Integer slotIndex;
        public final Long effectId, pendingGrantId;
        public final String levelId;
        private GrantResult(GrantRequest request, GrantStatus status, GrantFailure reason,
                            Integer slot, Long effect, Long pending, String level) {
            this.request = required(request, "request"); this.status = status; this.reason = reason;
            slotIndex = slot; effectId = effect; pendingGrantId = pending;
            levelId = level == null ? null : text(level, "levelId");
        }
        public boolean isSuccess() { return status != GrantStatus.REJECTED; }
        static GrantResult stored(GrantRequest r, int slot, String level) {
            if (slot < 0) throw new IllegalArgumentException("slot");
            return new GrantResult(r, GrantStatus.STORED, null, slot, null, null, level);
        }
        static GrantResult applied(GrantRequest r, Long effect, String level) {
            if (effect != null && effect <= 0) throw new IllegalArgumentException("effect");
            return new GrantResult(r, GrantStatus.APPLIED, null, null, effect, null, level);
        }
        static GrantResult queued(GrantRequest r, long pending, String level) {
            if (pending <= 0) throw new IllegalArgumentException("pending");
            return new GrantResult(r, GrantStatus.QUEUED_FOR_NEXT_LEVEL, null, null, null, pending, level);
        }
        static GrantResult rejected(GrantRequest r, GrantFailure reason, String level) {
            return new GrantResult(r, GrantStatus.REJECTED, required(reason, "reason"), null, null, null, level);
        }
    }

    public static final class GrantCheck {
        public final GrantCheckStatus status;
        public final GrantFailure reason;
        public final GrantResult previousResult;
        private GrantCheck(GrantCheckStatus s, GrantFailure reason, GrantResult previous) {
            status = s; this.reason = reason; previousResult = previous;
        }
        static GrantCheck eligible() { return new GrantCheck(GrantCheckStatus.ELIGIBLE, null, null); }
        static GrantCheck rejected(GrantFailure reason) {
            return new GrantCheck(GrantCheckStatus.REJECTED, required(reason, "reason"), null);
        }
        static GrantCheck processed(GrantResult previous) {
            return new GrantCheck(GrantCheckStatus.ALREADY_PROCESSED, null, required(previous, "previous"));
        }
    }

    public static final class DropView {
        public final long dropId;
        public final ItemInfo item;
        public final Bounds bounds;
        public final boolean grounded;
        public DropView(long id, ItemInfo item, Bounds bounds, boolean grounded) {
            if (id <= 0) throw new IllegalArgumentException("dropId");
            dropId = id; this.item = required(item, "item"); this.bounds = required(bounds, "bounds");
            this.grounded = grounded;
        }
    }

    public static final class EffectView {
        public final long effectId;
        public final ItemInfo item;
        public final Long remainingMillis;
        public final Integer remainingCharges;
        public EffectView(long id, ItemInfo item, Long time, Integer charges) {
            if (id <= 0) throw new IllegalArgumentException("effectId");
            if (time != null && time <= 0) throw new IllegalArgumentException("remainingMillis");
            if (charges != null && charges <= 0) throw new IllegalArgumentException("remainingCharges");
            effectId = id; this.item = required(item, "item"); remainingMillis = time; remainingCharges = charges;
        }
    }

    public static final class PendingGrantView {
        public final long pendingGrantId;
        public final GrantRequest request;
        public final ItemInfo item;
        PendingGrantView(long id, GrantRequest request, ItemInfo item) {
            if (id <= 0) throw new IllegalArgumentException("pendingGrantId");
            pendingGrantId = id; this.request = required(request, "request"); this.item = required(item, "item");
        }
    }

    public static final class View {
        public final List<DropView> drops;
        /** 목록 인덱스가 슬롯 번호. null 원소는 빈 슬롯이며 이 목록에서만 허용한다. */
        public final List<ItemInfo> slots;
        public final List<EffectView> effects;
        public final List<PendingGrantView> pendingGrants;
        public View(List<DropView> drops, List<ItemInfo> slots, List<EffectView> effects,
                    List<PendingGrantView> pending) {
            this.drops = frozen(drops, false); this.slots = frozen(slots, true);
            this.effects = frozen(effects, false); pendingGrants = frozen(pending, false);
        }
    }

    public static final class ItemEvent {
        public final long eventId;
        public final EventType type;
        public final String itemId, levelId;
        public final Long dropId, effectId, pendingGrantId;
        public final Integer slotIndex;
        public final GrantRequest request;
        public final GrantStatus grantStatus;
        public final EffectEndReason endReason;
        public final Bounds bounds;
        ItemEvent(long id, EventType type, String itemId, String level,
                  Long drop, Long effect, Integer slot, Long pending, GrantRequest request,
                  GrantStatus status, EffectEndReason reason, Bounds bounds) {
            if (id <= 0) throw new IllegalArgumentException("eventId");
            eventId = id; this.type = required(type, "type"); this.itemId = text(itemId, "itemId");
            levelId = level == null ? null : text(level, "levelId");
            dropId = drop; effectId = effect; slotIndex = slot; pendingGrantId = pending;
            this.request = request; grantStatus = status; endReason = reason; this.bounds = bounds;
        }
    }

    // 패키지 내부 공통 값 검증. 게임 규칙/난수/시계/외부 포트에 접근하지 않는다.
    static <T> T required(T value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " is required");
        return value;
    }
    static String text(String value, String name) {
        required(value, name);
        if (value.trim().length() == 0) throw new IllegalArgumentException(name + " is blank");
        return value;
    }
    static void finite(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) throw new IllegalArgumentException(name + " must be finite");
    }
    static void positive(double value, String name) {
        finite(value, name);
        if (value <= 0) throw new IllegalArgumentException(name + " must be positive");
    }
    static <T> List<T> frozen(List<T> values, boolean allowNullElements) {
        required(values, "list");
        List<T> copy = new ArrayList<T>(values);
        if (!allowNullElements && copy.contains(null)) throw new IllegalArgumentException("null list element");
        return Collections.unmodifiableList(copy);
    }
}
