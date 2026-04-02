public class LookAtLog31 {
    public static void main(String[] args) {
        System.out.println("Okay, `getIndex()` returns the paragraph index flawlessly, even when scrolling.");
        System.out.println("So `getCaretBounds()` works perfectly.");
        System.out.println("Why did the line vanish?");
        System.out.println("Is it possible that `indentCaret != null && openCaret != null && closeCaret != null` evaluates to TRUE, but `connectionLine.setVisible(true)` is NOT making it visible?");
        System.out.println("Wait! I created a FXML bug?");
        System.out.println("In `EditorViewController.java`:");
        System.out.println("```java");
        System.out.println("connectionLine.setStartX(verticalX);");
        System.out.println("connectionLine.setStartY(openCaret.getMaxY());");
        System.out.println("connectionLine.setEndX(verticalX);");
        System.out.println("connectionLine.setEndY(closeCaret.getMinY());");
        System.out.println("```");
        System.out.println("If `verticalX` is 0 or off-screen, it won't be seen, but `FEHLER` is on screen.");
        System.out.println("What if `bracketChar()` is WRONG?");
        System.out.println("Wait! The user selected `{`. So `caretPosition` was ON the `{`. `openIdx` is the `{`. `bracketChar()` is `{`.");
        System.out.println("Wait... what if `findMatchingBracket` fails to find the matching bracket?");
        System.out.println("If `matchIdx` is -1, `matchedBrackets` is set to `null`!");
        System.out.println("If `matchedBrackets` is `null`, `connectionLine.setVisible(false)` and we return!");
        System.out.println("Did `findMatchingBracket` FAIL in the user's grammar?");
        System.out.println("Let's look at `image.png`. The `FEHLER` block has `{` and `}`. But look inside it!");
        System.out.println("```");
        System.out.println("FEHLER {");
        System.out.println("    ERGEBNIS (FEHLER , \"Relative Lage...\")");
        System.out.println("}");
        System.out.println("```");
        System.out.println("Wait! `findMatchingBracket` counts `{` and `}`.");
        System.out.println("But what if there are strings `\"... {\"`?");
        System.out.println("`findMatchingBracket` is naive! It doesn't ignore strings or comments!");
        System.out.println("If the user's string has `{` or `}`, it will mismatch!");
        System.out.println("But in `image.png`, `\"Relative Lage...\"` doesn't have a brace in the visible part.");
        System.out.println("Wait... look at the image! The `{` and `}` ARE HIGHLIGHTED WITH GREY BACKGROUND!");
        System.out.println("If they are highlighted with grey background, it means `matchedBrackets` IS NOT NULL!");
        System.out.println("Because the syntax decorator uses `matchedBrackets` to draw the grey background!");
        System.out.println("So `matchedBrackets` is VALID.");
        System.out.println("So `updateBracketLine` IS called with a valid record.");
    }
}
