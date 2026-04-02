public class LookAtLog21 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("Wait! `openPos.offset()` is the index of the `{` character in the line.");
        System.out.println("If `openLineText` is `" + "FEHLER {" + "`, `firstNonWsIdx` = 0. `{` is at offset 7.");
        System.out.println("What if the user clicks the `{`? `MatchedBracketsRecord` has `openIdx` and `closeIdx`.");
        System.out.println("Wait! `matchedBrackets.openIndex()` is the ABSOLUTE index.");
        System.out.println("If I type `FEHLER {\n}`, `openIndex` is 7. `computeTextPosFromOffset(7)` -> `TextPos(0, 7)`.");
        System.out.println("`indentPos` -> `TextPos(0, 0)`.");
        System.out.println("Does `getCaretBounds` work for `TextPos(0, 0)`? YES.");
        System.out.println("What if `TextCell` is found, `TextFlow` is found, but `caretInfo` is NULL?");
        System.out.println("Can `caretInfoAt` return NULL? Yes, it can return null. It did not for `0`.");
        System.out.println("Let's just fix the `>=` operator first, and remove the `catch` swallowing so we can see if it throws something.");
    }
}
