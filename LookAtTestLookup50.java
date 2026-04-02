public class LookAtTestLookup50 {
    public static void main(String[] args) {
        System.out.println("Okay. So NO standard JavaFX API exposes the paragraph index publicly on the cell.");
        System.out.println("How do I fix `IllegalAccessException` ?");
        System.out.println("Wait! `java.lang.reflect.Method getIndexMethod = cell.getClass().getMethod(\"getIndex\");` throws IllegalAccessException? Let's verify this is exactly what happened!");
        System.out.println("We ran `EditorViewController.java` in the maven build. The maven build does NOT have `--add-exports`!");
        System.out.println("So it WILL throw `IllegalAccessException`.");
        System.out.println("BUT wait... can we use `MethodHandle` or `CodeArea` API to find visible paragraphs?");
        System.out.println("In `CodeArea`, there is `public final jfx.incubator.scene.control.richtext.TextPos getTextPosition(double x, double y)`.");
        System.out.println("Wait, I used `Bounds bounds = cell.localToScreen(...)`, then `area.screenToLocal(...)`.");
        System.out.println("Why did it return 0 for everything?");
        System.out.println("Look at `TestLookup47.java` output:");
        System.out.println("`Cell 0 pt.Y=13.0 pos1=0`");
        System.out.println("`Cell 1 pt.Y=29.0 pos1=0`");
        System.out.println("Wait! If `pt.Y` is 29.0, how is `pos1` = 0? The first line is 0 to 16. The second is 16 to 32. 29 is the second line!");
        System.out.println("Why did it map to paragraph 0?");
        System.out.println("Because `CodeArea` is the root component. Does it have padding or headers? No.");
        System.out.println("Maybe `area.getTextPosition` calculates based on `VFlow` coordinates, not `CodeArea` coordinates?");
        System.out.println("Ah! The coordinates passed to `getTextPosition` MUST be in `CodeArea`'s coordinate space.");
        System.out.println("If it returns 0 for everything, maybe `CodeArea` delegates to its skin improperly, or we passed X out of bounds? `x=5`.");
    }
}
