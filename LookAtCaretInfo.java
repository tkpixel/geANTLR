import java.lang.reflect.Method;
public class LookAtCaretInfo {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("javafx.scene.text.CaretInfo");
        for (Method m : clazz.getMethods()) {
            System.out.println(m);
        }
    }
}
