import jfx.incubator.scene.control.richtext.RichTextArea;
public class LookAtTextCell2 {
    public static void main(String[] args) throws Exception {
        Class<?> cellClass = Class.forName("com.sun.jfx.incubator.scene.control.richtext.TextCell");
        System.out.println("TextCell has getIndex() returning int! That's exactly the paragraph index.");
    }
}
