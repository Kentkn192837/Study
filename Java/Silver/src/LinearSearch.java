import java.util.Scanner;

/**
 * 線形探索(LinearSearch)のプログラム
 * ランダムな並びの配列から探索を行う時に用いる。
 */
class LinearSearch {
    static int LinearSearch(int[] a, int n, int key) {
        for (int i = 0; i < n; i++) {
            if (a[i] == key)
                return i;
        }
        return -1;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("要素数:");
        final int num = sc.nextInt();
        int[] array = new int[num];

        for (int i = 0; i < num; i++) {
            System.out.print("array[" + i + "]:");
            array[i] = sc.nextInt();
        }

        while (true) {
            System.out.print("探す値:");
            int key = sc.nextInt();

            // 探索開始
            int idx = LinearSearch(array, num, key);

            if (idx == -1) {
                System.out.println("その値の要素は存在しません。");
                continue;
            }
            System.out.println("その値はarray[" + idx + "]にあります。");
        }
    }
}
