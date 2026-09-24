package item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import item.ItemAPI.*;

/**
 * 내부 조정자: 이 파일의 조정 로직은 작성되어 있다.
 * 드랍/인벤토리/효과의 원본 상태는 각각 담당 파일이 소유한다.
 * 이 파일은 단계, 현재 연결, 외부 지급 결과, 예약, 사후 이벤트만 소유한다.
 * 네 협력 파일의 STUB을 구현하기 전에는 전체 기능을 실행할 수 없다.
 */
final class ItemManager {
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

    ItemManager(int capacity, Random random) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity");
        definitions = new ItemDefinitions();
        drops = new ItemDropSystem(definitions, ItemAPI.required(random, "random"));
        inventory = new ItemInventory(capacity);
        effects = new ItemEffectSystem();
    }

    /** 패키지 내부 테스트 조립용. 외부 팀의 API가 아니다. 네 객체는 이 매니저 전용이어야 한다. */
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
            // 사용/효과 만료/스테이지 전환 후에도 최초 결과를 그대로 반환한다.
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
            return pending.containsKey(item.effectKind) ? GrantFailure.EFFECT_ALREADY_QUEUED : null;
        }
        if (item.activationMode == ActivationMode.MANUAL)
            return inventory.firstEmptySlot() < 0 ? GrantFailure.INVENTORY_FULL : null;
        if (!active) return GrantFailure.LEVEL_NOT_ACTIVE;
        return effects.check(item, lifePort); // LIFE라면 읽기 전용 canAddLife만 사용해야 한다.
    }

    void beginLevel(LevelRules newRules, LifePort newPort) {
        if (active) throw new IllegalStateException("level is already active");
        ItemAPI.required(newRules, "rules"); ItemAPI.required(newPort, "lifePort");
        definitions.validate(newRules); // 전체 규칙/카탈로그를 상태 변경 전에 검증한다.
        // INACTIVE의 효과 목록은 비어 있어야 한다. 예약은 포트 없는 스테이지 효과로 제한한다.
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
        DropView spawned = drops.spawn(source, x, y);
        if (spawned != null)
            emit(EventType.ITEM_SPAWNED, spawned.item.itemId, spawned.dropId,
                null, null, null, null, null, null, spawned.bounds);
    }

    void update(long delta, PlayerSnapshot currentPlayer) {
        requireActive(); ItemAPI.required(currentPlayer, "player");
        if (delta < 0) throw new IllegalArgumentException("negative delta");
        // 지난 시간의 만료를 먼저 처리한 뒤 이번 갱신에 얻는 효과를 적용한다.
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
            // 같은 직렬 호출 동안 접촉 개체가 유지됨을 DropSystem 계약이 보장한다.
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
        inventory.consume(slot); // 적용 성공 전에는 절대 슬롯을 비우지 않는다.
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

    void endLevel() {
        if (!active) return;
        List<ItemEffectSystem.Ended> ended = effects.clear();
        drops.clear();
        for (ItemEffectSystem.Ended value : ended) emitEnded(value); // 종료될 levelId를 유지한다.
        active = false; rules = null; lifePort = null; player = null;
        // 인벤토리/미적용 예약/지급 결과/ID/대기 이벤트는 한 판 동안 보존한다.
    }

    /** 바닥 획득과 NOW 직접 지급이 공유한다. 바닥 제거/영수 기록/원인 이벤트는 호출자가 처리한다. */
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
            && item.durationKind == DurationKind.UNTIL_LEVEL_END
            && (item.effectKind == EffectKind.RAPID_FIRE || item.effectKind == EffectKind.BULLET_SPEED);
    }
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
        events.add(new ItemEvent(nextEventId++, type, itemId, levelId(), dropId, effectId,
            slot, pendingId, request, status, reason, bounds));
    }
}
