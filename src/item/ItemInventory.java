package item;

import java.util.List;
import item.ItemAPI.*;

/**
 * TODO[INVENTORY]: 한 판의 액티브 슬롯 원본을 유일하게 관리한다.
 * 슬롯당 ItemInfo 하나 또는 null. 인덱스는 0부터, 자동 압축/이동/수량 스택 없음.
 * 게임/상점 객체를 보관하지 않는다. 효과 적용은 매니저가 조정한다.
 */
class ItemInventory {
    private final int capacity;
    ItemInventory(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity");
        this.capacity = capacity;
        // TODO: 이 파일 내부에만 원본 슬롯 배열을 생성한다. 지금은 실제 저장 로직을 만들지 않는다.
    }
    int capacity() { return capacity; }

    /** TODO: 읽기 전용. 가장 작은 빈 슬롯 인덱스, 없으면 -1. 예약/저장하지 않는다. */
    int firstEmptySlot() { throw pending("firstEmptySlot"); }

    /** TODO: 해당 칸 조회, 비어 있으면 null. 범위 오류는 예외. */
    ItemInfo at(int slot) { throw pending("at"); }

    /**
     * TODO: 매니저가 확인한 빈 슬롯에 MANUAL 아이템을 저장한다.
     * 같은 직렬 호출 중 정상 입력이라면 완료된다. 비어 있지 않거나 인덱스/분류가 잘못되면
     * 변경 전에 예외를 던진다. 조용히 덮어쓰거나 false/no-op로 실패를 숨기지 않는다.
     */
    void store(int slot, ItemInfo item) { throw pending("store"); }

    /** TODO: 매니저가 효과 적용에 성공한 점유 슬롯을 비운다. 잘못된 호출은 변경 전 예외. */
    void consume(int slot) { throw pending("consume"); }

    /** TODO: capacity 길이의 수정 불가 사본. null은 실제 빈 슬롯이며 이 목록에서만 허용. */
    List<ItemInfo> snapshot() { throw pending("snapshot"); }

    private UnsupportedOperationException pending(String method) {
        return new UnsupportedOperationException("[TODO][INVENTORY] ItemInventory." + method);
    }
}
