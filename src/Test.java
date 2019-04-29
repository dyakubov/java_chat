import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<String> a = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            a.add(Integer.toString(i));
        }

        System.out.println(a.toString());
    }
}
