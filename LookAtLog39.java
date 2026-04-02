public class LookAtLog39 {
    public static void main(String[] args) {
        System.out.println("Let's look at `RichTextArea` API again.");
        System.out.println("It has `getCaretPosition()` and `getTextPosition(x, y)`.");
        System.out.println("Is there `getParagraphBounds()`? No.");
        System.out.println("Wait, if we map the `TextFlow`s inside `.content > *`, we can SORT them by their Y-coordinate!");
        System.out.println("Because they are laid out sequentially from top to bottom.");
        System.out.println("If we know the sequence of `TextFlow`s in the viewport, and we know their `flowText` string... can we match them to the document's paragraphs?");
        System.out.println("The visible paragraphs are a contiguous block of text from `model.getPlainText(...)`.");
        System.out.println("If we take the entire list of `TextFlow` nodes, sort them by Y coordinate, and collect their texts `[T1, T2, T3...]`.");
        System.out.println("We can search the entire `model` to find a contiguous block of paragraphs `P_k, P_{k+1}, P_{k+2}...` that exactly matches `[T1, T2, T3...]`.");
        System.out.println("This is a substring matching problem! The text content uniquely identifies the window of visible paragraphs.");
        System.out.println("Once we find `k` (the index of the first visible paragraph), we can just assign the `i`-th `TextFlow` to index `k + i`!");
        System.out.println("Is that reliable?");
        System.out.println("If there are multiple identical blocks of paragraphs in the document? Yes, it could be ambiguous if the entire viewport is exactly identical to another viewport segment. E.g., a viewport showing only `\\n\\n\\n`.");
        System.out.println("Wait! We have the Caret! The Caret is on screen, and its `caretPosition.index()` is the currently focused paragraph!");
        System.out.println("If the caret is visible, we can match the sequence of `TextFlow`s using the caret's index.");
        System.out.println("Wait, what if the caret is NOT visible?");
        System.out.println("What if we just use `java.lang.reflect.Method` but with `.setAccessible(true)`? Will it work? Java 25 blocks it by default (`InaccessibleObjectException`).");
        System.out.println("Let's try to find an alternative way to get the `index` property.");
    }
}
