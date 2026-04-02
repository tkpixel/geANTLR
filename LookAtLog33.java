public class LookAtLog33 {
    public static void main(String[] args) {
        System.out.println("Wait! In `updateBracketLine` I did:");
        System.out.println("`int firstNonWsIdx = 0; while (...) firstNonWsIdx++;`");
        System.out.println("`if (firstNonWsIdx >= openLineText.length()) firstNonWsIdx = openPos.offset();`");
        System.out.println("Wait... what if `openLineText` does NOT end exactly where we think it does?");
        System.out.println("No, `getPlainText(index)` is just a string.");
        System.out.println("Wait! Look at `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("`firstNonWsIdx` is just an integer.");
        System.out.println("What if the FXML initialization of `connectionLine` failed? NO, it was working before.");
        System.out.println("Wait... I am iterating over `.content > *`. In my earlier script, it worked beautifully!");
        System.out.println("Could there be a bug in how `closePos.index()` compares to `openPos.index()`?");
        System.out.println("```java");
        System.out.println("if (openPos.index() == closePos.index()) { connectionLine.setVisible(false); return; }");
        System.out.println("```");
        System.out.println("Are `openPos` and `closePos` on the same line in the user's image?");
        System.out.println("No, `FEHLER {` is line 1, `}` is line 3.");
        System.out.println("Wait! Look closely at `image.png`.");
        System.out.println("The user clicked on `{` on line 1.");
        System.out.println("The `}` on line 3 is highlighted.");
        System.out.println("So `openPos.index()` is the index of line 1, and `closePos.index()` is the index of line 3.");
        System.out.println("Then WHY did the line vanish?");
        System.out.println("Let's look at `EditorViewController.java` lines 504:");
        System.out.println("```java");
        System.out.println("double verticalX = indentCaret.getMinX();");
        System.out.println("```");
        System.out.println("Wait, if `verticalX` is calculated... what if `indentCaret.getMinX()` is NaN or Infinity?");
        System.out.println("No, it returns a valid `Rectangle2D`.");
    }
}
