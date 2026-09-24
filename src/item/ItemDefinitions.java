package item;

import java.util.List;
import item.ItemAPI.*;

/**
 * TODO[DEFINITIONS]: 종류의 데이터 사전. 게임 상태/가격/재고/드랍 확률은 저장하지 않는다.
 * 이 클래스는 package-private이다. 다른 팀은 ItemAPI의 조회 함수만 사용한다.
 * ItemInfo 자체를 불변 정의로 재사용한다. 별도 Definition/Repository 파일을 만들지 않는다.
 * 미구현 업무 메서드는 명시적으로 예외를 던진다. 빈 카탈로그로 성공한 척하지 않는다.
 */
class ItemDefinitions {
    ItemDefinitions() {
        // TODO: 기본 정의를 등록하고 종류/수치/NEXT_LEVEL 조합을 검증한 뒤 불변으로 확정한다.
        // find/all은 이후 조회만 수행한다. beginLevel 전 직접 지급에서도 유효한 정의여야 한다.
    }

    /**
     * TODO: 등록된 ID를 조회한다. 없는 유효 ID만 null이다. 목록은 한 판 동안 불변이다.
     * 기본 등록: life(즉시 목숨1), shield(수동 10초/1회), rapid_fire(즉시 1.5배/스테이지),
     * bullet_speed(즉시 1.10배/스테이지), freeze(수동 5초 이동차단).
     * 모두 NOW 지원. rapid_fire/bullet_speed만 NEXT_LEVEL 지원. 각 수치는 기획 합의와 대조한다.
     */
    ItemInfo find(String itemId) { throw pending("find"); }

    /** TODO: 등록 순서의 불변 목록. 상점의 판매 목록이 아니다. find와 같은 정의를 사용한다. */
    List<ItemInfo> all() { throw pending("all"); }

    /**
     * TODO: 상태 변경 없이 모든 정의/규칙을 검증한다.
     * 두 DropSource 규칙, 존재하는 ID, 양의 유한 가중치 합(p>0일 때), 설정 영역을 검증한다.
     * 정의의 kind/duration/수치 조합도 확인한다. 동일 kind 중복 효과 정책은 REJECT다.
     * NEXT_LEVEL은 ON_PICKUP + UNTIL_LEVEL_END인 RAPID_FIRE/BULLET_SPEED만 가능하다.
     * LIFE는 INSTANT, SHIELD/FREEZE는 TIMED, 두 배율 효과는 UNTIL_LEVEL_END로 고정한다.
     * SHIELD는 durationMillis/charges, FREEZE는 durationMillis, 두 배율은 magnitude가 필수다.
     * 지원하지 않는 값 조합은 예외. 잘못된 설정을 자동 보정하거나 게임을 시작하지 않는다.
     */
    void validate(LevelRules rules) { throw pending("validate"); }

    private UnsupportedOperationException pending(String method) {
        return new UnsupportedOperationException("[TODO][DEFINITIONS] ItemDefinitions." + method);
    }
}
