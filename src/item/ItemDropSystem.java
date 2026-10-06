package item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import item.ItemAPI.*;

/**
 * 드롭 관리: 아이템 개체 생성/낙하/착지/접촉/소멸의 원본 상태를 소유함.
 * 내부에 private static class DroppedItem을 만들어 id/ItemInfo/double 위치/착지/TTL을 저장함.
 * 별도 파일, Entity 상속, 인벤토리/효과 변경, 게임 객체 참조를 추가하지 않음.
 * 이벤트 큐는 여기 두지 않음. 생성/만료/접촉 결과를 매니저가 받아 사건으로 기록함.
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

    /** 검증된 새 규칙을 설치하고 개체 목록 초기화. dropId 증가 카운터는 재사용하지 않음. */
    void beginLevel(LevelRules rules) {
        this.rules = ItemAPI.required(rules, "rules");
        items.clear();
    }

    /**
     * source의 확률/가중치로 종류 하나를 추첨하고 실제 목록에 등록한 뒤 불변 View 반환.
     * 정상 미드랍만 null. p==0/1은 확률용 난수를 소비하지 않음. 단일 후보도 선택 난수 생략.
     * 가중치 순서는 LinkedHashMap 삽입 순서. 중심을 cx/cy에 맞추고 플레이 영역으로 보정함.
     * 일반 확률 0.15/특수 1.0은 데모 정책이며 실제 수치는 LevelRules에서 받음.
     * 같은 적의 중복 처치 통보 방지는 외부 게임이 담당함.
     */
    DropView spawn(DropSource source, double cx, double cy) {
        requireActive();
        ItemAPI.required(source, "source");
        ItemAPI.finite(cx, "cx");
        ItemAPI.finite(cy, "cy");
        DropRule rule = rules.dropRules.get(source);
        if (rule == null) throw new IllegalStateException("missing drop rule: " + source);
        if (rule.probability == 0) return null;
        if (rule.probability < 1 && random.nextDouble() >= rule.probability) return null;

        double total = 0;
        int candidates = 0;
        String selected = null;
        for (Map.Entry<String, Double> entry : rule.weights.entrySet()) {
            if (entry.getValue() > 0) {
                total += entry.getValue();
                candidates++;
                selected = entry.getKey();
            }
        }
        if (candidates == 0 || !Double.isFinite(total))
            throw new IllegalStateException("invalid drop weights");
        if (candidates > 1) {
            double target = random.nextDouble() * total;
            double cumulative = 0;
            for (Map.Entry<String, Double> entry : rule.weights.entrySet()) {
                if (entry.getValue() <= 0) continue;
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
     * delta>=0 밀리초만큼 개체 이동. y += fallSpeed * delta / 1000.0.
     * 아래쪽이 floorY에 닿으면 멈추며, 프레임 중간 착지라면 착지 이후 시간만 TTL 차감.
     * 1) 만료 개체를 먼저 목록에서 제거해 expired에 넣음.
     * 2) 나머지는 수직 이전/현재 구간과 플레이어 사각형의 접촉을 검사함(경계 포함).
     *    canPickup=false이면 contacts는 빈 목록. delta=0이라도 허용된 현재 접촉은 검사.
     * 3) contacts는 중복 없는 dropId 오름차순이며, 이 개체들은 아직 원본 목록에서 제거하지 않음.
     * 같은 직렬 update 동안 completePickup 이외에 contacts를 변경/제거하지 않음.
     * 획득 가능 여부/효과 적용은 매니저가 판단함. 여기서 저장·소비·이벤트 발행은 금지함.
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
            // 만료가 접촉보다 우선함. 접촉만으로 원본 아이템을 제거하지 않음.
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
     * 매니저가 획득 성공한 contact 하나를 제거함. 유효한 단일 갱신 처리 안에서 반드시 완료됨.
     * 없는 ID면 통합 버그이므로 예외. 이미 지급됐는데 조용히 제거 실패하는 코드는 금지함.
     */
    void completePickup(long dropId) {
        if (items.remove(dropId) == null)
            throw new IllegalStateException("unknown drop: " + dropId);
    }

    /** dropId 순서의 불변 View 사본. 원본 개체/목록은 노출하지 않음. */
    List<DropView> snapshot() {
        List<DropView> result = new ArrayList<DropView>();
        for (DroppedItem item : items.values()) result.add(item.view(rules));
        return ItemAPI.frozen(result, false);
    }

    /** 스테이지 종료 시 개체/현재 규칙 해제. 시간 만료 사건은 만들지 않고 ID는 유지함. */
    void clear() {
        items.clear();
        rules = null;
    }

    /** 내부 협력용 결과. 값 객체만 완성하며 이동/접촉 계산은 없음. */
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
