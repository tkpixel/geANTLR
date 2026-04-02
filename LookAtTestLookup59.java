public class LookAtTestLookup59 {
    public static void main(String[] args) {
        System.out.println("Still nothing.");
        System.out.println("Ah! `SyntaxDecorator` does NOT apply CSS classes directly to `Text` nodes in a way that `lookupAll` can find them?");
        System.out.println("Maybe `RichTextArea` wraps them differently or applies styles internally?");
        System.out.println("Let's just use `TextFlow.getBoundsInParent().getMinY()` to sort all `TextFlow`s!");
        System.out.println("Wait! If we have the caret position (`TextPos caretPos = editorCodeArea.getCaretPosition()`), we know the paragraph index of the caret!");
        System.out.println("And we can get the caret node `Path caret = (Path) editorCodeArea.lookup(\".caret\");`");
        System.out.println("Wait, if `caret` is just a Path, it has a Y coordinate.");
        System.out.println("If we find which `TextFlow` contains that Y coordinate, we KNOW that `TextFlow` corresponds to `caretPos.index()`!");
        System.out.println("And since `TextFlow`s are laid out sequentially, we can deduce the index of ALL OTHER `TextFlow`s just by counting up or down from the caret's `TextFlow`!");
        System.out.println("This is 100% robust WITHOUT reflection and WITHOUT bugs from duplicate texts.");
    }
}
