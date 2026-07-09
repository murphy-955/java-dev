package io.github.murphy955.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

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
    @SuppressWarnings("unchecked")
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
     * 根据键获取值
     *
     * @param key 键
     * @return 值
     * @author 李泽聿
     * @since 2026-07-09 09:56
     */
    @SuppressWarnings("unchecked")
    public Object get(Object key) {
        int hash = hash(key);
        int purposePos = indexFor(hash, this.data.size());
        Object currentNode = this.data.get(purposePos);
        // 1. 无冲突
        if (!(currentNode instanceof LinkedList<?>)
                && !(currentNode instanceof TreeMap<?, ?>)) {
            System.out.println("get success.无冲突");
            return currentNode;
        }
        // 2. 有冲突(链表)
        if (currentNode instanceof LinkedList<?>) {
            LinkedList<Node> list = (LinkedList<Node>) currentNode;
            for (Node node : list) {
                if (node.hash == hash && keyEquals(node.key, key)) {
                    System.out.println("get success.有冲突(链表)");
                    return node.value;
                }
            }
        }
        // 3. 有冲突(红黑树)
        if (currentNode instanceof TreeMap<?, ?>) {
            TreeMap<Object, Node> tree = (TreeMap<Object, Node>) currentNode;
            Node node = tree.get(key);
            if (node != null) {
                System.out.println("get success.有冲突(红黑树)");
                return node.value;
            }
        }
        return null;
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

    /**
     * @param hash   哈希值
     * @param length 长度
     * @return int 哈希值的下标
     * @author 李泽聿
     * @since 2026-07-09 10:01
     */
    private int indexFor(int hash, int length) {
        return hash % length;
    }

    private boolean keyEquals(Object k1, Object k2) {
        return Objects.equals(k1, k2);
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

    /**
     * 扩容：创建新数组，容量翻倍，把旧节点全部重新哈希后迁移过去。
     *
     * @param newCapacity 新数组容量
     * @author 李泽聿
     * @since 2026-07-09 10:08
     */
    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        List<Object> oldData = this.data;

        this.data = new ArrayList<>(newCapacity);
        for (int i = 0; i < newCapacity; i++) {
            this.data.add(null);
        }

        this.size = 0;

        for (Object bucket : oldData) {
            switch (bucket) {
                case Node node -> {
                    if (putNodeTo(node, this.data)) {
                        this.size++;
                    }
                }
                case LinkedList<?> list -> {
                    for (Node node : (LinkedList<Node>) list) {
                        if (putNodeTo(node, this.data)) {
                            this.size++;
                        }
                    }
                }
                case TreeMap<?, ?> tree -> {
                    for (Node node : ((TreeMap<Object, Node>) tree).values()) {
                        if (putNodeTo(node, this.data)) {
                            this.size++;
                        }
                    }
                }
                case null, default -> {
                }
            }
        }
    }

    /**
     * 把单个节点放入目标数组，不修改 size、不触发扩容。
     *
     * @return true 表示新增了节点；false 表示 key 命中，覆盖了旧 value
     */
    @SuppressWarnings("unchecked")
    private boolean putNodeTo(Node node, List<Object> target) {
        int purposePos = indexFor(node.hash, target.size());
        Object current = target.get(purposePos);

        switch (current) {
            case null -> {
                target.set(purposePos, node);
                return true;
            }
            // 处理单个节点
            case Node head -> {
                if (head.hash == node.hash && keyEquals(head.key, node.key)) {
                    head.value = node.value;
                    return false;
                }
                LinkedList<Node> list = new LinkedList<>();
                list.add(head);
                list.add(node);
                target.set(purposePos, list);
                return true;
            }
            // 处理链表
            case LinkedList<?> objects -> {
                LinkedList<Node> list = (LinkedList<Node>) current;
                for (Node n : list) {
                    if (n.hash == node.hash && keyEquals(n.key, node.key)) {
                        n.value = node.value;
                        return false;
                    }
                }
                list.add(node);
                if (list.size() > LINKED_LIST_THRESHOLD) {
                    convertToTree(purposePos, list);
                }
                return true;
            }
            // 处理红黑树
            case TreeMap<?, ?> treeMap -> {
                TreeMap<Object, Node> tree = (TreeMap<Object, Node>) current;
                Node old = tree.put(node.key, node);
                return old == null;
            }
            default -> {
                return true;
            }
        }
    }

    @Data
    @AllArgsConstructor
    static class Node {
        Object key;
        Object value;
        int hash;
    }
}
