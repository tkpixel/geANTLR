public class LookAtLog12 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextFlow flow` child loop:");
        System.out.println("for (javafx.scene.Node child : parentCell.getChildrenUnmodifiable()) {");
        System.out.println("    if (child instanceof javafx.scene.text.TextFlow flow) { textFlow = flow; break; }");
        System.out.println("}");
        System.out.println("But what if `parentCell` is a `TextCell`, and the `TextFlow` is NOT a direct child?!");
        System.out.println("Look at `InspectCodeAreaNodes.java` dump from earlier:");
        System.out.println("com.sun.jfx.incubator.scene.control.richtext.TextCell");
        System.out.println("  javafx.scene.text.TextFlow");
        System.out.println("Yes! `TextFlow` is a direct child of `TextCell`!");
        System.out.println("Wait! `TextCell` extends `BorderPane`.");
        System.out.println("Is `TextFlow` placed in the `center` or in the `children` list?");
        System.out.println("It's in the `children` list because `getChildrenUnmodifiable()` returned it in the dump.");
    }
}
