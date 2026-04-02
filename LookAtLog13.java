public class LookAtLog13 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("What if `openPos.index()` is the index of the `openIndex` character, BUT `MatchedBracketsRecord` has `openIndex` and `closeIndex` as ABSOLUTE offsets!!");
        System.out.println("YES! `matchedBracketsProperty().get().openIndex()` is an ABSOLUTE offset!");
        System.out.println("Let's check `EditorViewController.java` lines 517-518:");
        System.out.println("`int openIdx = matchedBrackets.openIndex();`");
        System.out.println("`int closeIdx = matchedBrackets.closeIndex();`");
        System.out.println("`TextPos openPos = computeTextPosFromOffset(openIdx);`");
        System.out.println("`TextPos closePos = computeTextPosFromOffset(closeIdx);`");
        System.out.println("So `openPos.index()` is the PARAGRAPH index!");
        System.out.println("And `openPos.offset()` is the INTRA-PARAGRAPH index!");
        System.out.println("This is correct.");
        System.out.println("Wait... what if `indentCaret.getMinX()` is calculating the indent position properly... BUT wait!");
        System.out.println("`if (closeCaret.getMinY() > openCaret.getMaxY())`");
        System.out.println("In the previous code, I changed it to `closeCaret.getMinY() > openCaret.getMaxY()`.");
        System.out.println("What if the closing bracket is on the SAME visual line, but the line wrapped?");
        System.out.println("No, the user image shows them on completely different lines.");
        System.out.println("What if `getMinY()` returns 0 and `getMaxY()` returns 0?");
        System.out.println("No, we checked `caretInfo.getSegmentAt(0)` has height 15.13.");
        System.out.println("Wait! We mapped them to `bracketLineOverlay.sceneToLocal(...)`.");
        System.out.println("Could `bracketLineOverlay.sceneToLocal(...)` fail or return wrong bounds because it's not attached properly?");
        System.out.println("Let's review the code carefully.");
    }
}
