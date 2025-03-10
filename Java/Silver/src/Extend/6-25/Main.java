import java.util.*;

public class Main {
    public static void main(String[] args) {
        String[] ary = { "tokyo", "nagasaki", "mie", "nara" };
        // 要素数が変化するような処理をArrays.asList(ary)に適用できない
        // List<String> city = Arrays.asList(ary); UnsupportedOperationException
        List<String> city = new ArrayList<>(Arrays.asList(ary));
        if (city.removeIf(str -> str.length() <= 3)) {
            System.out.println(city);
        }
    }
}
