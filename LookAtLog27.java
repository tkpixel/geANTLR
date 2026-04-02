public class LookAtLog27 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at the image `image.png`.");
        System.out.println("The user clicked on `{` of the `FEHLER` keyword.");
        System.out.println("Wait, if `firstNonWsIdx` gets evaluated as 4, and `indentCaret` gets calculated.");
        System.out.println("And `openCaret` is the caret of `{`.");
        System.out.println("And `closeCaret` is the caret of `}`.");
        System.out.println("Is it possible that `indentCaret.getMinX()` is the EXACT SAME as `openCaret.getMinX()`?");
        System.out.println("Yes, if `{` is the very first character on the line!");
        System.out.println("But in the image, it's `FEHLER {`. So `indentCaret` is `F`, `openCaret` is `{`.");
        System.out.println("What if `bracketLineOverlay` IS NOT ADDED TO THE STACK PANE CORRECTLY?");
        System.out.println("Wait, in `EditorView.fxml`:");
        System.out.println("```xml");
        System.out.println("<StackPane>");
        System.out.println("    <CodeArea fx:id=\"editorCodeArea\" ... />");
        System.out.println("    <javafx.scene.layout.Pane fx:id=\"bracketLineOverlay\" mouseTransparent=\"true\" />");
        System.out.println("```");
        System.out.println("This means `bracketLineOverlay` sits OVER the `CodeArea`.");
        System.out.println("Does a `Pane` automatically size itself to the `StackPane`?");
        System.out.println("YES. It should cover the `CodeArea`.");
        System.out.println("Wait! Is there ANY CHANCE `connectionLine` is drawn behind `CodeArea`?");
        System.out.println("No, it's declared AFTER `CodeArea` in the FXML, so it's drawn ON TOP.");
        System.out.println("Wait! If `connectionLine` is drawn OUT OF BOUNDS of `bracketLineOverlay`?");
        System.out.println("`Pane` does not clip its children by default, but it could be drawn somewhere completely wrong.");
        System.out.println("But wait, in my previous commit, the diagonal line was VISIBLE! So `bracketLineOverlay` IS correctly sized and visible.");
        System.out.println("If it vanished AFTER my last commit, the ONLY explanation is `connectionLine.setVisible(false)` is being triggered OR `startX/startY` are totally wrong.");
        System.out.println("Let's look closely at `EditorViewController.java`!");
    }
}
