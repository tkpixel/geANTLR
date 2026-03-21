import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InspectTextPos {
    public static void main(String[] args) {
        for (Method m : CodeTextModel.class.getMethods()) {
            System.out.println(m);
        }
    }
}
