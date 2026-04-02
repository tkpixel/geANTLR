public class LookAtLog36 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `firstNonWsIdx` logic:");
        System.out.println("`String openLineText = editorCodeArea.getModel().getPlainText(openPos.index());`");
        System.out.println("If `openPos.index()` is the index of the line containing `{`, this gets the line text.");
        System.out.println("`int firstNonWsIdx = 0;`");
        System.out.println("`while (firstNonWsIdx < openLineText.length() && Character.isWhitespace(openLineText.charAt(firstNonWsIdx))) firstNonWsIdx++;`");
        System.out.println("`TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("Wait! `charIdx` in `getCaretBounds` gets set to `indentPos.offset()`, which is `firstNonWsIdx`.");
        System.out.println("Then `layoutInfo.caretInfoAt(charIdx, true)`.");
        System.out.println("Does `caretInfoAt` return NULL if `charIdx` is out of bounds? YES.");
        System.out.println("Is `firstNonWsIdx` out of bounds? No, `firstNonWsIdx <= openLineText.length()`.");
        System.out.println("But what if `firstNonWsIdx` is pointing to the END OF THE LINE (e.g. empty line, `length == 0`)?");
        System.out.println("`caretInfoAt(0, true)` works.");
        System.out.println("Wait! Look at `TextPos closePos = computeTextPosFromOffset(closeIdx);`");
        System.out.println("Wait! What if `openPos.index() == closePos.index()`?");
        System.out.println("`if (openPos == null || closePos == null || openPos.index() == closePos.index())`");
        System.out.println("It returns if they are on the same line.");
        System.out.println("So they are on DIFFERENT lines.");
        System.out.println("Wait, I found the bug.");
        System.out.println("In `EditorViewController.java`:");
        System.out.println("```java");
        System.out.println("if (indexVal instanceof Integer && ((Integer) indexVal) == targetPara) {");
        System.out.println("```");
        System.out.println("BUT `indexVal` is NOT an `Integer` if `getIndex` returns an `int`!");
        System.out.println("`Method.invoke` boxes primitive `int` to `java.lang.Integer`.");
        System.out.println("So `indexVal instanceof Integer` is TRUE.");
        System.out.println("Wait! Look at `((Integer) indexVal) == targetPara`.");
        System.out.println("This unboxes `indexVal` and compares it to primitive `targetPara`.");
        System.out.println("So `isMatch = true` gets executed!");
        System.out.println("BUT wait... if `isMatch` is TRUE, it breaks out of the loop at the end:");
        System.out.println("```java");
        System.out.println("    }");
        System.out.println("    break;");
        System.out.println("}");
        System.out.println("```");
        System.out.println("Wait! If `layoutInfo == null` or `caretInfo == null`, it still executes `break;`!");
        System.out.println("Because the `break;` is at the end of `if (isMatch)` block!");
        System.out.println("And returning `null` from `getCaretBounds`!");
        System.out.println("BUT wait, if `layoutInfo` is null, or `caretInfo` is null, it should return null anyway, because the cell is the ONLY one for that paragraph!");
        System.out.println("Yes! `break` is correct! There's only one cell for `targetPara`.");
        System.out.println("So it's not the loop.");
    }
}
