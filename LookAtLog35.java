public class LookAtLog35 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("If `firstNonWsIdx >= openLineText.length()`, we fallback to `openPos.offset()`.");
        System.out.println("Is `openLineText` missing whitespace because it's stripped? No, `getPlainText(index)` gives the exact raw text.");
        System.out.println("Wait, look at how the connection line is drawn:");
        System.out.println("```java");
        System.out.println("double verticalX = indentCaret.getMinX();");
        System.out.println("connectionLine.setStartX(verticalX);");
        System.out.println("connectionLine.setStartY(openCaret.getMaxY());");
        System.out.println("connectionLine.setEndX(verticalX);");
        System.out.println("connectionLine.setEndY(closeCaret.getMinY());");
        System.out.println("connectionLine.setVisible(true);");
        System.out.println("```");
        System.out.println("Is `verticalX` properly populated?");
        System.out.println("Let's look at my last commit. I changed ONLY the `{}` alignment and drawing logic. And the `getCaretBounds` matching logic.");
        System.out.println("If I changed `getCaretBounds` to `cell.getClass().getMethod(\"getIndex\")`, AND I added `bracketChar() != '{'` condition.");
        System.out.println("Wait! `matchedBrackets.bracketChar()` could be WRONG!");
        System.out.println("Let's look at `EditorViewModel.java`! The FXML is fine.");
        System.out.println("If `matchedBrackets.bracketChar()` evaluates to `}`, it will execute `connectionLine.setVisible(false)` and `return`.");
        System.out.println("Why would `matchedBrackets.bracketChar()` be `}`?");
        System.out.println("```java");
        System.out.println("int openIdx = Math.min(charIdx, matchIdx);");
        System.out.println("char actualBracketChar = text.charAt(openIdx);");
        System.out.println("```");
        System.out.println("If `charIdx` is `}` and `matchIdx` is `{`. Then `openIdx` is `{`.");
        System.out.println("So `actualBracketChar` is ALWAYS the OPENING bracket! `(`, `{`, `[`, `` ` ``.");
        System.out.println("So for `{}` pair, `actualBracketChar` is ALWAYS `{`.");
        System.out.println("So `bracketChar() != '{'` is ONLY FALSE when the pair is `()`, `[]`, or `` ` ``.");
        System.out.println("Wait, I used `matchedBrackets.bracketChar() != '{'` in `EditorViewController.java`!");
        System.out.println("```java");
        System.out.println("if (matchedBrackets.bracketChar() != '{') {");
        System.out.println("    connectionLine.setVisible(false);");
        System.out.println("    return;");
        System.out.println("}");
        System.out.println("```");
        System.out.println("What if the `bracketChar` IS actually `{`?");
        System.out.println("Let's test `actualBracketChar = text.charAt(openIdx)`.");
    }
}
