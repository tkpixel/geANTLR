public class LookAtLog14 {
    public static void main(String[] args) {
        System.out.println("Look at my test: `minY2 >= maxY1 ? true`!");
        System.out.println("Wait! The output was `maxY1=20.1328125 minY2=21.0`.");
        System.out.println("So `minY2 > maxY1` IS TRUE (21.0 > 20.1328125).");
        System.out.println("BUT WAIT! That was for `FEHLER {` and `    }` with NO LINES IN BETWEEN.");
        System.out.println("What if there are lines in between, but the user scrolled so that the opening brace is AT THE TOP of the viewport?");
        System.out.println("No, in the user's image, both braces are in the middle of the viewport!");
        System.out.println("What if `TextFlow` doesn't have `TextCell` index corresponding to `openPos.index()`?");
        System.out.println("Ah! Does `CodeArea` only create `TextCell`s for VISIBLE paragraphs?");
        System.out.println("YES! Virtualized controls only create cells for visible items.");
        System.out.println("If both braces are visible, BOTH cells exist.");
        System.out.println("Wait, if `indentCaret != null && openCaret != null && closeCaret != null` is false, it hides the line.");
        System.out.println("If any of them is null, it hides the line.");
        System.out.println("Why would `indentCaret` be null?");
        System.out.println("`indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("`firstNonWsIdx` is calculated as:");
        System.out.println("`int firstNonWsIdx = 0;`");
        System.out.println("`while (firstNonWsIdx < openLineText.length() && Character.isWhitespace(openLineText.charAt(firstNonWsIdx))) { firstNonWsIdx++; }`");
        System.out.println("If `openLineText` is `" + "FEHLER {" + "`, `firstNonWsIdx` is 0.");
        System.out.println("So `indentPos` is index 0, offset 0.");
        System.out.println("`getCaretBounds(indentPos)` calls `caretInfoAt(charIdx, true)`. `charIdx` is 0.");
        System.out.println("Does `caretInfoAt(0, true)` return null for `" + "FEHLER {" + "`? NO! We saw it returns valid bounds.");
        System.out.println("Wait... what if `indentCaret` calculation uses `targetPara = indentPos.index()` which is `openPos.index()`. That works.");
        System.out.println("Wait... what if `closeCaret.getMinY() > openCaret.getMaxY()` is failing?");
        System.out.println("Let's look at `EditorViewController.java` lines 582-585:");
    }
}
