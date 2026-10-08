package item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import item.ItemAPI.*;

/**
 * 모든 효과를 한 파일에서 관리한다. 효과별 소스 파일/상속 프레임워크는 만들지 않는다.
 * 내부 RunningEffect에 effectId/ItemInfo/잔여 시간/횟수 등을 보관한다. kind별 최대 한 실행 효과.
 * 기본은 빈 상태, clear 이후도 빈 상태. effectId는 한 판 내 재사용하지 않는다.
 * 이벤트 ID/큐는 매니저 소유. 여기서는 적용/종료/방어 결과만 반환한다.
 */
class ItemEffectSystem {
    /** 실행 중인 지속 효과 원본. effectId 오름차순으로 순회된다. kind별 최대 1개. */
    private final TreeMap<Long, RunningEffect> running = new TreeMap<Long, RunningEffect>();
    /** 한 판 내 재사용하지 않는다. clear 후에도 이어서 증가한다. */
    private long nextEffectId = 1;

    ItemEffectSystem() { }

    /**
     * TODO: 실제 적용 가능성의 읽기 전용 검사. 가능하면 null, 불가하면 실패 사유.
     * LIFE는 port.canAddLife만 호출한다. 나머지는 같은 kind가 실행 중인지 확인한다.
     * 정상 실패는 EFFECT_ALREADY_ACTIVE 또는 EFFECT_REJECTED만 반환한다.
     * 포트는 필요한 LIFE에서만 접근하며 슬롯·효과·ID·난수·시간을 변경하지 않는다.
     */
    GrantFailure check(ItemInfo item, LifePort port) {
        ItemAPI.required(item, "item");
        if (item.effectKind == EffectKind.LIFE)
            return ItemAPI.required(port, "port").canAddLife() ? null : GrantFailure.EFFECT_REJECTED;
        return findByKind(item.effectKind) != null ? GrantFailure.EFFECT_ALREADY_ACTIVE : null;
    }

    /**
     * TODO: 동기적으로 실제 적용하고 Applied 반환. 성공 전 예상 가능한 검증을 전부 수행한다.
     * LIFE: port.tryAddLife() 한 번, true면 ok(null), false면 failed(EFFECT_REJECTED).
     * SHIELD: durationMillis/charges를 보관. RAPID_FIRE/BULLET_SPEED: magnitude와 스테이지 수명.
     * FREEZE: durationMillis 동안 이동 차단 상태. 모두 원래 기체 능력치를 직접 변경하지 않는다.
     * 지속 효과 성공은 새 effectId의 EffectView를 반환하며 그 전에 원본 등록이 끝나 있어야 한다.
     * 거절은 원본 변경/시간 갱신/ID 소비 없음. MANUAL 분류여도 useSlot에서 이 메서드로 발동 가능.
     * 입력/카탈로그/코딩 오류는 정상 거절로 숨기지 않는다. 포트/다른 콜백으로 매니저에 재진입 금지.
     */
    Applied apply(ItemInfo item, LifePort port) {
        ItemAPI.required(item, "item");
        if (item.effectKind == EffectKind.LIFE) {
            ItemAPI.required(port, "port");
            return port.tryAddLife() ? Applied.ok(null) : Applied.failed(GrantFailure.EFFECT_REJECTED);
        }
        validateDefinition(item); // 카탈로그 오류는 거절이 아니라 예외로 드러낸다.
        if (findByKind(item.effectKind) != null) return Applied.failed(GrantFailure.EFFECT_ALREADY_ACTIVE);

        RunningEffect effect = new RunningEffect(nextEffectId++, item);
        running.put(effect.effectId, effect); // 원본 등록을 끝낸 뒤 View를 반환한다.
        return Applied.ok(effect.view());
    }

    /**
     * 기존 효과의 시간만 delta만큼 감소. 만료되면 제거 후 Ended(EXPIRED) 반환.
     * 스테이지 지속 효과는 시간으로 만료시키지 않는다. id순서로 종료를 보고한다.
     * 이번 update에서 나중에 새로 적용되는 효과에는 지난 delta를 소급 적용하지 않는다.
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
     * 유효한 방패가 없으면 null. 있으면 차감 전 불변 View를 보관하고 방어 횟수 감소.
     * 마지막 횟수면 원본 효과도 제거하고 Hit(before,true), 아니면 Hit(before,false).
     * 판정과 소비를 한 호출에서 완료한다. 매니저가 SHIELD_BLOCKED/필요한 종료 사건을 기록한다.
     * advance와 clear에서 이미 제거된 방패를 다시 종료 보고하지 않는다.
     */
    Hit tryBlockHit() {
        RunningEffect shield = findByKind(EffectKind.SHIELD);
        if (shield == null) return null;
        EffectView before = shield.view();
        boolean exhausted = --shield.remainingCharges == 0;
        if (exhausted) running.remove(shield.effectId);
        return new Hit(before, exhausted);
    }

    /** 항상 1/1/false부터 현재 효과를 계산한다. 같은 kind는 최대 하나만 존재한다. */
    Modifiers modifiers() {
        double fireRate = 1.0;
        double bulletSpeed = 1.0;
        boolean movementBlocked = false;
        for (RunningEffect effect : running.values()) {
            switch (effect.item.effectKind) {
                case RAPID_FIRE:
                    fireRate = effect.item.magnitude;
                    break;
                case BULLET_SPEED:
                    bulletSpeed = effect.item.magnitude;
                    break;
                case FREEZE:
                    movementBlocked = true;
                    break;
                default:
                    break; // 방패는 방어 횟수로 처리하며 능력치 배율에는 영향을 주지 않는다.
            }
        }
        return new Modifiers(fireRate, bulletSpeed, movementBlocked);
    }

    /** TODO: 실행 중인 지속 효과만 id순서 불변 목록. LIFE/종료된 효과는 포함하지 않는다. */
    List<EffectView> snapshot() {
        List<EffectView> views = new ArrayList<EffectView>(running.size());
        for (RunningEffect effect : running.values()) views.add(effect.view());
        return Collections.unmodifiableList(views);
    }

    /** TODO: 실행 효과 전부 제거, 각각 Ended(LEVEL_ENDED) 반환. ID는 보존한다. */
    List<Ended> clear() {
        List<Ended> ended = new ArrayList<Ended>(running.size());
        for (RunningEffect effect : running.values())
            ended.add(new Ended(effect.item.itemId, effect.effectId, EffectEndReason.LEVEL_ENDED));
        running.clear(); // nextEffectId는 보존한다.
        return Collections.unmodifiableList(ended);
    }

    private RunningEffect findByKind(EffectKind kind) {
        for (RunningEffect effect : running.values()) if (effect.item.effectKind == kind) return effect;
        return null;
    }

    /** ItemDefinitions.validate와 같은 kind/duration/수치 조합 규칙. LIFE는 호출하지 않는다. */
    private static void validateDefinition(ItemInfo item) {
        switch (item.effectKind) {
            case SHIELD:
                expect(item, DurationKind.TIMED, item.durationMillis != null && item.charges != null);
                break;
            case FREEZE:
                expect(item, DurationKind.TIMED, item.durationMillis != null);
                break;
            case RAPID_FIRE:
            case BULLET_SPEED:
                expect(item, DurationKind.UNTIL_LEVEL_END, item.magnitude != null);
                break;
            default:
                throw new IllegalStateException("unsupported effect kind: " + item.effectKind);
        }
    }
    private static void expect(ItemInfo item, DurationKind duration, boolean valuesPresent) {
        if (item.durationKind != duration || !valuesPresent)
            throw new IllegalStateException("invalid effect definition: " + item.itemId);
    }

    /** 내부 가변 원본. 외부에는 view()의 불변 EffectView만 내보낸다. */
    private static final class RunningEffect {
        final long effectId;
        final ItemInfo item;
        /** TIMED 잔여 시간. UNTIL_LEVEL_END는 0을 저장하며 시간으로 만료시키지 않는다. */
        long remainingMillis;
        /** SHIELD 잔여 방어 횟수. 그 외 null. tryBlockHit이 감소시킨다. */
        Integer remainingCharges;

        RunningEffect(long effectId, ItemInfo item) {
            this.effectId = effectId; this.item = item;
            remainingMillis = item.durationKind == DurationKind.TIMED ? item.durationMillis : 0;
            remainingCharges = item.effectKind == EffectKind.SHIELD ? item.charges : null;
        }
        boolean timed() { return item.durationKind == DurationKind.TIMED; }
        EffectView view() {
            return new EffectView(effectId, item, timed() ? Long.valueOf(remainingMillis) : null, remainingCharges);
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
