import java.util.function.*;

public class Main {
    // public static void main(string[] args) コンパイルエラー
    // public static main(String[] args) コンパイルエラー
    // public static void main(String[]) コンパイルエラー
    // public static void main(String args) 実行時エラー
    // public void main(String[] args) 実行時エラー
    // public static void Main(String[] args) 実行時エラー
    // public static void main(String args[]) これはOK
    public static void main(String[] args) {
        foo(() -> 10);
    }

    public static void foo(Supplier<Integer> obj) {
        System.out.println(obj.get());
    }
}
