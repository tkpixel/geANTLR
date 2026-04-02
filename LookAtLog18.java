public class LookAtLog18 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("What if `openPos.index()` is the closing bracket index?");
        System.out.println("Ah! Look at `EditorViewController.java`:");
        System.out.println("`int openIdx = matchedBrackets.openIndex();`");
        System.out.println("`int closeIdx = matchedBrackets.closeIndex();`");
        System.out.println("`char bracketChar = matchedBrackets.bracketChar();`");
        System.out.println("Wait, I changed `bracketChar` to be `matchedBrackets.bracketChar()`, which is `text.charAt(openIdx)`.");
        System.out.println("If I type `}` the `openIdx` is the `{` character! So `matchedBrackets.bracketChar()` is `{`.");
        System.out.println("So `if (matchedBrackets.bracketChar() != '{') return;` is fine.");
        System.out.println("Wait! `matchedBrackets.bracketChar()` could be `}` if the user typed `}` and the `findMatchingBracket` returned the smaller index as `openIdx`.");
        System.out.println("Wait! I did:");
        System.out.println("`char actualBracketChar = text.charAt(openIdx);`");
        System.out.println("`matchedBrackets.set(new MatchedBracketsRecord(openIdx, closeIdx, actualBracketChar));`");
        System.out.println("Yes! `openIdx` is the SMALLER index, so it is the OPENING bracket! (Since they match pairs).");
        System.out.println("Wait! If the user puts the cursor on `}`, `charIdx` is the `}`. `matchIdx` is the `{`.");
        System.out.println("`openIdx` = `{`, `closeIdx` = `}`.");
        System.out.println("So `text.charAt(openIdx)` is `{`.");
        System.out.println("So `actualBracketChar` is ALWAYS `{` (if it's a `{}` pair).");
        System.out.println("So `bracketChar() != '{'` condition is correct.");
        System.out.println("Why did it vanish?");
        System.out.println("Wait, `closeCaret.getMinY() > openCaret.getMaxY()` was `true`.");
        System.out.println("Let's look at `EditorViewController.java` line 515:");
        System.out.println("`if (closeCaret.getMinY() > openCaret.getMaxY()) {`");
        System.out.println("What if `openCaret.getMaxY()` is BIGGER than `closeCaret.getMinY()`?");
        System.out.println("This happens if the closing bracket is ABOVE the opening bracket? But `closeIdx > openIdx`, so `closePos.index() > openPos.index()`!");
        System.out.println("If `closePos.index() > openPos.index()`, then `closeCaret.getMinY()` is below `openCaret.getMaxY()`... WAIT NO!");
        System.out.println("Wait... `localCaret.getMinY()` is LOCAL to the `TextFlow`!");
        System.out.println("`TextFlow`'s local coordinates are `0, 0` to `width, 16` for BOTH cells!");
        System.out.println("So `localCaret.getMinY()` is 0 for BOTH `openCaret` and `closeCaret`!");
        System.out.println("And we did: `javafx.geometry.Bounds overlayBounds = bracketLineOverlay.sceneToLocal(textFlow.localToScene(localBounds));`");
        System.out.println("This maps it to the `bracketLineOverlay` space. So `overlayBounds` should have the true Y coordinates.");
        System.out.println("Let's double check if `bracketLineOverlay` exists and is visible.");
        System.out.println("Yes, we see it was working BEFORE I changed the condition.");
        System.out.println("Wait! What did I change? I changed it from `closeCaret.getMinY() > openCaret.getMaxY()` to `closeCaret.getMinY() >= openCaret.getMaxY()`?");
        System.out.println("Wait, NO! I changed `closeCaret.getMinY() > openCaret.getMaxY()`? No, I added the `indentCaret`.");
        System.out.println("Let's look at what I ACTUALLY changed in the last step.");
    }
}
