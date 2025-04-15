import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.Comparator;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) {
        List<Integer> data = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        data.removeIf(i -> i % 2 != 0); // Pridicate
        System.out.println(data);

        List<String> words = Arrays.asList("Tanaka", "Sato");
        words.replaceAll(str -> str.toUpperCase()); // UnaryOperator
        System.out.println(words);

        List<Integer> list = Arrays.asList(20, 30, 10);
        list.sort((o1, o2) -> o2.compareTo(o1)); // Comparator
        System.out.println(list);

        List<Integer> lists = Arrays.asList(20, 30, 10);
        lists.forEach(num -> System.out.println(num + " ")); // Consumer
    }
}
