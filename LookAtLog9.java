public class LookAtLog9 {
    public static void main(String[] args) {
        System.out.println("If BOTH are visible, WHY would `indentCaret` or `openCaret` be null?");
        System.out.println("Wait! Look at `firstNonWsIdx >= openLineText.length()` fallback.");
        System.out.println("If it falls back, it sets `firstNonWsIdx = openPos.offset();`.");
        System.out.println("What if `TextFlow` doesn't have `caretInfoAt` for that index?");
        System.out.println("Wait... what if `closeCaret.getMinY() > openCaret.getMaxY()` is failing because `getMinY()` is returning something unexpected?");
        System.out.println("Look at `EditorViewController.java` lines 525:");
        System.out.println("`if (closeCaret.getMinY() > openCaret.getMaxY()) {`");
        System.out.println("If I change it to `>=`, maybe it fixes it?");
        System.out.println("Wait... in the image, the `FEHLER {` and `}` are separated by lines:");
        System.out.println("`FEHLER {` (line 1)");
        System.out.println("`    ERGEBNIS (...)` (line 2)");
        System.out.println("`}` (line 3)");
        System.out.println("So `closeCaret.getMinY()` is WAY larger than `openCaret.getMaxY()`. It's not a `>` vs `>=` issue.");
        System.out.println("Wait! What if `closeCaret.getMinY()` is smaller?");
        System.out.println("Is it possible that `sceneToLocal` returns negative/weird values because `bracketLineOverlay` is not positioned at 0,0?");
        System.out.println("No, `bracketLineOverlay` is a `Pane` in a `StackPane`, so its `sceneToLocal` should be standard and same for both.");
        System.out.println("Let's look at `getCaretBounds` again.");
    }
}
