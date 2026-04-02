public class LookAtTestLookup52 {
    public static void main(String[] args) {
        System.out.println("Wow. `getTextPosition(x, y)` is fundamentally broken in this early access build of JavaFX 25 CodeArea.");
        System.out.println("It ALWAYS returns 0.");
        System.out.println("So we CANNOT use `getTextPosition`.");
        System.out.println("And we CANNOT use `getIndex()` via reflection due to JPMS encapsulation (`IllegalAccessException`).");
        System.out.println("How did I match cells before? By `flowText.toString().equals(modelText)`!");
        System.out.println("But that had a bug with DUPLICATE lines, causing jumps.");
        System.out.println("Wait! If there are duplicates, we need to disambiguate.");
        System.out.println("How do we disambiguate?");
        System.out.println("We can look at the CARET position!");
        System.out.println("If `TextFlow` text equals `modelText`, there might be MULTIPLE `TextFlow`s that match.");
        System.out.println("Which one is the correct one for `targetPara`?");
        System.out.println("We can build a list of ALL visible `TextFlow`s by collecting them from `.content > *` and sorting them by `getBoundsInParent().getMinY()`.");
        System.out.println("Now we have a visually-ordered list `[F_0, F_1, F_2, ..., F_n]`.");
        System.out.println("And we have their texts `[T_0, T_1, T_2, ..., T_n]`.");
        System.out.println("We need to find the paragraph index of `F_0`.");
        System.out.println("Wait! We can get the `currentCaretPosition` from `editorCodeArea.getCaretPosition().index()`!");
        System.out.println("If the caret is currently visible, its paragraph index is known.");
        System.out.println("Is the caret node `.caret` visible?");
        System.out.println("Yes! `CodeArea` has `.caret` node.");
        System.out.println("If we find `.caret` and its Y coordinate, we can see WHICH `TextFlow` it overlaps with!");
        System.out.println("Then we know THAT `TextFlow` corresponds to `caretPosition.index()`.");
        System.out.println("Then `F_i` corresponds to `caretPosition.index() - (caretFlowIndex - i)`.");
        System.out.println("And we know exactly the index of EVERY `TextFlow`!");
        System.out.println("What if the caret is NOT visible?");
        System.out.println("Then this method fails. But for bracket matching, the user JUST CLICKED or moved the caret to the bracket! So the caret IS visible!");
    }
}
