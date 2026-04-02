import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LookAtLog7 {
    public static void main(String[] args) throws Exception {
        System.out.println("Wait! Look at the first issue the user had:");
        System.out.println("User noticed that the `{` line connected correctly initially when we used `.getMinX()`, but diagonally for other instances.");
        System.out.println("Then I changed it to index-based lookup `cell.getClass().getMethod(\"getIndex\")` AND I also added `indentCaret` calculation in `updateBracketLine`.");
        System.out.println("Wait... `int firstNonWsIdx = 0; ... TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("What if `firstNonWsIdx` is out of bounds or `getCaretBounds` fails for it?");
        System.out.println("Wait, if `charIdx` is greater than the TextFlow's length? We removed the `charIdx <= flowText.length()` check. `LayoutInfo.caretInfoAt(charIdx, true)` might throw an exception if `charIdx` is out of bounds!");
        System.out.println("Does `caretInfoAt` throw an exception for out of bounds? YES.");
        System.out.println("If it throws an exception, `getCaretBounds` catches it, ignores it, and RETURNS NULL.");
        System.out.println("And if `indentCaret` is NULL, `connectionLine.setVisible(false)` is executed!");
        System.out.println("Why would `indentCaret` throw an exception? `firstNonWsIdx` might be out of bounds?");
        System.out.println("If `openLineText` is `" + "    FEHLER {" + "` then `firstNonWsIdx` is 4. The length is 12. 4 is fine.");
        System.out.println("Wait! `TextFlow` text does NOT always contain exactly the `openLineText`. Remember? `TextFlow` might only contain the *visible* or *styled* parts, but usually it contains the exact string for that paragraph.");
        System.out.println("Let's look at `TestLookup3.java` output from earlier... `caretInfoAt(0, true)` works. What about `caretInfoAt(4, true)`?");
    }
}
