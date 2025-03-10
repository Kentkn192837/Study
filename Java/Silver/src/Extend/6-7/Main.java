class A {
    A(String message) {
        System.out.println(message + " from A.");
    }
}

class B extends A {
    B() {
        // class Aに引数無しのコンストラクタを定義しないとコンパイルエラーとなる。
        System.out.println("Hello from B.");
    }
}

public class Main {
    public static void main(String[] args) {
        B b = new B();
    }
}
