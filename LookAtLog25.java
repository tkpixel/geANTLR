public class LookAtLog25 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `EditorViewController.java` lines 529:");
        System.out.println("`if (closeCaret.getMinY() >= openCaret.getMaxY())`");
        System.out.println("Wait... what if `closeCaret.getMinY()` is SMALLER than `openCaret.getMaxY()`?");
        System.out.println("In my test `minY=21.0 maxY=20.1328125` so `minY > maxY` is true.");
        System.out.println("What if the user scrolls the CodeArea such that the opening brace is OUT OF THE VIEWPORT?");
        System.out.println("Then `openCaret` is null! `if (indentCaret != null && openCaret != null && closeCaret != null)` will be false! And the line is HIDDEN!");
        System.out.println("If they scroll, it's hidden. That's fine, the line vanishes if either is out of view.");
        System.out.println("Wait... the user clicked `FEHLER {`. They are both in view in the screenshot!");
        System.out.println("So `openCaret` is NOT null.");
        System.out.println("Why did it vanish?");
        System.out.println("Wait! I changed `closeCaret.getMinY() > openCaret.getMaxY()` to `>=`.");
        System.out.println("What if I changed the logic to use `indentCaret.getMinX()` and `indentCaret` was null?");
        System.out.println("Why would `indentCaret` be null?");
        System.out.println("```java");
        System.out.println("String openLineText = editorCodeArea.getModel().getPlainText(openPos.index());");
        System.out.println("int firstNonWsIdx = 0;");
        System.out.println("while (firstNonWsIdx < openLineText.length() && Character.isWhitespace(openLineText.charAt(firstNonWsIdx))) {");
        System.out.println("    firstNonWsIdx++;");
        System.out.println("}");
        System.out.println("if (firstNonWsIdx >= openLineText.length()) {");
        System.out.println("    firstNonWsIdx = openPos.offset();");
        System.out.println("}");
        System.out.println("TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);");
        System.out.println("```");
        System.out.println("Wait! `TextPos.ofLeading` is correct.");
        System.out.println("Wait! Did the `catch` block swallow an exception inside `getCaretBounds`?");
        System.out.println("I changed the catch block to log it!");
        System.out.println("```java");
        System.out.println("} catch (Exception ex) {");
        System.out.println("    LOG.warning(\"caretInfoAt/localToScene threw an exception: \" + ex.getMessage());");
        System.out.println("}");
        System.out.println("```");
        System.out.println("Let's look for `caretInfoAt` in `RichTextArea` model API. Is there ANY bug with `charIdx`?");
        System.out.println("What if the `bracketLineOverlay` is not attached to the scene YET? `Platform.runLater` doesn't guarantee the `Scene` is fully built.");
    }
}
