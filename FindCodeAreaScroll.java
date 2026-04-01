import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.TextPos;
import java.lang.reflect.Method;
public class FindCodeAreaScroll {
    public static void main(String[] args) throws Exception {
        for (Method m : CodeArea.class.getMethods()) {
            if (m.getName().toLowerCase().contains("scroll") || m.getName().toLowerCase().contains("show") || m.getName().toLowerCase().contains("viewport") || m.getName().toLowerCase().contains("caret") || m.getName().toLowerCase().contains("select")) {
                System.out.println(m.getName() + " " + java.util.Arrays.toString(m.getParameterTypes()));
            }
        }
    }
}
