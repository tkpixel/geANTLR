public class LookAtLog34 {
    public static void main(String[] args) {
        System.out.println("Let's review the code changes one more time.");
        System.out.println("Wait! Look at `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("What if `firstNonWsIdx` is correctly identified, e.g. 4.");
        System.out.println("BUT `charIdx` used in `getCaretBounds` is the offset of `indentPos`, which is 4.");
        System.out.println("BUT `textFlow.getLayoutInfo().caretInfoAt(charIdx, true)`.");
        System.out.println("Wait, if `charIdx` is 4, and `textFlow` contains the entire paragraph.");
        System.out.println("Is `caretInfoAt(4, true)` throwing an exception, or returning null?");
        System.out.println("In `TestLookup8`, `caretInfoAt(4, true)` returned a valid bound.");
        System.out.println("Could `firstNonWsIdx` be out of bounds of the actual `TextFlow` text? What if `TextFlow` ONLY contains the visible characters (no whitespace)?");
        System.out.println("No, we checked that the string was `" + "    FEHLER {" + "`.");
        System.out.println("Wait! We used `SyntaxDecorator`!");
        System.out.println("In `EditorViewController.java`, we use `SyntaxDecorator` to build `RichParagraph`.");
        System.out.println("Does `SyntaxDecorator` build `TextFlow`s that have `Text` nodes?");
        System.out.println("Yes. And `TextFlow` contains them all.");
        System.out.println("Wait! `TextFlow.getLayoutInfo()` gives the layout of the `TextFlow`!");
        System.out.println("Does it include all the text?");
        System.out.println("Yes, it should. Let's look at `TestLookup8` with a `CodeArea` that does NOT have a `SyntaxDecorator`.");
        System.out.println("What if `SyntaxDecorator` makes it break?");
        System.out.println("Let's add a `SyntaxDecorator` to `TestLookup8` and see if `caretInfoAt` fails.");
    }
}
