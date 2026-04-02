public class LookAtLog23 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`.");
        System.out.println("What if `firstNonWsIdx` is correctly identified, but the `indentPos` evaluates to something invalid?");
        System.out.println("No, we just tested it in `LookAtUpdateBracketLine.java` and it returned valid coordinates.");
        System.out.println("Wait! What if `TextCell` is found, but the `TextFlow` has no `LayoutInfo` because the cell is not laid out yet?");
        System.out.println("But we ran `Platform.runLater()`. So it should be laid out.");
        System.out.println("Is there ANY chance that the `openLineText.length()` is wrong? No.");
        System.out.println("Let's look at the only change I made between it working diagonally and vanishing entirely:");
        System.out.println("1. `if (matchedBrackets.bracketChar() != '{')` -> we established this works.");
        System.out.println("2. `TextPos indentPos` and `getCaretBounds(indentPos)` -> we tested this works.");
        System.out.println("3. `if (closeCaret.getMinY() >= openCaret.getMaxY())` -> we changed `>` to `>=`.");
        System.out.println("4. In `getCaretBounds`, I changed `.content > *` to find the exact cell using `getIndex()` via reflection.");
        System.out.println("Wait! The user says: `now the vertical line for {} brackets is completly vanished, no bracket or block has this line anymore`");
        System.out.println("So ANY bracket connection line is missing.");
        System.out.println("This means `getCaretBounds` ALWAYS returns NULL in the real app!");
        System.out.println("Why would `getCaretBounds` ALWAYS return NULL in the real app, but WORK in my test script `LookAtUpdateBracketLine.java`?");
        System.out.println("Ah! Look at `editorCodeArea.lookupAll(\".content > *\")`!");
        System.out.println("Wait, in `InspectCodeAreaNodes.java` output:");
        System.out.println("```");
        System.out.println("jfx.incubator.scene.control.richtext.CodeArea");
        System.out.println("  com.sun.jfx.incubator.scene.control.richtext.VFlow");
        System.out.println("    com.sun.jfx.incubator.scene.control.richtext.ClippedPane id=null styleClass=vport");
        System.out.println("      javafx.scene.layout.Pane id=null styleClass=content");
        System.out.println("        com.sun.jfx.incubator.scene.control.richtext.TextCell");
        System.out.println("```");
        System.out.println("Wait, `lookupAll(\".content > *\")` uses CSS selectors!");
        System.out.println("Does `.content > *` work reliably in JavaFX?");
        System.out.println("Yes, it found 7 nodes in my test script `TestLookup.java`.");
        System.out.println("BUT wait! `CodeArea`'s CSS structure might NOT be populated if CSS hasn't been fully applied or if `lookupAll` is buggy?");
        System.out.println("What if I use `lookupAll(\".content\")` instead?");
    }
}
