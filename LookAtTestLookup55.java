public class LookAtTestLookup55 {
    public static void main(String[] args) {
        System.out.println("If we add `para-N` to the CSS classes, it gets added to the `Text` nodes!");
        System.out.println("Then in `getCaretBounds(TextPos pos)`:");
        System.out.println("`String targetClass = \"para-\" + pos.index();`");
        System.out.println("`for (Node textNode : editorCodeArea.lookupAll(\".\" + targetClass))`");
        System.out.println("Wait! `lookupAll` will find the `Text` nodes directly!");
        System.out.println("Then we can do `textNode.getParent()` to get the `TextFlow`!");
        System.out.println("Then we don't need reflection!");
        System.out.println("Is `addSegment(text, style)` available? `addWithStyleNames(text, \"para-\" + index)`!");
        System.out.println("But wait, what if the user types and `createRichParagraph` is called? It creates with the new index.");
        System.out.println("Wait, I used `addSegment(text)` for unstyled text.");
        System.out.println("I can just change it to `addWithStyleNames(text, \"para-\" + paragraphIndex)`.");
        System.out.println("Will it affect styling? If there is no CSS rule for `.para-N`, it does nothing visually.");
        System.out.println("Is this legal in JavaFX CSS? Yes, `.para-10` is a valid class name.");
        System.out.println("Let's verify this in a small script!");
    }
}
