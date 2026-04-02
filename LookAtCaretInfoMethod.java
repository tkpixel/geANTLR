import javafx.scene.text.CaretInfo;
public class LookAtCaretInfoMethod {
    public static void main(String[] args) {
        System.out.println("How could it compile if getSegmentCount doesn't exist on CaretInfo?");
        for (java.lang.reflect.Method m : CaretInfo.class.getMethods()) {
            System.out.println(m);
        }
    }
}
