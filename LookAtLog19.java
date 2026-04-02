public class LookAtLog19 {
    public static void main(String[] args) {
        System.out.println("Wait! The submit created a new commit on the `bugfix/...` branch, but in the sandbox, changes made via `submit` do NOT commit locally in my `HEAD` (or it checks out a detached HEAD?).");
        System.out.println("Actually `submit` submits to the *remote* branch, the local branch might be detached or updated.");
        System.out.println("Let's look at `EditorViewController.java` lines 504:");
        System.out.println("`if (closeCaret.getMinY() > openCaret.getMaxY()) {`");
        System.out.println("Let's just print out `openCaret` and `closeCaret` and `indentCaret` using a script that patches `EditorViewController`.");
        System.out.println("Wait! `closeCaret.getMinY() >= openCaret.getMaxY()` is what I should change it to!");
        System.out.println("If it's on adjacent lines, `minY` of `close` is exactly `maxY` of `open`!");
        System.out.println("If `minY == maxY`, then `minY > maxY` is FALSE!");
        System.out.println("So `>` will hide the line for ADJACENT lines.");
        System.out.println("But what if there's a space between them? `getMinY() > getMaxY()` would be TRUE.");
        System.out.println("So the ONLY case it vanishes for > 1 lines apart is if `indentCaret` threw an exception, or `firstNonWsIdx` math is wrong.");
        System.out.println("Let's look at `firstNonWsIdx`:");
        System.out.println("```java");
        System.out.println("String openLineText = editorCodeArea.getModel().getPlainText(openPos.index());");
        System.out.println("int firstNonWsIdx = 0;");
        System.out.println("while (firstNonWsIdx < openLineText.length() && Character.isWhitespace(openLineText.charAt(firstNonWsIdx))) {");
        System.out.println("    firstNonWsIdx++;");
        System.out.println("}");
        System.out.println("```");
        System.out.println("Wait! Is `openLineText` a correct string? Yes, `getPlainText`.");
        System.out.println("Wait! What if `openCaret` or `indentCaret` evaluates to null?");
        System.out.println("`getCaretBounds(indentPos)` calls `caretInfoAt(firstNonWsIdx, true)`.");
        System.out.println("Does `caretInfoAt` work for `firstNonWsIdx`? Yes.");
        System.out.println("Let's check if `bracketLineOverlay.sceneToLocal(...)` throws.");
    }
}
