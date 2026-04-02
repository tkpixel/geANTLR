public class LookAtLog32 {
    public static void main(String[] args) {
        System.out.println("Why did `updateBracketLine` NOT draw the line if `matchedBrackets` is valid?");
        System.out.println("It must be one of:");
        System.out.println("1. `indentCaret` or `openCaret` or `closeCaret` is NULL.");
        System.out.println("2. `closeCaret.getMinY() >= openCaret.getMaxY()` is FALSE.");
        System.out.println("3. `connectionLine.setVisible(true)` does not make it visible because of FXML ordering or CSS or a crash.");
        System.out.println("Let's look at `EditorViewController.java` at `getCaretBounds`:");
        System.out.println("```java");
        System.out.println("for (javafx.scene.Node child : parentCell.getChildrenUnmodifiable()) {");
        System.out.println("    if (child instanceof javafx.scene.text.TextFlow flow) {");
        System.out.println("        textFlow = flow;");
        System.out.println("        break;");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");
        System.out.println("Wait! Look at `InspectCodeAreaNodes.java` dump from earlier:");
        System.out.println("```");
        System.out.println("com.sun.jfx.incubator.scene.control.richtext.TextCell");
        System.out.println("  javafx.scene.text.TextFlow");
        System.out.println("```");
        System.out.println("Wait, if `openPos` and `closePos` are rendered, then `TextCell` exists. And `TextFlow` exists.");
        System.out.println("But wait, in `updateBracketLine`: `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("Does `indentPos` calculate correctly?");
        System.out.println("Yes. If `firstNonWsIdx` is out of bounds, it falls back to `openPos.offset()`. We checked this.");
        System.out.println("What if `firstNonWsIdx` IS NOT out of bounds, but it is equal to `openLineText.length()`?");
        System.out.println("`if (firstNonWsIdx >= openLineText.length())` catches it.");
        System.out.println("Wait! `TextFlow` in `getCaretBounds`:");
        System.out.println("```java");
        System.out.println("javafx.scene.text.CaretInfo caretInfo = layoutInfo.caretInfoAt(charIdx, true);");
        System.out.println("if (caretInfo != null && caretInfo.getSegmentCount() > 0) {");
        System.out.println("    javafx.geometry.Rectangle2D localCaret = caretInfo.getSegmentAt(0);");
        System.out.println("```");
        System.out.println("Wait! `charIdx` can be `0`. What if the paragraph is EMPTY?");
        System.out.println("If paragraph is empty, `charIdx` is 0. `caretInfoAt(0)` works.");
        System.out.println("Wait! I am catching ALL exceptions and throwing them to `LOG.warning`!");
        System.out.println("If `LOG.warning` happens, does the user see it? The user runs the packaged app. They wouldn't see it.");
        System.out.println("Could `layoutInfo.caretInfoAt(charIdx, true)` throw an exception?");
        System.out.println("In `TestLookup12`, `caretInfoAt(999, true)` did NOT throw an exception! It returned `null` or a valid bound.");
        System.out.println("Wait! If `caretInfoAt` throws NO exception, and returns bounds, it works.");
    }
}
