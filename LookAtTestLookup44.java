public class LookAtTestLookup44 {
    public static void main(String[] args) {
        System.out.println("Wow, no properties whatsoever.");
        System.out.println("Wait! We can get the text of the `TextFlow`! `flowText`.");
        System.out.println("But we CANNOT rely on exact string match because duplicates exist!");
        System.out.println("If we can't use `getIndex()`, and we can't use `getTextPosition()`, how do we distinguish between `TextCell` #1 and `TextCell` #2 that have the exact same text?");
        System.out.println("We know that `TextCell`s in `.content` are ordered VISUALLY! The first one is the top-most visible paragraph!");
        System.out.println("If we sort `TextCell`s by their `getLayoutY()`, they are ordered.");
        System.out.println("We can read their text and build a list of strings: `[S_1, S_2, S_3, ... S_N]`.");
        System.out.println("We know the entire document's paragraphs: `[P_0, P_1, P_2, ... P_M]`.");
        System.out.println("We can find the subarray of `P` that exactly matches `S`!");
        System.out.println("If it exactly matches, we know the starting index `k`.");
        System.out.println("Then `TextCell` at visual index `i` corresponds to paragraph `k + i`!");
        System.out.println("Wait... what if there are multiple matches?");
        System.out.println("If there are multiple matches, we can disambiguate using the CARET position!");
        System.out.println("`CodeArea` has `getCaretPosition()`. The caret is AT a specific paragraph index.");
        System.out.println("And we can look up `.caret` node in `CodeArea`!");
        System.out.println("We can get the Y coordinate of the `.caret` node!");
        System.out.println("And find which `TextCell` it falls into! Then we know THAT `TextCell` is the caret's paragraph!");
        System.out.println("Then we can deduce the paragraph index of all other `TextCell`s based on their visual order relative to the caret's `TextCell`!");
    }
}
