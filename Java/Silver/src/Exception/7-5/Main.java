public class Main {
    public static void main(String[] args) throws Exception {
        // 例外をキャッチするcatch句が無いので、すべてのメソッドにthrows Exceptionが必要
        x();
        System.out.println("1 ");
    }

    static void x() throws Exception {
        y();
        System.out.println("2 ");
    }

    static void y() throws Exception {
        throw new Exception();
    }
}
