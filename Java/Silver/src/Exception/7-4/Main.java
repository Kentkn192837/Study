public class Main {
    public static void main(String[] args) {
        System.out.println("1 ");
        x();
    }

    static void x() {
        y();
    }

    static void y() {
        if (Math.random() > 0.1) {
            throw new RuntimeException();
        }
        System.out.print("2 ");
    }
}
