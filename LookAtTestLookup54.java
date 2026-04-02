public class LookAtTestLookup54 {
    public static void main(String[] args) {
        System.out.println("Wait! The user can click a bracket... wait, `updateBracketLine()` is called on EVERY layout pass! `editorCodeArea.needsLayoutProperty()`.");
        System.out.println("If they scroll, `updateBracketLine` is called! The caret might NOT be visible!");
        System.out.println("Wait, if we track the Top Visible Index?");
        System.out.println("Does `editorCodeArea` expose the top visible index?");
        System.out.println("`CodeArea` API methods:");
        System.out.println("Does it have `getFirstVisibleIndex()`?");
        System.out.println("No, we checked the methods.");
        System.out.println("But wait... `area.lookup(\".scroll-bar\")` gives the vertical scroll bar!");
        System.out.println("`scrollBar.getValue()` gives a value between 0 and `max`.");
        System.out.println("If we can't accurately get the index, how does `SyntaxDecorator` know which paragraph to decorate??");
        System.out.println("Ah! `SyntaxDecorator` has `createRichParagraph(CodeTextModel model, int paragraphIndex)`!");
        System.out.println("Wait, when `SyntaxDecorator` creates a paragraph, it KNOWS the index!");
        System.out.println("Could we attach the `paragraphIndex` to the `RichParagraph`? We can't attach data to `RichParagraph`.");
        System.out.println("Could we store the `TextCell` index in a side-channel? No, we don't control the `TextCell`.");
        System.out.println("What if we put a special ID on the segments? `builder.addSegment(text, \"para-\" + index)`?");
        System.out.println("If we add `\"para-\" + index` as a CSS class to the text segments!");
        System.out.println("Then `Text` nodes inside the `TextFlow` will have the CSS class `para-N`!");
        System.out.println("This is GENIUS! We control the `SyntaxDecorator`!");
    }
}
