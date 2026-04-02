public class LookAtLog8 {
    public static void main(String[] args) {
        System.out.println("Wait, if `openPos.index()` == `closePos.index()`, we already `return;`");
        System.out.println("But what if `closeCaret.getMinY() < openCaret.getMaxY()`?");
        System.out.println("Why would it be? `TextFlow` local bounds are mapped using `sceneToLocal(textFlow.localToScene(...))`.");
        System.out.println("What if the `TextCell` is scrolled OUT of view?");
        System.out.println("If it's scrolled out of view, does `localToScene` return a valid coordinate? Yes, but it could be above the visible area, so negative `minY`.");
        System.out.println("But relative to each other, they should be fine. EXCEPT... virtual flow un-instantiates cells!");
        System.out.println("If the opening brace is scrolled out of view, the cell might be REUSED for another paragraph, OR it doesn't exist!");
        System.out.println("If `TextCell` doesn't exist for the paragraph, `getCaretBounds` returns `null`!");
        System.out.println("And if `indentCaret`, `openCaret`, or `closeCaret` is `null`, `connectionLine.setVisible(false)` is executed!");
        System.out.println("Let's look at `image.png`! In `image.png`, the opening `FEHLER {` is VISIBLE! And the closing `}` is VISIBLE!");
        System.out.println("So BOTH `TextCell`s are visible!");
    }
}
