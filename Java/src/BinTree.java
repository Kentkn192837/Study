import java.util.Comparator;

class BinTree<K, V> {

    /**
     * 2分探索木の実装
     * 
     * 2文探索木上の個々のノードを表すNode<K, V>クラスは次の4つのフィールドを持つ。
     * ・key: キー値
     * ・data: データ
     * ・left: 左子ノードへの参照
     * ・right: 右子ノードへの参照
     */
    static class Node<K, V> {
        private K key;
        private V data;
        private Node<K, V> left;
        private Node<K, V> right;

        Node(K key, V data, Node<K, V> left, Node<K, V> right) {
            this.key = key;
            this.data = data;
            this.left = left;
            this.right = right;
        }

        K getKey() {
            return key;
        }

        V getValue() {
            return data;
        }

        void print() {
            System.out.println(data);
        }
    }

    /**
     * 2分探索木クラスBinTree<K, V>は2つのフィールドを持つ。
     * ・root: 根への参照
     * ・comparator: キー値の大小関係を比較するためのコンパレータ
     * 
     * <? super T>や<? extends T>のことをワイルドカードという。
     * <? super T>: クラスT またはクラスTのスーパークラスを受け取る。
     * <? extends T> クラスT またはクラスTのサブクラスを受け取る。
     */
    private Node<K, V> root;
    private Comparator<? super K> comparator = null;
    public BinTree() {
        root = null;
    }

    public BinTree(Comparator<? super K> c) {
        this();
        comparator = c;
    }

    private int comp(K key1, K key2) {
        return (comparator == null) ? ((Comparable<K>)key1).compareTo(key2)
                                    : comparator.compare(key1, key2);
    }

    /**
     * 2分探索木の探索
     * key: 探したいノードのキー値
     */
    public V search(K key) {
        Node<K, V> p = root;

        while (true) {
            // ノードが存在せず、探索失敗
            if (p == null) return null;

            // keyの値とpのキー値を比較する
            int cond = comp(key, p.getKey());

            // key == p.getKey() のとき
            if (cond == 0) return p.getValue(); // 探索成功
            
            // key < p.getKey() のとき
            if (cond < 0) {
                // 左側のノードを辿る
                p = p.left;
                continue;
            }
            
            // key > p.getKey() のとき右側のノードを辿る
            if (cond > 0) {
                p = p.right;
                continue;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
