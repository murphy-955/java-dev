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
    // 负载因子
    private static final float LOAD_FACTOR = 0.75f;
    // 链表阈值
    private static final int LINKED_LIST_THRESHOLD = 8;
    // 当前元素个数
    private int size = 0;

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
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                this.data.add(null);
            }
        }

        // 计算下标，避免负数
        int hash = hash(key);
        int purposePos = indexFor(hash, this.data.size());
        Object current = this.data.get(purposePos);

        // 1. 目标位为空，直接放头节点
        switch (current) {
            case null -> {
                this.data.set(purposePos, new Node(key, value, hash));
                size++;
                System.out.println("put success");
                checkResize();
            }


            // 2. 目标位是单个 Node，检查是否 key 命中
            case Node head -> {
                if (head.hash == hash && keyEquals(head.key, key)) {
                    head.value = value;
                    System.out.println("put success, override");
                    return;
                }
                // 未命中，转为链表
                LinkedList<Node> list = new LinkedList<>();
                list.add(head);
                list.add(new Node(key, value, hash));
                this.data.set(purposePos, list);
                size++;
                System.out.println("put success");
                checkResize();
            }


            // 3. 目标位是链表
            case LinkedList<?> objects -> {
                LinkedList<Node> list = (LinkedList<Node>) current;
                for (Node node : list) {
                    if (node.hash == hash && keyEquals(node.key, key)) {
                        node.value = value;
                        System.out.println("put success, override");
                        return;
                    }
                }
                list.add(new Node(key, value, hash));
                size++;
                System.out.println("put success");
                // 链表长度超过阈值，转为红黑树（简化版）
                if (list.size() > LINKED_LIST_THRESHOLD) {
                    convertToTree(purposePos, list);
                }
                checkResize();
            }


            // 4. 目标位是红黑树（简化处理：按 key 找，找不到就 put）
            case TreeMap<?, ?> treeMap -> {
                TreeMap<Object, Node> tree = (TreeMap<Object, Node>) current;
                Node old = tree.put(key, new Node(key, value, hash));
                if (old == null) {
                    size++;
                    checkResize();
                }
                System.out.println("put success");
            }
            default -> {
            }
        }

    }

    /**
     * 计算 hash 值
     *
     * @param key 键
     * @return int
     * @author 李泽聿
     * @since 2026-07-09 09:56
     */
    private int hash(Object key) {
        int h = key.hashCode();
        return (h ^ (h >>> 16)) & 0x7FFFFFFF;
    }

    private int indexFor(int hash, int length) {
        return hash % length;
    }

    private boolean keyEquals(Object k1, Object k2) {
        return k1 == k2 || (k1 != null && k1.equals(k2));
    }

    private void checkResize() {
        int threshold = (int) (this.data.size() * LOAD_FACTOR);
        if (this.size > threshold) {
            resize(this.data.size() << 1);
        }
    }

    private void convertToTree(int pos, LinkedList<Node> list) {
        TreeMap<Object, Node> tree = new TreeMap<>();
        for (Node node : list) {
            tree.put(node.key, node);
        }
        this.data.set(pos, tree);
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
