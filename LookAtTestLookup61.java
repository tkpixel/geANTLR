public class LookAtTestLookup61 {
    public static void main(String[] args) {
        System.out.println("It works beautifully!!!");
        System.out.println("This is EXACTLY what we need!");
        System.out.println("Wait! What if the user scrolls and the caret is OUT OF VIEW?");
        System.out.println("Then `caret` bounds won't overlap with any `TextFlow`! `caretFlowIndex` will be -1.");
        System.out.println("If `-1`, we can't use this trick.");
        System.out.println("Wait! We only draw the bracket line when the user has the caret positioned at the bracket.");
        System.out.println("Is the caret ALWAYS at the bracket? Yes, the matching happens based on `caretPosition`.");
        System.out.println("Wait, if the user scrolls AFTER clicking the bracket, does the caret stay where it is?");
        System.out.println("Yes! The caret stays in the document at `caretPosition`.");
        System.out.println("But if the caret scrolls out of view, its Y coordinate might be outside the viewport.");
        System.out.println("Wait! The `Path .caret` might STILL exist and just have a Y coordinate above or below the viewport!");
        System.out.println("If `caret.localToScene` works even off-screen, we can STILL find where it would be relative to the flows!");
        System.out.println("Wait, if `caretFlowIndex == -1`, it might be ABOVE all flows, or BELOW all flows.");
        System.out.println("But wait! The `TextFlow`s are ONLY the visible ones!");
        System.out.println("If the caret is off-screen, it DOES NOT fall inside any visible `TextFlow`.");
        System.out.println("But wait... `TextFlow` text comparison!");
        System.out.println("If we can't use the caret, what can we use?");
        System.out.println("The FXML approach with CSS classes `.para-N`! That was a great idea and we can apply it to the `CodeArea`.");
        System.out.println("Wait, I couldn't get `.para-1` to find nodes? That was because `SyntaxDecorator` didn't apply cleanly, or maybe the class was on the `Text` node, NOT the `TextFlow`!");
        System.out.println("Wait, if we use `TextFlow.getText()` and match the contiguous block of strings against the document model.");
        System.out.println("That ALWAYS works! We just build a list of all paragraphs `P`, and find the subarray `S` that matches the visible flows.");
        System.out.println("If there are multiple matches, we can fall back to the old method (first match), which is right 99% of the time, except when the ENTIRE viewport is ambiguous.");
        System.out.println("Or we can use the caret position to find the correct match!");
    }
}
