import java.util.Scanner;
import java.util.ArrayList;

/**
 * 線形探索(LinearSearch)のプログラム
 * 番兵法により、繰返し終了判定を削減する。
 * 探索するキーの値と同じ値を配列の末尾に追加する。
 */
class LinearSearchSentinel {
    static int LinearSearchSentinel(ArrayList<int> a, int n, int key) {
        a.add(key);
        for (int i = 0; i < size(); i++) {
            if (a.get(i) == key) break;
        }
        return i == n ? -1 : i;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("要素数:");
        final int num = sc.nextInt();
        ArrayList<int> array = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            System.out.print("array[" + i + "]:");
            array.add(sc.nextInt());
        }

        while (true) {
            System.out.print("探す値:");
            int key = sc.nextInt();

            // 探索開始
            int idx = LinearSearchSentinel(array, num, key);

            if (idx == -1) {
                System.out.println("その値の要素は存在しません。");
                continue;
            }
            System.out.println("その値はarray[" + idx + "]にあります。");
        }
    }
}
