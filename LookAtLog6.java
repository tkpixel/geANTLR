public class LookAtLog6 {
    public static void main(String[] args) {
        System.out.println("Wait! The inner loop finds TextFlow `if (child instanceof javafx.scene.text.TextFlow flow)`.");
        System.out.println("In the debug output earlier: `com.sun.jfx.incubator.scene.control.richtext.TextCell` contains `javafx.scene.text.TextFlow`.");
        System.out.println("Wait, looking at the previous structural dump: ");
        System.out.println("com.sun.jfx.incubator.scene.control.richtext.TextCell\n  javafx.scene.text.TextFlow\n    javafx.scene.text.Text");
        System.out.println("So `child instanceof javafx.scene.text.TextFlow flow` SHOULD be true.");
        System.out.println("Why did it fail then?");
        System.out.println("Maybe `bracketLineOverlay.sceneToLocal(...)` is throwing an exception? Because it's not attached to the scene yet? No, this runs in Platform.runLater inside updateBracketLine().");
        System.out.println("Let's add logging to `EditorViewController.updateBracketLine` or just simulate it here.");
    }
}
