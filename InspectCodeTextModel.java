import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InspectCodeTextModel {
    public static void main(String[] args) throws Exception {
        for (Method m : Class.forName("jfx.incubator.scene.control.richtext.model.StyledTextModel").getMethods()) {
            System.out.println(m.getName());
        }
    }
}
