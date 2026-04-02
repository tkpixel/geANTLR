public class LookAtLog40 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextPos jfx.incubator.scene.control.richtext.RichTextArea.getTextPosition(double x, double y)`.");
        System.out.println("If I pass `x` and `y` corresponding to the `TextFlow`'s center, it returns a `TextPos`!");
        System.out.println("`TextPos` HAS `index()` which is the PARAGRAPH INDEX!");
        System.out.println("This is PERFECT!");
        System.out.println("We iterate over `.content > *` cells.");
        System.out.println("For each cell, we find its center `y` in the `RichTextArea`'s coordinate space.");
        System.out.println("Then `editorCodeArea.getTextPosition(1, y)`.");
        System.out.println("It returns a `TextPos`. We just call `index()` on it!");
        System.out.println("This is a fully public API and it maps the visual coordinate back to the paragraph index!");
    }
}
