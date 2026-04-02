public class LookAtTestLookup37 {
    public static void main(String[] args) {
        System.out.println("Wait! The output is exactly the same. No exception.");
        System.out.println("Let's look at `EditorViewController.updateBracketLine` again.");
        System.out.println("```java");
        System.out.println("if (closeCaret.getMinY() >= openCaret.getMaxY()) {");
        System.out.println("    connectionLine.setStartX(verticalX);");
        System.out.println("    connectionLine.setStartY(openCaret.getMaxY());");
        System.out.println("    connectionLine.setEndX(verticalX);");
        System.out.println("    connectionLine.setEndY(closeCaret.getMinY());");
        System.out.println("    connectionLine.setVisible(true);");
        System.out.println("} else { connectionLine.setVisible(false); }");
        System.out.println("```");
        System.out.println("Wait! `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("What if `matchedBrackets.bracketChar() != '{'`?");
        System.out.println("If the user clicked on `}` of the `FEHLER` block. `matchedBrackets.bracketChar()` is `{` (since `openIdx` points to `{`).");
        System.out.println("So `if (matchedBrackets.bracketChar() != '{') return;` is fine.");
        System.out.println("Wait! `matchedBracketsProperty().get().bracketChar()` was initialized with:");
        System.out.println("`char actualBracketChar = text.charAt(openIdx);`");
        System.out.println("Is `openIdx` ALWAYS `{`? Yes, if it matched a `{}` pair.");
        System.out.println("Wait! `firstNonWsIdx >= openLineText.length()`?");
        System.out.println("What if the `openLineText` is `" + "    FEHLER {" + "`?");
        System.out.println("`firstNonWsIdx` is 4. The `TextPos` is `(0, 4)`.");
        System.out.println("Wait, if it's returning `null`, then maybe `isMatch` is false? No, we saw `isMatch` becomes true.");
        System.out.println("Maybe `textFlow` is null? We saw `textFlow` is not null.");
        System.out.println("Maybe `layoutInfo` is null? We saw it's not null.");
        System.out.println("Maybe `caretInfoAt` throws? We saw it doesn't.");
        System.out.println("Maybe `localToScene` throws? We saw it doesn't.");
        System.out.println("Why did the user say the line is completely vanished for ALL curly brackets?");
        System.out.println("Let's look at what could hide the line.");
        System.out.println("Ah! `EditorViewModel.java` lines 458: `char actualBracketChar = text.charAt(openIdx);`");
        System.out.println("If I type `FEHLER {\\n\\n}`, and place the caret at `}`, `text.charAt(openIdx)` gets `{`.");
        System.out.println("BUT WAIT! `charIdx` in `EditorViewModel.java`:");
        System.out.println("If `caretPosition < text.length()` ... `text.charAt(caretPosition)`");
        System.out.println("If `caretPosition == text.length()` ... it falls back to `caretPosition - 1`.");
        System.out.println("Wait! If `actualBracketChar` is indeed `{`... then WHY does `matchedBrackets.bracketChar() != '{'` evaluate to true?");
        System.out.println("Wait... what if `actualBracketChar` is NOT `{`?");
        System.out.println("Let's add some System.out.println / debug logging inside `updateBracketLine` by doing a FAKE run with logging!");
        System.out.println("Oh wait... is `actualBracketChar` ACTUALLY `{`?");
    }
}
