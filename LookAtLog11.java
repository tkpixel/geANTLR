public class LookAtLog11 {
    public static void main(String[] args) {
        System.out.println("Could `bracketLineOverlay` be null?");
        System.out.println("No, it was loaded from FXML.");
        System.out.println("Could `cell.getClass().getMethod(\"getIndex\")` fail?");
        System.out.println("If it fails, we `catch(Exception e)` and ignore. Then it returns null.");
        System.out.println("Is it possible that the `TextCell`s are not direct children of `.content` anymore?");
        System.out.println("Wait! Look at `LookAtTextCell5.java` output:");
        System.out.println("Node Path no index\nNode Path no index\nNode Path no index\nNode TextCell index=0\n...");
        System.out.println("Wait, what if `CodeArea` re-arranges children or adds wrapping nodes like Groups?");
        System.out.println("No, `.content > *` works for all `TextCell`s.");
        System.out.println("But wait! If the user scrolls, `TextCell` indices change?");
        System.out.println("Yes! `TextCell` is reused for different paragraphs!");
        System.out.println("But its `getIndex()` should return the NEW paragraph index.");
        System.out.println("What if `TextCell` for a paragraph isn't found because it's not rendered yet?");
        System.out.println("If it's not rendered, `TextCell` with that index won't exist. So `getCaretBounds` returns `null`.");
        System.out.println("But in the image, `FEHLER {` and `}` are both visible on screen!");
        System.out.println("So `TextCell` for both should exist.");
        System.out.println("Wait, if `charIdx` > `textFlow.getText().length()`?");
        System.out.println("`charIdx` comes from `openPos.offset()`. `offset()` is the index in the paragraph.");
        System.out.println("`indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx)`");
        System.out.println("If `openPos.offset()` is the exact length of the paragraph, can it be out of bounds?");
        System.out.println("Yes, if the paragraph is `\"FEHLER {\"`, length is 8. `charIdx` for `{` is 7. So `charIdx` = 7. `caretInfoAt(7)` is valid.");
        System.out.println("What about `}`? Length is 1. `charIdx` = 0. `caretInfoAt(0)` is valid.");
    }
}
