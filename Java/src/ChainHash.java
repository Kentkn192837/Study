/**
 * ハッシュ法(hashing)
 * データを格納すべき位置を単純な演算で求めることで、
 * 探索・追加・削除を効率よく行う。
 * 
 * 衝突が起こった際の対処方法
 * ・チェイン法: 同一のハッシュ値をもつ要素を線形リストで管理する。
 * ・オープンアドレス法: 空きバケットを見つけるまで、ハッシュを繰り返す。
 * ※バケット(bucket): ハッシュ表の各要素
 */
class ChainHash<K, V> {

    /**
     * ハッシュを構成するノード
     */
    class Node<K, V> {
        private K key;
        private V data;
        private Node<K, V> next;    // 後続ノードへの参照

        Node(K key, V data, Node<K, V> next) {
            this.key = key;
            this.data = data;
            this.next = next;
        }

        K getKey() {
            return key;
        }

        V getValue() {
            return value;
        }

        public int hashCode() {
            return key.hashCode();
        }
    }

    private int size;               // ハッシュ表の大きさ
    private Node<K, V>[] table;     // ハッシュ表

    ChainHash(int capacity) {
        try {
            table = new Node[capacity];
            this.size = capacity;
        } catch (OutOfMemoryError e) {
            this.size = 0;
        }
    }

    // ハッシュ関数
    public int hashValue(Object key) {
        return key.hashCode() % size;
    }

    // 探索
    // 追加
    // 削除

    public static void main(String[] args) {
        return;
    }
}
