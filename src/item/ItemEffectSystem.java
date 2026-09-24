package item;

import java.util.List;
import item.ItemAPI.*;

/**
 * TODO[EFFECT]: 모든 효과를 한 파일에서 관리한다. 효과별 소스 파일/상속 프레임워크는 만들지 않는다.
 * 내부 RunningEffect에 effectId/ItemInfo/잔여 시간/횟수 등을 보관한다. kind별 최대 한 실행 효과.
 * 기본은 빈 상태, clear 이후도 빈 상태. effectId는 한 판 내 재사용하지 않는다.
 * 이벤트 ID/큐는 매니저 소유. 여기서는 적용/종료/방어 결과만 반환한다.
 */
class ItemEffectSystem {
    ItemEffectSystem() { /* TODO: 이 파일 내부에 실행 효과 원본 목록/맵을 생성한다. */ }

    /**
     * TODO: 실제 적용 가능성의 읽기 전용 검사. 가능하면 null, 불가하면 실패 사유.
     * LIFE는 port.canAddLife만 호출한다. 나머지는 같은 kind가 실행 중인지 확인한다.
     * 정상 실패는 EFFECT_ALREADY_ACTIVE 또는 EFFECT_REJECTED만 반환한다.
     * 포트는 필요한 LIFE에서만 접근하며 슬롯·효과·ID·난수·시간을 변경하지 않는다.
     */
    GrantFailure check(ItemInfo item, LifePort port) { throw pending("check"); }

    /**
     * TODO: 동기적으로 실제 적용하고 Applied 반환. 성공 전 예상 가능한 검증을 전부 수행한다.
     * LIFE: port.tryAddLife() 한 번, true면 ok(null), false면 failed(EFFECT_REJECTED).
     * SHIELD: durationMillis/charges를 보관. RAPID_FIRE/BULLET_SPEED: magnitude와 스테이지 수명.
     * FREEZE: durationMillis 동안 이동 차단 상태. 모두 원래 기체 능력치를 직접 변경하지 않는다.
     * 지속 효과 성공은 새 effectId의 EffectView를 반환하며 그 전에 원본 등록이 끝나 있어야 한다.
     * 거절은 원본 변경/시간 갱신/ID 소비 없음. MANUAL 분류여도 useSlot에서 이 메서드로 발동 가능.
     * 입력/카탈로그/코딩 오류는 정상 거절로 숨기지 않는다. 포트/다른 콜백으로 매니저에 재진입 금지.
     */
    Applied apply(ItemInfo item, LifePort port) { throw pending("apply"); }

    /**
     * TODO: 기존 효과의 시간만 delta만큼 감소. <=0이면 제거 후 Ended(EXPIRED) 반환.
     * 스테이지 지속 효과는 시간으로 만료시키지 않는다. id순서로 종료를 보고한다.
     * 이번 update에서 나중에 새로 적용되는 효과에는 지난 delta를 소급 적용하지 않는다.
     */
    List<Ended> advance(long delta) { throw pending("advance"); }

    /**
     * TODO: 유효한 방패가 없으면 null. 있으면 차감 전 불변 View를 보관하고 방어 횟수 감소.
     * 마지막 횟수면 원본 효과도 제거하고 Hit(before,true), 아니면 Hit(before,false).
     * 판정과 소비를 한 호출에서 완료한다. 매니저가 SHIELD_BLOCKED/필요한 종료 사건을 기록한다.
     * advance와 clear에서 이미 제거된 방패를 다시 종료 보고하지 않는다.
     */
    Hit tryBlockHit() { throw pending("tryBlockHit"); }

    /** TODO: 항상 1/1/false부터 유효 효과를 합성. 이전 배율에 반복 곱하지 않는다. */
    Modifiers modifiers() { throw pending("modifiers"); }

    /** TODO: 실행 중인 지속 효과만 id순서 불변 목록. LIFE/종료된 효과는 포함하지 않는다. */
    List<EffectView> snapshot() { throw pending("snapshot"); }

    /** TODO: 실행 효과 전부 제거, 각각 Ended(LEVEL_ENDED) 반환. ID는 보존한다. */
    List<Ended> clear() { throw pending("clear"); }

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
    private UnsupportedOperationException pending(String method) {
        return new UnsupportedOperationException("[TODO][EFFECT] ItemEffectSystem." + method);
    }
}
