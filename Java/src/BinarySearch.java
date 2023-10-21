import java.util.Scanner;
import java.util.ArrayList;

/**
 * 探索する配列はソートされていることが条件。
 */
class BinarySearch {
    static int BinarySearch(ArrayList<int> array, int key) {
        int left = 0;                        // 探索範囲の先頭のインデックス
        int right = array.size() - 1;        // 探索範囲の末尾のインデックス
        while (left <= right) {
            int center = (left + right) / 2; // 探索範囲の中間のインデックス
            if (array[center] == key) {
                return center;
            }

            if (array[center] < key) {
                left = center + 1;
                continue;
            }

            if (array[center] > key) {
                right = center - 1;
                continue;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        ArrayList<int> array = new ArrayList<>();
        for (int val : new int[]{5, 7, 15, 28, 29, 31, 39, 58, 68, 70, 95}) {
            array.add(val);
        }
    }
}
