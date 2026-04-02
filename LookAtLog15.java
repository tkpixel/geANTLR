public class LookAtLog15 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextFlow` initialization inside `getCaretBounds`:");
        System.out.println("`if (child instanceof javafx.scene.text.TextFlow flow) { textFlow = flow; break; }`");
        System.out.println("We established that `TextCell`'s immediate children are `Path` (caret), `Path` (selection), `TextFlow`.");
        System.out.println("Wait, NO! The previous dump of `.content > *` was:");
        System.out.println("`Node Path no index`, `Node Path no index`, `Node Path no index`, `Node TextCell index=0`...");
        System.out.println("So `TextCell` is a child of `.content`. Then its children are `TextFlow`.");
        System.out.println("But look at the FIRST dump of the node tree in `InspectCodeAreaNodes.java`:");
        System.out.println("```");
        System.out.println("com.sun.jfx.incubator.scene.control.richtext.TextCell id=null styleClass=");
        System.out.println("  javafx.scene.text.TextFlow id=null styleClass=");
        System.out.println("    javafx.scene.text.Text id=null styleClass=");
        System.out.println("```");
        System.out.println("Wait, if `child instanceof TextFlow` is true, it finds it. But if the `TextFlow` is NOT the only one?");
        System.out.println("There is only one `TextFlow` in the dump.");
        System.out.println("Could `firstNonWsIdx` be out of bounds if the line has NO text? (e.g. empty line)");
        System.out.println("If it's empty, `firstNonWsIdx >= openLineText.length()`, so `firstNonWsIdx = openPos.offset()`. If the line is empty, `openPos.offset()` is 0. So it uses index 0, offset 0. `caretInfoAt(0)`.");
        System.out.println("Wait... what if `caretInfoAt` returns a 0 width, 0 height rect?");
        System.out.println("In `TestLookup11.java`, we saw `caretInfo=[Rectangle2D [minX=0.0, minY=0.0, maxX=0.0, maxY=15.1328125, width=0.0, height=15.1328125]]`.");
        System.out.println("So `width` is 0.0, but `height` is 15.13. So `getMinY()` and `getMaxY()` are different.");
        System.out.println("Wait. Is there ANY chance `caretInfoAt` throws an exception that gets swallowed?");
        System.out.println("Yes! `catch (Exception e) { // ignore nodes that do not have getIndex() }`");
        System.out.println("Wait! The catch block is OUTSIDE the `if (indexVal == targetPara)` block!");
        System.out.println("Look at `EditorViewController.java`:");
        System.out.println("```");
        System.out.println("try {");
        System.out.println("    Method getIndexMethod = cell.getClass().getMethod(\"getIndex\");");
        System.out.println("    Object indexVal = getIndexMethod.invoke(cell);");
        System.out.println("    if (indexVal == targetPara) { ... break; }");
        System.out.println("} catch (Exception e) { }");
        System.out.println("```");
        System.out.println("If ANYTHING inside the `if` block throws an exception, it gets caught and swallowed! And the loop continues, but we already called `break;`? No, if it throws, `break;` is skipped, but it's the `targetPara` cell, so the next cells won't match `targetPara`! So it returns `null`!");
        System.out.println("What could throw an exception?");
        System.out.println("1. `textFlow.getLayoutInfo().caretInfoAt(charIdx, true)`");
        System.out.println("2. `caretInfo.getSegmentAt(0)`");
        System.out.println("3. `textFlow.localToScene(...)`");
        System.out.println("4. `bracketLineOverlay.sceneToLocal(...)`");
        System.out.println("Let's check if any of these throws an exception.");
    }
}
