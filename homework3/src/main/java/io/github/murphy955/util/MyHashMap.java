package io.github.murphy955.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * 手写hashmap
 *
 * @author : 李泽聿
 * @since : 2026:07:09 09:04
 */
public class MyHashMap {
    // 数组
    private List<Object> data = null;
    // 默认容量
    private static final int DEFAULT_CAPACITY = 16;
    // 链表阈值
    private static final int LINKED_LIST_THRESHOLD = 8;

    /**
     * <ol>
     *     <li>数组未初始化</li>
     *     <li>目标位为空</li>
     *     <li>冲突位置小于等于8时，链表</li>
     *     <li>冲突位置大于8时，红黑树</li>
     * </ol>
     *
     * @param key   键
     * @param value 值
     * @author 李泽聿
     * @since 2026-07-09 09:08
     */
    public void put(Object key, Object value) {
        if (this.data == null || this.data.isEmpty()) {
            this.data = new ArrayList<>(DEFAULT_CAPACITY);
        }
        int hash = key.hashCode();
        int purposePos = hash % this.data.size();
        Object currentNode = this.data.get(purposePos);
        // 目标位为空
        if (currentNode == null) {
            this.data.set(purposePos, new Node(key, value, hash));
        }
        // 处理hash冲突
        // 1. put时冲突点仅含两个元素
        if (currentNode != null
                && !(currentNode instanceof LinkedList<?>)
                && !(currentNode instanceof TreeMap<?, ?>)){
            LinkedList<Node> linkedList = new LinkedList<>();
            linkedList.add((Node) currentNode);
            linkedList.add(new Node(key, value, hash));
            this.data.set(purposePos, linkedList);
        }
        // 2. put时冲突点已含多个元素,且链表长度小于等于8
        if (currentNode instanceof LinkedList<?>
                && ((LinkedList<?>) currentNode).size() <= LINKED_LIST_THRESHOLD) {
            ((LinkedList<Node>) currentNode).add(new Node(key, value, hash));
        }
        // 3. put时冲突点已含多个元素,且链表长度大于8
        if (currentNode instanceof LinkedList<?>
                && ((LinkedList<?>) currentNode).size() > LINKED_LIST_THRESHOLD) {
            // 链表长度大于8,转为红黑树
            TreeMap<Object, Object> treeMap = new TreeMap<>();
            while (!((LinkedList<?>) currentNode).isEmpty()) {
                treeMap.put(((LinkedList<?>) currentNode).removeFirst(), new Node(key, value, hash));
            }
            treeMap.put(key, value);
            this.data.set(purposePos, treeMap);
        }
        System.out.println("put success");
    }

    private void resize(int len) {

    }

    @Data
    @AllArgsConstructor
    class Node {
        Object key;
        Object value;
        int hash;
    }
}
