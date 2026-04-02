import jfx.incubator.scene.control.richtext.RichTextArea;
public class LookAtTextCell {
    public static void main(String[] args) throws Exception {
        Class<?> cellClass = Class.forName("com.sun.jfx.incubator.scene.control.richtext.TextCell");
        System.out.println("TextCell superclass: " + cellClass.getSuperclass().getName());
        for(Class<?> iface : cellClass.getInterfaces()) {
            System.out.println("Iface: " + iface.getName());
        }
        for(java.lang.reflect.Method m : cellClass.getMethods()) {
            if(m.getName().toLowerCase().contains("index")) {
                System.out.println(m);
            }
        }
    }
}
