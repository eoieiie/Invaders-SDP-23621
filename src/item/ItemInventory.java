package item;

import java.util.Arrays;
import java.util.List;
import item.ItemAPI.*;

/**
 * 한 판의 액티브 슬롯 원본을 유일하게 관리한다.
 * 슬롯당 ItemInfo 하나 또는 null. 인덱스는 0부터, 자동 압축/이동/수량 스택 없음.
 * 게임/상점 객체를 보관하지 않는다. 효과 적용은 매니저가 조정한다.
 * 잘못된 호출(범위 밖, 점유 슬롯에 저장, 빈 슬롯 소비, MANUAL이 아닌 아이템 저장)은
 * 상태를 바꾸기 전에 예외를 던진다. 조용히 실패하거나 no-op로 숨기지 않는다.
 */
class ItemInventory {
    private final int capacity;
    /** 원본 슬롯 배열. 이 파일 밖으로 절대 노출하지 않는다 (snapshot은 사본). */
    private final ItemInfo[] slots;

    ItemInventory(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity");
        this.capacity = capacity;
        this.slots = new ItemInfo[capacity];
    }
    int capacity() { return capacity; }

    /** 읽기 전용. 가장 작은 빈 슬롯 인덱스, 없으면 -1. 예약/저장하지 않는다. */
    int firstEmptySlot() {
        for (int i = 0; i < capacity; i++) {
            if (slots[i] == null) return i;
        }
        return -1;
    }

    /** 해당 칸 조회, 비어 있으면 null. 범위 오류는 예외. */
    ItemInfo at(int slot) {
        checkRange(slot);
        return slots[slot];
    }

    /**
     * 매니저가 확인한 빈 슬롯에 MANUAL 아이템을 저장한다.
     * 비어 있지 않거나 인덱스/분류가 잘못되면 변경 전에 예외를 던진다.
     */
    void store(int slot, ItemInfo item) {
        checkRange(slot);
        ItemAPI.required(item, "item");
        if (item.activationMode != ActivationMode.MANUAL)
            throw new IllegalArgumentException("only MANUAL items can be stored: " + item.itemId);
        if (slots[slot] != null)
            throw new IllegalStateException("slot " + slot + " is occupied by " + slots[slot].itemId);
        slots[slot] = item;
    }

    /** 매니저가 효과 적용에 성공한 점유 슬롯을 비운다. 빈 슬롯이면 변경 전 예외. */
    void consume(int slot) {
        checkRange(slot);
        if (slots[slot] == null)
            throw new IllegalStateException("slot " + slot + " is already empty");
        slots[slot] = null;
    }

    /** capacity 길이의 수정 불가 사본. null은 실제 빈 슬롯이며 이 목록에서만 허용. */
    List<ItemInfo> snapshot() {
        return ItemAPI.frozen(Arrays.asList(slots), true);
    }

    private void checkRange(int slot) {
        if (slot < 0 || slot >= capacity)
            throw new IndexOutOfBoundsException("slot " + slot + " out of range [0, " + capacity + ")");
    }
}
