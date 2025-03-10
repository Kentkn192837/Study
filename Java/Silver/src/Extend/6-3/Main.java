class Parent {
    int num = 100;

    void show() {
        // 暗黙的にthis.numに変換されている
        System.out.println(num);
    }
}

class Child extends Parent {
    int num = 500;
}

public class Main {
    public static void main(String[] args) {
        Child c = new Child();
        c.show();
    }
}
